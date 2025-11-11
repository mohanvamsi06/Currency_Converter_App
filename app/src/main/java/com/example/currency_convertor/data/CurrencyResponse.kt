package com.example.currency_convertor.data

import com.google.gson.annotations.SerializedName

data class CurrencyResponse(
    @SerializedName("result") val result: String,
    @SerializedName("base_code") val baseCode: String,
    @SerializedName("target_code") val targetCode: String,
    @SerializedName("conversion_rate") val conversionRate: Double
)
