package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.HologramCardBg
import com.example.ui.theme.HologramCardBorder

@Composable
fun GlassCard(
  modifier: Modifier = Modifier,
  cornerRadius: Dp = 16.dp,
  borderColor: Color = HologramCardBorder,
  backgroundColor: Color = HologramCardBg,
  onClick: (() -> Unit)? = null,
  content: @Composable () -> Unit
) {
  val shape = RoundedCornerShape(cornerRadius)

  Card(
    modifier = modifier
      .clip(shape)
      .then(
        if (onClick != null) {
          Modifier.clickable(onClick = onClick)
        } else Modifier
      ),
    shape = shape,
    colors = CardDefaults.cardColors(
      containerColor = backgroundColor
    ),
    border = BorderStroke(1.dp, borderColor),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Box(modifier = Modifier.padding(16.dp)) {
      content()
    }
  }
}
