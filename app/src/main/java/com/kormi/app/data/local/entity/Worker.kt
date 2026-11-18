package com.kormi.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    ADMIN,
    MANAGEMENT,
    OFFICER,
    WORKER,
    PENDING
}

@Entity(tableName = "workers")
data class Worker(
    @PrimaryKey val workerId: String,
    val fullName: String,
    val password: String,
    val role: UserRole,
    val phone: String = "",
    val department: String = "",
    val section: String = "",
    val shift: String = "",
    val cardNo: String = "",
    val salary: Double = 0.0,
    val isActive: Boolean = true
)
