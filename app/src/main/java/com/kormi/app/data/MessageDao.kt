package com.kormi.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)

    @Query("SELECT * FROM messages WHERE (senderId = :user1 AND receiverId = :user2) OR (senderId = :user2 AND receiverId = :user1) ORDER BY timestamp ASC")
    fun getChatMessages(user1: String, user2: String): Flow<List<Message>>

    // নতুন: চ্যাট ওপেন করলে মেসেজ রিড মার্ক করা
    @Query("UPDATE messages SET isRead = 1 WHERE senderId = :senderId AND receiverId = :receiverId")
    suspend fun markMessagesAsRead(senderId: String, receiverId: String)
}
