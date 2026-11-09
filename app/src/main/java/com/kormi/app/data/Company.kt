package com.kormi.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "companies")
data class Company(
    @PrimaryKey(autoGenerate = true) val companyId: Int = 0,
    val companyName: String,
    val logoUri: String? = null,
    val nightBillAmount: Double = 20.0,
    val shiftChangeDay: String = "FRIDAY",
    val lastRotationDate: String? = null // নতুন ফিল্ড (সর্বশেষ কবে পালা পরিবর্তন হয়েছে)
)
