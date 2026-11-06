package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.HolographicCore
import com.example.ui.components.JarvisCoreState
import com.example.ui.theme.HologramCardBg
import com.example.ui.theme.HologramCardBorder
import com.example.ui.theme.HologramCyan
import com.example.ui.theme.HologramCyanBright
import com.example.ui.theme.HologramEmerald
import com.example.ui.theme.HologramTextPrimary
import com.example.ui.theme.HologramTextSecondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
  onBootComplete: () -> Unit
) {
  val progress = remember { Animatable(0f) }
  val logs = remember { mutableStateListOf<String>() }
  var isReady by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    progress.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 2800, easing = LinearEasing)
    )
  }

  LaunchedEffect(Unit) {
    delay(300)
    logs.add("[SYS] INITIALIZING JARVIS PROTOCOL v4.2")
    delay(500)
    logs.add("[CORE] NEURAL MATRIX SYNCHRONIZED")
    delay(500)
    logs.add("[VOICE] BILINGUAL SPEECH (EN / BN) READY")
    delay(500)
    logs.add("[CMD] 6 VOICE SHORTCUT PROTOCOLS LOADED")
    delay(500)
    logs.add("[HUD] ALL SYSTEMS NOMINAL // ENGAGING")
    isReady = true
    delay(600)
    onBootComplete()
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Text(
        text = "J.A.R.V.I.S.",
        fontSize = 32.sp,
        fontWeight = FontWeight.ExtraBold,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 6.sp,
        color = HologramCyanBright
      )

      Text(
        text = "JUST A RATHER VERY INTELLIGENT SYSTEM",
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        color = HologramTextSecondary,
        letterSpacing = 2.sp,
        modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
      )

      HolographicCore(
        size = 190.dp,
        state = if (isReady) JarvisCoreState.PROCESSING else JarvisCoreState.LISTENING
      )

      Spacer(modifier = Modifier.height(32.dp))

      // Boot Progress Bar
      LinearProgressIndicator(
        progress = { progress.value },
        modifier = Modifier
          .fillMaxWidth(0.75f)
          .height(4.dp)
          .clip(RoundedCornerShape(2.dp)),
        color = HologramCyan,
        trackColor = HologramCardBorder
      )

      Spacer(modifier = Modifier.height(24.dp))

      // Diagnostic Logs Terminal
      Box(
        modifier = Modifier
          .fillMaxWidth(0.9f)
          .clip(RoundedCornerShape(12.dp))
          .background(HologramCardBg)
          .padding(14.dp)
      ) {
        Column {
          logs.takeLast(4).forEach { log ->
            Row(
              modifier = Modifier.padding(vertical = 2.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .clip(RoundedCornerShape(3.dp))
                  .background(HologramEmerald)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = log,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = HologramTextPrimary,
                letterSpacing = 0.5.sp
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      Button(
        onClick = onBootComplete,
        modifier = Modifier
          .testTag("skip_boot_button")
          .alpha(if (progress.value > 0.4f) 1f else 0.5f),
        colors = ButtonDefaults.buttonColors(
          containerColor = HologramCyan,
          contentColor = MaterialTheme.colorScheme.background
        ),
        shape = RoundedCornerShape(24.dp)
      ) {
        Text(
          text = if (isReady) "ENGAGE INTERFACE" else "INITIALIZING...",
          fontWeight = FontWeight.Bold,
          fontFamily = FontFamily.Monospace,
          letterSpacing = 1.sp
        )
      }
    }
  }
}
