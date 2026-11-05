package com.kormi.app.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Worker::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workerDao(): WorkerDao
}
