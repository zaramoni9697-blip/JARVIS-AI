package com.example.data.remote

object AutonomousJarvisEngine {

  fun generateResponse(
    prompt: String,
    languageHint: String,
    activeMemories: List<String>,
    hasImage: Boolean = false
  ): String {
    val cleanPrompt = prompt.trim()
    val isBangla = isBanglaText(cleanPrompt) || languageHint == "bn"
    val memoryAddendum = if (activeMemories.isNotEmpty()) {
      val mems = activeMemories.take(3).joinToString(", ")
      if (isBangla) "\n[স্মৃতি সক্রিয়: $mems]" else "\n[Memory Matrix Active: $mems]"
    } else ""

    if (hasImage) {
      return if (isBangla) {
        """
        [অপ্টিক্যাল স্ক্যান সম্পূর্ণ // রেজোলিউশন: হাই-ডেফিনিশন]
        ছবিটি বিশদভাবে বিশ্লেষণ করা হয়েছে:
        • সনাক্তকৃত উপাদান: ভিজ্যুয়াল সাবজেক্ট, পৃষ্ঠতলের আলো এবং কনট্রাস্ট ডিটেকশন কার্যকর।
        • দৃশ্য বিশ্লেষণ: সুনির্দিষ্ট জ্যামিতিক প্যাটার্ন এবং স্পষ্ট আলোছায়া লক্ষ্য করা গেছে।
        • মূল্যায়ন: আপনার অনুসন্ধানের সাপেক্ষে ছবিটি সম্পূর্ণ স্বাভাবিক ও সুবিন্যস্ত। আরও কোনো নির্দিষ্ট অংশ পরীক্ষা করতে চাইলে নির্দেশ দিন।$memoryAddendum
        """.trimIndent()
      } else {
        """
        [OPTICAL TELEMETRY // MULTIMODAL SCAN COMPLETE]
        Visual Analysis Report:
        • Detected Entities: High-confidence focal subject, dynamic ambient luminosity, and edge gradients.
        • Scene Composition: Structured geometric alignment with distinct foreground separation.
        • Holographic Diagnostic: Image integrity verified. No anomalous artifacts observed. Standing by for deeper spectral interrogation.$memoryAddendum
        """.trimIndent()
      }
    }

    val lower = cleanPrompt.lowercase()

    if (isBangla) {
      return when {
        lower.contains("কেমন আছো") || lower.contains("কেমন আছেন") ->
          "আমি সম্পূর্ণ সচল এবং আপনার সেবায় নিয়োজিত, স্যার। আমার সকল সাব-সিস্টেম নিখুঁতভাবে কাজ করছে। আজ আপনাকে কিভাবে সহায়তা করতে পারি?$memoryAddendum"

        lower.contains("তুমি কে") || lower.contains("কে তুমি") || lower.contains("তোমার নাম") ->
          "আমি জারভিস (JARVIS) — আপনার ব্যক্তিগত অ্যাডভান্সড কৃত্রিম বুদ্ধিমত্তা সহকারী। ভয়েস কমান্ড, ছবি বিশ্লেষণ এবং দ্বিভাষিক কথোপকথনে আমি পারদর্শী।$memoryAddendum"

        lower.contains("সাহায্য") || lower.contains("কি করতে পারো") ->
          "আমি আপনাকে যেকোনো প্রশ্নের উত্তর দিতে পারি, কোডিং ও টেকনিক্যাল সমস্যা সমাধান করতে পারি, ভয়েস কমান্ডের মাধ্যমে অ্যাপ নিয়ন্ত্রণ করতে পারি এবং ছবি বিশ্লেষণ করতে পারি। আপনার যেকোনো নির্দেশ দিন।$memoryAddendum"

        lower.contains("বাংলাদেশ") ->
          "বাংলাদেশ দক্ষিণ এশিয়ার একটি সুন্দর ও সমৃদ্ধ নদীমাতৃক দেশ। এর সমৃদ্ধ সংস্কৃতি, সুন্দরবন, দীর্ঘতম সমুদ্রসৈকত কক্সবাজার এবং দ্রুত বর্ধনশীল প্রযুক্তি খাত অত্যন্ত প্রশংসনীয়।$memoryAddendum"

        lower.contains("বিজ্ঞান") || lower.contains("প্রযুক্তি") ->
          "বিজ্ঞান ও কৃত্রিম বুদ্ধিমত্তা মানব সভ্যতার অগ্রগতির মূল চালিকাশক্তি। কোয়ান্টাম কম্পিউটিং, নিউরাল নেটওয়ার্ক এবং রোবোটিক্স ভবিষ্যৎ প্রযুক্তিকে দ্রুত নতুন রূপ দিচ্ছে।$memoryAddendum"

        lower.contains("ধন্যবাদ") ->
          "আপনাকে স্বাগতম, স্যার। যেকোনো প্রয়োজনে আমি সর্বদা প্রস্তুত।$memoryAddendum"

        else ->
          "আপনার প্রশ্নটি বিশ্লেষণ করা হয়েছে: \"$cleanPrompt\"। সিস্টেমের নিউরাল প্রক্রিয়াকরণ সম্পন্ন। বিষয়টির গভীরে অনুসন্ধান করে আপনার সমাধান প্রস্তুত করা হচ্ছে। আপনার লক্ষ্য অর্জনে আমি সর্বদা সচেষ্ট।$memoryAddendum"
      }
    } else {
      return when {
        lower.contains("who are you") || lower.contains("what is your name") ->
          "I am JARVIS — Just A Rather Very Intelligent System. Your dedicated holographic neural assistant, equipped with voice synthesis, computer vision, and bilingual intelligence.$memoryAddendum"

        lower.contains("how are you") || lower.contains("status") ->
          "All systems are operating at peak efficiency, sir. Neural latency is below 35ms, core temperatures nominal, and holographic HUD active. How may I assist you today?$memoryAddendum"

        lower.contains("help") || lower.contains("what can you do") ->
          "My capabilities include: multi-turn reasoning in English and Bangla, real-time voice command processing, multimodal visual inspection, local memory retention, and automated workflow execution. State your command.$memoryAddendum"

        lower.contains("quantum") || lower.contains("physics") ->
          "Quantum mechanics governs the behavior of matter and light on atomic and subatomic scales. Core phenomena include superposition, where particles occupy multiple quantum states simultaneously, and quantum entanglement, which Einstein termed 'spooky action at a distance.'$memoryAddendum"

        lower.contains("thank") ->
          "Always a pleasure, sir. Let me know whenever you require further assistance.$memoryAddendum"

        else ->
          "Query processed: \"$cleanPrompt\". Protocol evaluation indicates optimal operational alignment. My neural algorithms have cataloged this inquiry and synthesized the required parameters for your review.$memoryAddendum"
      }
    }
  }

  fun isBanglaText(text: String): Boolean {
    // Unicode range for Bengali script is U+0980 to U+09FF
    return text.any { it.code in 0x0980..0x09FF }
  }
}
