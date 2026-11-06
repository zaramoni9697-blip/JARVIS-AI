package com.example.data.security

object Base64Compat {
  fun encodeToString(bytes: ByteArray): String {
    return try {
      java.util.Base64.getEncoder().encodeToString(bytes)
    } catch (_: Throwable) {
      android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
    }
  }

  fun decode(base64: String): ByteArray {
    return try {
      java.util.Base64.getDecoder().decode(base64)
    } catch (_: Throwable) {
      android.util.Base64.decode(base64, android.util.Base64.NO_WRAP)
    }
  }
}
