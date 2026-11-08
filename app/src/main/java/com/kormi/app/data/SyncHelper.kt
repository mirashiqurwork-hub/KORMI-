package com.kormi.app.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

object SyncHelper {
    // এই URL-টি আপনার Apps Script এর Web App URL
    const val APPS_SCRIPT_URL = "https://script.google.com/macros/s/AKfycbwlY0TWH1ogsZ43ieZIRGxl8a8Hf_dByxTQQ4JofSI9G8GnlASUDHmwwIlcUrjX9aVQjQ/exec"

    suspend fun postData(action: String, jsonArray: JSONArray): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(APPS_SCRIPT_URL)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json")

                val payload = JSONObject()
                payload.put("action", action)
                payload.put("data", jsonArray)

                val os: OutputStream = conn.outputStream
                os.write(payload.toString().toByteArray())
                os.flush()
                os.close()

                val responseCode = conn.responseCode
                responseCode == HttpURLConnection.HTTP_OK
            } catch (e: Exception) {
                Log.e("SyncError", e.toString())
                false
            }
        }
    }
}
