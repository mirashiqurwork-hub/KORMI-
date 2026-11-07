package com.kormi.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workers")
data class Worker(
    @PrimaryKey
    val workerId: String, // Card Number / Email
    val fullName: String,
    val role: String, // Worker, Officer, Management, Admin
    val designation: String, // Helper, APO, PM, ইত্যাদি
    val companyId: Int, // কোন কোম্পানিতে কাজ করে
    
    val photo: String? = null,
    val phone: String = "",
    val nidBirthCert: String? = null,
    val dateOfBirth: String? = null,
    val presentAddress: String? = null,
    val latitude: Double? = null, // গুগল ম্যাপ লোকেশন
    val longitude: Double? = null,
    
    val shift: String? = null, // P/A, P/B, P/C, All Shift
    val section: String? = null, // Backside, Ring, Autocone, Packing, All Section
    val unit: String? = null, // Unit-01, Unit-02, G/F, F/F, All Unit
    val weeklyOffDay: String? = null,
    
    val status: String = "Pending", // Active, Inactive, Pending
    val salary: Double = 0.0, // সবার বেতন
    val passwordHash: String = "1234",
    
    // এক্সেস কন্ট্রোল (ভবিষ্যতে কাজে লাগবে)
    val accessUnits: String? = null, // কমা দিয়ে আলাদা করা
    val accessSections: String? = null
)
