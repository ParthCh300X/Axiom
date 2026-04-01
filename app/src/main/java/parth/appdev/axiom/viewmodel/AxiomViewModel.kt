package parth.appdev.axiom.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import parth.appdev.axiom.data.local.AppDatabase
import parth.appdev.axiom.data.local.PreferenceManager
import parth.appdev.axiom.data.local.entity.CategoryEntity
import parth.appdev.axiom.data.local.entity.TransactionEntity
import parth.appdev.axiom.data.repository.AxiomRepository
import java.util.Calendar

class AxiomViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AxiomRepository

    val categories: StateFlow<List<CategoryEntity>>

    private val prefs = PreferenceManager(application)

    val totalBudgetFlow = prefs.totalBudgetFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    init {
        val db = AppDatabase.getDatabase(application)
        repository = AxiomRepository(db.categoryDao(), db.transactionDao())

        categories = repository.getAllCategories()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    // ---------------------------
    // CATEGORY OPERATIONS
    // ---------------------------

    fun setTotalBudget(amount: Double) {
        viewModelScope.launch {
            prefs.setTotalBudget(amount)
        }
    }

    fun addCategory(name: String, type: String, budget: Double) {
        viewModelScope.launch {
            repository.insertCategory(
                CategoryEntity(
                    name = name,
                    type = type,
                    budget = budget
                )
            )
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    // ---------------------------
    // TRANSACTION LOGIC
    // ---------------------------

    fun addExpense(category: CategoryEntity, amount: Double, note: String) {

        if (category.isLocked) return

        viewModelScope.launch {

            val newSpent = category.spent + amount

            val updatedCategory = when (category.type) {

                "ONE_TIME" -> {
                    category.copy(
                        spent = newSpent,
                        isLocked = newSpent >= category.budget
                    )
                }

                "DAILY" -> {
                    category.copy(spent = newSpent)
                }

                "VARIABLE" -> {
                    category.copy(spent = newSpent)
                }

                else -> category
            }

            repository.updateCategory(updatedCategory)

            repository.insertTransaction(
                TransactionEntity(
                    categoryId = category.id,
                    amount = amount,
                    note = note,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    // ---------------------------
    // DAILY LOGIC (FIXED)
    // ---------------------------

    fun getDailyAllowance(category: CategoryEntity): Double {

        if (category.type != "DAILY") return 0.0

        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_MONTH)
        val totalDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val remainingDays = (totalDays - today + 1).coerceAtLeast(1)
        val remainingBudget = category.budget - category.spent

        return remainingBudget / remainingDays
    }

    fun getTodayRemaining(
        category: CategoryEntity,
        todaySpent: Double
    ): Double {

        val allowance = getDailyAllowance(category)
        return (allowance - todaySpent).coerceAtLeast(0.0)
    }

    // ---------------------------
    // TRANSACTION FLOW (IMPORTANT)
    // ---------------------------

    fun getTransactionsForCategory(categoryId: Int): Flow<List<TransactionEntity>> {
        return repository.getTransactions(categoryId)
    }

    fun getTodaySpent(transactions: List<TransactionEntity>): Double {

        val calendar = Calendar.getInstance()

        val startOfDay = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        return transactions
            .filter { it.timestamp >= startOfDay }
            .sumOf { it.amount }
    }

    // ---------------------------
    // GLOBAL CALCULATIONS
    // ---------------------------

    fun getAllocatedBudget(categories: List<CategoryEntity>): Double {
        return categories.sumOf { it.budget }
    }

    fun canAddCategory(
        categories: List<CategoryEntity>,
        newBudget: Double,
        totalBudget: Double
    ): Boolean {
        val allocated = getAllocatedBudget(categories)
        return (allocated + newBudget) <= totalBudget
    }

    fun getTotalSpent(categories: List<CategoryEntity>): Double {
        return categories.sumOf { it.spent }
    }
    fun checkAndResetMonthly() {

        viewModelScope.launch {

            val currentMonth = Calendar.getInstance().get(Calendar.MONTH)

            val lastReset = prefs.lastResetMonthFlow.first()

            if (lastReset != currentMonth) {

                val currentCategories = categories.value

                currentCategories.forEach { category ->
                    repository.updateCategory(
                        category.copy(
                            spent = 0.0,
                            isLocked = false
                        )
                    )
                }

                prefs.setLastResetMonth(currentMonth)
            }
        }
    }
    fun deleteTransaction(transaction: TransactionEntity) {

        viewModelScope.launch {

            // Get current category
            val category = categories.value.find { it.id == transaction.categoryId } ?: return@launch

            val newSpent = (category.spent - transaction.amount).coerceAtLeast(0.0)

            val updatedCategory = when (category.type) {

                "ONE_TIME" -> {
                    category.copy(
                        spent = newSpent,
                        isLocked = false // unlock if needed
                    )
                }

                else -> {
                    category.copy(
                        spent = newSpent
                    )
                }
            }

            repository.updateCategory(updatedCategory)
            repository.deleteTransaction(transaction)
        }
    }
    fun updateCategory(
        category: CategoryEntity,
        newName: String,
        newType: String,
        newBudget: Double,
        allCategories: List<CategoryEntity>,
        totalBudget: Double
    ) {

        viewModelScope.launch {

            val allocatedExcludingCurrent =
                allCategories
                    .filter { it.id != category.id }
                    .sumOf { it.budget }

            // Prevent over-allocation
            if (allocatedExcludingCurrent + newBudget > totalBudget) return@launch

            val updated = category.copy(
                name = newName,
                type = newType,
                budget = newBudget,
                isLocked = if (newType == "ONE_TIME")
                    category.spent >= newBudget
                else false
            )

            repository.updateCategory(updated)
        }
    }
}