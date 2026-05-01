package parth.appdev.axiom.viewmodel

import android.app.Application
import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import parth.appdev.axiom.data.local.AppDatabase
import parth.appdev.axiom.data.local.PreferenceManager
import parth.appdev.axiom.data.local.entity.CategoryEntity
import parth.appdev.axiom.data.local.entity.TransactionEntity
import parth.appdev.axiom.data.repository.AxiomRepository
import java.text.SimpleDateFormat
import java.util.*

class AxiomViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AxiomRepository
    private val prefs = PreferenceManager(application)

    val categories: StateFlow<List<CategoryEntity>>

    val totalBudgetFlow = prefs.totalBudgetFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val carryForwardFlow = prefs.carryForwardFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // ---------------------------
    // RESET DIALOG TRIGGER
    // ---------------------------
    // Emits true when a new month is detected and user needs to confirm reset
    private val _showResetDialog = MutableStateFlow(false)
    val showResetDialog: StateFlow<Boolean> = _showResetDialog

    private var pendingResetMonth: Int = -1

    init {
        val db = AppDatabase.getDatabase(application)
        repository = AxiomRepository(db.categoryDao(), db.transactionDao())

        categories = repository.getAllCategories()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    // ---------------------------
    // BUDGET OPERATIONS
    // ---------------------------

    fun setTotalBudget(amount: Double) {
        viewModelScope.launch { prefs.setTotalBudget(amount) }
    }

    fun addFunds(amount: Double) {
        viewModelScope.launch { prefs.addToBudget(amount) }
    }

    fun setCarryForward(enabled: Boolean) {
        viewModelScope.launch { prefs.setCarryForward(enabled) }
    }

    // ---------------------------
    // CATEGORY OPERATIONS
    // ---------------------------

    fun addCategory(name: String, type: String, budget: Double) {
        viewModelScope.launch {
            repository.insertCategory(CategoryEntity(name = name, type = type, budget = budget))
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch { repository.deleteCategory(category) }
    }

    fun togglePinCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.updatePinStatus(category.copy(isPinned = !category.isPinned))
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
            val allocatedExcludingCurrent = allCategories
                .filter { it.id != category.id }
                .sumOf { it.budget }

            if (allocatedExcludingCurrent + newBudget > totalBudget) return@launch

            repository.updateCategory(
                category.copy(
                    name = newName,
                    type = newType,
                    budget = newBudget,
                    isLocked = if (newType == "ONE_TIME") category.spent >= newBudget else false
                )
            )
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
                "ONE_TIME" -> category.copy(spent = newSpent, isLocked = newSpent >= category.budget)
                else -> category.copy(spent = newSpent)
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

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            val category = categories.value.find { it.id == transaction.categoryId } ?: return@launch
            val newSpent = (category.spent - transaction.amount).coerceAtLeast(0.0)
            val updatedCategory = when (category.type) {
                "ONE_TIME" -> category.copy(spent = newSpent, isLocked = false)
                else -> category.copy(spent = newSpent)
            }
            repository.updateCategory(updatedCategory)
            repository.deleteTransaction(transaction)
        }
    }

    // ---------------------------
    // DAILY LOGIC
    // ---------------------------

    fun getDailyAllowance(category: CategoryEntity): Double {
        if (category.type != "DAILY") return 0.0
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_MONTH)
        val totalDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val remainingDays = (totalDays - today + 1).coerceAtLeast(1)
        return (category.budget - category.spent) / remainingDays
    }

    fun getTodayRemaining(category: CategoryEntity, todaySpent: Double): Double {
        return (getDailyAllowance(category) - todaySpent).coerceAtLeast(0.0)
    }

    fun getTransactionsForCategory(categoryId: Int): Flow<List<TransactionEntity>> =
        repository.getTransactions(categoryId)

    fun getTodaySpent(transactions: List<TransactionEntity>): Double {
        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return transactions.filter { it.timestamp >= startOfDay }.sumOf { it.amount }
    }

    // ---------------------------
    // GLOBAL CALCULATIONS
    // ---------------------------

    fun getAllocatedBudget(categories: List<CategoryEntity>) = categories.sumOf { it.budget }

    fun canAddCategory(categories: List<CategoryEntity>, newBudget: Double, totalBudget: Double): Boolean {
        return (getAllocatedBudget(categories) + newBudget) <= totalBudget
    }

    fun getTotalSpent(categories: List<CategoryEntity>) = categories.sumOf { it.spent }

    // ---------------------------
    // SPENDING INSIGHT
    // ---------------------------

    fun getSpendingInsight(
        totalBudget: Double,
        totalSpent: Double
    ): String {
        if (totalBudget == 0.0) return ""

        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_MONTH)
        val totalDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val daysLeft = (totalDays - today + 1).coerceAtLeast(1)
        val remaining = totalBudget - totalSpent
        val spentPercent = ((totalSpent / totalBudget) * 100).toInt()
        val dayPercent = ((today.toDouble() / totalDays) * 100).toInt()

        return when {
            totalSpent == 0.0 -> "No spending yet this month"
            remaining <= 0 -> "Budget exhausted — ₹${(-remaining).toInt()} over limit"
            spentPercent > dayPercent + 20 ->
                "Spending fast · ₹${remaining.toInt()} left for $daysLeft days"
            spentPercent < dayPercent - 20 ->
                "Great pace · ₹${remaining.toInt()} left for $daysLeft days"
            else ->
                "On track · ₹${remaining.toInt()} left for $daysLeft days"
        }
    }

    // ---------------------------
    // MONTHLY RESET
    // ---------------------------

    fun checkAndResetMonthly() {
        viewModelScope.launch {
            val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
            val lastReset = prefs.lastResetMonthFlow.first()

            if (lastReset != currentMonth) {
                pendingResetMonth = currentMonth
                _showResetDialog.value = true
            }
        }
    }

    fun confirmReset(carryForward: Boolean) {
        viewModelScope.launch {
            val currentCategories = categories.value

            currentCategories.forEach { category ->
                val newBudget = if (carryForward) {
                    // Add unspent amount to next month's budget
                    val unspent = (category.budget - category.spent).coerceAtLeast(0.0)
                    category.budget + unspent
                } else {
                    category.budget
                }

                repository.updateCategory(
                    category.copy(
                        spent = 0.0,
                        isLocked = false,
                        budget = newBudget
                    )
                )
            }

            prefs.setLastResetMonth(pendingResetMonth)
            _showResetDialog.value = false
        }
    }

    fun dismissReset() {
        // User dismissed — don't reset, don't update month so it asks again next launch
        _showResetDialog.value = false
    }

    // ---------------------------
    // EXPORT CSV
    // ---------------------------

    fun exportToCsv(onResult: (success: Boolean, path: String) -> Unit) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val fileName = "axiom_export_${System.currentTimeMillis()}.csv"

                val sb = StringBuilder()

                // --- Section 1: Category Summary ---
                sb.appendLine("=== CATEGORY SUMMARY ===")
                sb.appendLine("Name,Type,Budget,Spent,Remaining,%Used")

                val currentCategories = categories.value
                currentCategories.forEach { cat ->
                    val remaining = cat.budget - cat.spent
                    val percent = if (cat.budget > 0) (cat.spent / cat.budget * 100).toInt() else 0
                    sb.appendLine("${cat.name},${cat.type},${cat.budget},${cat.spent},$remaining,$percent%")
                }

                sb.appendLine()

                // --- Section 2: All Transactions ---
                sb.appendLine("=== ALL TRANSACTIONS ===")
                sb.appendLine("Date,Time,Category,Amount,Note")

                currentCategories.forEach { cat ->
                    val txns = repository.getTransactions(cat.id).first()
                    txns.forEach { txn ->
                        val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(Date(txn.timestamp))
                        val time = SimpleDateFormat("HH:mm", Locale.getDefault())
                            .format(Date(txn.timestamp))
                        val note = txn.note.replace(",", ";") // avoid CSV breaking
                        sb.appendLine("$date,$time,${cat.name},${txn.amount},$note")
                    }
                }

                // Write to Downloads using MediaStore (API 29+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                        put(MediaStore.Downloads.MIME_TYPE, "text/csv")
                        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }
                    val uri = context.contentResolver.insert(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                        contentValues
                    )
                    uri?.let {
                        context.contentResolver.openOutputStream(it)?.use { stream ->
                            stream.write(sb.toString().toByteArray())
                        }
                        onResult(true, "Downloads/$fileName")
                    } ?: onResult(false, "")
                } else {
                    // Fallback for API < 29
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS
                    )
                    downloadsDir.mkdirs()
                    val file = java.io.File(downloadsDir, fileName)
                    file.writeText(sb.toString())
                    onResult(true, file.absolutePath)
                }

            } catch (e: Exception) {
                onResult(false, "")
            }
        }
    }
}