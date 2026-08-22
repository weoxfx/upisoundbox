package com.weox.upisoundbox.net

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Local cache for generated voice audio, keyed by amount. Your backend
 * (not this app) holds the ElevenLabs keys and does the actual generation
 * + rotation — this class just asks "give me audio for ₹500" and caches
 * the resulting file so it's never re-fetched for a repeat amount.
 */
object VoiceCache {
    // Point this at your own backend, not ElevenLabs directly.
    private const val BACKEND_BASE_URL = "https://your-backend.example.com"
    private const val TAG = "UpiSoundBox"

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val mainHandler = Handler(Looper.getMainLooper())

    fun getAudioForAmount(
        context: Context,
        amountRupees: Double,
        callback: (filePath: String?) -> Unit
    ) {
        val cacheFile = fileForAmount(context, amountRupees)
        if (cacheFile.exists()) {
            Log.d(TAG, "Using cached audio: ${cacheFile.absolutePath}")
            callback(cacheFile.absolutePath)
            return
        }

        // Not cached locally — ask the backend to generate (or return its own cached copy).
        Thread {
            try {
                val request = Request.Builder()
                    .url("$BACKEND_BASE_URL/tts/amount?value=$amountRupees")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        Log.d(TAG, "Backend call failed (${response.code}), falling back to on-device TTS")
                        fallbackToOnDeviceTts(context, amountRupees, cacheFile, callback)
                        return@Thread
                    }
                    response.body?.byteStream()?.use { input ->
                        FileOutputStream(cacheFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                }
                mainHandler.post { callback(cacheFile.absolutePath) }
            } catch (e: Exception) {
                // Backend unreachable (e.g. it doesn't exist yet) — fall back so
                // you can still test the pipeline end to end on-device.
                Log.d(TAG, "Backend unreachable (${e.message}), falling back to on-device TTS")
                fallbackToOnDeviceTts(context, amountRupees, cacheFile, callback)
            }
        }.start()
    }

    /**
     * Uses Android's built-in TTS engine to synthesize "Rupees X received"
     * directly to a file, so playback can be tested before the ElevenLabs
     * backend is built. Quality is robotic compared to ElevenLabs, but the
     * detection -> synthesis -> volume boost -> playback chain is identical.
     */
    private fun fallbackToOnDeviceTts(
        context: Context,
        amountRupees: Double,
        outputFile: File,
        callback: (String?) -> Unit
    ) {
        var tts: TextToSpeech? = null
        tts = TextToSpeech(context) { status ->
            if (status != TextToSpeech.SUCCESS) {
                Log.e(TAG, "TextToSpeech init failed with status $status")
                mainHandler.post { callback(null) }
                tts?.shutdown()
                return@TextToSpeech
            }
            tts?.language = Locale.forLanguageTag("en-IN")

            val utteranceId = "amount_${System.currentTimeMillis()}"
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(id: String?) {
                    Log.d(TAG, "TTS synthesis started")
                }
                override fun onDone(id: String?) {
                    Log.d(TAG, "TTS synthesis done, file exists: ${outputFile.exists()}, size: ${outputFile.length()}")
                    mainHandler.post { callback(outputFile.absolutePath) }
                    tts?.shutdown()
                }

                @Deprecated("Deprecated in API, required for interface")
                override fun onError(id: String?) {
                    Log.e(TAG, "TTS synthesis error")
                    mainHandler.post { callback(null) }
                    tts?.shutdown()
                }
            })

            val amountText = if (amountRupees == amountRupees.toLong().toDouble()) {
                amountRupees.toLong().toString()
            } else {
                "%.2f".format(amountRupees)
            }
            val phrase = "Rupees $amountText received"
            Log.d(TAG, "Synthesizing phrase: '$phrase' to ${outputFile.absolutePath}")

            val params = Bundle()
            val result = tts?.synthesizeToFile(phrase, params, outputFile, utteranceId)
            Log.d(TAG, "synthesizeToFile call returned: $result")
        }
    }

    private fun fileForAmount(context: Context, amountRupees: Double): File {
        val dir = File(context.filesDir, "voice_cache").apply { mkdirs() }
        // Round to 2dp in the filename so ₹500 and ₹500.00 share a cache entry.
        val key = "%.2f".format(amountRupees).replace(".", "_")
        return File(dir, "amount_$key.mp3")
    }
}
