package parth.appdev.axiom.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import parth.appdev.axiom.ui.theme.Amber
import parth.appdev.axiom.ui.theme.Crimson
import parth.appdev.axiom.ui.theme.DeepOrange
import parth.appdev.axiom.utils.formatCurrency

@Composable
fun CategoryCard(
    name: String,
    budget: Double,
    spent: Double,
    isLocked: Boolean,
    dailyLimit: Double? = null,   // todayRemaining
    todaySpent: Double = 0.0
) {

    val remaining = budget - spent
    val progress = if (budget == 0.0) 0f else (spent / budget).toFloat()

    val animatedProgress by animateFloatAsState(targetValue = progress, label = "")

    val dangerColor = when {
        progress < 0.6f -> DeepOrange
        progress < 0.85f -> Amber
        else -> Crimson
    }

    // ---------------------------
    // Daily Intelligence
    // ---------------------------
    val todayRemaining = dailyLimit ?: 0.0

    val stateText = when {
        dailyLimit == null -> ""
        todayRemaining > todaySpent * 0.8 -> "On track"
        todayRemaining > 0 -> "Slow down"
        else -> "Overspent"
    }

    val stateColor = when {
        dailyLimit == null -> MaterialTheme.colorScheme.onSurface
        todayRemaining > todaySpent * 0.8 -> DeepOrange
        todayRemaining > 0 -> Amber
        else -> Crimson
    }

    // ---------------------------
    // Subtle Pulse (only when overspent)
    // ---------------------------
    val infiniteTransition = rememberInfiniteTransition(label = "")

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.0f,
        targetValue = if (todayRemaining <= 0 && dailyLimit != null) 0.15f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    val surfaceColor by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.surface.copy(alpha = 1f - pulseAlpha),
        label = ""
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(surfaceColor)
            .padding(16.dp)
    ) {

        // Header
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(name, style = MaterialTheme.typography.titleMedium)

            if (isLocked) {
                Icon(Icons.Default.Lock, contentDescription = "Locked")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Monthly remaining
        Text(
            text = "${formatCurrency(remaining)} left",
            style = MaterialTheme.typography.bodyLarge
        )

        // ---------------------------
        // DAILY SECTION (ELITE)
        // ---------------------------
        if (dailyLimit != null) {

            Spacer(modifier = Modifier.height(10.dp))

            Column {

                // Main number
                Text(
                    text = "Today left: ${formatCurrency(todayRemaining)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (todayRemaining <= 0) Crimson else MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Context line
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text(
                        text = "Spent: ${formatCurrency(todaySpent)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Text(
                        text = stateText,
                        style = MaterialTheme.typography.bodySmall,
                        color = stateColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(dangerColor, dangerColor)
                        )
                    )
            )
        }
    }
}