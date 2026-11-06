package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MemoryEntity
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
fun MemoryScreen(
  viewModel: JarvisViewModel,
  onNavigateBack: () -> Unit
) {
  val context = LocalContext.current
  val memories by viewModel.allMemories.collectAsState()
  val settings by viewModel.settings.collectAsState()

  var selectedCategory by remember { mutableStateOf("All") }
  var showAddDialog by remember { mutableStateOf(false) }
  var memoryToEdit by remember { mutableStateOf<MemoryEntity?>(null) }

  val categories = listOf("All", "Profile", "Preference", "Fact", "Instruction")

  val filteredMemories = remember(memories, selectedCategory) {
    if (selectedCategory == "All") memories
    else memories.filter { it.category.equals(selectedCategory, ignoreCase = true) }
  }

  // Add / Edit Dialog
  if (showAddDialog || memoryToEdit != null) {
    var dialogCategory by remember { mutableStateOf(memoryToEdit?.category ?: "Preference") }
    var dialogKey by remember { mutableStateOf(memoryToEdit?.key ?: "") }
    var dialogValue by remember { mutableStateOf(memoryToEdit?.value ?: "") }

    AlertDialog(
      onDismissRequest = {
        showAddDialog = false
        memoryToEdit = null
      },
      title = {
        Text(if (memoryToEdit != null) "Edit Memory Record" else "Add Neural Memory")
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = dialogCategory,
            onValueChange = { dialogCategory = it },
            label = { Text("Category (e.g. Profile, Preference, Fact)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )
          OutlinedTextField(
            value = dialogKey,
            onValueChange = { dialogKey = it },
            label = { Text("Memory Key (e.g. User Name, Language)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )
          OutlinedTextField(
            value = dialogValue,
            onValueChange = { dialogValue = it },
            label = { Text("Value / Content") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )
        }
      },
      confirmButton = {
        TextButton(
          onClick = {
            if (dialogKey.isNotBlank() && dialogValue.isNotBlank()) {
              if (memoryToEdit != null) {
                viewModel.updateMemory(
                  memoryToEdit!!.copy(
                    category = dialogCategory,
                    key = dialogKey,
                    value = dialogValue,
                    updatedAt = System.currentTimeMillis()
                  )
                )
                Toast.makeText(context, "Memory updated", Toast.LENGTH_SHORT).show()
              } else {
                viewModel.addMemory(dialogCategory, dialogKey, dialogValue)
                Toast.makeText(context, "Memory recorded", Toast.LENGTH_SHORT).show()
              }
              showAddDialog = false
              memoryToEdit = null
            }
          }
        ) {
          Text(if (memoryToEdit != null) "Update" else "Save", color = HologramCyan)
        }
      },
      dismissButton = {
        TextButton(
          onClick = {
            showAddDialog = false
            memoryToEdit = null
          }
        ) {
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
      title = "MEMORY MANAGER",
      subtitle = "OPT-IN CONTEXTUAL STORAGE",
      showBackButton = true,
      onBackClick = onNavigateBack,
      actions = {
        IconButton(
          onClick = { showAddDialog = true },
          modifier = Modifier.testTag("add_memory_button")
        ) {
          Icon(imageVector = Icons.Default.Add, contentDescription = "Add Memory", tint = HologramCyan)
        }
      }
    )

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
      contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
      // 1. Opt-in Control & Privacy Card
      item {
        GlassCard(
          modifier = Modifier.fillMaxWidth(),
          borderColor = if (settings.isMemoryOptIn) HologramEmerald else HologramCardBorder
        ) {
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "AUTONOMOUS MEMORY RETENTION",
                  fontSize = 12.sp,
                  fontFamily = FontFamily.Monospace,
                  fontWeight = FontWeight.Bold,
                  color = HologramTextPrimary
                )
                Text(
                  text = if (settings.isMemoryOptIn) "ACTIVE // JARVIS uses local memory for context" else "DISABLED // No memory accessed",
                  fontSize = 11.sp,
                  color = if (settings.isMemoryOptIn) HologramEmerald else HologramTextSecondary
                )
              }

              Switch(
                checked = settings.isMemoryOptIn,
                onCheckedChange = { viewModel.toggleMemoryOptIn(it) },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = HologramEmerald,
                  checkedTrackColor = HologramEmerald.copy(alpha = 0.3f),
                  uncheckedThumbColor = HologramTextSecondary,
                  uncheckedTrackColor = HologramCardBg
                ),
                modifier = Modifier.testTag("memory_opt_in_switch")
              )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = HologramCyan, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Stored exclusively on this device. Never uploaded without consent.",
                fontSize = 10.sp,
                color = HologramTextSecondary
              )
            }
          }
        }
      }

      // 2. Category Filter Row
      item {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          items(categories) { cat ->
            val isSelected = selectedCategory == cat
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) HologramCyan.copy(alpha = 0.25f) else Color(0x33101B30))
                .border(1.dp, if (isSelected) HologramCyan else HologramCardBorder, RoundedCornerShape(12.dp))
                .clickable { selectedCategory = cat }
                .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Text(
                text = cat,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) HologramCyanBright else HologramTextSecondary
              )
            }
          }
        }
      }

      // 3. Memory Items List
      if (filteredMemories.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 40.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = null,
                tint = HologramPurple.copy(alpha = 0.5f),
                modifier = Modifier.size(44.dp)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "NO MEMORY RECORDS FOUND",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = HologramPurple,
                letterSpacing = 1.sp
              )
              Text(
                text = "Tap + at top right to add user facts or instructions",
                fontSize = 11.sp,
                color = HologramTextSecondary
              )
            }
          }
        }
      }

      items(filteredMemories, key = { it.id }) { memory ->
        GlassCard(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("memory_item_${memory.id}"),
          borderColor = if (memory.isEnabled) HologramPurple.copy(alpha = 0.6f) else HologramCardBorder
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(HologramPurple.copy(alpha = 0.2f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = memory.category.uppercase(),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = HologramPurple
                  )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = memory.key,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  color = HologramTextPrimary
                )
              }

              Spacer(modifier = Modifier.height(4.dp))

              Text(
                text = memory.value,
                fontSize = 12.sp,
                color = HologramTextSecondary
              )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              Switch(
                checked = memory.isEnabled,
                onCheckedChange = { isChecked ->
                  viewModel.updateMemory(memory.copy(isEnabled = isChecked))
                },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = HologramPurple,
                  checkedTrackColor = HologramPurple.copy(alpha = 0.3f)
                ),
                modifier = Modifier.size(36.dp)
              )

              IconButton(
                onClick = { memoryToEdit = memory },
                modifier = Modifier.size(32.dp)
              ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = HologramCyan, modifier = Modifier.size(16.dp))
              }

              IconButton(
                onClick = {
                  viewModel.deleteMemory(memory.id)
                  Toast.makeText(context, "Memory deleted", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(32.dp)
              ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = HologramRed.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
              }
            }
          }
        }
      }
    }
  }
}
