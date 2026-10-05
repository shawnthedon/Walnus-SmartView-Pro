package com.example.smartview.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.smartview.ui.theme.ConstructionAmber
import com.example.smartview.ui.theme.PrecisionEmerald
import com.example.smartview.ui.theme.PrecisionSkyLight
import com.example.smartview.ui.theme.StatusRed

enum class BadgeStyle {
    READY,
    PENDING_TASK,
    ACTIVE,
    INFO,
    WARNING,
    ERROR,
    NEUTRAL
}

@Composable
fun TechnicalStatusBadge(
    text: String,
    style: BadgeStyle = BadgeStyle.INFO,
    modifier: Modifier = Modifier
) {
    val (dotColor, textColor, bgColor, borderColor) = when (style) {
        BadgeStyle.READY -> Quadruple(
            PrecisionEmerald,
            PrecisionEmerald,
            PrecisionEmerald.copy(alpha = 0.12f),
            PrecisionEmerald.copy(alpha = 0.35f)
        )
        BadgeStyle.PENDING_TASK, BadgeStyle.WARNING -> Quadruple(
            ConstructionAmber,
            ConstructionAmber,
            ConstructionAmber.copy(alpha = 0.12f),
            ConstructionAmber.copy(alpha = 0.35f)
        )
        BadgeStyle.ACTIVE -> Quadruple(
            PrecisionSkyLight,
            PrecisionSkyLight,
            PrecisionSkyLight.copy(alpha = 0.12f),
            PrecisionSkyLight.copy(alpha = 0.35f)
        )
        BadgeStyle.ERROR -> Quadruple(
            StatusRed,
            StatusRed,
            StatusRed.copy(alpha = 0.12f),
            StatusRed.copy(alpha = 0.35f)
        )
        BadgeStyle.INFO, BadgeStyle.NEUTRAL -> Quadruple(
            MaterialTheme.colorScheme.onSurfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    }

    Row(
        modifier = modifier
            .testTag("status_badge_${text.lowercase().replace(" ", "_")}")
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
