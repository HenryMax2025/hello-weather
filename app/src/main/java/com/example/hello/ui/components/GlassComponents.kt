package com.example.hello.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(cornerRadius),
                spotColor = Color.White.copy(alpha = 0.25f),
                ambientColor = Color.White.copy(alpha = 0.1f)
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.25f),
                        Color.White.copy(alpha = 0.1f)
                    )
                )
            )
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.3f),
                        Color.Transparent
                    ),
                    radius = 600f
                )
            )
            .padding(cornerRadius),
        content = content
    )
}

@Composable
fun GlassCardSolid(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 15.dp,
                shape = RoundedCornerShape(cornerRadius),
                spotColor = Color.Black.copy(alpha = 0.1f),
                ambientColor = Color.White.copy(alpha = 0.05f)
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.2f),
                        Color.White.copy(alpha = 0.08f)
                    )
                )
            )
            .padding(cornerRadius - 4.dp),
        content = content
    )
}