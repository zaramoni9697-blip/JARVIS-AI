# JARVIS AI — Futuristic Holographic AI Assistant

A native Android application built with **Kotlin**, **Jetpack Compose**, **Room Database**, and **Material 3**, featuring a Tony Stark-inspired holographic AI interface with deep navy surfaces, glowing cyan neon lighting, an animated circular AI core, local multi-profile authentication, and a personal API key system (BYOK).

---

## 🚀 Key Features

### 1. Local-Only Authentication & Profile Isolation
- **Signup Screen**: Register local on-device profiles with Full Name, Email, Password, and Confirm Password.
- **Login Screen**: Authenticate into existing local profiles with Remember Me and quick profile switching.
- **Security & Privacy Disclosure**:
  - All accounts and passwords are stored **strictly on this device** in an encrypted SQLite/Room database using salted SHA-256 password hashing.
  - **No Cloud Account**: No remote servers or cloud databases store credentials. There is no remote password recovery.
  - **Data Retention**: Clearing application storage or uninstalling the app permanently erases local profiles and chats.
- **Data Isolation**: Each local profile maintains isolated conversation sessions, messages, memory matrix entries, settings, and personal API keys. Profile A cannot access Profile B's data.

### 2. Personal API Key System (BYOK - Bring Your Own Key)
- **AI Provider Setup Screen**: Configurable after signup, on first login, or anytime in Settings.
- **Google Gemini API Support**: Uses your personal Gemini API Key directly from Google AI Studio.
- **Android Keystore Encryption**: Keys are encrypted at rest with hardware-backed **AES-256-GCM** encryption via `AndroidKeyStore`.
- **Safe Test Validation**: Minimal HTTPS handshake test against the Google Gemini API to verify validity before saving.
- **Zero Exposure**:
  - Keys are masked in the UI (`AIzaSy••••••••xxxx`).
  - Keys are never output to logs, analytics, or shared backends.
  - Each profile uses only its own encrypted key.
  - Autonomous bilingual neural engine acts as fallback if offline or no key is provided.

### 3. Tactical Futuristic Holographic UI
- **Animated Circular AI Core**: Concentric spinning segmented rings, pulsing core aura, and state reactivity (`IDLE`, `LISTENING`, `PROCESSING`, `SPEAKING`).
- **Glassmorphic HUD Cards**: Semi-transparent containers with cyan neon glowing borders (`#00F2FE`, `#00E5FF`).
- **Dynamic Audio Waveform Visualizer**: Live animated frequency bars reacting to microphone audio levels and speech output.
- **Multiple Holographic Palettes**: Neon Cyan, Matrix Green, and Amethyst Neon, plus High-Contrast Light Mode.

### 4. Bilingual AI Chat (English & বাংলা)
- Native support for English and authentic Bengali (বাংলা) dialogue.
- Quick protocol switcher (`Auto` / `English` / `বাংলা`).
- Read-aloud Text-to-Speech (TTS) with playback controls, latency diagnostics, and one-tap copy.
- Microphone voice dictation with runtime permission handling.

### 5. Configurable Voice Command Shortcuts
- 6 pre-configured voice shortcuts with fuzzy matching and prefix tolerance:
  1. *"Open Image Analysis"* (`"open image analysis"`, `"open camera"`, `"ছবি বিশ্লেষণ"`) → Launches Multimodal Vision HUD.
  2. *"Start New Chat"* (`"start new chat"`, `"new session"`, `"নতুন চ্যাট"`) → Resets conversation matrix.
  3. *"JARVIS Status"* (`"jarvis status"`, `"system diagnostics"`, `"সিস্টেম স্ট্যাটাস"`) → Speaks diagnostic status aloud.
  4. *"Speak Bangla Quote"* (`"bangla quote"`, `"বাংলা বাণী"`, `"বাংলা উদ্ধৃতি"`) → Speaks an inspiring quote in Bangla.
  5. *"Open Memory Manager"* (`"open memory"`, `"মেমোরি দেখাও"`) → Launches Memory Matrix.
  6. *"Open Voice Assistant"* (`"voice mode"`, `"কথা বলো"`) → Enters hands-free voice mode.
- Editable phrases, variation triggers, enable/disable toggles, and live testing in Settings.

### 6. Multimodal Image Analysis
- Zero-permission Android Photo Picker (`ActivityResultContracts.PickVisualMedia`).
- Secure camera capture with runtime permission checks and `FileProvider`.
- Animated vertical holographic laser scanning overlay.
- Contextual vision directives and comprehensive optical telemetry report.

### 7. Local Memory Matrix & Data Export
- Opt-in local contextual memory (`Profile`, `Preference`, `Fact`, `Instruction`).
- Add, edit, toggle, or delete memory records.
- **Export Data**: Export isolated local profile data (chats, sessions, memories) to structured JSON.

---

## 📱 User Flow

```
App Launch
    │
    ▼
[Auto-Login via Remember Me?]
    ├── Yes ──► [API Key Configured?] ──► Yes ──► Home Dashboard
    │                                 └── No  ──► AI Provider Setup ──► Home Dashboard
    │
    └── No  ──► Login Screen ◄──► Signup Screen
                     │
                     ▼
             AI Provider Setup (Verify & Save Gemini Key)
                     │
                     ▼
               Home Dashboard
```

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin 2.2.10
- **UI Toolkit**: Jetpack Compose with Material Design 3 (M3)
- **Architecture**: MVVM (Model-View-ViewModel) + Repository Pattern
- **Local Database**: Room 2.7.0 (with complete `userId` isolation)
- **Cryptography**: Android Keystore (`AES/GCM/NoPadding`) + Salted SHA-256
- **Networking**: OkHttp 4.10.0 + Moshi 1.15.2 (HTTPS only)
- **Audio & Speech**: Android `SpeechRecognizer` + `TextToSpeech`
- **Image Loading**: Coil Compose 2.7.0
- **Testing**: JUnit + Robolectric JVM Tests

---

## 🔐 Security & Privacy Architecture

1. **No Cloud Storage**: All personal chat logs, local profiles, and memories reside solely on the physical device.
2. **Keystore Isolation**: API keys are encrypted at rest with hardware-backed AES-256 keys.
3. **No Key Leaks**: Keys are masked in all UI components and never emitted in error dialogs, logs, or network telemetry.
4. **Offline Resilience**: The built-in Autonomous Neural Engine handles queries locally without sending data when offline.
