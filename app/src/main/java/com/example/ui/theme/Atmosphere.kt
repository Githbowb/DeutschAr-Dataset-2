package com.example.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Atmospheric background and gradient helpers for the premium DeutschAr dark theme.
 */
fun Modifier.ambientAtmosphere(): Modifier = this.drawBehind {
    // Base dark canvas
    drawRect(color = DarkBg)

    // Top-right subtle emerald aura
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                PrimaryAccent.copy(alpha = 0.08f),
                PrimaryAccent.copy(alpha = 0.02f),
                Color.Transparent
            ),
            center = Offset(size.width * 0.9f, size.height * 0.05f),
            radius = size.width * 0.7f
        )
    )

    // Mid-left subtle sapphire blue aura
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                SecondaryAccent.copy(alpha = 0.07f),
                SecondaryAccent.copy(alpha = 0.015f),
                Color.Transparent
            ),
            center = Offset(size.width * 0.1f, size.height * 0.45f),
            radius = size.width * 0.65f
        )
    )

    // Bottom-center soft glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                PrimaryAccent.copy(alpha = 0.05f),
                Color.Transparent
            ),
            center = Offset(size.width * 0.5f, size.height * 0.9f),
            radius = size.width * 0.5f
        )
    )
}

object Atmosphere {

    // Dynamic linear gradients for cards and accents
    val primaryGlowGradient = Brush.linearGradient(
        colors = listOf(PrimaryAccent, Color(0xFF00BFA5))
    )

    val sapphireGlowGradient = Brush.linearGradient(
        colors = listOf(SecondaryAccent, Color(0xFF1D4ED8))
    )

    val derCardGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF1E3A8A), Color(0xFF2563EB), Color(0xFF3B82F6))
    )

    val dieCardGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF831843), Color(0xFFDC2626), Color(0xFFF43F5E))
    )

    val dasCardGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF064E3B), Color(0xFF059669), Color(0xFF10B981))
    )

    val glassBorder = Brush.linearGradient(
        colors = listOf(
            PrimaryAccent.copy(alpha = 0.35f),
            SecondaryAccent.copy(alpha = 0.2f),
            DarkCardBorder
        )
    )

    val subtleBorder = Brush.linearGradient(
        colors = listOf(
            Color(0x33FFFFFF),
            DarkCardBorder,
            Color(0x11FFFFFF)
        )
    )

    val heroGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF162032),
            Color(0xFF111827),
            Color(0xFF0F172A)
        )
    )
}
