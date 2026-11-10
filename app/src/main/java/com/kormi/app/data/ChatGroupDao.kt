package com.kormi.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatGroupDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: ChatGroup)

    @Query("SELECT * FROM chat_groups")
    fun getAllGroups(): Flow<List<ChatGroup>>

    // নির্দিষ্ট ইউজার যে গ্রুপগুলোতে আছে, সেগুলো খোঁজা
    @Query("SELECT * FROM chat_groups WHERE members LIKE '%' || :userId || '%'")
    fun getGroupsByUser(userId: String): Flow<List<ChatGroup>>

    @Query("SELECT * FROM chat_groups WHERE id = :groupId")
    suspend fun getGroupById(groupId: Int): ChatGroup?
}
