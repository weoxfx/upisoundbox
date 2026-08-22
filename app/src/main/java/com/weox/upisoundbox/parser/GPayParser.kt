package com.weox.upisoundbox.parser

/**
 * GPay notification text is typically something like:
 *   Title: "₹500 received"
 *   Text:  "From Rohit Sharma"
 * or a combined line: "You received ₹500 from Rohit Sharma"
 *
 * NOTE: verify current wording against a real device — GPay has changed
 * this copy before and will again. Keep this file as the single place
 * to patch when it does.
 */
class GPayParser : PaymentParser {
    override val packageName = "com.google.android.apps.nbu.paisa.user"

    override fun tryParse(title: String?, text: String?): PaymentEvent? {
        val combined = listOfNotNull(title, text).joinToString(" ")
        if (!combined.contains("received", ignoreCase = true)) return null
        // Skip outgoing/failed payment notifications explicitly.
        if (combined.contains("sent", ignoreCase = true) ||
            combined.contains("failed", ignoreCase = true)
        ) return null

        val amount = extractRupeeAmount(combined) ?: return null
        return PaymentEvent(amount, combined, packageName)
    }
}
