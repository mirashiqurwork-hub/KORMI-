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

    // ১-১ চ্যাট
    @Query("SELECT * FROM messages WHERE groupId IS NULL AND ((senderId = :user1 AND receiverId = :user2) OR (senderId = :user2 AND receiverId = :user1)) ORDER BY timestamp ASC")
    fun getChatMessages(user1: String, user2: String): Flow<List<Message>>

    // গ্রুপ চ্যাট
    @Query("SELECT * FROM messages WHERE groupId = :groupId ORDER BY timestamp ASC")
    fun getGroupMessages(groupId: Int): Flow<List<Message>>

    // ১-১ চ্যাট রিড মার্ক
    @Query("UPDATE messages SET isRead = 1 WHERE groupId IS NULL AND senderId = :senderId AND receiverId = :receiverId")
    suspend fun markMessagesAsRead(senderId: String, receiverId: String)
}
