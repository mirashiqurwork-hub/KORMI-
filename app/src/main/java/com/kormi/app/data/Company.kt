package com.kormi.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "companies")
data class Company(
    @PrimaryKey(autoGenerate = true) val companyId: Int = 0,
    val companyName: String,
    val logoUri: String? = null,
    val nightBillAmount: Double = 20.0, // ডিফল্ট নাইট বিল
    val shiftChangeDay: String = "FRIDAY" // ডিফল্ট পালা পরিবর্তনের দিন
)
