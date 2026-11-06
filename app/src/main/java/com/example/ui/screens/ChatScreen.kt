package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ChatMessageEntity
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.components.HologramHeader
import com.example.ui.theme.HologramCardBg
import com.example.ui.theme.HologramCardBorder
import com.example.ui.theme.HologramCyan
import com.example.ui.theme.HologramCyanBright
import com.example.ui.theme.HologramEmerald
import com.example.ui.theme.HologramPurple
import com.example.ui.theme.HologramTextPrimary
import com.example.ui.theme.HologramTextSecondary
import com.example.ui.viewmodel.JarvisViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
  viewModel: JarvisViewModel,
  onNavigateBack: () -> Unit
) {
  val context = LocalContext.current
  val messages by viewModel.currentMessages.collectAsState()
  val isProcessing by viewModel.isQueryProcessing.collectAsState()
  val chatLang by viewModel.chatLanguage.collectAsState()
  val isListening by viewModel.speechManager.isListening.collectAsState()
  val rmsLevel by viewModel.speechManager.rmsLevel.collectAsState()
  val isSpeaking by viewModel.ttsManager.isSpeaking.collectAsState()
  val settings by viewModel.settings.collectAsState()

  var inputText by remember { mutableStateOf("") }
  var showMicPermissionDialog by remember { mutableStateOf(false) }
  val listState = rememberLazyListState()

  val micPermissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    if (isGranted) {
      val langCode = if (chatLang == "bn") "bn-BD" else "en-US"
      viewModel.startVoiceListening(langCode)
    } else {
      Toast.makeText(context, "Microphone permission is required for voice input.", Toast.LENGTH_SHORT).show()
    }
  }

  // Auto scroll to latest message
  LaunchedEffect(messages.size, isProcessing) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  val promptSuggestions = if (chatLang == "bn") {
    listOf(
      "আমাকে কৃত্রিম বুদ্ধিমত্তা সম্পর্কে বলো",
      "আজকের কাজের পরিকল্পনা করো",
      "একটি সুন্দর অনুপ্রেরণামূলক বাণী দাও",
      "কোডিং শেখার সহজ উপায় কি?"
    )
  } else {
    listOf(
      "Explain neural networks simply",
      "Help me draft an email",
      "Give me 3 productivity tips",
      "What is the theory of relativity?"
    )
  }

  if (showMicPermissionDialog) {
    AlertDialog(
      onDismissRequest = { showMicPermissionDialog = false },
      title = { Text("Microphone Access Required") },
      text = {
        Text("JARVIS needs microphone access to convert your voice to text and process voice commands.")
      },
      confirmButton = {
        TextButton(
          onClick = {
            showMicPermissionDialog = false
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
          }
        ) {
          Text("Grant Access", color = HologramCyan)
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
      title = "AI CHAT",
      subtitle = "BILINGUAL NEURAL LINK",
      showBackButton = true,
      onBackClick = onNavigateBack,
      actions = {
        IconButton(
          onClick = {
            viewModel.createNewSessionAndSwitch {
              Toast.makeText(context, "New session started", Toast.LENGTH_SHORT).show()
            }
          },
          modifier = Modifier.testTag("new_chat_button")
        ) {
          Icon(
            imageVector = Icons.Default.AddComment,
            contentDescription = "New Chat",
            tint = HologramCyan
          )
        }
      }
    )

    // Language Selector Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "LANGUAGE PROTOCOL:",
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace,
        color = HologramTextSecondary
      )

      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf("auto" to "Auto", "en" to "English", "bn" to "বাংলা").forEach { (code, label) ->
          val isSelected = chatLang == code
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(if (isSelected) HologramCyan.copy(alpha = 0.25f) else Color(0x33101B30))
              .border(
                1.dp,
                if (isSelected) HologramCyan else HologramCardBorder,
                RoundedCornerShape(12.dp)
              )
              .clickable { viewModel.setChatLanguage(code) }
              .padding(horizontal = 10.dp, vertical = 4.dp)
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
    }

    // Message List
    LazyColumn(
      state = listState,
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      contentPadding = PaddingValues(top = 8.dp, bottom = 12.dp)
    ) {
      if (messages.isEmpty() && !isProcessing) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 40.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "JARVIS IS ONLINE",
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                color = HologramCyan,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = if (chatLang == "bn")
                  "বাংলা বা ইংরেজিতে আপনার যেকোনো প্রশ্ন করুন..."
                else
                  "Transmit your command in English or বাংলা...",
                fontSize = 12.sp,
                color = HologramTextSecondary
              )
            }
          }
        }
      }

      items(messages, key = { it.id }) { message ->
        ChatMessageItem(
          message = message,
          isSpeaking = isSpeaking,
          onSpeak = {
            viewModel.ttsManager.speak(message.content, settings.ttsPitch, settings.ttsSpeed)
          },
          onStopSpeaking = { viewModel.ttsManager.stop() },
          onCopy = {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("JARVIS Message", message.content))
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
          }
        )
      }

      if (isProcessing) {
        item {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            CircularProgressIndicator(
              modifier = Modifier.size(18.dp),
              color = HologramCyan,
              strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "JARVIS is synthesizing response...",
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              color = HologramCyan
            )
          }
        }
      }
    }

    // Audio Visualizer if mic is active
    AnimatedVisibility(visible = isListening) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color(0xCC070B14))
          .padding(vertical = 8.dp)
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "LISTENING // SPEAK NOW",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = HologramPurple,
            letterSpacing = 1.sp
          )
          Spacer(modifier = Modifier.height(4.dp))
          AudioWaveformVisualizer(isActive = true, audioLevel = rmsLevel, maxHeight = 36.dp)
        }
      }
    }

    // Suggested Prompts
    LazyRow(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 4.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(promptSuggestions) { suggestion ->
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x3300F2FE))
            .border(1.dp, HologramCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .clickable {
              inputText = suggestion
              viewModel.sendChatMessage(suggestion)
              inputText = ""
            }
            .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
          Text(
            text = suggestion,
            fontSize = 11.sp,
            color = HologramCyanBright
          )
        }
      }
    }

    // Input Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedTextField(
        value = inputText,
        onValueChange = { inputText = it },
        placeholder = {
          Text(
            text = if (chatLang == "bn") "বার্তা লিখুন..." else "Type command...",
            fontSize = 13.sp,
            color = HologramTextSecondary
          )
        },
        modifier = Modifier
          .weight(1f)
          .testTag("chat_input_field"),
        shape = RoundedCornerShape(24.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = HologramCyan,
          unfocusedBorderColor = HologramCardBorder,
          focusedTextColor = HologramTextPrimary,
          unfocusedTextColor = HologramTextPrimary,
          focusedContainerColor = HologramCardBg,
          unfocusedContainerColor = HologramCardBg
        ),
        maxLines = 4
      )

      Spacer(modifier = Modifier.width(8.dp))

      // Microphone Button
      IconButton(
        onClick = {
          if (isListening) {
            viewModel.stopVoiceListening()
          } else {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
          }
        },
        modifier = Modifier
          .testTag("chat_mic_button")
          .size(44.dp)
          .clip(CircleShape)
          .background(if (isListening) HologramPurple else HologramCardBg)
          .border(1.dp, if (isListening) HologramPurple else HologramCardBorder, CircleShape)
      ) {
        Icon(
          imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
          contentDescription = "Voice Input",
          tint = if (isListening) Color.White else HologramCyan
        )
      }

      Spacer(modifier = Modifier.width(6.dp))

      // Send Button
      IconButton(
        onClick = {
          if (inputText.isNotBlank()) {
            viewModel.sendChatMessage(inputText)
            inputText = ""
          }
        },
        enabled = inputText.isNotBlank() && !isProcessing,
        modifier = Modifier
          .testTag("chat_send_button")
          .size(44.dp)
          .clip(CircleShape)
          .background(if (inputText.isNotBlank()) HologramCyan else HologramCardBorder)
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.Send,
          contentDescription = "Send",
          tint = if (inputText.isNotBlank()) MaterialTheme.colorScheme.background else HologramTextSecondary
        )
      }
    }
  }
}

