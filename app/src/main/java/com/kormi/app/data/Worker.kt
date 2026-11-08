package com.kormi.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workers")
data class Worker(
    @PrimaryKey
    val workerId: String, 
    val fullName: String,
    val role: String, 
    val designation: String, 
    val companyId: Int, 
    
    val photo: String? = null,
    val phone: String = "",
    val nidBirthCert: String? = null,
    val dateOfBirth: String? = null,
    val presentAddress: String? = null,
    val latitude: Double? = null, 
    val longitude: Double? = null,
    
    val shift: String? = null, 
    val section: String? = null, 
    val unit: String? = null, 
    val weeklyOffDay: String? = null,
    
    val status: String = "Pending", 
    val salary: Double = 0.0, 
    val passwordHash: String = "1234",
    
    val accessUnits: String? = null, 
    val accessSections: String? = null,
    
    // নতুন: কোন কোন মডিউল দেখতে পারবে (কমা দিয়ে আলাদা করা)
    val allowedModules: String = "Workers,Attendance,Manpower,Production,Breakdown,Messages,Location,Notifications"
)
