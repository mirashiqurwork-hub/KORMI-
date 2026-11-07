package com.kormi.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BreakdownDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBreakdown(breakdown: Breakdown)

    @Query("SELECT * FROM breakdowns WHERE date = :date ORDER BY id DESC")
    fun getBreakdownsByDate(date: String): Flow<List<Breakdown>>
}
