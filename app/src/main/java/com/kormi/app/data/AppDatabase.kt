package com.kormi.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Worker::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workerDao(): WorkerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kormi_database"
                )
                .fallbackToDestructiveMigration() // ডেভেলপমেন্টের সময় পুরনো ডাটা মুছে নতুন স্ট্রাকচার নেবে
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
