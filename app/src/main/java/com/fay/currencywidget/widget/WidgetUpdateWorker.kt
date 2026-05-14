package com.fay.currencywidget.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.fay.currencywidget.data.repository.ExchangeRateRepository
import com.fay.currencywidget.data.store.CurrencyPairStore
import java.net.UnknownHostException

class WidgetUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val store = CurrencyPairStore(applicationContext)
        val pairs = store.getAllPairs()

        if (pairs.isEmpty()) return Result.success()

        return try {
            val repository = ExchangeRateRepository()
            val prefs = applicationContext.getSharedPreferences(
                "widget_rates", Context.MODE_PRIVATE
            )
            val editor = prefs.edit()

            pairs.forEach { pair ->
                val result = repository.getRateWithVariation(
                    pair.fromCurrency,
                    pair.toCurrency
                )
                if (result != null) {
                    editor.putFloat("rate_${pair.id}", result.rate.toFloat())
                    editor.putFloat("variation_${pair.id}", result.variationPercent.toFloat())
                }
            }

            // Sauvegarde le timestamp de mise à jour
            editor.putLong("last_updated", System.currentTimeMillis())
            editor.apply()

            CurrencyWidgetProvider.updateAllWidgets(applicationContext)
            Result.success()

        } catch (e: UnknownHostException) {
            CurrencyWidgetProvider.showError(applicationContext, "Pas de connexion")
            Result.retry()
        } catch (e: Exception) {
            CurrencyWidgetProvider.showError(applicationContext, "Erreur réseau")
            Result.retry()
        }
    }
}