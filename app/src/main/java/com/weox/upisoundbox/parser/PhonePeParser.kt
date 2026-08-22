package com.weox.upisoundbox.parser

/**
 * PhonePe notifications commonly read like:
 *   "You received ₹500 from Rohit Sharma"
 * or split as title "Payment received" / text "₹500 from Rohit Sharma".
 *
 * Verify against a real device — this is a starting guess, not confirmed wording.
 */
class PhonePeParser : PaymentParser {
    override val packageName = "com.phonepe.app"

    override fun tryParse(title: String?, text: String?): PaymentEvent? {
        val combined = listOfNotNull(title, text).joinToString(" ")
        if (!combined.contains("received", ignoreCase = true) &&
            !combined.contains("credited", ignoreCase = true)
        ) return null
        if (combined.contains("sent", ignoreCase = true) ||
            combined.contains("debited", ignoreCase = true) ||
            combined.contains("failed", ignoreCase = true)
        ) return null

        val amount = extractRupeeAmount(combined) ?: return null
        return PaymentEvent(amount, combined, packageName)
    }
}
