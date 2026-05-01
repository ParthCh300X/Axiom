package parth.appdev.axiom.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "axiom_prefs")

class PreferenceManager(private val context: Context) {

    companion object {
        val TOTAL_BUDGET        = doublePreferencesKey("total_budget")
        val LAST_RESET_MONTH    = intPreferencesKey("last_reset_month")
        val CARRY_FORWARD       = booleanPreferencesKey("carry_forward")
    }

    // ---------------------------
    // TOTAL BUDGET
    // ---------------------------

    val totalBudgetFlow: Flow<Double> =
        context.dataStore.data.map { it[TOTAL_BUDGET] ?: 0.0 }

    suspend fun setTotalBudget(amount: Double) {
        context.dataStore.edit { it[TOTAL_BUDGET] = amount }
    }

    suspend fun addToBudget(amount: Double) {
        context.dataStore.edit { prefs ->
            val current = prefs[TOTAL_BUDGET] ?: 0.0
            prefs[TOTAL_BUDGET] = current + amount
        }
    }

    // ---------------------------
    // LAST RESET MONTH
    // ---------------------------

    val lastResetMonthFlow: Flow<Int> =
        context.dataStore.data.map { it[LAST_RESET_MONTH] ?: -1 }

    suspend fun setLastResetMonth(month: Int) {
        context.dataStore.edit { it[LAST_RESET_MONTH] = month }
    }

    // ---------------------------
    // CARRY FORWARD
    // ---------------------------

    val carryForwardFlow: Flow<Boolean> =
        context.dataStore.data.map { it[CARRY_FORWARD] ?: false }

    suspend fun setCarryForward(enabled: Boolean) {
        context.dataStore.edit { it[CARRY_FORWARD] = enabled }
    }
}