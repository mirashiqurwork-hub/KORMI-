package com.kormi.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val companyId: Int, // 0 মানে সব কোম্পানি (শুধুমাত্র অ্যাডমিনের জন্য)
    val targetUnit: String = "ALL UNIT", 
    val targetRole: String = "ALL", 
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false,
    val senderName: String = "System",
    val senderDesignation: String = ""
)
