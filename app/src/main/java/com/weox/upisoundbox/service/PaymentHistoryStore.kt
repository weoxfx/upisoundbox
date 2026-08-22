package com.weox.upisoundbox.service

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.weox.upisoundbox.model.PaymentHistoryItem
import java.util.UUID

object PaymentHistoryStore {
    private const val PREFS_NAME = "upi_sound_box_prefs"
    private const val KEY_HISTORY = "payment_history"
    private const val MAX_ITEMS = 100
    private val gson = Gson()

    fun getHistory(context: Context): List<PaymentHistoryItem> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<PaymentHistoryItem>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addPayment(context: Context, item: PaymentHistoryItem) {
        val history = getHistory(context).toMutableList()
        history.add(0, item)
        if (history.size > MAX_ITEMS) {
            history.removeAt(history.size - 1)
        }
        saveHistory(context, history)
    }

    fun clearHistory(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_HISTORY).apply()
    }

    fun getTotalCount(context: Context): Int = getHistory(context).size

    fun getTodayCount(context: Context): Int {
        val now = System.currentTimeMillis()
        val dayMs = 86400000L
        return getHistory(context).count { it.timestamp > (now - dayMs) }
    }

    private fun saveHistory(context: Context, history: List<PaymentHistoryItem>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = gson.toJson(history)
        prefs.edit().putString(KEY_HISTORY, json).apply()
    }

    fun createItem(amountRupees: Double, sourceApp: String, rawText: String): PaymentHistoryItem {
        return PaymentHistoryItem(
            id = UUID.randomUUID().toString(),
            amountRupees = amountRupees,
            sourceApp = sourceApp,
            rawText = rawText,
            timestamp = System.currentTimeMillis()
        )
    }
}
