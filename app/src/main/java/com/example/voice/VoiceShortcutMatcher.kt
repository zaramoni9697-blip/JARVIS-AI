package com.example.voice

import com.example.data.local.VoiceShortcutEntity

object VoiceShortcutMatcher {

  val DEFAULT_SHORTCUTS = listOf(
    VoiceShortcutEntity(
      id = "shortcut_image_analysis",
      title = "Open Image Analysis",
      phrase = "open image analysis",
      alternativePhrases = "open camera, analyze photo, scan image, visual scan, ছবি বিশ্লেষণ, ক্যামেরা খোলো",
      actionType = "NAVIGATE_IMAGE_ANALYSIS",
      isEnabled = true,
      description = "Triggers the multimodal holographic image scanner and camera."
    ),
    VoiceShortcutEntity(
      id = "shortcut_new_chat",
      title = "Start New Chat",
      phrase = "start new chat",
      alternativePhrases = "new chat, reset chat, clear conversation, new session, নতুন চ্যাট, নতুন বার্তা",
      actionType = "NAVIGATE_NEW_CHAT",
      isEnabled = true,
      description = "Initializes a fresh bilingual conversation thread."
    ),
    VoiceShortcutEntity(
      id = "shortcut_jarvis_status",
      title = "JARVIS System Status",
      phrase = "jarvis status",
      alternativePhrases = "system status, diagnostics, are you online, system report, সিস্টেম স্ট্যাটাস, ডায়াগনস্টিক",
      actionType = "PLAY_VOICE_STATUS",
      isEnabled = true,
      description = "Speaks a comprehensive diagnostic and status confirmation."
    ),
    VoiceShortcutEntity(
      id = "shortcut_bangla_quote",
      title = "Speak Bangla Quote",
      phrase = "bangla quote",
      alternativePhrases = "speak bangla quote, inspiring quote, bangla baani, বাংলা বাণী, বাংলা উদ্ধৃতি",
      actionType = "PLAY_BANGLA_QUOTE",
      isEnabled = true,
      description = "Speaks an uplifting philosophical quote in authentic Bangla."
    ),
    VoiceShortcutEntity(
      id = "shortcut_memory",
      title = "Open Memory Matrix",
      phrase = "open memory",
      alternativePhrases = "show memory, memory manager, user memory, মেমোরি দেখাও, স্মৃতি তালিকা",
      actionType = "NAVIGATE_MEMORY",
      isEnabled = true,
      description = "Navigates directly to user memory and preferences matrix."
    ),
    VoiceShortcutEntity(
      id = "shortcut_voice_mode",
      title = "Open Voice Assistant",
      phrase = "voice mode",
      alternativePhrases = "open voice assistant, voice assistant, voice control, কথা বলো",
      actionType = "NAVIGATE_VOICE",
      isEnabled = true,
      description = "Enters hands-free holographic voice assistant mode."
    )
  )

  /**
   * Matches spoken input against enabled shortcuts with robust tolerance for variations,
   * prefixes (e.g. "please", "jarvis", "can you"), and punctuation.
   */
  fun matchCommand(spokenInput: String, shortcuts: List<VoiceShortcutEntity>): VoiceShortcutEntity? {
    val normalized = normalize(spokenInput)
    if (normalized.isBlank()) return null

    // 1. Direct or contains check on primary phrase
    for (shortcut in shortcuts) {
      if (!shortcut.isEnabled) continue

      val normPrimary = normalize(shortcut.phrase)
      if (normalized == normPrimary || normalized.contains(normPrimary)) {
        return shortcut
      }

      // 2. Check alternative phrases
      val alts = shortcut.alternativePhrases.split(",").map { normalize(it) }.filter { it.isNotBlank() }
      for (alt in alts) {
        if (normalized == alt || normalized.contains(alt)) {
          return shortcut
        }
      }

      // 3. Fuzzy token match: if all key words of shortcut are present in spoken sentence
      val primaryTokens = normPrimary.split(" ").filter { it.isNotBlank() && it !in commonStopWords }
      if (primaryTokens.isNotEmpty()) {
        val inputTokens = normalized.split(" ").filter { it.isNotBlank() }
        if (primaryTokens.all { token -> inputTokens.any { it.contains(token) || token.contains(it) } }) {
          return shortcut
        }
      }
    }

    return null
  }

  private val commonStopWords = setOf("jarvis", "please", "can", "you", "could", "to", "the", "a", "an", "hey", "hello", "hi")

  private fun normalize(text: String): String {
    return text.lowercase()
      .replace(Regex("[.,/#!$%^&*;:{}=\\-_`~()?]"), " ")
      .replace(Regex("\\s+"), " ")
      .trim()
  }
}
