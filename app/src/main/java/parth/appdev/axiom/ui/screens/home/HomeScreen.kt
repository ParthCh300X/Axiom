package parth.appdev.axiom.ui.screens.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import parth.appdev.axiom.data.local.entity.CategoryEntity
import parth.appdev.axiom.ui.components.*
import parth.appdev.axiom.ui.screens.history.HistoryScreen
import parth.appdev.axiom.utils.formatCurrency
import parth.appdev.axiom.viewmodel.AxiomViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: AxiomViewModel = viewModel()
) {

    val categories by viewModel.categories.collectAsState()
    val totalBudget by viewModel.totalBudgetFlow.collectAsState()

    val allocated = viewModel.getAllocatedBudget(categories)
    val remainingToAllocate = totalBudget - allocated
    val totalSpent = viewModel.getTotalSpent(categories)

    var selectedCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var showSheet by remember { mutableStateOf(false) }
    var showAddCategory by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var showEdit by remember { mutableStateOf(false) }

    // ---------------------------
    // HISTORY SCREEN (WITH BACK)
    // ---------------------------
    if (showHistory && selectedCategory != null) {
        HistoryScreen(
            categoryId = selectedCategory!!.id,
            categoryName = selectedCategory!!.name,
            onBack = {
                showHistory = false
                selectedCategory = null
            }
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {

            Text("AXIOM", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text("This Month", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(20.dp))

            GlobalBudgetCard(
                totalBudget = totalBudget,
                spent = totalSpent
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Unallocated: ${formatCurrency(remainingToAllocate)}",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(categories) { category ->

                    val todayTransactions by viewModel
                        .getTransactionsForCategory(category.id)
                        .collectAsState(initial = emptyList())

                    val todaySpent = viewModel.getTodaySpent(todayTransactions)

                    val dailyAllowance =
                        if (category.type == "DAILY")
                            viewModel.getDailyAllowance(category)
                        else null

                    val todayRemaining =
                        if (dailyAllowance != null)
                            viewModel.getTodayRemaining(category, todaySpent)
                        else null

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
                                    if (!category.isLocked) {
                                        selectedCategory = category
                                        showSheet = true
                                    }
                                },
                                onLongClick = {
                                    selectedCategory = category
                                    showMenu = true
                                }
                            )
                    ) {
                        CategoryCard(
                            name = category.name,
                            budget = category.budget,
                            spent = category.spent,
                            isLocked = category.isLocked,
                            dailyLimit = todayRemaining,
                            todaySpent = todaySpent
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddCategory = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Category")
        }
    }

    // ---------------------------
    // ADD EXPENSE
    // ---------------------------
    if (showSheet && selectedCategory != null) {
        AddExpenseBottomSheet(
            onDismiss = {
                showSheet = false
                selectedCategory = null
            },
            onAdd = { amount, note ->
                viewModel.addExpense(
                    category = selectedCategory!!,
                    amount = amount,
                    note = note
                )
            }
        )
    }

    // ---------------------------
    // ADD CATEGORY
    // ---------------------------
    if (showAddCategory) {
        AddCategoryDialog(
            remainingBudget = remainingToAllocate,
            onDismiss = { showAddCategory = false },
            onAdd = { name, type, budget ->
                if (viewModel.canAddCategory(categories, budget, totalBudget)) {
                    viewModel.addCategory(name, type, budget)
                }
            }
        )
    }

    // ---------------------------
    // EDIT CATEGORY
    // ---------------------------
    if (showEdit && selectedCategory != null) {

        val allocated = viewModel.getAllocatedBudget(categories)
        val remaining =
            totalBudget - (allocated - selectedCategory!!.budget)

        EditCategoryDialog(
            category = selectedCategory!!,
            remainingBudget = remaining,
            onDismiss = { showEdit = false },
            onUpdate = { name, type, budget ->
                viewModel.updateCategory(
                    category = selectedCategory!!,
                    newName = name,
                    newType = type,
                    newBudget = budget,
                    allCategories = categories,
                    totalBudget = totalBudget
                )
            }
        )
    }

    // ---------------------------
    // LONG PRESS MENU
    // ---------------------------
    if (showMenu && selectedCategory != null) {
        AlertDialog(
            onDismissRequest = { showMenu = false },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showMenu = false }) {
                    Text("Close")
                }
            },
            title = { Text("Category Options") },
            text = {
                Column {

                    TextButton(
                        onClick = {
                            showMenu = false
                            showHistory = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View History")
                    }

                    TextButton(
                        onClick = {
                            showMenu = false
                            showEdit = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Edit")
                    }

                    TextButton(
                        onClick = {
                            viewModel.deleteCategory(selectedCategory!!)
                            showMenu = false
                            selectedCategory = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        )
    }
}