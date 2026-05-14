package com.fay.currencywidget.data.store

import android.content.Context
import com.fay.currencywidget.domain.model.CurrencyPair
import org.json.JSONObject

class CurrencyPairStore(private val context: Context) {

    private val prefs = context.getSharedPreferences("currency_pairs", Context.MODE_PRIVATE)

    // Sauvegarde une paire (depuis l'app ou depuis un widget)
    fun savePair(pair: CurrencyPair) {
        val json = JSONObject().apply {
            put("id", pair.id)
            put("fromCurrency", pair.fromCurrency)
            put("toCurrency", pair.toCurrency)
            put("widgetId", pair.widgetId ?: -1)
        }.toString()
        prefs.edit().putString("pair_${pair.id}", json).apply()
    }

    // Crée une nouvelle paire depuis l'app (sans widget)
    fun createPair(from: String, to: String): CurrencyPair {
        val id = System.currentTimeMillis().toInt()
        val pair = CurrencyPair(id = id, fromCurrency = from, toCurrency = to, widgetId = null)
        savePair(pair)
        return pair
    }

    // Associe un widgetId à une paire existante
    fun attachWidget(pairId: Int, widgetId: Int) {
        val pair = getPairById(pairId) ?: return
        savePair(pair.copy(widgetId = widgetId))
    }

    // Détache le widget d'une paire (widget supprimé)
    fun detachWidget(widgetId: Int) {
        val pair = getPairByWidgetId(widgetId) ?: return
        savePair(pair.copy(widgetId = null))
    }

    fun getPairById(id: Int): CurrencyPair? {
        val json = prefs.getString("pair_$id", null) ?: return null
        return parsePair(json)
    }

    fun getPairByWidgetId(widgetId: Int): CurrencyPair? {
        return getAllPairs().firstOrNull { it.widgetId == widgetId }
    }

    // Compatibilité avec l'ancien code widget
    fun getPair(widgetId: Int): CurrencyPair? = getPairByWidgetId(widgetId)

    fun deletePair(id: Int) {
        prefs.edit().remove("pair_$id").apply()
    }

    fun deleteAppPair(id: Int) = deletePair(id)

    fun getAllPairs(): List<CurrencyPair> {
        return prefs.all
            .filter { it.key.startsWith("pair_") }
            .mapNotNull { parsePair(it.value as? String ?: return@mapNotNull null) }
            .sortedBy { it.id }
    }

    private fun parsePair(json: String): CurrencyPair? {
        return try {
            val obj = JSONObject(json)
            val widgetId = obj.getInt("widgetId").takeIf { it != -1 }
            CurrencyPair(
                id = obj.getInt("id"),
                fromCurrency = obj.getString("fromCurrency"),
                toCurrency = obj.getString("toCurrency"),
                widgetId = widgetId
            )
        } catch (e: Exception) {
            null
        }
    }
}