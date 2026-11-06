package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
fun SignupScreen(
  viewModel: JarvisViewModel,
  onSignupSuccess: () -> Unit,
  onNavigateToLogin: () -> Unit
) {
  var fullName by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }
  var confirmPasswordVisible by remember { mutableStateOf(false) }
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
    Spacer(modifier = Modifier.height(16.dp))

    HolographicCore(
      size = 130.dp,
      state = if (isLoading) JarvisCoreState.PROCESSING else JarvisCoreState.IDLE
    )

    Spacer(modifier = Modifier.height(14.dp))

    Text(
      text = "NEW LOCAL PROFILE",
      fontSize = 20.sp,
      fontWeight = FontWeight.ExtraBold,
      fontFamily = FontFamily.Monospace,
      letterSpacing = 2.sp,
      color = HologramCyanBright
    )

    Text(
      text = "INITIALIZE ISOLATED ON-DEVICE USER",
      fontSize = 10.sp,
      fontFamily = FontFamily.Monospace,
      letterSpacing = 1.sp,
      color = HologramTextSecondary,
      modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
    )

    GlassCard(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("signup_card"),
      cornerRadius = 20.dp,
      borderColor = HologramCyan
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Local Notice
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
              text = "LOCAL-ONLY REGISTRATION: This account is created and stored exclusively on your device. It does not sync to cloud servers. Password is protected with salted SHA-256 hashing.",
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace,
              color = HologramTextPrimary,
              lineHeight = 14.sp
            )
          }
        }

        // Full Name Field
        OutlinedTextField(
          value = fullName,
          onValueChange = {
            fullName = it
            errorMessage = null
          },
          label = { Text("Full Name", fontSize = 12.sp) },
          leadingIcon = {
            Icon(imageVector = Icons.Default.Badge, contentDescription = null, tint = HologramCyan)
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("signup_name_input"),
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

        // Email Field
        OutlinedTextField(
          value = email,
          onValueChange = {
            email = it
            errorMessage = null
          },
          label = { Text("Local Email Identifier", fontSize = 12.sp) },
          leadingIcon = {
            Icon(imageVector = Icons.Default.Mail, contentDescription = null, tint = HologramCyan)
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("signup_email_input"),
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
          label = { Text("Master Password (min 6 chars)", fontSize = 12.sp) },
          leadingIcon = {
            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = HologramCyan)
          },
          trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
              Icon(
                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDescription = null,
                tint = HologramTextSecondary
              )
            }
          },
          visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("signup_password_input"),
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

        // Confirm Password Field
        OutlinedTextField(
          value = confirmPassword,
          onValueChange = {
            confirmPassword = it
            errorMessage = null
          },
          label = { Text("Confirm Master Password", fontSize = 12.sp) },
          leadingIcon = {
            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = HologramCyan)
          },
          trailingIcon = {
            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
              Icon(
                imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDescription = null,
                tint = HologramTextSecondary
              )
            }
          },
          visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("signup_confirm_password_input"),
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

        // Remember Me
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Remember this device session",
            fontSize = 12.sp,
            color = HologramTextSecondary
          )
          Switch(
            checked = rememberMe,
            onCheckedChange = { rememberMe = it },
            colors = SwitchDefaults.colors(
              checkedThumbColor = HologramCyan,
              checkedTrackColor = HologramCyan.copy(alpha = 0.3f)
            ),
            modifier = Modifier.testTag("signup_remember_switch")
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

        // Signup Submit Button
        Button(
          onClick = {
            if (fullName.isBlank() || email.isBlank() || password.isBlank()) {
              errorMessage = "All fields are required."
              return@Button
            }
            if (password != confirmPassword) {
              errorMessage = "Passwords do not match."
              return@Button
            }
            isLoading = true
            errorMessage = null

            viewModel.signup(
              fullName = fullName,
              email = email,
              password = password,
              confirmPassword = confirmPassword,
              rememberMe = rememberMe,
              onSuccess = {
                isLoading = false
                onSignupSuccess()
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
            .testTag("signup_submit_button"),
          colors = ButtonDefaults.buttonColors(
            containerColor = HologramCyan,
            contentColor = MaterialTheme.colorScheme.background
          ),
          shape = RoundedCornerShape(14.dp)
        ) {
          if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.background, strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Text("INITIALIZING PROFILE...", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
          } else {
            Text("CREATE LOCAL PROFILE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp)
          }
        }

        // Navigate to Login
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Already have a profile on this device?",
            fontSize = 12.sp,
            color = HologramTextSecondary
          )
          TextButton(
            onClick = onNavigateToLogin,
            modifier = Modifier.testTag("go_to_login_button")
          ) {
            Text(
              text = "Log In",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = HologramCyanBright
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
    Text(
      text = "IMPORTANT: If app data is cleared, local accounts and data will be permanently wiped.",
      fontSize = 9.sp,
      fontFamily = FontFamily.Monospace,
      color = HologramTextSecondary.copy(alpha = 0.7f),
      lineHeight = 13.sp
    )
  }
}
