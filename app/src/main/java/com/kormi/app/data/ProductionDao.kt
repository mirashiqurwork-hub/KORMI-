package com.kormi.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduction(production: Production)

    // নির্দিষ্ট তারিখের সব প্রোডাকশন
    @Query("SELECT * FROM production WHERE date = :date ORDER BY id DESC")
    fun getProductionByDate(date: String): Flow<List<Production>>

    // নির্দিষ্ট তারিখ ও ইউনিটের প্রোডাকশন
    @Query("SELECT * FROM production WHERE date = :date AND unit = :unit ORDER BY id DESC")
    fun getProductionByDateAndUnit(date: String, unit: String): Flow<List<Production>>
}
