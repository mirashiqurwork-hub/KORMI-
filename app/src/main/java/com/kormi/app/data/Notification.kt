package com.kormi.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val companyId: Int, // কোন কোম্পানির নোটিশ
    val targetUnit: String = "ALL UNIT", // কোন ইউনিটের জন্য (ALL UNIT, UNIT-01, ইত্যাদি)
    val targetRole: String = "ALL", // কার জন্য (ALL, Worker, Officer, Management)
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val senderName: String = "System",
    val senderDesignation: String = ""
)
