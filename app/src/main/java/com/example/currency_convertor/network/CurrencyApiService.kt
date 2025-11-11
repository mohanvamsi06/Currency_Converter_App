package com.example.currency_convertor.network

import com.example.currency_convertor.data.CurrencyResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface CurrencyApiService {
    @GET("pair/{base_code}/{target_code}")
    suspend fun getConversionRate(
        @Path("base_code") baseCode: String,
        @Path("target_code") targetCode: String
    ): CurrencyResponse
}
