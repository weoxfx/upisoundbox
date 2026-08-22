package com.weox.upisoundbox.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.weox.upisoundbox.model.PaymentHistoryItem
import com.weox.upisoundbox.net.VoiceCache
import com.weox.upisoundbox.parser.ParserRegistry
import com.weox.upisoundbox.parser.PaymentEvent

class UpiNotificationListenerService : NotificationListenerService() {

    private lateinit var audioManager: AudioManager

    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        Log.d(TAG, "Service onCreate — listener process started")
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "onListenerConnected — system has bound the listener")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "onListenerDisconnected — system unbound the listener")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkg = sbn.packageName
        Log.d(TAG, "onNotificationPosted from package: $pkg")

        val parser = ParserRegistry.forPackage(pkg)
        if (parser == null) {
            Log.d(TAG, "No parser registered for $pkg, ignoring")
            return
        }

        val isSelfTest = pkg == applicationContext.packageName
        if (!isSelfTest) {
            val selectedApps = SelectedAppsStore.getSelected(applicationContext)
            if (pkg !in selectedApps) return
        }

        val extras = sbn.notification.extras
        val title = extras.getCharSequence("android.title")?.toString()
        val text = extras.getCharSequence("android.text")?.toString()
        Log.d(TAG, "Notification content — title: '$title', text: '$text'")

        val event = parser.tryParse(title, text)
        if (event == null) {
            Log.d(TAG, "Parser did not recognize this as a payment notification")
            return
        }
        Log.d(TAG, "Parsed payment: $event")

        saveToHistory(event)
        handlePaymentEvent(event)
    }

    private fun saveToHistory(event: PaymentEvent) {
        val item = PaymentHistoryStore.createItem(
            amountRupees = event.amountRupees,
            sourceApp = event.sourceApp,
            rawText = event.rawText
        )
        PaymentHistoryStore.addPayment(applicationContext, item)
    }

    private fun handlePaymentEvent(event: PaymentEvent) {
        VoiceCache.getAudioForAmount(applicationContext, event.amountRupees) { audioFilePath ->
            if (audioFilePath == null) {
                Log.e(TAG, "No audio available for amount ${event.amountRupees}")
                return@getAudioForAmount
            }
            playAlert(audioFilePath)
        }
    }

    private fun playAlert(filePath: String) {
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
        audioManager.setStreamVolume(
            AudioManager.STREAM_ALARM,
            maxVolume,
            0
        )
        Log.d(TAG, "Set STREAM_ALARM volume to max ($maxVolume)")

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()

        val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(audioAttributes)
            .build()
        audioManager.requestAudioFocus(focusRequest)

        try {
            MediaPlayer().apply {
                setAudioAttributes(audioAttributes)
                setDataSource(filePath)
                setOnCompletionListener {
                    audioManager.abandonAudioFocusRequest(focusRequest)
                    release()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what extra=$extra")
                    true
                }
                prepare()
                start()
                Log.d(TAG, "Playback started for $filePath")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Playback failed", e)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // No-op — we only care about notifications as they arrive.
    }

    companion object {
        private const val TAG = "UpiSoundBox"
    }
}
