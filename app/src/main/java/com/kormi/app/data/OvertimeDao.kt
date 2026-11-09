package com.kormi.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OvertimeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOvertime(overtime: Overtime)

    // নির্দিষ্ট কর্মীর সব ওভারটাইম
    @Query("SELECT * FROM overtime WHERE workerId = :workerId")
    suspend fun getOvertimeByWorker(workerId: String): List<Overtime>

    // নির্দিষ্ট মাসের ওভারটাইম (মাসিক বেতন হিসাবের জন্য)
    @Query("SELECT * FROM overtime WHERE workerId = :workerId AND date LIKE :monthStr || '%'")
    suspend fun getMonthlyOvertime(workerId: String, monthStr: String): List<Overtime>
}
