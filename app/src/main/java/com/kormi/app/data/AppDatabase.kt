package com.kormi.app.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kormi.app.data.local.dao.WorkerDao
import com.kormi.app.data.local.entity.Worker

@Database(
    entities = [
        Worker::class
    ],
    version = 19,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workerDao(): WorkerDao
}
