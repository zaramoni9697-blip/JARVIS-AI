package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ChatSessionEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.HologramHeader
import com.example.ui.theme.HologramCardBg
import com.example.ui.theme.HologramCardBorder
import com.example.ui.theme.HologramCyan
import com.example.ui.theme.HologramCyanBright
import com.example.ui.theme.HologramRed
import com.example.ui.theme.HologramTextPrimary
import com.example.ui.theme.HologramTextSecondary
import com.example.ui.viewmodel.JarvisViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
  viewModel: JarvisViewModel,
  onNavigateBack: () -> Unit,
  onOpenSession: () -> Unit
) {
  val context = LocalContext.current
  val sessions by viewModel.allSessions.collectAsState()
  var searchQuery by remember { mutableStateOf("") }
  var showClearAllDialog by remember { mutableStateOf(false) }

  val filteredSessions = remember(sessions, searchQuery) {
    if (searchQuery.isBlank()) sessions
    else sessions.filter {
      it.title.contains(searchQuery, ignoreCase = true) ||
        it.lastMessage.contains(searchQuery, ignoreCase = true)
    }
  }

  if (showClearAllDialog) {
    AlertDialog(
      onDismissRequest = { showClearAllDialog = false },
      title = { Text("Purge Conversation Matrix?") },
      text = { Text("This will permanently delete all conversation transcripts and chat sessions from local memory.") },
      confirmButton = {
        TextButton(
          onClick = {
            showClearAllDialog = false
            viewModel.clearAllHistory()
            Toast.makeText(context, "All history purged", Toast.LENGTH_SHORT).show()
          }
        ) {
          Text("Purge All", color = HologramRed)
        }
      },
      dismissButton = {
        TextButton(onClick = { showClearAllDialog = false }) {
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
      title = "CONVERSATION ARCHIVES",
      subtitle = "TACTICAL SESSION LOGS",
      showBackButton = true,
      onBackClick = onNavigateBack,
      actions = {
        if (sessions.isNotEmpty()) {
          IconButton(
            onClick = { showClearAllDialog = true },
            modifier = Modifier.testTag("clear_all_history_button")
          ) {
            Icon(
              imageVector = Icons.Default.DeleteSweep,
              contentDescription = "Clear All",
              tint = HologramRed
            )
          }
        }
      }
    )

    // Search Bar
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      leadingIcon = {
        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = HologramCyan)
      },
      placeholder = { Text("Filter archives by keyword...", fontSize = 12.sp, color = HologramTextSecondary) },
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp)
        .testTag("history_search_input"),
      shape = RoundedCornerShape(20.dp),
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

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
      contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
      if (filteredSessions.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 60.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.Forum,
                contentDescription = null,
                tint = HologramCyan.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
              )
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = "NO CONVERSATIONS ARCHIVED",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = HologramCyan,
                letterSpacing = 1.sp
              )
              Text(
                text = "Initiate a chat session to populate neural logs",
                fontSize = 11.sp,
                color = HologramTextSecondary
              )
            }
          }
        }
      }

      items(filteredSessions, key = { it.sessionId }) { session ->
        HistorySessionCard(
          session = session,
          onClick = {
            viewModel.switchSession(session.sessionId)
            onOpenSession()
          },
          onDelete = {
            viewModel.deleteSession(session.sessionId)
            Toast.makeText(context, "Session deleted", Toast.LENGTH_SHORT).show()
          }
        )
      }
    }
  }
}

@Composable
fun HistorySessionCard(
  session: ChatSessionEntity,
  onClick: () -> Unit,
  onDelete: () -> Unit
) {
  val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()) }
  val dateStr = remember(session.updatedAt) { dateFormat.format(Date(session.updatedAt)) }

  GlassCard(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("session_card_${session.sessionId}"),
    cornerRadius = 14.dp,
    onClick = onClick
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = session.title,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = HologramCyanBright
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
          text = session.lastMessage,
          fontSize = 12.sp,
          color = HologramTextSecondary,
          maxLines = 2
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = dateStr,
          fontSize = 10.sp,
          fontFamily = FontFamily.Monospace,
          color = HologramCyan.copy(alpha = 0.7f)
        )
      }

      IconButton(
        onClick = onDelete,
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
      ) {
        Icon(
          imageVector = Icons.Default.Delete,
          contentDescription = "Delete Session",
          tint = HologramRed.copy(alpha = 0.8f),
          modifier = Modifier.size(18.dp)
        )
      }
    }
  }
}
