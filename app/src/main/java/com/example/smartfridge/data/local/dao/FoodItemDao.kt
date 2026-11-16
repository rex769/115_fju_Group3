package com.example.smartfridge.data.local.dao

import androidx.room.*
import com.example.smartfridge.data.local.entity.FoodItemEntity
import com.example.smartfridge.domain.model.ItemActionState
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodItemDao {
    @Query("SELECT * FROM food_items WHERE status = :status ORDER BY expiryDateMillis ASC")
    fun getFoodItemsByStatus(status: ItemActionState = ItemActionState.IN_FRIDGE): Flow<List<FoodItemEntity>>

    @Query("SELECT * FROM food_items WHERE status != 'IN_FRIDGE' ORDER BY actionDateMillis DESC")
    fun getHistoryFoodItems(): Flow<List<FoodItemEntity>>

    @Query("SELECT * FROM food_items WHERE id = :id LIMIT 1")
    suspend fun getFoodItemById(id: Long): FoodItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodItem(foodItem: FoodItemEntity): Long

    @Update
    suspend fun updateFoodItem(foodItem: FoodItemEntity)

    @Delete
    suspend fun deleteFoodItem(foodItem: FoodItemEntity)
}
