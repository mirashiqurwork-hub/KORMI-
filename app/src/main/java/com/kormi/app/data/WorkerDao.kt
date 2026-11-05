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

    @Query("SELECT * FROM workers WHERE workerId = :workerId")
    suspend fun getWorkerByCard(workerId: String): Worker?

    @Query("SELECT * FROM workers WHERE status = 'Active'")
    fun getAllActiveWorkers(): Flow<List<Worker>>

    // নতুন: অ্যাডমিনের জন্য সব কর্মীর তালিকা
    @Query("SELECT * FROM workers ORDER BY fullName ASC")
    fun getAllWorkers(): Flow<List<Worker>>

    // নতুন: কর্মী ডিলিট করার জন্য
    @Query("DELETE FROM workers WHERE workerId = :workerId")
    suspend fun deleteWorker(workerId: String)
}
