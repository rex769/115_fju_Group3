package com.example.smartfridge.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.smartfridge.domain.model.ExpiryStatus
import com.example.smartfridge.domain.model.ItemActionState
import com.example.smartfridge.domain.model.StorageLocation
import java.util.concurrent.TimeUnit

@Entity(tableName = "food_items")
data class FoodItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val quantity: Int,
    val unit: String = "份",
    val location: StorageLocation,
    val addedDateMillis: Long,
    val expiryDateMillis: Long,
    val category: String = "一般",
    val status: ItemActionState = ItemActionState.IN_FRIDGE,
    val actionDateMillis: Long? = null,
    val note: String = "",
    val imageUri: String? = null
) {
    fun getRemainingDays(currentTimeMillis: Long = System.currentTimeMillis()): Int {
        val diff = expiryDateMillis - currentTimeMillis
        return TimeUnit.MILLISECONDS.toDays(diff).toInt()
    }

    fun getExpiryStatus(currentTimeMillis: Long = System.currentTimeMillis()): ExpiryStatus {
        val days = getRemainingDays(currentTimeMillis)
        return when {
            days <= 2 -> ExpiryStatus.RED
            days in 3..5 -> ExpiryStatus.YELLOW
            else -> ExpiryStatus.GREEN
        }
    }
}
