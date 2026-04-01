package parth.appdev.axiom.data.repository

import kotlinx.coroutines.flow.Flow
import parth.appdev.axiom.data.local.dao.CategoryDao
import parth.appdev.axiom.data.local.dao.TransactionDao
import parth.appdev.axiom.data.local.entity.CategoryEntity
import parth.appdev.axiom.data.local.entity.TransactionEntity

class AxiomRepository(
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao
) {

    // ---------------------------
    // CATEGORY
    // ---------------------------

    fun getAllCategories(): Flow<List<CategoryEntity>> =
        categoryDao.getAllCategories()

    suspend fun insertCategory(category: CategoryEntity) =
        categoryDao.insertCategory(category)

    suspend fun updateCategory(category: CategoryEntity) =
        categoryDao.updateCategory(category)

    suspend fun deleteCategory(category: CategoryEntity) =
        categoryDao.deleteCategory(category)

    // ---------------------------
    // TRANSACTIONS
    // ---------------------------

    suspend fun insertTransaction(transaction: TransactionEntity) =
        transactionDao.insertTransaction(transaction)

    fun getTransactions(categoryId: Int): Flow<List<TransactionEntity>> =
        transactionDao.getTransactionsForCategory(categoryId)
    suspend fun deleteTransaction(transaction: TransactionEntity) =
        transactionDao.deleteTransaction(transaction)

    // ✅ NEW: today's transactions only
    fun getTodayTransactions(
        categoryId: Int,
        startOfDay: Long
    ): Flow<List<TransactionEntity>> =
        transactionDao.getTodayTransactions(categoryId, startOfDay)
}