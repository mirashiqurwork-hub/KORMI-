package com.kormi.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workers")
data class Worker(
    @PrimaryKey
    val workerId: String, // Card Number
    val photo: String?,
    val fullName: String,
    val phone: String,
    val nidBirthCert: String?,
    val dateOfBirth: String?,
    val presentAddress: String?,
    val section: String,
    val shift: String,
    val weeklyOffDay: String,
    val status: String, // "Active" or "Inactive"
    val unit: String,
    val passwordHash: String = "1234" // আপাতত ডিফল্ট পাসওয়ার্ড, পরে অ্যাডমিন প্যানেল থেকে সেট করা যাবে
)
