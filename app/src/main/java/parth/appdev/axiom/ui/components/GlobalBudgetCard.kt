package parth.appdev.axiom.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import parth.appdev.axiom.ui.theme.Amber
import parth.appdev.axiom.ui.theme.Crimson
import parth.appdev.axiom.ui.theme.DeepOrange

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GlobalBudgetCard(
    totalBudget: Double,
    spent: Double,
    insight: String = "",
    onEditBudget: (Double) -> Unit    // called when user long presses and sets new total
) {
    val remaining = totalBudget - spent
    val progress = if (totalBudget == 0.0) 0f else (spent / totalBudget).toFloat()

    var showEditDialog by remember { mutableStateOf(false) }
    var editAmount by remember { mutableStateOf(totalBudget.toInt().toString()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .combinedClickable(
                onClick = {},
                onLongClick = {
                    editAmount = totalBudget.toInt().toString()
                    showEditDialog = true
                }
            )
            .padding(20.dp)
    ) {

        Text(
            text = "Remaining",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "₹ ${remaining.toInt()}",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "of ₹ ${totalBudget.toInt()}",
            style = MaterialTheme.typography.bodySmall
        )

        // Insight line
        if (insight.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = insight,
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    insight.startsWith("Budget exhausted") -> Crimson
                    insight.startsWith("Spending fast") -> Amber
                    insight.startsWith("Great pace") -> DeepOrange
                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(DeepOrange, Amber, Crimson)
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Long press to edit total budget",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
    }

    // ---------------------------
    // EDIT TOTAL BUDGET DIALOG
    // ---------------------------
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            confirmButton = {
                Button(onClick = {
                    val amt = editAmount.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onEditBudget(amt)
                        showEditDialog = false
                    }
                }) {
                    Text("Set")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Edit Total Budget") },
            text = {
                Column {
                    Text(
                        text = "Current: ₹ ${totalBudget.toInt()}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = editAmount,
                        onValueChange = { editAmount = it },
                        label = { Text("New Total Budget") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }
}