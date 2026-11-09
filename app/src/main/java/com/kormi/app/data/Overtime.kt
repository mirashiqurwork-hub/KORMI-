package com.kormi.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "overtime")
data class Overtime(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val workerId: String,
    val date: String, // YYYY-MM-DD
    val hours: Double,
    val ratePerHour: Double = 0.0 // ওভারটাইমের রেট (পরে সেট করা যাবে)
)
