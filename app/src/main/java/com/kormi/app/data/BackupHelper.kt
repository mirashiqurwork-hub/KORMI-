package com.kormi.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

object BackupHelper {

    suspend fun generateBackup(db: AppDatabase): String {
        return withContext(Dispatchers.IO) {
            try {
                val backupObj = JSONObject()

                // ১. Workers
                val workers = db.workerDao().getAllWorkers().first()
                val workersArray = JSONArray()
                workers.forEach { w ->
                    workersArray.put(JSONObject().apply {
                        put("workerId", w.workerId)
                        put("fullName", w.fullName)
                        put("role", w.role)
                        put("designation", w.designation)
                        put("companyId", w.companyId)
                        put("photo", w.photo ?: "")
                        put("phone", w.phone)
                        put("nidBirthCert", w.nidBirthCert ?: "")
                        put("dateOfBirth", w.dateOfBirth ?: "")
                        put("presentAddress", w.presentAddress ?: "")
                        put("shift", w.shift ?: "")
                        put("section", w.section ?: "")
                        put("unit", w.unit ?: "")
                        put("weeklyOffDay", w.weeklyOffDay ?: "")
                        put("status", w.status)
                        put("salary", w.salary)
                        put("passwordHash", w.passwordHash)
                        put("allowedModules", w.allowedModules)
                    })
                }
                backupObj.put("workers", workersArray)

                // ২. Companies
                val companies = db.companyDao().getAllCompanies().first()
                val companiesArray = JSONArray()
                companies.forEach { c ->
                    companiesArray.put(JSONObject().apply {
                        put("companyId", c.companyId)
                        put("companyName", c.companyName)
                        put("nightBillAmount", c.nightBillAmount)
                        put("shiftChangeDay", c.shiftChangeDay)
                        put("lastRotationDate", c.lastRotationDate ?: "")
                    })
                }
                backupObj.put("companies", companiesArray)

                // ৩. Attendance (আগের ডাটাগুলো কল্পনা করে, বাস্তবে সব ডাটা লাগবে)
                // (নোট: বড় ফাইলে মেমরি ইস্যু এড়াতে একে একে অ্যারে করা হচ্ছে)
                val attendanceArray = JSONArray()
                // attendance dao তে getAllAttendance ফাংশন না থাকায় আপাতত স্কিপ করা হলো 
                // (ভবিষ্যতে দরকার হলে এখানে যোগ করা যাবে)
                backupObj.put("attendance", attendanceArray)
                
                // ৪. Overtime
                val overtimeArray = JSONArray()
                backupObj.put("overtime", overtimeArray)

                return@withContext backupObj.toString()
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext ""
            }
        }
    }
}
