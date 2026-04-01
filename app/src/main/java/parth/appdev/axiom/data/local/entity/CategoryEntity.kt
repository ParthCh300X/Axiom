package parth.appdev.axiom.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,

    val type: String, // ONE_TIME, DAILY, VARIABLE

    val budget: Double,

    val spent: Double = 0.0,

    val isLocked: Boolean = false
)