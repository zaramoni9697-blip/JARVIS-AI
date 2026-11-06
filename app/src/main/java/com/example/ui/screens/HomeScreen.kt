package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.HologramHeader
import com.example.ui.components.HolographicCore
import com.example.ui.components.JarvisCoreState
import com.example.ui.theme.HologramCardBorder
import com.example.ui.theme.HologramCyan
import com.example.ui.theme.HologramCyanBright
import com.example.ui.theme.HologramEmerald
import com.example.ui.theme.HologramPurple
import com.example.ui.theme.HologramTextPrimary
import com.example.ui.theme.HologramTextSecondary
import com.example.ui.viewmodel.JarvisViewModel

data class DashboardAction(
  val id: String,
  val title: String,
  val subtitle: String,
  val icon: ImageVector,
  val accentColor: Color,
  val destination: String
)

@Composable
fun HomeScreen(
  viewModel: JarvisViewModel,
  onNavigate: (String) -> Unit
) {
  val coreState by viewModel.coreState.collectAsState()
  val shortcuts by viewModel.allShortcuts.collectAsState()
  val currentUser by viewModel.currentUser.collectAsState()
  val maskedKey by viewModel.maskedApiKey.collectAsState()

  val actions = listOf(
    DashboardAction("chat", "AI Chat", "Bilingual EN / বাংলা", Icons.Default.ChatBubble, HologramCyan, "chat"),
    DashboardAction("voice", "Voice Assistant", "Speech & Live Audio", Icons.Default.Mic, HologramPurple, "voice"),
    DashboardAction("vision", "Image Analysis", "Multimodal Vision AI", Icons.Default.CameraAlt, HologramEmerald, "vision"),
    DashboardAction("history", "History", "Session Archives", Icons.Default.History, HologramCyanBright, "history"),
    DashboardAction("memory", "Memory Matrix", "Opt-in Local Context", Icons.Default.Psychology, HologramPurple, "memory"),
    DashboardAction("settings", "Settings", "Backend & Shortcuts", Icons.Default.Settings, HologramCyan, "settings")
  )

  val quickPrompts = listOf(
    "Explain quantum computing",
    "আজকের আবহাওয়া ও প্রযুক্তি",
    "Run system diagnostics",
    "বাংলায় অনুপ্রেরণামূলক কথা বলো",
    "Who created JARVIS?"
  )

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    HologramHeader(
      title = "JARVIS HUD",
      subtitle = "TACTICAL AI COMMAND DASHBOARD",
      actions = {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x3300F2FE))
            .border(1.dp, HologramCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .clickable { onNavigate("settings") }
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (maskedKey.isNotBlank()) HologramEmerald else HologramCyan)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = currentUser?.fullName?.take(12) ?: "Profile",
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              color = HologramCyanBright
            )
          }
        }
      }
    )

    // User Profile & BYOK Quick Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 4.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(Color(0x22101B30))
        .border(1.dp, HologramCardBorder, RoundedCornerShape(10.dp))
        .clickable { onNavigate("api_setup") }
        .padding(horizontal = 12.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "OPERATOR: ${currentUser?.fullName?.uppercase() ?: "LOCAL"}",
          fontSize = 10.sp,
          fontFamily = FontFamily.Monospace,
          color = HologramTextPrimary
        )
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = if (maskedKey.isNotBlank()) "BYOK: $maskedKey" else "BYOK: CONFIGURE KEY",
          fontSize = 9.sp,
          fontFamily = FontFamily.Monospace,
          color = if (maskedKey.isNotBlank()) HologramEmerald else HologramCyan
        )
      }
    }

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
    ) {
      // 1. Center Holographic Core Card
      item {
        GlassCard(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("home_core_card"),
          cornerRadius = 20.dp
        ) {
          Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "NEURAL CORE STATUS",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = HologramCyan,
                letterSpacing = 1.2.sp
              )
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(
                      when (coreState) {
                        JarvisCoreState.LISTENING -> HologramPurple
                        JarvisCoreState.PROCESSING -> HologramCyan
                        JarvisCoreState.SPEAKING -> HologramEmerald
                        else -> HologramCyan
                      }
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = coreState.name,
                  fontSize = 10.sp,
                  fontFamily = FontFamily.Monospace,
                  color = HologramTextSecondary
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HolographicCore(
              size = 160.dp,
              state = coreState,
              onClick = {
                onNavigate("voice")
              }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
              text = when (coreState) {
                JarvisCoreState.LISTENING -> "Listening to audio frequency..."
                JarvisCoreState.PROCESSING -> "Synthesizing neural response..."
                JarvisCoreState.SPEAKING -> "Transmitting vocal telemetry..."
                else -> "Tap core to activate Voice Assistant"
              },
              fontSize = 12.sp,
              fontFamily = FontFamily.Monospace,
              color = HologramTextSecondary,
              letterSpacing = 0.5.sp
            )
          }
        }
      }

      // 2. Active Voice Command Shortcuts Strip
      item {
        Column {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "VOICE COMMAND SHORTCUTS",
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              color = HologramTextPrimary,
              letterSpacing = 1.sp
            )
            Text(
              text = "Tap to execute",
              fontSize = 10.sp,
              fontFamily = FontFamily.Monospace,
              color = HologramCyan,
              modifier = Modifier.clickable { onNavigate("settings") }
            )
          }

          LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
          ) {
            items(shortcuts) { shortcut ->
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .background(Color(0xAA101B30))
                  .border(1.dp, HologramCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                  .clickable {
                    viewModel.handleSpokenInput(shortcut.phrase)
                  }
                  .padding(horizontal = 12.dp, vertical = 8.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = HologramCyan,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Column {
                    Text(
                      text = shortcut.title,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = HologramTextPrimary
                    )
                    Text(
                      text = "\"${shortcut.phrase}\"",
                      fontSize = 9.sp,
                      fontFamily = FontFamily.Monospace,
                      color = HologramCyan
                    )
                  }
                }
              }
            }
          }
        }
      }

      // 3. Grid of Main Modules (Action Hub)
      item {
        Text(
          text = "TACTICAL MODULES",
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          color = HologramTextPrimary,
          letterSpacing = 1.sp,
          modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
        )
      }

      items(actions.chunked(2)) { pair ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          for (action in pair) {
            GlassCard(
              modifier = Modifier
                .weight(1f)
                .testTag("action_${action.id}"),
              borderColor = action.accentColor.copy(alpha = 0.5f),
              onClick = { onNavigate(action.destination) }
            ) {
              Column {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(action.accentColor.copy(alpha = 0.15f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = action.icon,
                    contentDescription = action.title,
                    tint = action.accentColor,
                    modifier = Modifier.size(20.dp)
                  )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                  text = action.title,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = HologramTextPrimary
                )

                Text(
                  text = action.subtitle,
                  fontSize = 10.sp,
                  color = HologramTextSecondary,
                  maxLines = 1
                )
              }
            }
          }
        }
      }

      // 4. Suggested AI Inquiries
      item {
        Column(modifier = Modifier.padding(top = 8.dp)) {
          Text(
            text = "QUICK INTELLIGENCE QUERIES",
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
            items(quickPrompts) { prompt ->
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(16.dp))
                  .background(Color(0x5500F2FE))
                  .border(1.dp, HologramCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                  .clickable {
                    viewModel.sendChatMessage(prompt)
                    onNavigate("chat")
                  }
                  .padding(horizontal = 14.dp, vertical = 8.dp)
              ) {
                Text(
                  text = prompt,
                  fontSize = 11.sp,
                  color = HologramCyanBright
                )
              }
            }
          }
        }
      }
    }
  }
}
