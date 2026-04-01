package parth.appdev.axiom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import parth.appdev.axiom.ui.theme.AxiomTheme
import parth.appdev.axiom.ui.screens.home.HomeScreen
import parth.appdev.axiom.ui.screens.setup.SetupScreen
import parth.appdev.axiom.viewmodel.AxiomViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AxiomTheme {

                val viewModel: AxiomViewModel = viewModel()

                LaunchedEffect(Unit) {
                    viewModel.checkAndResetMonthly()
                }
                val totalBudget by viewModel.totalBudgetFlow.collectAsState()

                Surface {

                    if (totalBudget == 0.0) {
                        SetupScreen(
                            onSetBudget = {
                                viewModel.setTotalBudget(it)
                            }
                        )
                    } else {
                        HomeScreen(viewModel)
                    }
                }
            }
        }
    }
}