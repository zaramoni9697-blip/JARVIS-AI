package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.HolographicCore
import com.example.ui.components.JarvisCoreState
import com.example.ui.theme.HologramCardBg
import com.example.ui.theme.HologramCardBorder
import com.example.ui.theme.HologramCyan
import com.example.ui.theme.HologramCyanBright
import com.example.ui.theme.HologramRed
import com.example.ui.theme.HologramTextPrimary
import com.example.ui.theme.HologramTextSecondary
import com.example.ui.viewmodel.JarvisViewModel

@Composable
fun LoginScreen(
  viewModel: JarvisViewModel,
  onLoginSuccess: (hasApiKey: Boolean) -> Unit,
  onNavigateToSignup: () -> Unit
) {
  val context = LocalContext.current
  val allProfiles by viewModel.allProfiles.collectAsState()

  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }
  var rememberMe by remember { mutableStateOf(true) }
  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .verticalScroll(rememberScrollState())
      .padding(20.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Spacer(modifier = Modifier.height(20.dp))

    // Holographic Core Indicator
    HolographicCore(
      size = 140.dp,
      state = if (isLoading) JarvisCoreState.PROCESSING else JarvisCoreState.IDLE
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = "JARVIS ACCESS PORTAL",
      fontSize = 20.sp,
      fontWeight = FontWeight.ExtraBold,
      fontFamily = FontFamily.Monospace,
      letterSpacing = 2.sp,
      color = HologramCyanBright
    )

    Text(
      text = "LOCAL DEVICE AUTHENTICATION MATRIX",
      fontSize = 10.sp,
      fontFamily = FontFamily.Monospace,
      letterSpacing = 1.sp,
      color = HologramTextSecondary,
      modifier = Modifier.padding(top = 2.dp, bottom = 18.dp)
    )

    // Holographic Login GlassCard
    GlassCard(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("login_card"),
      cornerRadius = 20.dp,
      borderColor = HologramCyan
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Local-only Disclaimer Notice
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0x3300F2FE))
            .border(1.dp, HologramCyan.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(10.dp)
        ) {
          Row(verticalAlignment = Alignment.Top) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = null,
              tint = HologramCyan,
              modifier = Modifier
                .size(16.dp)
                .padding(top = 1.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "LOCAL AUTHENTICATION: Profile records reside strictly in encrypted on-device SQLite storage. Not a cloud account. No server identity or password recovery.",
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace,
              color = HologramTextPrimary,
              lineHeight = 14.sp
            )
          }
        }

        // Email Field
        OutlinedTextField(
          value = email,
          onValueChange = {
            email = it
            errorMessage = null
          },
          label = { Text("Local Email Address", fontSize = 12.sp) },
          leadingIcon = {
            Icon(imageVector = Icons.Default.Mail, contentDescription = null, tint = HologramCyan)
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("login_email_input"),
          shape = RoundedCornerShape(14.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = HologramCyan,
            unfocusedBorderColor = HologramCardBorder,
            focusedTextColor = HologramTextPrimary,
            unfocusedTextColor = HologramTextPrimary,
            focusedContainerColor = HologramCardBg,
            unfocusedContainerColor = HologramCardBg
          ),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
          singleLine = true
        )

        // Password Field
        OutlinedTextField(
          value = password,
          onValueChange = {
            password = it
            errorMessage = null
          },
          label = { Text("Local Master Password", fontSize = 12.sp) },
          leadingIcon = {
            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = HologramCyan)
          },
          trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
              Icon(
                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDescription = "Toggle password",
                tint = HologramTextSecondary
              )
            }
          },
          visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("login_password_input"),
          shape = RoundedCornerShape(14.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = HologramCyan,
            unfocusedBorderColor = HologramCardBorder,
            focusedTextColor = HologramTextPrimary,
            unfocusedTextColor = HologramTextPrimary,
            focusedContainerColor = HologramCardBg,
            unfocusedContainerColor = HologramCardBg
          ),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
          singleLine = true
        )

        // Remember Me Switch
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "Remember this device session",
              fontSize = 12.sp,
              color = HologramTextSecondary
            )
          }

          Switch(
            checked = rememberMe,
            onCheckedChange = { rememberMe = it },
            colors = SwitchDefaults.colors(
              checkedThumbColor = HologramCyan,
              checkedTrackColor = HologramCyan.copy(alpha = 0.3f)
            ),
            modifier = Modifier.testTag("remember_me_switch")
          )
        }

        if (errorMessage != null) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(HologramRed.copy(alpha = 0.15f))
              .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = HologramRed, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = errorMessage ?: "",
              fontSize = 11.sp,
              color = HologramRed
            )
          }
        }

        // Login Button
        Button(
          onClick = {
            if (email.isBlank() || password.isBlank()) {
              errorMessage = "Please enter both email and password."
              return@Button
            }
            isLoading = true
            errorMessage = null

            viewModel.login(
              email = email,
              password = password,
              rememberMe = rememberMe,
              onSuccess = { user ->
                isLoading = false
                val hasKey = !user.encryptedApiKey.isNullOrBlank()
                onLoginSuccess(hasKey)
              },
              onError = { err ->
                isLoading = false
                errorMessage = err
              }
            )
          },
          enabled = !isLoading,
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("login_submit_button"),
          colors = ButtonDefaults.buttonColors(
            containerColor = HologramCyan,
            contentColor = MaterialTheme.colorScheme.background
          ),
          shape = RoundedCornerShape(14.dp)
        ) {
          if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.background, strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Text("VERIFYING LOCAL CREDENTIALS...", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
          } else {
            Text("AUTHENTICATE // ACCESS JARVIS", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
        }

        // Navigate to Signup
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "No profile on this device?",
            fontSize = 12.sp,
            color = HologramTextSecondary
          )
          TextButton(
            onClick = onNavigateToSignup,
            modifier = Modifier.testTag("go_to_signup_button")
          ) {
            Text(
              text = "Create Profile",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = HologramCyanBright
            )
          }
        }
      }
    }

    // Existing Local Profiles Quick Switcher (if any)
    if (allProfiles.isNotEmpty()) {
      Spacer(modifier = Modifier.height(18.dp))
      Text(
        text = "EXISTING LOCAL PROFILES ON DEVICE",
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        color = HologramTextSecondary,
        letterSpacing = 1.sp
      )
      Spacer(modifier = Modifier.height(8.dp))

      allProfiles.forEach { profile ->
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x33101B30))
            .border(1.dp, HologramCardBorder, RoundedCornerShape(12.dp))
            .clickable {
              email = profile.email
              Toast.makeText(context, "Loaded ${profile.fullName}", Toast.LENGTH_SHORT).show()
            }
            .padding(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .clip(CircleShape)
                  .background(HologramCyan.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Person,
                  contentDescription = null,
                  tint = HologramCyan,
                  modifier = Modifier.size(18.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = profile.fullName,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  color = HologramTextPrimary
                )
                Text(
                  text = profile.email,
                  fontSize = 11.sp,
                  color = HologramTextSecondary
                )
              }
            }

            Text(
              text = "Select",
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              color = HologramCyan
            )
          }
        }
      }
    }

    // Storage Wipe Warning Note
    Spacer(modifier = Modifier.height(16.dp))
    Text(
      text = "IMPORTANT: If app storage is cleared or app is uninstalled, all local profiles, chats, and memories will be permanently wiped.",
      fontSize = 9.sp,
      fontFamily = FontFamily.Monospace,
      color = HologramTextSecondary.copy(alpha = 0.7f),
      lineHeight = 13.sp
    )
  }
}
