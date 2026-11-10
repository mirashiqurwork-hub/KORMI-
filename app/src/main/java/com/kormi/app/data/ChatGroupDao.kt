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

    @Query("SELECT * FROM chat_groups WHERE id = :groupId")
    suspend fun getGroupById(groupId: Int): ChatGroup?
}
