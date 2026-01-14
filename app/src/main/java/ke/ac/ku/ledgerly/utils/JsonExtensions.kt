package ke.ac.ku.ledgerly.utils;

import com.google.gson.Gson
import ke.ac.ku.ledgerly.data.model.ExchangeRateResponse

val gson = Gson()

fun ExchangeRateResponse.toJson(): String =
    gson.toJson(this)

fun String.toExchangeRateResponse(): ExchangeRateResponse =
    gson.fromJson(this, ExchangeRateResponse::class.java)
