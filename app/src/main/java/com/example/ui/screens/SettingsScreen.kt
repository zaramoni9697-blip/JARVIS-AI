package com.example.ui.screens

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.data.local.VoiceShortcutEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.HologramHeader
import com.example.ui.theme.HologramCardBg
import com.example.ui.theme.HologramCardBorder
import com.example.ui.theme.HologramCyan
import com.example.ui.theme.HologramCyanBright
import com.example.ui.theme.HologramEmerald
import com.example.ui.theme.HologramPurple
import com.example.ui.theme.HologramRed
import com.example.ui.theme.HologramTextPrimary
import com.example.ui.theme.HologramTextSecondary
import com.example.ui.viewmodel.JarvisViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
  viewModel: JarvisViewModel,
  onNavigateBack: () -> Unit,
  onNavigateToApiSetup: () -> Unit,
  onLogout: () -> Unit
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val currentUser by viewModel.currentUser.collectAsState()
  val allProfiles by viewModel.allProfiles.collectAsState()
  val maskedKey by viewModel.maskedApiKey.collectAsState()
  val settings by viewModel.settings.collectAsState()
  val shortcuts by viewModel.allShortcuts.collectAsState()
  val isTestingConn by viewModel.isTestingConnection.collectAsState()
  val testConnResult by viewModel.testConnectionResult.collectAsState()

  var backendUrlInput by remember(settings.apiBaseUrl) { mutableStateOf(settings.apiBaseUrl) }
  var ttsSpeedSlider by remember(settings.ttsSpeed) { mutableFloatStateOf(settings.ttsSpeed) }
  var ttsPitchSlider by remember(settings.ttsPitch) { mutableFloatStateOf(settings.ttsPitch) }

  var shortcutToEdit by remember { mutableStateOf<VoiceShortcutEntity?>(null) }
  var showNotificationRationale by remember { mutableStateOf(false) }
  var showSwitchProfileDialog by remember { mutableStateOf(false) }
  var showExportDialog by remember { mutableStateOf<String?>(null) }
  var showDeleteProfileDialog by remember { mutableStateOf(false) }

  val notificationPermissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    viewModel.updateNotifications(isGranted)
    if (isGranted) {
      showTestNotification(context)
      Toast.makeText(context, "Notification permission granted", Toast.LENGTH_SHORT).show()
    } else {
      Toast.makeText(context, "Notification permission denied", Toast.LENGTH_SHORT).show()
    }
  }

  // Switch Profile Dialog
  if (showSwitchProfileDialog) {
    AlertDialog(
      onDismissRequest = { showSwitchProfileDialog = false },
      title = { Text("Switch Local User Profile") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Select another local profile on this device:", fontSize = 12.sp, color = HologramTextSecondary)
          allProfiles.forEach { profile ->
            val isCurrent = profile.userId == currentUser?.userId
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(if (isCurrent) HologramCyan.copy(alpha = 0.2f) else HologramCardBg)
                .border(1.dp, if (isCurrent) HologramCyan else HologramCardBorder, RoundedCornerShape(10.dp))
                .clickable {
                  showSwitchProfileDialog = false
                  viewModel.switchProfile(
                    profile.userId,
                    onSuccess = {
                      Toast.makeText(context, "Switched to ${profile.fullName}", Toast.LENGTH_SHORT).show()
                    },
                    onError = {
                      Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                    }
                  )
                }
                .padding(10.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(text = profile.fullName, fontWeight = FontWeight.Bold, color = HologramTextPrimary)
                  Text(text = profile.email, fontSize = 11.sp, color = HologramTextSecondary)
                }
                if (isCurrent) {
                  Text("ACTIVE", fontSize = 10.sp, color = HologramCyan, fontFamily = FontFamily.Monospace)
                }
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showSwitchProfileDialog = false }) {
          Text("Close", color = HologramCyan)
        }
      },
      containerColor = HologramCardBg
    )
  }

  // Export Data Dialog
  if (showExportDialog != null) {
    AlertDialog(
      onDismissRequest = { showExportDialog = null },
      title = { Text("Local Data Export (JSON)") },
      text = {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
          Text("Exported conversation history and memory matrix:", fontSize = 11.sp, color = HologramTextSecondary)
          Spacer(modifier = Modifier.height(8.dp))
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFF070B14))
              .padding(8.dp)
          ) {
            Text(
              text = showExportDialog ?: "",
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace,
              color = HologramCyanBright
            )
          }
        }
      },
      confirmButton = {
        TextButton(
          onClick = {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("JARVIS Export", showExportDialog))
            Toast.makeText(context, "JSON copied to clipboard", Toast.LENGTH_SHORT).show()
            showExportDialog = null
          }
        ) {
          Text("Copy JSON", color = HologramCyan)
        }
      },
      dismissButton = {
        TextButton(onClick = { showExportDialog = null }) {
          Text("Close", color = HologramTextSecondary)
        }
      },
      containerColor = HologramCardBg
    )
  }

  // Delete Profile Dialog
  if (showDeleteProfileDialog) {
    AlertDialog(
      onDismissRequest = { showDeleteProfileDialog = false },
      title = { Text("Delete Local Profile?") },
      text = {
        Text("This will permanently erase your local profile, all conversation history, local memory records, and encrypted API key from this device.")
      },
      confirmButton = {
        TextButton(
          onClick = {
            showDeleteProfileDialog = false
            currentUser?.let {
              viewModel.deleteProfile(it.userId) {
                Toast.makeText(context, "Profile permanently deleted", Toast.LENGTH_SHORT).show()
                onLogout()
              }
            }
          }
        ) {
          Text("Delete Profile", color = HologramRed)
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteProfileDialog = false }) {
          Text("Cancel", color = HologramTextSecondary)
        }
      },
      containerColor = HologramCardBg
    )
  }

  // Voice Shortcut Edit Dialog
  if (shortcutToEdit != null) {
    var editPhrase by remember { mutableStateOf(shortcutToEdit!!.phrase) }
    var editAlts by remember { mutableStateOf(shortcutToEdit!!.alternativePhrases) }

    AlertDialog(
      onDismissRequest = { shortcutToEdit = null },
      title = { Text("Configure Voice Command") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Command Action: ${shortcutToEdit!!.title}",
            fontWeight = FontWeight.Bold,
            color = HologramCyan
          )
          OutlinedTextField(
            value = editPhrase,
            onValueChange = { editPhrase = it },
            label = { Text("Primary Trigger Phrase") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )
          OutlinedTextField(
            value = editAlts,
            onValueChange = { editAlts = it },
            label = { Text("Alternative Phrases (comma-separated)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )
        }
      },
      confirmButton = {
        TextButton(
          onClick = {
            if (editPhrase.isNotBlank()) {
              viewModel.updateVoiceShortcut(
                shortcutToEdit!!.copy(
                  phrase = editPhrase.trim(),
                  alternativePhrases = editAlts.trim()
                )
              )
              shortcutToEdit = null
              Toast.makeText(context, "Voice command updated", Toast.LENGTH_SHORT).show()
            }
          }
        ) {
          Text("Save Shortcut", color = HologramCyan)
        }
      },
      dismissButton = {
        TextButton(onClick = { shortcutToEdit = null }) {
          Text("Cancel", color = HologramTextSecondary)
        }
      },
      containerColor = HologramCardBg
    )
  }

  if (showNotificationRationale) {
    AlertDialog(
      onDismissRequest = { showNotificationRationale = false },
      title = { Text("Notification Permission") },
      text = {
        Text("JARVIS uses notifications to provide background diagnostic alerts and telemetry updates.")
      },
      confirmButton = {
        TextButton(
          onClick = {
            showNotificationRationale = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
              notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
              viewModel.updateNotifications(true)
            }
          }
        ) {
          Text("Grant Permission", color = HologramCyan)
        }
      },
      dismissButton = {
        TextButton(onClick = { showNotificationRationale = false }) {
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
      title = "SETTINGS & SYSTEM",
      subtitle = "TACTICAL TELEMETRY & PREFERENCES",
      showBackButton = true,
      onBackClick = onNavigateBack
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
      // 1. Current User Profile & Account Management
      GlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = HologramPurple
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .clip(CircleShape)
                  .background(HologramPurple.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null, tint = HologramPurple, modifier = Modifier.size(24.dp))
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = currentUser?.fullName ?: "Local User",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = HologramTextPrimary
                )
                Text(
                  text = currentUser?.email ?: "No active account",
                  fontSize = 11.sp,
                  color = HologramTextSecondary
                )
              }
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(HologramEmerald.copy(alpha = 0.2f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(text = "LOCAL ONLY", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = HologramEmerald)
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = { showSwitchProfileDialog = true },
              modifier = Modifier
                .weight(1f)
                .height(40.dp),
              colors = ButtonDefaults.buttonColors(containerColor = HologramCardBg, contentColor = HologramCyan),
              border = androidx.compose.foundation.BorderStroke(1.dp, HologramCyan),
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Switch", fontSize = 11.sp)
            }

            Button(
              onClick = {
                viewModel.logout {
                  onLogout()
                }
              },
              modifier = Modifier
                .weight(1f)
                .height(40.dp),
              colors = ButtonDefaults.buttonColors(containerColor = HologramCardBg, contentColor = HologramTextSecondary),
              border = androidx.compose.foundation.BorderStroke(1.dp, HologramCardBorder),
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(imageVector = Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Log Out", fontSize = 11.sp)
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = {
                coroutineScope.launch {
                  val exported = viewModel.exportCurrentUserData()
                  showExportDialog = exported
                }
              },
              modifier = Modifier
                .weight(1f)
                .height(38.dp),
              colors = ButtonDefaults.buttonColors(containerColor = HologramCardBg, contentColor = HologramTextPrimary),
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Export Data", fontSize = 11.sp)
            }

            Button(
              onClick = { showDeleteProfileDialog = true },
              modifier = Modifier
                .weight(1f)
                .height(38.dp),
              colors = ButtonDefaults.buttonColors(containerColor = HologramRed.copy(alpha = 0.2f), contentColor = HologramRed),
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Delete Profile", fontSize = 11.sp)
            }
          }
        }
      }

      // 2. Personal API Key (BYOK) Status & Configuration
      GlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = HologramCyan
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "PERSONAL API KEY (BYOK)",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = HologramCyanBright
              )
              Text(
                text = "Google Gemini Key • Android Keystore Encrypted",
                fontSize = 11.sp,
                color = HologramTextSecondary
              )
            }

            Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = HologramCyan)
          }

          if (maskedKey.isNotBlank()) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0x3300F2FE))
                .padding(10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(text = "CURRENT KEY (MASKED):", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = HologramCyan)
                Text(text = maskedKey, fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = HologramTextPrimary)
              }
              IconButton(
                onClick = {
                  viewModel.removeApiKey {
                    Toast.makeText(context, "API Key removed", Toast.LENGTH_SHORT).show()
                  }
                },
                modifier = Modifier.size(32.dp)
              ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove Key", tint = HologramRed, modifier = Modifier.size(16.dp))
              }
            }
          } else {
            Text(
              text = "No personal Gemini API key configured for this profile. Using fallback autonomous engine.",
              fontSize = 11.sp,
              color = HologramTextSecondary
            )
          }

          Button(
            onClick = onNavigateToApiSetup,
            modifier = Modifier
              .fillMaxWidth()
              .height(44.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HologramCyan, contentColor = MaterialTheme.colorScheme.background),
            shape = RoundedCornerShape(10.dp)
          ) {
            Text(
              text = if (maskedKey.isNotBlank()) "REPLACE / VERIFY API KEY" else "CONFIGURE GOOGLE GEMINI KEY",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }

      // 3. Voice Command Shortcuts Section
      GlassCard(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("voice_shortcuts_settings_card"),
        borderColor = HologramCyan
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "VOICE COMMAND SHORTCUTS",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = HologramCyanBright
              )
              Text(
                text = "Customize triggers for hands-free actions",
                fontSize = 11.sp,
                color = HologramTextSecondary
              )
            }

            IconButton(
              onClick = {
                viewModel.resetDefaultShortcuts()
                Toast.makeText(context, "Voice shortcuts reset to defaults", Toast.LENGTH_SHORT).show()
              },
              modifier = Modifier.size(32.dp)
            ) {
              Icon(imageVector = Icons.Default.RestartAlt, contentDescription = "Reset Defaults", tint = HologramCyan)
            }
          }

          shortcuts.forEach { shortcut ->
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x2200F2FE))
                .border(1.dp, if (shortcut.isEnabled) HologramCyan.copy(alpha = 0.4f) else HologramCardBorder, RoundedCornerShape(12.dp))
                .padding(10.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = shortcut.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = HologramTextPrimary
                  )
                  Text(
                    text = "Trigger: \"${shortcut.phrase}\"",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = HologramCyan
                  )
                  if (shortcut.alternativePhrases.isNotBlank()) {
                    Text(
                      text = "Alts: ${shortcut.alternativePhrases}",
                      fontSize = 9.sp,
                      color = HologramTextSecondary,
                      maxLines = 1
                    )
                  }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                  IconButton(
                    onClick = {
                      viewModel.handleSpokenInput(shortcut.phrase)
                      Toast.makeText(context, "Executing \"${shortcut.phrase}\"", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(32.dp)
                  ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Test Voice Action", tint = HologramEmerald)
                  }

                  IconButton(
                    onClick = { shortcutToEdit = shortcut },
                    modifier = Modifier.size(32.dp)
                  ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Phrase", tint = HologramCyan, modifier = Modifier.size(16.dp))
                  }

                  Switch(
                    checked = shortcut.isEnabled,
                    onCheckedChange = { isChecked ->
                      viewModel.updateVoiceShortcut(shortcut.copy(isEnabled = isChecked))
                    },
                    colors = SwitchDefaults.colors(
                      checkedThumbColor = HologramCyan,
                      checkedTrackColor = HologramCyan.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.size(36.dp)
                  )
                }
              }
            }
          }
        }
      }

      // 4. AI Backend Custom HTTPS Endpoint Configuration
      GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "AI BACKEND HTTPS ENDPOINT",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = HologramCyanBright
          )
          Text(
            text = "Custom proxy / backend URL (optional).",
            fontSize = 11.sp,
            color = HologramTextSecondary
          )

          OutlinedTextField(
            value = backendUrlInput,
            onValueChange = {
              backendUrlInput = it
              viewModel.updateApiBaseUrl(it)
            },
            label = { Text("HTTPS API Base URL", fontSize = 12.sp) },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("api_url_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = HologramCyan,
              unfocusedBorderColor = HologramCardBorder,
              focusedTextColor = HologramTextPrimary,
              unfocusedTextColor = HologramTextPrimary,
              focusedContainerColor = HologramCardBg,
              unfocusedContainerColor = HologramCardBg
            ),
            singleLine = true
          )

          // Test Connection Button
          Button(
            onClick = { viewModel.testBackendConnection() },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("test_backend_button"),
            colors = ButtonDefaults.buttonColors(
              containerColor = HologramCyan,
              contentColor = MaterialTheme.colorScheme.background
            ),
            shape = RoundedCornerShape(12.dp)
          ) {
            if (isTestingConn) {
              CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.background, strokeWidth = 2.dp)
              Spacer(modifier = Modifier.width(8.dp))
              Text("TESTING HTTPS HANDSHAKE...", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
            } else {
              Icon(imageVector = Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("TEST HTTPS CONNECTION", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
          }

          if (testConnResult != null) {
            Text(
              text = testConnResult ?: "",
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              color = if (testConnResult!!.startsWith("ONLINE")) HologramEmerald else HologramCyan
            )
          }
        }
      }

      // 5. Holographic Theme & Appearance
      GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "HOLOGRAPHIC APPEARANCE",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = HologramCyanBright
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(text = "Dark Hologram Interface", fontSize = 13.sp, color = HologramTextPrimary)
            Switch(
              checked = settings.isDarkTheme,
              onCheckedChange = { viewModel.updateDarkTheme(it) },
              colors = SwitchDefaults.colors(
                checkedThumbColor = HologramCyan,
                checkedTrackColor = HologramCyan.copy(alpha = 0.3f)
              )
            )
          }

          Text(text = "Neon Color Palette:", fontSize = 12.sp, color = HologramTextSecondary)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            listOf(
              "CYAN_DEFAULT" to "Neon Cyan",
              "EMERALD_MATRIX" to "Matrix Green",
              "PURPLE_NEON" to "Amethyst"
            ).forEach { (mode, label) ->
              val isSelected = settings.themeMode == mode
              Box(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(10.dp))
                  .background(if (isSelected) HologramCyan.copy(alpha = 0.25f) else Color(0x33101B30))
                  .border(1.dp, if (isSelected) HologramCyan else HologramCardBorder, RoundedCornerShape(10.dp))
                  .clickable { viewModel.updateTheme(mode) }
                  .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = label,
                  fontSize = 10.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  color = if (isSelected) HologramCyanBright else HologramTextSecondary
                )
              }
            }
          }
        }
      }

      // 6. Voice & Speech Synthesis (TTS) Settings
      GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "SPEECH SYNTHESIS ENGINE",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = HologramCyanBright
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(text = "Auto-Speak AI Responses", fontSize = 13.sp, color = HologramTextPrimary)
            Switch(
              checked = settings.autoSpeakResponse,
              onCheckedChange = {
                viewModel.updateTtsSettings(settings.ttsSpeed, settings.ttsPitch, it)
              },
              colors = SwitchDefaults.colors(
                checkedThumbColor = HologramCyan,
                checkedTrackColor = HologramCyan.copy(alpha = 0.3f)
              )
            )
          }

          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(text = "Speech Rate", fontSize = 12.sp, color = HologramTextSecondary)
              Text(text = "${String.format(java.util.Locale.US, "%.1f", ttsSpeedSlider)}x", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = HologramCyan)
            }
            Slider(
              value = ttsSpeedSlider,
              onValueChange = {
                ttsSpeedSlider = it
                viewModel.updateTtsSettings(it, ttsPitchSlider, settings.autoSpeakResponse)
              },
              valueRange = 0.5f..2.0f,
              colors = SliderDefaults.colors(thumbColor = HologramCyan, activeTrackColor = HologramCyan)
            )
          }

          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(text = "Voice Pitch", fontSize = 12.sp, color = HologramTextSecondary)
              Text(text = "${String.format(java.util.Locale.US, "%.1f", ttsPitchSlider)}x", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = HologramPurple)
            }
            Slider(
              value = ttsPitchSlider,
              onValueChange = {
                ttsPitchSlider = it
                viewModel.updateTtsSettings(ttsSpeedSlider, it, settings.autoSpeakResponse)
              },
              valueRange = 0.5f..2.0f,
              colors = SliderDefaults.colors(thumbColor = HologramPurple, activeTrackColor = HologramPurple)
            )
          }
        }
      }

      // 7. Notifications & Permissions Diagnostics HUD
      GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "NOTIFICATIONS & PERMISSIONS",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = HologramCyanBright
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(text = "JARVIS Protocol Notifications", fontSize = 13.sp, color = HologramTextPrimary)
            Switch(
              checked = settings.notificationsEnabled,
              onCheckedChange = { enabled ->
                if (enabled) {
                  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                  ) {
                    showNotificationRationale = true
                  } else {
                    viewModel.updateNotifications(true)
                    showTestNotification(context)
                  }
                } else {
                  viewModel.updateNotifications(false)
                }
              },
              colors = SwitchDefaults.colors(
                checkedThumbColor = HologramCyan,
                checkedTrackColor = HologramCyan.copy(alpha = 0.3f)
              )
            )
          }

          Button(
            onClick = {
              showTestNotification(context)
              Toast.makeText(context, "Test notification dispatched", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = HologramCardBg, contentColor = HologramCyan),
            border = androidx.compose.foundation.BorderStroke(1.dp, HologramCyan),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(imageVector = Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Dispatch Test Protocol Notification", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
          }

          val micGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
          val camGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            PermissionBadge("Microphone", micGranted)
            PermissionBadge("Camera", camGranted)
            PermissionBadge("Network", true)
          }
        }
      }
    }
  }
}

