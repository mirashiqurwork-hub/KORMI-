package com.kormi.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workers")
data class Worker(
    @PrimaryKey
    val cardNumber: String, // কার্ড নম্বরই মূল আইডি
    val name: String,
    val unit: String,
    val section: String,
    val shift: String,
    val phone: String,
    val passwordHash: String, // পাসওয়ার্ড সরাসরি নয়, হ্যাশ করে রাখব
    val isActive: Boolean = true,
    val profileImageUri: String? = null // ছবির লিংক/URI
)
