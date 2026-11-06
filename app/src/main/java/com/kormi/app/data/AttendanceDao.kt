package com.kormi.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AttendanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(attendance: Attendance)

    @Query("SELECT * FROM attendance WHERE date = :date")
    suspend fun getAttendanceByDate(date: String): List<Attendance>

    // নতুন: একজন কর্মীর সব অ্যাটেনডেন্স দেখা
    @Query("SELECT * FROM attendance WHERE workerId = :workerId")
    suspend fun getWorkerAttendance(workerId: String): List<Attendance>
}
