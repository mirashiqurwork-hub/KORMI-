package com.kormi.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorker(worker: Worker)

    @Query("SELECT * FROM workers WHERE cardNumber = :cardNumber")
    suspend fun getWorkerByCard(cardNumber: String): Worker?

    @Query("SELECT * FROM workers WHERE isActive = 1")
    fun getAllActiveWorkers(): Flow<List<Worker>>
}
