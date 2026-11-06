package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.ui.components.GlassCard
import com.example.ui.components.HologramHeader
import com.example.ui.theme.HologramCardBg
import com.example.ui.theme.HologramCardBorder
import com.example.ui.theme.HologramCyan
import com.example.ui.theme.HologramCyanBright
import com.example.ui.theme.HologramEmerald
import com.example.ui.theme.HologramTextPrimary
import com.example.ui.theme.HologramTextSecondary
import com.example.ui.viewmodel.JarvisViewModel
import java.io.File

@Composable
fun VisionScreen(
  viewModel: JarvisViewModel,
  onNavigateBack: () -> Unit
) {
  val context = LocalContext.current
  val selectedUri by viewModel.selectedImageUri.collectAsState()
  val isAnalyzing by viewModel.isAnalyzingImage.collectAsState()
  val analysisResult by viewModel.imageAnalysisResult.collectAsState()
  val settings by viewModel.settings.collectAsState()

  var customPrompt by remember { mutableStateOf("Analyze this visual input and provide a holographic telemetry report.") }
  var showCameraPermissionDialog by remember { mutableStateOf(false) }
  var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

  // Gallery Photo Picker
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) {
      viewModel.setImageUri(uri)
    }
  }

  // Camera TakePicture launcher
  val takePictureLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicture()
  ) { success ->
    if (success && tempCameraUri != null) {
      viewModel.setImageUri(tempCameraUri)
    }
  }

  // Camera Permission launcher
  val cameraPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    if (isGranted) {
      val imageFile = File(context.cacheDir, "images/capture_${System.currentTimeMillis()}.jpg").apply {
        parentFile?.mkdirs()
      }
      val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
      )
      tempCameraUri = uri
      takePictureLauncher.launch(uri)
    } else {
      Toast.makeText(context, "Camera permission is needed to capture photos.", Toast.LENGTH_SHORT).show()
    }
  }

  if (showCameraPermissionDialog) {
    AlertDialog(
      onDismissRequest = { showCameraPermissionDialog = false },
      title = { Text("Camera Permission Needed") },
      text = {
        Text("JARVIS requires camera access to capture photographs for optical inspection and multimodal analysis.")
      },
      confirmButton = {
        TextButton(
          onClick = {
            showCameraPermissionDialog = false
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
          }
        ) {
          Text("Grant Permission", color = HologramCyan)
        }
      },
      dismissButton = {
        TextButton(onClick = { showCameraPermissionDialog = false }) {
          Text("Cancel", color = HologramTextSecondary)
        }
      },
      containerColor = HologramCardBg
    )
  }

  val promptPresets = listOf(
    "Analyze objects and context",
    "বাংলায় ছবিটির বিস্তারিত বর্ণনা দাও",
    "Identify text and inscriptions",
    "Detect colors and atmosphere",
    "Check for anomalies or hazards"
  )

  // Animated laser scan effect
  val transition = rememberInfiniteTransition(label = "laser_transition")
  val laserProgress by transition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 2000, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "laser_anim"
  )

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
  ) {
    HologramHeader(
      title = "IMAGE ANALYSIS",
      subtitle = "OPTICAL TELEMETRY & VISION AI",
      showBackButton = true,
      onBackClick = onNavigateBack
    )

    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Image Preview & Scanning Viewport
      GlassCard(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("vision_preview_card"),
        cornerRadius = 20.dp
      ) {
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          if (selectedUri != null) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, HologramCyan, RoundedCornerShape(12.dp))
            ) {
              AsyncImage(
                model = selectedUri,
                contentDescription = "Selected Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
              )

              // Holographic Laser Scanner Line
              if (isAnalyzing) {
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .offset(y = (240.dp * laserProgress))
                    .background(
                      Brush.horizontalGradient(
                        listOf(
                          Color.Transparent,
                          HologramCyanBright,
                          Color.White,
                          HologramCyanBright,
                          Color.Transparent
                        )
                      )
                    )
                )
              }

              // Clear button
              IconButton(
                onClick = { viewModel.setImageUri(null) },
                modifier = Modifier
                  .align(Alignment.TopEnd)
                  .padding(8.dp)
                  .size(32.dp)
                  .clip(CircleShape)
                  .background(Color(0x99000000))
              ) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Clear",
                  tint = Color.White,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          } else {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .border(1.dp, HologramCardBorder, RoundedCornerShape(12.dp))
                .padding(16.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = null,
                tint = HologramCyan,
                modifier = Modifier.size(48.dp)
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "NO OPTICAL INPUT LOADED",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = HologramCyan,
                letterSpacing = 1.sp
              )
              Text(
                text = "Select from gallery or capture with camera",
                fontSize = 11.sp,
                color = HologramTextSecondary
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Action Buttons: Gallery & Camera
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Button(
              onClick = {
                photoPickerLauncher.launch(
                  PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
              },
              modifier = Modifier
                .weight(1f)
                .testTag("pick_gallery_button"),
              colors = ButtonDefaults.buttonColors(
                containerColor = HologramCardBg,
                contentColor = HologramCyan
              ),
              border = androidx.compose.foundation.BorderStroke(1.dp, HologramCyan),
              shape = RoundedCornerShape(14.dp)
            ) {
              Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Gallery", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Button(
              onClick = {
                val imageFile = File(context.cacheDir, "images/capture_${System.currentTimeMillis()}.jpg").apply {
                  parentFile?.mkdirs()
                }
                val uri = FileProvider.getUriForFile(
                  context,
                  "${context.packageName}.fileprovider",
                  imageFile
                )
                tempCameraUri = uri
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
              },
              modifier = Modifier
                .weight(1f)
                .testTag("open_camera_button"),
              colors = ButtonDefaults.buttonColors(
                containerColor = HologramCyan,
                contentColor = MaterialTheme.colorScheme.background
              ),
              shape = RoundedCornerShape(14.dp)
            ) {
              Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Camera", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      // 2. Vision Prompt Selection
      Column {
        Text(
          text = "ANALYSIS DIRECTIVES",
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          color = HologramTextPrimary,
          letterSpacing = 1.sp,
          modifier = Modifier.padding(bottom = 6.dp)
        )

        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(promptPresets) { preset ->
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x3300F2FE))
                .border(1.dp, HologramCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .clickable { customPrompt = preset }
                .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Text(text = preset, fontSize = 11.sp, color = HologramCyanBright)
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = customPrompt,
          onValueChange = { customPrompt = it },
          label = { Text("Vision Query Prompt", fontSize = 12.sp) },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("vision_prompt_input"),
          shape = RoundedCornerShape(16.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = HologramCyan,
            unfocusedBorderColor = HologramCardBorder,
            focusedTextColor = HologramTextPrimary,
            unfocusedTextColor = HologramTextPrimary,
            focusedContainerColor = HologramCardBg,
            unfocusedContainerColor = HologramCardBg
          ),
          maxLines = 3
        )
      }

      // 3. Initiate Scan Button
      Button(
        onClick = { viewModel.analyzeImage(customPrompt) },
        enabled = selectedUri != null && !isAnalyzing,
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("start_analysis_button"),
        colors = ButtonDefaults.buttonColors(
          containerColor = HologramCyan,
          contentColor = MaterialTheme.colorScheme.background
        ),
        shape = RoundedCornerShape(16.dp)
      ) {
        if (isAnalyzing) {
          CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.background, strokeWidth = 2.dp)
          Spacer(modifier = Modifier.width(8.dp))
          Text("PROCESSING MULTIMODAL TELEMETRY...", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
        } else {
          Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("INITIATE HOLOGRAPHIC SCAN", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
      }

      // 4. Analysis Report Card
      if (analysisResult != null) {
        GlassCard(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("vision_result_card"),
          borderColor = HologramEmerald
        ) {
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "OPTICAL SPECTRAL REPORT",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = HologramEmerald,
                letterSpacing = 1.sp
              )

              Row {
                IconButton(
                  onClick = {
                    viewModel.ttsManager.speak(analysisResult ?: "", settings.ttsPitch, settings.ttsSpeed)
                  },
                  modifier = Modifier.size(32.dp)
                ) {
                  Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "Read Report", tint = HologramCyan, modifier = Modifier.size(18.dp))
                }

                IconButton(
                  onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("JARVIS Vision Report", analysisResult))
                    Toast.makeText(context, "Report copied to clipboard", Toast.LENGTH_SHORT).show()
                  },
                  modifier = Modifier.size(32.dp)
                ) {
                  Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy Report", tint = HologramTextSecondary, modifier = Modifier.size(16.dp))
                }
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = analysisResult ?: "",
              fontSize = 13.sp,
              color = HologramTextPrimary,
              lineHeight = 20.sp
            )
          }
        }
      }
    }
  }
}
