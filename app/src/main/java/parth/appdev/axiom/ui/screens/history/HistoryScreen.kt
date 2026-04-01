package parth.appdev.axiom.ui.screens.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import parth.appdev.axiom.viewmodel.AxiomViewModel
import parth.appdev.axiom.utils.formatCurrency
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    categoryId: Int,
    categoryName: String,
    onBack: () -> Unit,
    viewModel: AxiomViewModel = viewModel()
) {

    val transactions by viewModel
        .getTransactionsForCategory(categoryId)
        .collectAsState(initial = emptyList())

    var selectedTxn by remember {
        mutableStateOf<parth.appdev.axiom.data.local.entity.TransactionEntity?>(null)
    }

    val sdf = remember { SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()) }

    // ---------------------------
    // GROUP BY DATE
    // ---------------------------
    val grouped = transactions.groupBy { txn ->
        sdf.format(Date(txn.timestamp))
    }

    val sortedKeys = grouped.keys.sortedByDescending {
        sdf.parse(it)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        // ---------------------------
        // HEADER (WITH BACK)
        // ---------------------------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            TextButton(onClick = onBack) {
                Text("← Back")
            }

            Text(
                text = categoryName,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            sortedKeys.forEach { date ->

                // Date Header
                item {
                    Text(
                        text = date,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Transactions
                items(grouped[date] ?: emptyList()) { txn ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {},
                                onLongClick = { selectedTxn = txn }
                            )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {

                            Text(
                                text = formatCurrency(txn.amount),
                                style = MaterialTheme.typography.bodyLarge
                            )

                            if (txn.note.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = txn.note,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ---------------------------
    // DELETE DIALOG
    // ---------------------------
    if (selectedTxn != null) {
        AlertDialog(
            onDismissRequest = { selectedTxn = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTransaction(selectedTxn!!)
                        selectedTxn = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedTxn = null }) {
                    Text("Cancel")
                }
            },
            title = { Text("Delete Transaction") },
            text = { Text("This cannot be undone") }
        )
    }
}