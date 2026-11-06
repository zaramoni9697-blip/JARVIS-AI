package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.HologramCyan
import com.example.ui.theme.HologramPurple
import kotlin.math.sin

@Composable
fun AudioWaveformVisualizer(
  modifier: Modifier = Modifier,
  isActive: Boolean = true,
  audioLevel: Float = 0.5f,
  barCount: Int = 16,
  maxHeight: Dp = 48.dp
) {
  val transition = rememberInfiniteTransition(label = "waveform_anim")
  val wavePhase by transition.animateFloat(
    initialValue = 0f,
    targetValue = 6.28318f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "wave_phase"
  )

  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(maxHeight),
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically
  ) {
    val primaryColor = MaterialTheme.colorScheme.primary

    for (i in 0 until barCount) {
      val dynamicFactor = if (isActive) {
        val sineWave = sin(wavePhase + (i * 0.45f)) * 0.5f + 0.5f
        (audioLevel * 0.7f + sineWave * 0.3f).coerceIn(0.12f, 1f)
      } else {
        0.08f
      }

      val currentHeight = maxHeight * dynamicFactor

      Box(
        modifier = Modifier
          .padding(horizontal = 2.dp)
          .width(4.dp)
          .height(currentHeight)
          .clip(RoundedCornerShape(2.dp))
          .background(
            brush = Brush.verticalGradient(
              colors = listOf(
                HologramPurple,
                primaryColor,
                HologramCyan
              )
            )
          )
      )
    }
  }
}
