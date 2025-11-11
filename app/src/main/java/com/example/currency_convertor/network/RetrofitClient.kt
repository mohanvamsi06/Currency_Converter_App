package com.example.currency_convertor.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://v6.exchangerate-api.com/v6/8646b33e5f4c52dd588eeab0/"

    val instance: CurrencyApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(CurrencyApiService::class.java)
    }
}
