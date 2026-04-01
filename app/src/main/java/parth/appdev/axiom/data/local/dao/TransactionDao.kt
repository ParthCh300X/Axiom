package parth.appdev.axiom.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import parth.appdev.axiom.data.local.entity.TransactionEntity

@Dao
interface TransactionDao {

    @Query("""
        SELECT * FROM transactions 
        WHERE categoryId = :categoryId 
        ORDER BY timestamp DESC
    """)
    fun getTransactionsForCategory(categoryId: Int): Flow<List<TransactionEntity>>

    // ✅ NEW: Only today's transactions
    @Query("""
        SELECT * FROM transactions 
        WHERE categoryId = :categoryId 
        AND timestamp >= :startOfDay
        ORDER BY timestamp DESC
    """)
    fun getTodayTransactions(
        categoryId: Int,
        startOfDay: Long
    ): Flow<List<TransactionEntity>>

    @Insert
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)
}