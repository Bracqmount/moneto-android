package com.fay.currencywidget.domain.model

data class CurrencyPair(
    val id: Int,
    val fromCurrency: String,
    val toCurrency: String,
    val widgetId: Int? = null   // null = pas de widget associé
)