@Composable
fun PermissionBadge(name: String, isGranted: Boolean) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(Color(0x33101B30))
      .border(1.dp, if (isGranted) HologramEmerald.copy(alpha = 0.4f) else HologramRed.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
      .padding(horizontal = 8.dp, vertical = 4.dp)
  ) {
    Icon(
      imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
      contentDescription = null,
      tint = if (isGranted) HologramEmerald else HologramRed,
      modifier = Modifier.size(12.dp)
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(
      text = name,
      fontSize = 10.sp,
      fontFamily = FontFamily.Monospace,
      color = if (isGranted) HologramEmerald else HologramRed
    )
  }
}

private fun showTestNotification(context: Context) {
  val channelId = "jarvis_alerts"
  val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    val channel = NotificationChannel(
      channelId,
      "JARVIS Protocol Alerts",
      NotificationManager.IMPORTANCE_DEFAULT
    ).apply {
      description = "Telemetry and status notifications from JARVIS AI"
    }
    notificationManager.createNotificationChannel(channel)
  }

  val notification = NotificationCompat.Builder(context, channelId)
    .setSmallIcon(android.R.drawable.ic_dialog_info)
    .setContentTitle("JARVIS AI Protocol")
    .setContentText("All systems nominal. Neural core v4.2 standing by.")
    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    .setAutoCancel(true)
    .build()

  try {
    notificationManager.notify(101, notification)
  } catch (_: SecurityException) {}
}
