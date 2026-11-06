package com.example

import com.example.data.remote.AutonomousJarvisEngine
import com.example.data.security.JarvisKeyStoreManager
import com.example.data.security.PasswordHasher
import com.example.voice.VoiceShortcutMatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testPasswordHashingAndVerification() {
    val password = "StarkProtocol2026!"
    val salt = PasswordHasher.generateSalt()
    assertNotNull(salt)
    assertTrue(salt.length >= 16)

    val hash = PasswordHasher.hashPassword(password, salt)
    assertNotNull(hash)
    assertTrue(hash.isNotEmpty())

    // Verification with correct password
    assertTrue(PasswordHasher.verifyPassword(password, salt, hash))

    // Verification with wrong password
    assertFalse(PasswordHasher.verifyPassword("WrongPassword123", salt, hash))

    // Distinct salts produce distinct hashes for same password
    val salt2 = PasswordHasher.generateSalt()
    val hash2 = PasswordHasher.hashPassword(password, salt2)
    assertNotEquals(hash, hash2)
  }

  @Test
  fun testApiKeyEncryptionAndSeparation() {
    val userA_Key = "AIzaSyUserA_SecretKey_1234567890"
    val userB_Key = "AIzaSyUserB_SecretKey_0987654321"

    val encryptedA = JarvisKeyStoreManager.encrypt(userA_Key)
    val encryptedB = JarvisKeyStoreManager.encrypt(userB_Key)

    // Ensure encrypted blobs are non-empty and isolated
    assertNotNull(encryptedA)
    assertNotNull(encryptedB)
    assertNotEquals(encryptedA, encryptedB)
    assertFalse(encryptedA.contains(userA_Key))
    assertFalse(encryptedB.contains(userB_Key))

    // Decrypt both and ensure proper user isolation
    val decryptedA = JarvisKeyStoreManager.decrypt(encryptedA)
    val decryptedB = JarvisKeyStoreManager.decrypt(encryptedB)

    assertEquals(userA_Key, decryptedA)
    assertEquals(userB_Key, decryptedB)
    assertNotEquals(decryptedA, decryptedB)

    // Verify key masking protects secrets in UI
    val maskedA = JarvisKeyStoreManager.maskApiKey(userA_Key)
    assertTrue(maskedA.startsWith("AIzaSy"))
    assertTrue(maskedA.contains("••••••••"))
    assertFalse(maskedA.contains("UserA_SecretKey"))
  }

  @Test
  fun testVoiceShortcutMatcher() {
    val shortcuts = VoiceShortcutMatcher.DEFAULT_SHORTCUTS

    // Test direct phrase
    val match1 = VoiceShortcutMatcher.matchCommand("open image analysis", shortcuts)
    assertNotNull(match1)
    assertEquals("NAVIGATE_IMAGE_ANALYSIS", match1?.actionType)

    // Test conversational prefix
    val match2 = VoiceShortcutMatcher.matchCommand("jarvis please start new chat", shortcuts)
    assertNotNull(match2)
    assertEquals("NAVIGATE_NEW_CHAT", match2?.actionType)

    // Test status query
    val match3 = VoiceShortcutMatcher.matchCommand("jarvis status report", shortcuts)
    assertNotNull(match3)
    assertEquals("PLAY_VOICE_STATUS", match3?.actionType)

    // Test Bangla alternative
    val match4 = VoiceShortcutMatcher.matchCommand("বাংলা বাণী শোনাও", shortcuts)
    assertNotNull(match4)
    assertEquals("PLAY_BANGLA_QUOTE", match4?.actionType)

    // Test memory shortcut
    val match5 = VoiceShortcutMatcher.matchCommand("open memory manager", shortcuts)
    assertNotNull(match5)
    assertEquals("NAVIGATE_MEMORY", match5?.actionType)

    // Test voice mode shortcut
    val match6 = VoiceShortcutMatcher.matchCommand("activate voice mode", shortcuts)
    assertNotNull(match6)
    assertEquals("NAVIGATE_VOICE", match6?.actionType)
  }

  @Test
  fun testAutonomousJarvisEngineBilingual() {
    val bnQuery = "কেমন আছো জারভিস?"
    assertTrue(AutonomousJarvisEngine.isBanglaText(bnQuery))
    val bnResponse = AutonomousJarvisEngine.generateResponse(bnQuery, "bn", emptyList())
    assertTrue(bnResponse.isNotEmpty())
    assertTrue(AutonomousJarvisEngine.isBanglaText(bnResponse))

    val enQuery = "JARVIS, what is quantum computing?"
    val enResponse = AutonomousJarvisEngine.generateResponse(enQuery, "en", listOf("User Name: Commander"))
    assertTrue(enResponse.isNotEmpty())
    assertTrue(enResponse.contains("Quantum") || enResponse.contains("Commander"))
  }
}
