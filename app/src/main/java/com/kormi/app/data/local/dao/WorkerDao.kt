package com.kormi.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kormi.app.data.local.entity.Worker

@Dao
interface WorkerDao {
    @Query("SELECT * FROM workers WHERE workerId = :id AND password = :pass LIMIT 1")
    suspend fun authenticate(id: String, pass: String): Worker?

    @Query("SELECT * FROM workers WHERE workerId = :id LIMIT 1")
    suspend fun getWorkerById(id: String): Worker?

    @Query("SELECT * FROM workers ORDER BY fullName ASC")
    suspend fun getAllWorkers(): List<Worker>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorker(worker: Worker)

    @Query("DELETE FROM workers WHERE workerId = :id")
    suspend fun deleteWorker(id: String)
}
