package parth.appdev.axiom.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val categoryId: Int,

    val amount: Double,

    val note: String,

    val timestamp: Long
)