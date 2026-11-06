package com.example.data.security

import java.security.MessageDigest
import java.security.SecureRandom

object PasswordHasher {

  fun generateSalt(): String {
    val random = SecureRandom()
    val saltBytes = ByteArray(16)
    random.nextBytes(saltBytes)
    return Base64Compat.encodeToString(saltBytes)
  }

  fun hashPassword(password: String, salt: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val combined = "$salt:$password"
    val hashBytes = digest.digest(combined.toByteArray(Charsets.UTF_8))
    return Base64Compat.encodeToString(hashBytes)
  }

  fun verifyPassword(password: String, salt: String, expectedHash: String): Boolean {
    val computedHash = hashPassword(password, salt)
    return computedHash == expectedHash
  }
}
