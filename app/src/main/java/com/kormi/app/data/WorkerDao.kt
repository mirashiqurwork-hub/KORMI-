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

    @Query("SELECT * FROM workers WHERE workerId = :id OR phone = :id")
    suspend fun getWorkerForLogin(id: String): Worker?

    @Query("SELECT * FROM workers WHERE companyId = :companyId ORDER BY fullName ASC")
    fun getWorkersByCompany(companyId: Int): Flow<List<Worker>>

    @Query("SELECT * FROM workers ORDER BY fullName ASC")
    fun getAllWorkers(): Flow<List<Worker>>

    // Manpower এবং Attendance স্ক্রিনের জন্য এই ২টি ফাংশন যুক্ত করা হলো
    @Query("SELECT * FROM workers WHERE status = 'Active'")
    fun getAllActiveWorkers(): Flow<List<Worker>>

    @Query("SELECT * FROM workers WHERE status = 'Active'")
    suspend fun getAllActiveWorkersList(): List<Worker>

    @Query("SELECT * FROM workers WHERE workerId = :workerId")
    suspend fun getWorkerByCard(workerId: String): Worker?

    @Query("UPDATE workers SET passwordHash = :newPassword WHERE workerId = :workerId")
    suspend fun updatePassword(workerId: String, newPassword: String)

    @Query("UPDATE workers SET photo = :photoUri WHERE workerId = :workerId")
    suspend fun updatePhoto(workerId: String, photoUri: String)

    @Query("DELETE FROM workers WHERE workerId = :workerId")
    suspend fun deleteWorker(workerId: String)
}
