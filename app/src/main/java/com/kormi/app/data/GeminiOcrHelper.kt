package com.kormi.app.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object GeminiOcrHelper {

    // আপনার দেওয়া পার্মানেন্ট API Key
    private const val API_KEY = "AQ.Ab8RN6JUc09SScgnlfekGJZDoE-kBmmPdiPT0qouod7dVYQ-fg"
    
    // Gemini 1.5 Flash মডেল (দ্রুত রেসপন্সের জন্য)
    private const val MODEL = "gemini-1.5-flash"
    private const val API_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent?key=$API_KEY"

    suspend fun extractWorkersFromImage(base64Image: String): List<ParsedWorker> {
        return withContext(Dispatchers.IO) {
            val result = mutableListOf<ParsedWorker>()
            try {
                val url = URL(API_URL)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true

                // AI-কে দেওয়া নির্দেশনা (Prompt)
                val promptText = """
                    You are an expert data entry assistant for a spinning mill.
                    Extract the worker data from this table image.
                    Return a JSON array. Each object must have these fields:
                    - id (string): The employee ID or card number (e.g., UCPD212593 or 503056).
                    - name (string): The worker's full name.
                    - phone (string): The phone number if present, else empty.
                    - section (string): One of: BACKSIDE, RING, AUTOCONE, PACKING.
                    - shift (string): One of: P/A, P/B, P/C.
                    - unit (string): One of: UNIT-01, UNIT-02, G/F, F/F.
                    If a field is not present in the image, leave it as an empty string.
                """.trimIndent()

                val jsonPayload = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().put("text", promptText))
                                put(JSONObject().apply {
                                    put("inline_data", JSONObject().apply {
                                        put("mime_type", "image/jpeg")
                                        put("data", base64Image)
                                    })
                                })
                            })
                        })
                    })
                    put("generationConfig", JSONObject().apply {
                        put("response_mime_type", "application/json")
                        put("response_schema", JSONObject().apply {
                            put("type", "ARRAY")
                            put("items", JSONObject().apply {
                                put("type", "OBJECT")
                                put("properties", JSONObject().apply {
                                    put("id", JSONObject().put("type", "STRING"))
                                    put("name", JSONObject().put("type", "STRING"))
                                    put("phone", JSONObject().put("type", "STRING"))
                                    put("section", JSONObject().put("type", "STRING"))
                                    put("shift", JSONObject().put("type", "STRING"))
                                    put("unit", JSONObject().put("type", "STRING"))
                                })
                            })
                        })
                    })
                }

                val os = OutputStreamWriter(conn.outputStream)
                os.write(jsonPayload.toString())
                os.flush()
                os.close()

                if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(conn.inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()

                    // রেসপন্স পার্স করা
                    val responseJson = JSONObject(response.toString())
                    val text = responseJson.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")

                    val jsonArray = JSONArray(text)
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        result.add(
                            ParsedWorker(
                                id = obj.optString("id", ""),
                                name = obj.optString("name", ""),
                                phone = obj.optString("phone", ""),
                                section = obj.optString("section", "BACKSIDE"),
                                shift = obj.optString("shift", "P/A"),
                                unit = obj.optString("unit", "UNIT-01")
                            )
                        )
                    }
                } else {
                    Log.e("GeminiError", "HTTP Error: ${conn.responseCode} - ${conn.errorStream?.bufferedReader()?.readText()}")
                }
            } catch (e: Exception) {
                Log.e("GeminiError", "Exception: ${e.message}", e)
            }
            result
        }
    }
}

data class ParsedWorker(
    var id: String,
    var name: String,
    var phone: String,
    var section: String,
    var shift: String,
    var unit: String
)
