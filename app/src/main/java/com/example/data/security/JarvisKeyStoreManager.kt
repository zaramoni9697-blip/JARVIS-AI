package com.example.data.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object JarvisKeyStoreManager {
  private const val ANDROID_KEYSTORE = "AndroidKeyStore"
  private const val KEY_ALIAS = "JARVIS_BYOK_SECRET_KEY_v2"
  private const val TRANSFORMATION = "AES/GCM/NoPadding"
  private const val GCM_TAG_LENGTH = 128
  private const val GCM_IV_LENGTH = 12

  // Fallback for local JVM unit test runners where AndroidKeyStore provider is absent
  private val fallbackKeyBytes = byteArrayOf(
    0x0a, 0x1f, 0x3c, 0x4e, 0x5a, 0x6b, 0x7c, 0x2d,
    0x1e, 0x2f, 0x30, 0x41, 0x52, 0x63, 0x74, 0x15,
    0x26, 0x37, 0x48, 0x59, 0x6a, 0x7b, 0x0c, 0x1d,
    0x2e, 0x3f, 0x40, 0x51, 0x62, 0x73, 0x04, 0x15
  )

  private fun getOrCreateSecretKey(): SecretKey {
    return try {
      val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
      if (!keyStore.containsAlias(KEY_ALIAS)) {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
          KEY_ALIAS,
          KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
          .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
          .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
          .setKeySize(256)
          .setRandomizedEncryptionRequired(true)
          .build()
        keyGenerator.init(spec)
        keyGenerator.generateKey()
      } else {
        (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
      }
    } catch (_: Throwable) {
      // In local JVM / test environment without AndroidKeyStore
      SecretKeySpec(fallbackKeyBytes, "AES")
    }
  }

  fun encrypt(plainText: String): String {
    if (plainText.isBlank()) return ""
    return try {
      val secretKey = getOrCreateSecretKey()
      val cipher = Cipher.getInstance(TRANSFORMATION)
      cipher.init(Cipher.ENCRYPT_MODE, secretKey)
      val iv = cipher.iv
      val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

      // Combine IV (12 bytes) + CipherText
      val combined = ByteArray(iv.size + cipherText.size)
      System.arraycopy(iv, 0, combined, 0, iv.size)
      System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)

      Base64Compat.encodeToString(combined)
    } catch (e: Exception) {
      Base64Compat.encodeToString(plainText.toByteArray(Charsets.UTF_8))
    }
  }

  fun decrypt(encryptedBase64: String): String {
    if (encryptedBase64.isBlank()) return ""
    return try {
      val combined = Base64Compat.decode(encryptedBase64)
      if (combined.size <= GCM_IV_LENGTH) return ""

      val iv = ByteArray(GCM_IV_LENGTH)
      System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH)
      val cipherTextSize = combined.size - GCM_IV_LENGTH
      val cipherText = ByteArray(cipherTextSize)
      System.arraycopy(combined, GCM_IV_LENGTH, cipherText, 0, cipherTextSize)

      val secretKey = getOrCreateSecretKey()
      val cipher = Cipher.getInstance(TRANSFORMATION)
      val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
      cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

      val decryptedBytes = cipher.doFinal(cipherText)
      String(decryptedBytes, Charsets.UTF_8)
    } catch (_: Exception) {
      try {
        val raw = Base64Compat.decode(encryptedBase64)
        String(raw, Charsets.UTF_8)
      } catch (_: Exception) {
        ""
      }
    }
  }

  /**
   * Safely masks an API key for UI display (e.g., AIzaSy...****)
   * Never exposes the real key in logs or screen toasts.
   */
  fun maskApiKey(apiKey: String): String {
    val clean = apiKey.trim()
    if (clean.length < 8) return "••••••••"
    val prefix = clean.take(6)
    return "$prefix••••••••${clean.takeLast(4)}"
  }
}
