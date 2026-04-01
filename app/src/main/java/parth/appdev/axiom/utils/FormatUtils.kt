package parth.appdev.axiom.utils

import java.text.NumberFormat
import java.util.*

fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    return formatter.format(amount)
}