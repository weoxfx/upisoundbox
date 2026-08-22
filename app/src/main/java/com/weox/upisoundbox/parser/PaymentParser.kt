package com.weox.upisoundbox.parser

import com.weox.upisoundbox.BuildConfig

/** Result of successfully parsing a payment notification. */
data class PaymentEvent(
    val amountRupees: Double,
    val rawText: String,
    val sourceApp: String
)

/**
 * One parser per UPI app. Keeping these separate means a wording change
 * in one app's notifications (which happens often, without warning)
 * never risks breaking detection for the others.
 */
interface PaymentParser {
    val packageName: String

    /** Return a PaymentEvent if this notification looks like a "money received" event, else null. */
    fun tryParse(title: String?, text: String?): PaymentEvent?
}

/** Pulls the first rupee amount out of a string like "Rs.500 received" or "₹1,250.00 credited". */
internal fun extractRupeeAmount(input: String): Double? {
    val regex = Regex("""(?:₹|Rs\.?|INR)\s?([0-9,]+(?:\.[0-9]{1,2})?)""", RegexOption.IGNORE_CASE)
    val match = regex.find(input) ?: return null
    return match.groupValues[1].replace(",", "").toDoubleOrNull()
}

/** Registry of all supported parsers, keyed by package name for fast lookup. */
object ParserRegistry {
    private val parsers: Map<String, PaymentParser> = buildList {
        add(GPayParser())
        add(PhonePeParser())
        add(PaytmParser())
        // Test-self parser only exists in debug builds — never ships to real users.
        if (BuildConfig.DEBUG) {
            add(TestSelfParser(BuildConfig.APPLICATION_ID))
        }
    }.associateBy { it.packageName }

    fun forPackage(packageName: String): PaymentParser? = parsers[packageName]

    fun supportedPackages(): Set<String> = parsers.keys
}
