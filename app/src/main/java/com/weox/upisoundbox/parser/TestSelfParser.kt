package com.weox.upisoundbox.parser

/**
 * DEBUG/TEST ONLY. Lets the app post a notification to itself and run it
 * through the exact same detection -> parse -> TTS -> playback pipeline
 * as a real UPI app, without needing GPay/PhonePe/Paytm installed.
 *
 * Do NOT ship this parser in a release build — it means any notification
 * from your own app matching this text pattern will trigger a "payment"
 * sound, which is fine for testing but not something real users should have
 * enabled. Gate its registration behind BuildConfig.DEBUG (see ParserRegistry).
 */
class TestSelfParser(override val packageName: String) : PaymentParser {

    override fun tryParse(title: String?, text: String?): PaymentEvent? {
        val combined = listOfNotNull(title, text).joinToString(" ")
        if (!combined.contains("TESTPAY", ignoreCase = true)) return null

        val amount = extractRupeeAmount(combined) ?: return null
        return PaymentEvent(amount, combined, packageName)
    }
}
