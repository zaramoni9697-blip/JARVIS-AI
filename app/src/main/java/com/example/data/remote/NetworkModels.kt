package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChatApiRequest(
  @Json(name = "prompt") val prompt: String,
  @Json(name = "language") val language: String = "auto",
  @Json(name = "memory_context") val memoryContext: String? = null,
  @Json(name = "image_base64") val imageBase64: String? = null
)

@JsonClass(generateAdapter = true)
data class ChatApiResponse(
  @Json(name = "response") val response: String? = null,
  @Json(name = "text") val text: String? = null,
  @Json(name = "message") val message: String? = null,
  @Json(name = "status") val status: String? = null
) {
  fun extractText(): String {
    return response ?: text ?: message ?: "Protocol acknowledged. Analysis complete."
  }
}