@Composable
fun ChatMessageItem(
  message: ChatMessageEntity,
  isSpeaking: Boolean,
  onSpeak: () -> Unit,
  onStopSpeaking: () -> Unit,
  onCopy: () -> Unit
) {
  val isUser = message.sender == "user"
  val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
  val formattedTime = remember(message.timestamp) { timeFormat.format(Date(message.timestamp)) }

  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth(if (isUser) 0.82f else 0.92f)
        .clip(
          RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = if (isUser) 16.dp else 4.dp,
            bottomEnd = if (isUser) 4.dp else 16.dp
          )
        )
        .background(if (isUser) Color(0x3300F2FE) else HologramCardBg)
        .border(
          1.dp,
          if (isUser) HologramCyan.copy(alpha = 0.6f) else HologramCardBorder,
          RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = if (isUser) 16.dp else 4.dp,
            bottomEnd = if (isUser) 4.dp else 16.dp
          )
        )
        .padding(12.dp)
    ) {
      Column {
        if (!isUser) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(HologramEmerald)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "JARVIS",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = HologramCyan
              )
            }

            if (message.latencyMs > 0) {
              Text(
                text = "${message.latencyMs}ms",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                color = HologramTextSecondary
              )
            }
          }
        }

        Text(
          text = message.content,
          fontSize = 14.sp,
          color = HologramTextPrimary,
          lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = formattedTime,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            color = HologramTextSecondary
          )

          Row {
            if (!isUser) {
              IconButton(
                onClick = { if (isSpeaking) onStopSpeaking() else onSpeak() },
                modifier = Modifier.size(28.dp)
              ) {
                Icon(
                  imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                  contentDescription = "Read Aloud",
                  tint = HologramCyan,
                  modifier = Modifier.size(16.dp)
                )
              }
            }

            IconButton(
              onClick = onCopy,
              modifier = Modifier.size(28.dp)
            ) {
              Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy",
                tint = HologramTextSecondary,
                modifier = Modifier.size(15.dp)
              )
            }
          }
        }
      }
    }
  }
}
