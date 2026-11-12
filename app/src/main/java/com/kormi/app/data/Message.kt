package com.kormi.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderId: String,
    val receiverId: String, 
    val groupId: Int? = null, 
    val messageText: String,
    val imageUri: String? = null, // 🌟 নতুন ফিল্ড: ছবি পাঠানোর জন্য
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
