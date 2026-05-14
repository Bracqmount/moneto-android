package com.fay.currencywidget.data.repository

import com.fay.currencywidget.di.NetworkModule
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class RateWithVariation(
    val rate: Double,
    val variationPercent: Double  // ex: +0.14 ou -0.09
)

class ExchangeRateRepository {

    private val api = NetworkModule.exchangeRateApi

    suspend fun getRate(from: String, to: String): Double? {
        return try {
            val response = api.getRate(from, to)
            response.rates[to]
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getRateWithVariation(from: String, to: String): RateWithVariation? {
        return try {
            // Taux actuel
            val currentResponse = api.getRate(from, to)
            val currentRate = currentResponse.rates[to] ?: return null

            // Taux d'hier
            val yesterday = getLastBusinessDay()
            val previousResponse = api.getRateAtDate(yesterday, from, to)
            val previousRate = previousResponse.rates[to] ?: return null

            // Calcul variation en %
            val variationPercent = ((currentRate - previousRate) / previousRate) * 100

            RateWithVariation(
                rate = currentRate,
                variationPercent = variationPercent
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun getLastBusinessDay(): String {
        val calendar = Calendar.getInstance()
        // Recule jusqu'au dernier jour ouvré (lundi→vendredi)
        do {
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        } while (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
            calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)

        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }

    suspend fun getAvailableCurrencies(): List<String> {
        return try {
            api.getCurrencies().keys.sorted()
        } catch (e: Exception) {
            listOf("AUD", "CAD", "CHF", "CNY", "EUR", "GBP", "JPY", "TRY", "USD")
        }
    }

    suspend fun getCurrenciesWithNames(): Map<String, String> {
        return try {
            api.getCurrencies().toSortedMap()
        } catch (e: Exception) {
            mapOf(
                "AUD" to "Australian Dollar",
                "CAD" to "Canadian Dollar",
                "CHF" to "Swiss Franc",
                "CNY" to "Chinese Renminbi Yuan",
                "EUR" to "Euro",
                "GBP" to "British Pound",
                "JPY" to "Japanese Yen",
                "TRY" to "Turkish Lira",
                "USD" to "US Dollar"
            )
        }
    }
}