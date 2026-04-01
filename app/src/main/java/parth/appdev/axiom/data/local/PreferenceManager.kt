package parth.appdev.axiom.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "axiom_prefs")

class PreferenceManager(private val context: Context) {

    companion object {
        val TOTAL_BUDGET = doublePreferencesKey("total_budget")
    }

    val totalBudgetFlow: Flow<Double> =
        context.dataStore.data.map { prefs ->
            prefs[TOTAL_BUDGET] ?: 0.0
        }

    suspend fun setTotalBudget(amount: Double) {
        context.dataStore.edit { prefs ->
            prefs[TOTAL_BUDGET] = amount
        }
    }
    val LAST_RESET_MONTH = intPreferencesKey("last_reset_month")
    suspend fun setLastResetMonth(month: Int) {
        context.dataStore.edit { prefs ->
            prefs[LAST_RESET_MONTH] = month
        }
    }

    val lastResetMonthFlow: Flow<Int> =
        context.dataStore.data.map { prefs ->
            prefs[LAST_RESET_MONTH] ?: -1
        }
}