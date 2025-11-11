package ke.ac.ku.ledgerly.data

import ke.ac.ku.ledgerly.data.model.ExchangeRateResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface ExchangeRateApi {
    @GET("v6/latest/{base}")
    suspend fun getRates(@Path("base") base: String): ExchangeRateResponse
}
