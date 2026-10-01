package com.example.ai

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.example.data.model.TimetableClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class TimetableAiParser {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val colors = listOf(
        "#2563EB", "#0D9488", "#8B5CF6", "#D97706",
        "#EC4899", "#10B981", "#06B6D4", "#6366F1"
    )

    private fun resolveApiKey(customKey: String?): String {
        if (!customKey.isNullOrBlank()) return customKey.trim()
        val buildKey = BuildConfig.GEMINI_API_KEY
        if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") {
            return buildKey.trim()
        }
        return ""
    }

    suspend fun parseFromImage(
        bitmap: Bitmap,
        role: String = "STUDENT",
        customApiKey: String? = null
    ): Result<List<TimetableClass>> = withContext(Dispatchers.IO) {
        val apiKey = resolveApiKey(customApiKey)
        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                Exception("Gemini API key not found. Please enter your Gemini API key in Settings -> Gemini API Key to scan timetable images, or add your timetable manually.")
            )
        }

        try {
            val base64Image = bitmapToBase64(bitmap)
            val roleInstructions = if (role == "TEACHER") {
                "This is a Teacher/Lecturer teaching timetable. Extract the teaching units, assigned venue/hall/room, class/group name, and time slots."
            } else {
                "This is a Student class timetable. Extract course units, venue/room, lecture/lab category, instructor name, and time slots."
            }

            val prompt = """
                $roleInstructions
                Analyze this timetable image thoroughly and extract every scheduled session.
                Accurately read the text from the image. Do not invent or fabricate classes.
                Return ONLY a JSON array of objects with the following keys for each class:
                - "courseCode": String (Unit code or course title, e.g. "CS101", "MTH202")
                - "courseName": String (Full unit title or description)
                - "instructor": String (Lecturer/instructor or group name if indicated, or empty string)
                - "location": String (Venue, classroom, or hall e.g. "LH 101", "Lab 2", or empty string)
                - "dayOfWeek": Integer (1 for Monday, 2 for Tuesday, 3 for Wednesday, 4 for Thursday, 5 for Friday, 6 for Saturday, 7 for Sunday)
                - "startTime": String in 24-hour "HH:mm" format (e.g. "08:30", "14:00")
                - "endTime": String in 24-hour "HH:mm" format (e.g. "10:00", "16:00")
                - "category": String ("Lecture", "Lab", "Tutorial", "Seminar", or "Exam")

                Return ONLY raw JSON, with no markdown code blocks.
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                            put(JSONObject().apply {
                                put("inlineData", JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", base64Image)
                                })
                            })
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.1)
                    put("responseMimeType", "application/json")
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = requestJson.toString().toRequestBody(mediaType)
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Gemini API error (${response.code}): $responseBody"))
            }

            val classes = parseGeminiResponse(responseBody, role)
            if (classes.isNotEmpty()) {
                Result.success(classes)
            } else {
                Result.failure(Exception("No class sessions could be identified from this image. Please ensure the timetable is clearly visible or input units manually."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Scan failed: ${e.message ?: "Unknown error"}. You can input units manually."))
        }
    }

    suspend fun parseFromText(
        text: String,
        role: String = "STUDENT",
        customApiKey: String? = null
    ): Result<List<TimetableClass>> = withContext(Dispatchers.IO) {
        val apiKey = resolveApiKey(customApiKey)
        if (apiKey.isNotBlank()) {
            try {
                val prompt = """
                    Extract all recurring timetable sessions from the following text into a JSON array:
                    $text

                    Return ONLY a JSON array with these keys for each class:
                    - "courseCode": String
                    - "courseName": String
                    - "instructor": String
                    - "location": String (Venue)
                    - "dayOfWeek": Integer (1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri, 6=Sat, 7=Sun)
                    - "startTime": String in 24h format "HH:mm"
                    - "endTime": String in 24h format "HH:mm"
                    - "category": String ("Lecture", "Lab", "Tutorial", "Seminar", "Exam")

                    Do not add markdown fences.
                """.trimIndent()

                val requestJson = JSONObject().apply {
                    val contents = JSONArray().apply {
                        val contentObj = JSONObject().apply {
                            val parts = JSONArray().apply {
                                put(JSONObject().apply { put("text", prompt) })
                            }
                            put("parts", parts)
                        }
                        put("content", contentObj)
                    }
                    put("contents", contents)
                    put("generationConfig", JSONObject().apply {
                        put("temperature", 0.1)
                        put("responseMimeType", "application/json")
                    })
                }

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = requestJson.toString().toRequestBody(mediaType)
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

                val request = Request.Builder().url(url).post(body).build()
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val classes = parseGeminiResponse(responseBody, role)
                    if (classes.isNotEmpty()) {
                        return@withContext Result.success(classes)
                    }
                }
            } catch (_: Exception) {
            }
        }

        // Local regex parser without AI fallback
        val local = parseTextLocally(text, role)
        if (local.isNotEmpty()) {
            Result.success(local)
        } else {
            Result.failure(Exception("Could not parse schedule text. Please use format like 'Mon 09:00-10:30 CS101 Venue Hall A' or add units manually."))
        }
    }

    private fun parseGeminiResponse(jsonString: String, role: String): List<TimetableClass> {
        val result = mutableListOf<TimetableClass>()
        try {
            val root = JSONObject(jsonString)
            val candidates = root.optJSONArray("candidates") ?: return emptyList()
            val firstCandidate = candidates.optJSONObject(0) ?: return emptyList()
            val content = firstCandidate.optJSONObject("content") ?: return emptyList()
            val parts = content.optJSONArray("parts") ?: return emptyList()
            val rawText = parts.optJSONObject(0)?.optString("text") ?: return emptyList()

            val startIndex = rawText.indexOf('[')
            val endIndex = rawText.lastIndexOf(']')
            if (startIndex == -1 || endIndex == -1 || endIndex <= startIndex) {
                return emptyList()
            }
            val jsonArray = JSONArray(rawText.substring(startIndex, endIndex + 1))
            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val code = item.optString("courseCode", "UNIT").ifBlank { "UNIT" }
                val name = item.optString("courseName", code)
                val instructor = item.optString("instructor", "")
                val location = item.optString("location", "")
                var day = item.optInt("dayOfWeek", 1)
                if (day !in 1..7) day = 1
                val start = item.optString("startTime", "09:00")
                val end = item.optString("endTime", "10:30")
                val category = item.optString("category", "Lecture")
                val colorHex = colors[i % colors.size]

                result.add(
                    TimetableClass(
                        courseCode = code.trim().uppercase(),
                        courseName = name.trim(),
                        instructor = instructor.trim(),
                        location = location.trim(),
                        dayOfWeek = day,
                        startTime = normalizeTime(start),
                        endTime = normalizeTime(end),
                        colorHex = colorHex,
                        category = category,
                        role = role
                    )
                )
            }
        } catch (_: Exception) {
        }
        return result
    }

    private fun normalizeTime(raw: String): String {
        val trimmed = raw.trim()
        val parts = trimmed.split(":")
        if (parts.size >= 2) {
            val h = parts[0].toIntOrNull() ?: 9
            val m = parts[1].take(2).toIntOrNull() ?: 0
            return String.format("%02d:%02d", h, m)
        }
        return "09:00"
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        val maxDim = 1280
        val scaled = if (bitmap.width > maxDim || bitmap.height > maxDim) {
            val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
            val (w, h) = if (bitmap.width > bitmap.height) {
                Pair(maxDim, (maxDim / ratio).toInt().coerceAtLeast(1))
            } else {
                Pair((maxDim * ratio).toInt().coerceAtLeast(1), maxDim)
            }
            Bitmap.createScaledBitmap(bitmap, w, h, true)
        } else {
            bitmap
        }
        scaled.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    private fun parseTextLocally(text: String, role: String): List<TimetableClass> {
        val list = mutableListOf<TimetableClass>()
        val lines = text.lines()
        var colorIdx = 0

        val daysMap = mapOf(
            "mon" to 1, "tue" to 2, "wed" to 3, "thu" to 4, "fri" to 5, "sat" to 6, "sun" to 7
        )

        for (line in lines) {
            val l = line.trim()
            if (l.isBlank()) continue

            var foundDay = 1
            for ((key, dayVal) in daysMap) {
                if (l.contains(key, ignoreCase = true)) {
                    foundDay = dayVal
                    break
                }
            }

            val timeRegex = Regex("""(\d{1,2}:\d{2})\s*(?:-|to)\s*(\d{1,2}:\d{2})""")
            val match = timeRegex.find(l)
            val startTime = match?.groupValues?.getOrNull(1) ?: "09:00"
            val endTime = match?.groupValues?.getOrNull(2) ?: "10:30"

            val codeRegex = Regex("""[A-Z]{2,6}\s*[-]?\s*\d{2,4}[A-Z]?""")
            val codeMatch = codeRegex.find(l)
            val courseCode = codeMatch?.value ?: "UNIT"

            val remaining = l.replace(courseCode, "")
                .replace(startTime, "")
                .replace(endTime, "")
                .replace("-", "")
                .trim()

            val courseName = if (remaining.isNotBlank()) remaining else "$courseCode Unit"

            list.add(
                TimetableClass(
                    courseCode = courseCode,
                    courseName = courseName,
                    dayOfWeek = foundDay,
                    startTime = normalizeTime(startTime),
                    endTime = normalizeTime(endTime),
                    colorHex = colors[colorIdx % colors.size],
                    category = if (l.contains("lab", ignoreCase = true)) "Lab" else "Lecture",
                    role = role
                )
            )
            colorIdx++
        }
        return list
    }
}
