package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.HologramCyan
import com.example.ui.theme.HologramCyanBright
import com.example.ui.theme.HologramPurple
import kotlin.math.cos
import kotlin.math.sin

enum class JarvisCoreState {
  IDLE,
  LISTENING,
  PROCESSING,
  SPEAKING
}

@Composable
fun HolographicCore(
  modifier: Modifier = Modifier,
  size: Dp = 200.dp,
  state: JarvisCoreState = JarvisCoreState.IDLE,
  audioLevel: Float = 0f,
  onClick: (() -> Unit)? = null
) {
  val infiniteTransition = rememberInfiniteTransition(label = "jarvis_core_transition")

  val rotationClockwise by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(
        durationMillis = when (state) {
          JarvisCoreState.PROCESSING -> 3000
          JarvisCoreState.LISTENING -> 5000
          JarvisCoreState.SPEAKING -> 4000
          else -> 12000
        },
        easing = LinearEasing
      ),
      repeatMode = RepeatMode.Restart
    ),
    label = "rotation_cw"
  )

  val rotationCounterClockwise by infiniteTransition.animateFloat(
    initialValue = 360f,
    targetValue = 0f,
    animationSpec = infiniteRepeatable(
      animation = tween(
        durationMillis = when (state) {
          JarvisCoreState.PROCESSING -> 2400
          JarvisCoreState.LISTENING -> 4200
          else -> 9000
        },
        easing = LinearEasing
      ),
      repeatMode = RepeatMode.Restart
    ),
    label = "rotation_ccw"
  )

  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.92f,
    targetValue = 1.08f,
    animationSpec = infiniteRepeatable(
      animation = tween(
        durationMillis = when (state) {
          JarvisCoreState.LISTENING -> 700
          JarvisCoreState.PROCESSING -> 500
          JarvisCoreState.SPEAKING -> 600
          else -> 2200
        },
        easing = LinearEasing
      ),
      repeatMode = RepeatMode.Reverse
    ),
    label = "core_pulse"
  )

  val primaryColor = MaterialTheme.colorScheme.primary
  val secondaryColor = MaterialTheme.colorScheme.secondary

  Box(
    modifier = modifier
      .size(size)
      .then(
        if (onClick != null) {
          Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(bounded = false, radius = size / 2),
            onClick = onClick
          )
        } else Modifier
      ),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.size(size)) {
      val center = Offset(this.size.width / 2f, this.size.height / 2f)
      val baseRadius = this.size.minDimension / 2f * 0.9f

      // 1. Ambient Glow Field
      val reactiveGlow = if (state == JarvisCoreState.LISTENING) {
        audioLevel.coerceIn(0.1f, 1f) * 0.6f
      } else if (state == JarvisCoreState.SPEAKING) 0.5f else 0.25f

      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            primaryColor.copy(alpha = reactiveGlow),
            primaryColor.copy(alpha = reactiveGlow * 0.4f),
            Color.Transparent
          ),
          center = center,
          radius = baseRadius * 1.1f * pulseScale
        ),
        radius = baseRadius * 1.1f * pulseScale,
        center = center
      )

      // 2. Outermost Segmented HUD Ring
      rotate(rotationClockwise, pivot = center) {
        drawArc(
          color = primaryColor.copy(alpha = 0.55f),
          startAngle = 0f,
          sweepAngle = 70f,
          useCenter = false,
          topLeft = Offset(center.x - baseRadius, center.y - baseRadius),
          size = Size(baseRadius * 2, baseRadius * 2),
          style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
        drawArc(
          color = primaryColor.copy(alpha = 0.55f),
          startAngle = 90f,
          sweepAngle = 60f,
          useCenter = false,
          topLeft = Offset(center.x - baseRadius, center.y - baseRadius),
          size = Size(baseRadius * 2, baseRadius * 2),
          style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
        drawArc(
          color = secondaryColor.copy(alpha = 0.7f),
          startAngle = 180f,
          sweepAngle = 75f,
          useCenter = false,
          topLeft = Offset(center.x - baseRadius, center.y - baseRadius),
          size = Size(baseRadius * 2, baseRadius * 2),
          style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
        drawArc(
          color = primaryColor.copy(alpha = 0.55f),
          startAngle = 270f,
          sweepAngle = 60f,
          useCenter = false,
          topLeft = Offset(center.x - baseRadius, center.y - baseRadius),
          size = Size(baseRadius * 2, baseRadius * 2),
          style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
      }

      // 3. Middle Concentric Tick Ring
      val middleRadius = baseRadius * 0.78f
      rotate(rotationCounterClockwise, pivot = center) {
        drawCircle(
          color = primaryColor.copy(alpha = 0.25f),
          radius = middleRadius,
          center = center,
          style = Stroke(
            width = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 18f), 0f)
          )
        )

        // Orbital Data Nodes
        val nodes = 6
        for (i in 0 until nodes) {
          val angle = Math.toRadians((i * (360.0 / nodes)))
          val nodeX = center.x + (middleRadius * cos(angle)).toFloat()
          val nodeY = center.y + (middleRadius * sin(angle)).toFloat()
          drawCircle(
            color = HologramCyanBright,
            radius = 3.5.dp.toPx(),
            center = Offset(nodeX, nodeY)
          )
        }
      }

      // 4. Inner Reactor Ring with Scanning Arcs
      val innerRadius = baseRadius * 0.55f * pulseScale
      rotate(rotationClockwise * 1.5f, pivot = center) {
        drawArc(
          color = primaryColor,
          startAngle = 45f,
          sweepAngle = 120f,
          useCenter = false,
          topLeft = Offset(center.x - innerRadius, center.y - innerRadius),
          size = Size(innerRadius * 2, innerRadius * 2),
          style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        )
        drawArc(
          color = HologramPurple,
          startAngle = 225f,
          sweepAngle = 100f,
          useCenter = false,
          topLeft = Offset(center.x - innerRadius, center.y - innerRadius),
          size = Size(innerRadius * 2, innerRadius * 2),
          style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        )
      }

      // 5. Center Arc Core
      val coreRadius = baseRadius * 0.32f * pulseScale
      drawCircle(
        brush = Brush.radialGradient(
          colors = listOf(
            Color.White,
            HologramCyanBright,
            primaryColor,
            Color.Transparent
          ),
          center = center,
          radius = coreRadius
        ),
        radius = coreRadius,
        center = center
      )

      // Center High-Tech Target Crosshair
      val crosshairLen = coreRadius * 0.6f
      drawLine(
        color = Color.White.copy(alpha = 0.85f),
        start = Offset(center.x - crosshairLen, center.y),
        end = Offset(center.x + crosshairLen, center.y),
        strokeWidth = 2.dp.toPx()
      )
      drawLine(
        color = Color.White.copy(alpha = 0.85f),
        start = Offset(center.x, center.y - crosshairLen),
        end = Offset(center.x, center.y + crosshairLen),
        strokeWidth = 2.dp.toPx()
      )
    }
  }
}
