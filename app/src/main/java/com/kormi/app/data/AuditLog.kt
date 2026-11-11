package com.kormi.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val action: String, // যেমন: "Worker Added", "Worker Edited"
    val details: String, // যেমন: "Added Worker: 503056"
    val performedBy: String, // অ্যাডমিনের নাম
    val timestamp: Long = System.currentTimeMillis()
)
