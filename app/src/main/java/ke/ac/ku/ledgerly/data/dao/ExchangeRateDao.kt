package ke.ac.ku.ledgerly.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ke.ac.ku.ledgerly.data.model.ExchangeRateEntity

@Dao
interface ExchangeRateDao {

    @Query("SELECT * FROM exchange_rates WHERE baseCurrency = :baseCurrency LIMIT 1")
    suspend fun getRates(baseCurrency: String): ExchangeRateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRates(entity: ExchangeRateEntity)

    @Query("DELETE FROM exchange_rates")
    suspend fun clearAll()
}
