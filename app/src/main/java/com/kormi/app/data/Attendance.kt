package com.kormi.app.data

import androidx.room.Entity

@Entity(tableName = "attendance", primaryKeys = ["workerId", "date"])
data class Attendance(
    val workerId: String,
    val date: String, // ফরম্যাট: YYYY-MM-DD
    val status: String // Present, Absent, Off Day
)
