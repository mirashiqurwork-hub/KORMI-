package com.kormi.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "production")
data class Production(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // YYYY-MM-DD
    val unit: String,
    val shift: String,
    val section: String, // BACKSIDE, RING, PACKING, ইত্যাদি
    val machineName: String, // C/D, B/D, F/D, S/F, R/F, L/C
    val weightKg: Double,
    val packingBags: Int = 0,
    val packingWeightKg: Double = 0.0,
    val inchargeName: String = "", // শিফট ইনচার্জের নাম
    val reportedBy: String // অফিসারের আইডি
)
