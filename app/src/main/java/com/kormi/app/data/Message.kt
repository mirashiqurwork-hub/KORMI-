package com.kormi.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderId: String,
    val receiverId: String, // ১-১ চ্যাটের জন্য
    val groupId: Int? = null, // গ্রুপ চ্যাটের জন্য (যদি গ্রুপ হয়)
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
