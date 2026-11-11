package com.kormi.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Worker::class, Company::class, Attendance::class, Message::class, Breakdown::class, Production::class, Notification::class, Overtime::class, ChatGroup::class, AuditLog::class], version = 18, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workerDao(): WorkerDao
    abstract fun companyDao(): CompanyDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun messageDao(): MessageDao
    abstract fun breakdownDao(): BreakdownDao
    abstract fun productionDao(): ProductionDao
    abstract fun notificationDao(): NotificationDao
    abstract fun overtimeDao(): OvertimeDao
    abstract fun chatGroupDao(): ChatGroupDao
    abstract fun auditLogDao(): AuditLogDao

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
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
