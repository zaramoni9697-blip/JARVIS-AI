package com.example.ui.screens

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.components.GlassCard
import com.example.ui.components.HologramHeader
import com.example.ui.components.HolographicCore
import com.example.ui.components.JarvisCoreState
import com.example.ui.theme.HologramCardBg
import com.example.ui.theme.HologramCardBorder
import com.example.ui.theme.HologramCyan
import com.example.ui.theme.HologramCyanBright
import com.example.ui.theme.HologramEmerald
import com.example.ui.theme.HologramPurple
import com.example.ui.theme.HologramTextPrimary
import com.example.ui.theme.HologramTextSecondary
import com.example.ui.viewmodel.JarvisViewModel
import com.example.ui.viewmodel.VoiceActionResult

@Composable
fun VoiceScreen(
  viewModel: JarvisViewModel,
  onNavigateBack: () -> Unit,
  onNavigateTo: (String) -> Unit
) {
  val context = LocalContext.current
  val coreState by viewModel.coreState.collectAsState()
  val isListening by viewModel.speechManager.isListening.collectAsState()
  val rmsLevel by viewModel.speechManager.rmsLevel.collectAsState()
  val lastSpokenText by viewModel.speechManager.lastRecognizedText.collectAsState()
  val speechError by viewModel.speechManager.speechError.collectAsState()
  val isSpeaking by viewModel.ttsManager.isSpeaking.collectAsState()
  val shortcuts by viewModel.allShortcuts.collectAsState()

  var selectedLang by remember { mutableStateOf("en-US") } // "en-US" or "bn-BD"
  var showMicPermissionDialog by remember { mutableStateOf(false) }
  var latestFeedbackText by remember { mutableStateOf<String?>(null) }

  val micPermissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    if (isGranted) {
      viewModel.startVoiceListening(selectedLang)
    } else {
      Toast.makeText(context, "Microphone access is required for voice commands.", Toast.LENGTH_SHORT).show()
    }
  }

  // Listen for voice action events
  LaunchedEffect(Unit) {
    viewModel.voiceActionEvent.collect { result ->
      when (result) {
        is VoiceActionResult.Navigated -> {
          latestFeedbackText = "Action Triggered: ${result.message}"
          onNavigateTo(result.destination)
        }
        is VoiceActionResult.SpokenFeedback -> {
          latestFeedbackText = result.phrase
        }
        is VoiceActionResult.Error -> {
          latestFeedbackText = "Error: ${result.reason}"
        }
      }
    }
  }

  if (showMicPermissionDialog) {
    AlertDialog(
      onDismissRequest = { showMicPermissionDialog = false },
      title = { Text("Microphone Access Required") },
      text = {
        Text("JARVIS requires continuous microphone telemetry to listen for voice commands and answer inquiries in real-time.")
      },
      confirmButton = {
        TextButton(
          onClick = {
            showMicPermissionDialog = false
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
          }
        ) {
          Text("Allow Access", color = HologramCyan)
        }
      },
      dismissButton = {
        TextButton(onClick = { showMicPermissionDialog = false }) {
          Text("Cancel", color = HologramTextSecondary)
        }
      },
      containerColor = HologramCardBg
    )
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    HologramHeader(
      title = "VOICE ASSISTANT",
      subtitle = "HANDS-FREE TACTICAL SPEECH",
      showBackButton = true,
      onBackClick = onNavigateBack,
      actions = {
        if (isSpeaking) {
          IconButton(
            onClick = { viewModel.ttsManager.stop() },
            modifier = Modifier.testTag("voice_stop_tts_button")
          ) {
            Icon(
              imageVector = Icons.Default.Stop,
              contentDescription = "Stop Speech",
              tint = HologramPurple
            )
          }
        }
      }
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Language Selector
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
      ) {
        listOf("en-US" to "English (US)", "bn-BD" to "বাংলা (Bangla)").forEach { (code, label) ->
          val isSelected = selectedLang == code
          Box(
            modifier = Modifier
              .padding(horizontal = 6.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(if (isSelected) HologramCyan.copy(alpha = 0.25f) else Color(0x33101B30))
              .border(1.dp, if (isSelected) HologramCyan else HologramCardBorder, RoundedCornerShape(16.dp))
              .clickable {
                selectedLang = code
                if (isListening) {
                  viewModel.startVoiceListening(code)
                }
              }
              .padding(horizontal = 14.dp, vertical = 6.dp)
          ) {
            Text(
              text = label,
              fontSize = 11.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              color = if (isSelected) HologramCyanBright else HologramTextSecondary
            )
          }
        }
      }

      // Central Massive Glowing Holographic Core
      Box(
        modifier = Modifier.padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
      ) {
        HolographicCore(
          size = 210.dp,
          state = coreState,
          audioLevel = rmsLevel,
          onClick = {
            if (isListening) {
              viewModel.stopVoiceListening()
            } else {
              micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
          }
        )
      }

      // Live Audio Visualizer Bars
      AudioWaveformVisualizer(
        isActive = isListening || isSpeaking,
        audioLevel = if (isListening) rmsLevel else 0.6f,
        maxHeight = 44.dp
      )

      // Spoken Text Feedback Card
      GlassCard(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("voice_transcript_card"),
        borderColor = if (isListening) HologramPurple else HologramCardBorder
      ) {
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = when {
              isListening -> "TRANSCRIBING VOCAL FREQUENCY..."
              isSpeaking -> "SYNTHESIZING VOCAL RESPONSE..."
              else -> "STANDBY // READY FOR VOICE COMMAND"
            },
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = if (isListening) HologramPurple else HologramCyan,
            letterSpacing = 1.2.sp
          )

          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = lastSpokenText?.let { "\"$it\"" }
              ?: if (selectedLang == "bn-BD")
                "মাইক্রোফোন চালু করতে নিচের বাটনে চাপ দিন এবং কথা বলুন..."
              else
                "Press the microphone button and state your command...",
            fontSize = 14.sp,
            color = HologramTextPrimary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
          )

          if (latestFeedbackText != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = latestFeedbackText ?: "",
              fontSize = 12.sp,
              fontFamily = FontFamily.Monospace,
              color = HologramEmerald,
              textAlign = TextAlign.Center
            )
          }

          if (speechError != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = speechError ?: "",
              fontSize = 11.sp,
              color = Color(0xFFEF4444)
            )
          }
        }
      }

      // Big Microphone Trigger Button
      Box(
        modifier = Modifier
          .size(72.dp)
          .clip(CircleShape)
          .background(if (isListening) HologramPurple else HologramCyan)
          .clickable {
            if (isListening) {
              viewModel.stopVoiceListening()
            } else {
              micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
          }
          .testTag("voice_toggle_mic_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
          contentDescription = "Microphone",
          tint = if (isListening) Color.White else MaterialTheme.colorScheme.background,
          modifier = Modifier.size(36.dp)
        )
      }

      Text(
        text = if (isListening) "TAP TO STOP LISTENING" else "TAP TO ENGAGE MICROPHONE",
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        color = HologramTextSecondary,
        letterSpacing = 1.sp
      )

      // Voice Shortcuts Cheatsheet
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp)
      ) {
        Text(
          text = "CONFIGURED VOICE SHORTCUTS",
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          color = HologramTextPrimary,
          letterSpacing = 1.sp,
          modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(shortcuts) { shortcut ->
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x33101B30))
                .border(1.dp, HologramCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .clickable {
                  viewModel.handleSpokenInput(shortcut.phrase)
                }
                .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
              Column {
                Text(
                  text = "Say: \"${shortcut.phrase}\"",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = HologramCyanBright
                )
                Text(
                  text = "→ ${shortcut.title}",
                  fontSize = 9.sp,
                  color = HologramTextSecondary
                )
              }
            }
          }
        }
      }
    }
  }
}
