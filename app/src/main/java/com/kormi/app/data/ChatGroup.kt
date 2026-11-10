package com.kormi.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_groups")
data class ChatGroup(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val groupName: String,
    val createdBy: String,
    val members: String // কমা দিয়ে আলাদা করা ওয়ার্কার আইডি (যেমন: UPD1,UPD2)
)
