package com.weox.upisoundbox.parser

/**
 * Paytm notifications commonly read like:
 *   "Payment received: ₹500 from Rohit Sharma"
 * or "₹500 credited to your Paytm account".
 *
 * Verify against a real device — this is a starting guess, not confirmed wording.
 */
class PaytmParser : PaymentParser {
    override val packageName = "net.one97.paytm"

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
