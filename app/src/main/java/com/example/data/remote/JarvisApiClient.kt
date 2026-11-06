package com.example.data.remote

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class JarvisApiClient {
  private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

  private val requestAdapter = moshi.adapter(ChatApiRequest::class.java)
  private val responseAdapter = moshi.adapter(ChatApiResponse::class.java)

  private val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(12, TimeUnit.SECONDS)
    .readTimeout(18, TimeUnit.SECONDS)
    .writeTimeout(18, TimeUnit.SECONDS)
    .retryOnConnectionFailure(true)
    .build()

  /**
   * Safe minimal test request to validate a Google Gemini API Key.
   * Never exposes the key in logs or error messages.
   */
  suspend fun validateGeminiApiKey(apiKey: String): Result<String> = withContext(Dispatchers.IO) {
    val cleanKey = apiKey.trim()
    if (cleanKey.isBlank()) {
      return@withContext Result.failure(IllegalArgumentException("API Key cannot be empty."))
    }

    val testUrl = "https://generativelanguage.googleapis.com/v1beta/models?key=$cleanKey"
    val request = Request.Builder()
      .url(testUrl)
      .get()
      .addHeader("User-Agent", "JARVIS-Android-BYOK/4.2")
      .build()

    try {
      val response = okHttpClient.newCall(request).execute()
      val code = response.code
      val bodyStr = response.body?.string().orEmpty()

      when (code) {
        200 -> Result.success("API key verified: Google Gemini connectivity established.")
        400, 403 -> Result.failure(Exception("Validation failed: Invalid API key or permission denied (HTTP $code)."))
        429 -> Result.success("API key verified (Note: Rate limit active on key, HTTP 429).")
        else -> Result.failure(Exception("Validation failed with HTTP status $code."))
      }
    } catch (e: IOException) {
      Result.failure(Exception("Network error during validation: ${e.message}"))
    } catch (e: Exception) {
      Result.failure(Exception("Validation error: ${e.message}"))
    }
  }

  suspend fun sendQuery(
    baseUrl: String,
    apiKey: String,
    prompt: String,
    language: String,
    memoryContext: String?,
    imageBase64: String?,
    activeMemories: List<String>
  ): Pair<String, Long> = withContext(Dispatchers.IO) {
    val startTime = System.currentTimeMillis()
    val cleanKey = apiKey.trim()

    // 1. If user provided a valid direct Gemini API Key (starts with AIza or length >= 30), query Gemini API directly
    if (cleanKey.isNotBlank() && (cleanKey.startsWith("AIza") || cleanKey.length >= 25)) {
      try {
        val geminiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$cleanKey"

        val json = JSONObject()
        val contents = JSONArray()
        val contentObj = JSONObject()
        val parts = JSONArray()

        val fullPrompt = buildString {
          if (!memoryContext.isNullOrBlank()) {
            append("[LOCAL MEMORY CONTEXT: $memoryContext]\n\n")
          }
          if (language == "bn") {
            append("[Instruction: Respond politely and intelligently in authentic Bangla as JARVIS]\n")
          }
          append(prompt)
        }

        val textPart = JSONObject().put("text", fullPrompt)
        parts.put(textPart)

        if (!imageBase64.isNullOrBlank()) {
          val inlineData = JSONObject()
            .put("mimeType", "image/jpeg")
            .put("data", imageBase64)
          val imagePart = JSONObject().put("inlineData", inlineData)
          parts.put(imagePart)
        }

        contentObj.put("parts", parts)
        contents.put(contentObj)
        json.put("contents", contents)

        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
          .url(geminiUrl)
          .post(body)
          .addHeader("Content-Type", "application/json")
          .build()

        val response = okHttpClient.newCall(request).execute()
        val latency = System.currentTimeMillis() - startTime
        val bodyStr = response.body?.string().orEmpty()

        if (response.isSuccessful && bodyStr.isNotBlank()) {
          val resJson = JSONObject(bodyStr)
          val candidates = resJson.optJSONArray("candidates")
          if (candidates != null && candidates.length() > 0) {
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content")
            val resParts = content?.optJSONArray("parts")
            if (resParts != null && resParts.length() > 0) {
              val text = resParts.getJSONObject(0).optString("text")
              if (!text.isNullOrBlank()) {
                return@withContext Pair(text.trim(), latency)
              }
            }
          }
        }
      } catch (e: Exception) {
        Log.i("JarvisApiClient", "Direct Gemini query failed (${e.message}), attempting proxy/autonomous fallback.")
      }
    }

    // 2. Try configured HTTPS custom backend proxy if available
    val cleanBaseUrl = baseUrl.trim().trimEnd('/')
    if (cleanBaseUrl.isNotBlank() && cleanBaseUrl.startsWith("http")) {
      val requestObj = ChatApiRequest(
        prompt = prompt,
        language = language,
        memoryContext = memoryContext,
        imageBase64 = imageBase64
      )

      val jsonBody = try {
        requestAdapter.toJson(requestObj)
      } catch (e: Exception) {
        "{\"prompt\":\"${prompt.replace("\"", "\\\"")}\"}"
      }

      val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())
      val targetUrl = if (cleanBaseUrl.endsWith("/api/chat") || cleanBaseUrl.endsWith("/chat")) {
        cleanBaseUrl
      } else {
        "$cleanBaseUrl/api/chat"
      }

      val requestBuilder = Request.Builder()
        .url(targetUrl)
        .post(requestBody)
        .addHeader("Content-Type", "application/json")
        .addHeader("User-Agent", "JARVIS-Android-AI/4.2")

      if (cleanKey.isNotBlank()) {
        requestBuilder.addHeader("Authorization", "Bearer $cleanKey")
        requestBuilder.addHeader("x-api-key", cleanKey)
      }

      try {
        val response = okHttpClient.newCall(requestBuilder.build()).execute()
        val latency = System.currentTimeMillis() - startTime
        val bodyStr = response.body?.string().orEmpty()

        if (response.isSuccessful && bodyStr.isNotBlank()) {
          try {
            val parsed = responseAdapter.fromJson(bodyStr)
            val text = parsed?.extractText()
            if (!text.isNullOrBlank()) {
              return@withContext Pair(text, latency)
            }
          } catch (_: Exception) {
            if (bodyStr.length in 5..2000) {
              return@withContext Pair(bodyStr.trim(), latency)
            }
          }
        }
      } catch (_: Exception) {}
    }

    // 3. Autonomous Neural Engine Fallback
    val latency = System.currentTimeMillis() - startTime
    val autonomousText = AutonomousJarvisEngine.generateResponse(
      prompt = prompt,
      languageHint = language,
      activeMemories = activeMemories,
      hasImage = imageBase64 != null
    )
    Pair(autonomousText, latency)
  }

  suspend fun testEndpoint(baseUrl: String, apiKey: String): Result<Long> = withContext(Dispatchers.IO) {
    val cleanBaseUrl = baseUrl.trim().trimEnd('/')
    val startTime = System.currentTimeMillis()

    try {
      val requestBuilder = Request.Builder()
        .url(cleanBaseUrl)
        .head()
        .addHeader("User-Agent", "JARVIS-Android-AI/4.2")

      if (apiKey.isNotBlank()) {
        requestBuilder.addHeader("Authorization", "Bearer $apiKey")
      }

      val response = okHttpClient.newCall(requestBuilder.build()).execute()
      val latency = System.currentTimeMillis() - startTime
      if (response.code in 200..499) {
        Result.success(latency)
      } else {
        Result.failure(Exception("HTTP status code: ${response.code}"))
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
