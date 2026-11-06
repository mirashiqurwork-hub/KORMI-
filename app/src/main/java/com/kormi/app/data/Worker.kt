package com.kormi.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workers")
data class Worker(
    @PrimaryKey
    val workerId: String,
    val photo: String?,
    val fullName: String,
    val phone: String,
    val nidBirthCert: String?,
    val dateOfBirth: String?,
    val presentAddress: String?,
    val section: String,
    val shift: String,
    val weeklyOffDay: String,
    val status: String,
    val unit: String,
    val passwordHash: String = "1234",
    val dailyWage: Double? = 500.0 // ডিফল্ট দৈনিক বেতন ৫০০ টাকা
)
