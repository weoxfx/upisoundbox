package com.weox.upisoundbox.model

import java.util.Date

data class PaymentHistoryItem(
    val id: String,
    val amountRupees: Double,
    val sourceApp: String,
    val rawText: String,
    val timestamp: Long
) {
    val displayAmount: String
        get() = if (amountRupees == amountRupees.toLong().toDouble()) {
            "₹${amountRupees.toLong()}"
        } else {
            "₹%.2f".format(amountRupees)
        }

    val timeAgo: String
        get() {
            val now = System.currentTimeMillis()
            val diff = now - timestamp
            return when {
                diff < 60000 -> "Just now"
                diff < 3600000 -> "${diff / 60000} min ago"
                diff < 86400000 -> "${diff / 3600000} hr ago"
                else -> "${diff / 86400000} days ago"
            }
        }
}
