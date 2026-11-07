package com.kormi.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "breakdowns")
data class Breakdown(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // YYYY-MM-DD
    val shift: String,
    val section: String,
    val machineName: String,
    val minutes: Int,
    val reason: String,
    val reportedBy: String // অফিসারের আইডি
)
