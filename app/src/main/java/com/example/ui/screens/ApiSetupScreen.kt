package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun ApiSetupScreen(
  viewModel: JarvisViewModel,
  onSetupComplete: () -> Unit,
  onNavigateBack: (() -> Unit)? = null
) {
  val context = LocalContext.current
  val currentUser by viewModel.currentUser.collectAsState()
  val maskedKey by viewModel.maskedApiKey.collectAsState()
  val isVerifying by viewModel.isVerifyingKey.collectAsState()
  val verificationStatus by viewModel.keyVerificationStatus.collectAsState()

  var apiKeyInput by remember { mutableStateOf("") }
  var isKeyVisible by remember { mutableStateOf(false) }
  var saveError by remember { mutableStateOf<String?>(null) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    HologramHeader(
      title = "AI PROVIDER SETUP",
      subtitle = "BRING YOUR OWN KEY (BYOK)",
      showBackButton = onNavigateBack != null,
      onBackClick = onNavigateBack
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Profile Context Badge
      GlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = HologramPurple
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "LOCAL PROFILE LINK",
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace,
              color = HologramPurple,
              letterSpacing = 1.sp
            )
            Text(
              text = currentUser?.fullName ?: "Anonymous Operator",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = HologramTextPrimary
            )
            Text(
              text = currentUser?.email ?: "",
              fontSize = 11.sp,
              color = HologramTextSecondary
            )
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(HologramPurple.copy(alpha = 0.2f))
              .padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Text(
              text = "ISOLATED",
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              color = HologramPurple
            )
          }
        }
      }

      // 2. Provider Card (Google Gemini)
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
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(HologramCyan.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.AutoAwesome,
                  contentDescription = null,
                  tint = HologramCyan,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = "Google Gemini AI",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = HologramTextPrimary
                )
                Text(
                  text = "Model: Gemini 1.5 Flash (Direct HTTPS)",
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
              Text(
                text = "SUPPORTED",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = HologramEmerald
              )
            }
          }

          Text(
            text = "Your API key is used exclusively for requests initiated by this profile. It is encrypted in Android Keystore (AES-256-GCM) and never transmitted to any shared servers or logging services.",
            fontSize = 11.sp,
            color = HologramTextSecondary,
            lineHeight = 16.sp
          )
        }
      }

      // 3. Current Key Status (if already saved)
      if (maskedKey.isNotBlank()) {
        GlassCard(
          modifier = Modifier.fillMaxWidth(),
          borderColor = HologramEmerald
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = HologramEmerald, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "ACTIVE PERSONAL API KEY",
                  fontSize = 11.sp,
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.Bold,
                  color = HologramEmerald
                )
              }
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = maskedKey,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = HologramTextPrimary
              )
            }

            IconButton(
              onClick = {
                viewModel.removeApiKey {
                  Toast.makeText(context, "API Key removed from this profile", Toast.LENGTH_SHORT).show()
                }
              },
              modifier = Modifier.size(36.dp)
            ) {
              Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove Key", tint = HologramRed, modifier = Modifier.size(18.dp))
            }
          }
        }
      }

      // 4. API Key Input Field with Show/Hide
      GlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = HologramCardBorder
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(
            text = if (maskedKey.isNotBlank()) "REPLACE / CONFIGURE API KEY" else "ENTER GOOGLE GEMINI API KEY",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = HologramCyanBright
          )

          OutlinedTextField(
            value = apiKeyInput,
            onValueChange = {
              apiKeyInput = it
              saveError = null
            },
            label = { Text("Gemini API Key (e.g. AIzaSy...)", fontSize = 12.sp) },
            leadingIcon = {
              Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = HologramCyan)
            },
            trailingIcon = {
              IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                Icon(
                  imageVector = if (isKeyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                  contentDescription = "Toggle key visibility",
                  tint = HologramTextSecondary
                )
              }
            },
            visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("byok_api_key_input"),
            shape = RoundedCornerShape(14.dp),
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

          // Action Buttons: Verify Key and Save Key
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Verify Button
            Button(
              onClick = {
                viewModel.verifyApiKeyOnly(apiKeyInput)
              },
              enabled = apiKeyInput.isNotBlank() && !isVerifying,
              modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .testTag("verify_api_key_button"),
              colors = ButtonDefaults.buttonColors(
                containerColor = HologramCardBg,
                contentColor = HologramCyan
              ),
              border = androidx.compose.foundation.BorderStroke(1.dp, HologramCyan),
              shape = RoundedCornerShape(12.dp)
            ) {
              if (isVerifying) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = HologramCyan, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Testing...", fontSize = 11.sp)
              } else {
                Text("Verify Key", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }
            }

            // Save Key Button
            Button(
              onClick = {
                viewModel.saveApiKey(
                  apiKey = apiKeyInput,
                  onSuccess = {
                    apiKeyInput = ""
                    Toast.makeText(context, "API Key verified and encrypted locally!", Toast.LENGTH_SHORT).show()
                    onSetupComplete()
                  },
                  onError = { err ->
                    saveError = err
                  }
                )
              },
              enabled = apiKeyInput.isNotBlank() && !isVerifying,
              modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .testTag("save_api_key_button"),
              colors = ButtonDefaults.buttonColors(
                containerColor = HologramCyan,
                contentColor = MaterialTheme.colorScheme.background
              ),
              shape = RoundedCornerShape(12.dp)
            ) {
              Text("Save Key", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }

          // Verification feedback message
          if (verificationStatus != null) {
            val isSuccess = verificationStatus!!.startsWith("SUCCESS")
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSuccess) HologramEmerald.copy(alpha = 0.15f) else HologramRed.copy(alpha = 0.15f))
                .padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (isSuccess) HologramEmerald else HologramRed,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = verificationStatus ?: "",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = if (isSuccess) HologramEmerald else HologramRed
              )
            }
          }

          if (saveError != null) {
            Text(
              text = saveError ?: "",
              fontSize = 11.sp,
              color = HologramRed
            )
          }
        }
      }

      // 5. Skip / Continue Button
      Button(
        onClick = onSetupComplete,
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("api_setup_continue_button"),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (maskedKey.isNotBlank()) HologramCyan else HologramCardBg,
          contentColor = if (maskedKey.isNotBlank()) MaterialTheme.colorScheme.background else HologramCyan
        ),
        border = if (maskedKey.isBlank()) androidx.compose.foundation.BorderStroke(1.dp, HologramCardBorder) else null,
        shape = RoundedCornerShape(14.dp)
      ) {
        Text(
          text = if (maskedKey.isNotBlank()) "ENTER JARVIS DASHBOARD" else "SKIP FOR NOW (USE AUTONOMOUS ENGINE)",
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp
        )
      }

      // Security Disclaimers Box
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(Color(0x22101B30))
          .border(1.dp, HologramCardBorder, RoundedCornerShape(12.dp))
        .padding(12.dp)
      ) {
        Row(verticalAlignment = Alignment.Top) {
          Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = HologramCyan, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = "SECURITY GUARANTEE:",
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              color = HologramCyan
            )
            Text(
              text = "• Keys are stored only on this physical device.\n• Encrypted with Android Keystore hardware-backed keys.\n• Never synced across devices or sent to analytics.\n• AI requests require active internet access to reach the Gemini API.",
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace,
              color = HologramTextSecondary,
              lineHeight = 14.sp
            )
          }
        }
      }
    }
  }
}
