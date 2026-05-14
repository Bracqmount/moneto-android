package com.fay.currencywidget.data.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ExchangeRateApi {

    @GET("latest")
    suspend fun getRate(
        @Query("from") from: String,
        @Query("to") to: String
    ): ExchangeRateResponse

    @GET("currencies")
    suspend fun getCurrencies(): Map<String, String>

    @GET("{date}")
    suspend fun getRateAtDate(
        @Path("date") date: String,
        @Query("from") from: String,
        @Query("to") to: String
    ): ExchangeRateResponse
}