package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HologramCyan
import com.example.ui.theme.HologramEmerald
import com.example.ui.theme.HologramTextPrimary
import com.example.ui.theme.HologramTextSecondary

@Composable
fun HologramHeader(
  title: String,
  subtitle: String? = null,
  showBackButton: Boolean = false,
  onBackClick: (() -> Unit)? = null,
  actions: @Composable (() -> Unit)? = null
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp)
  ) {
    // HUD Telemetry Ribbon
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
            .size(8.dp)
            .clip(CircleShape)
            .background(HologramEmerald)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "JARVIS CORE // ONLINE",
          fontSize = 10.sp,
          fontFamily = FontFamily.Monospace,
          color = HologramCyan,
          letterSpacing = 1.2.sp
        )
      }

      Text(
        text = "SECURE // SSL v3.4",
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace,
        color = HologramTextSecondary,
        letterSpacing = 1.sp
      )
    }

    // Title Row
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        if (showBackButton && onBackClick != null) {
          IconButton(
            onClick = onBackClick,
            modifier = Modifier
              .testTag("header_back_button")
              .size(40.dp)
              .clip(CircleShape)
              .border(1.dp, HologramCyan.copy(alpha = 0.4f), CircleShape)
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = HologramCyan
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
        }

        Column {
          Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = HologramTextPrimary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
          )
          if (subtitle != null) {
            Text(
              text = subtitle,
              style = MaterialTheme.typography.bodySmall,
              color = HologramTextSecondary
            )
          }
        }
      }

      if (actions != null) {
        actions()
      }
    }

    // Holographic Tech Divider Line
    Spacer(modifier = Modifier.height(8.dp))
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(1.dp)
        .background(
          brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
            colors = listOf(
              HologramCyan.copy(alpha = 0.8f),
              HologramCyan.copy(alpha = 0.2f),
              Color.Transparent
            )
          )
        )
    )
  }
}
