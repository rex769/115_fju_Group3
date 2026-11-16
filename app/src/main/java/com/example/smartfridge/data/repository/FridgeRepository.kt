package com.example.smartfridge.data.repository

import com.example.smartfridge.data.local.dao.FoodItemDao
import com.example.smartfridge.data.local.dao.QuickTemplateDao
import com.example.smartfridge.data.local.dao.ShoppingItemDao
import com.example.smartfridge.data.local.dao.StorageTipDao
import com.example.smartfridge.data.local.entity.FoodItemEntity
import com.example.smartfridge.data.local.entity.QuickTemplateEntity
import com.example.smartfridge.data.local.entity.ShoppingItemEntity
import com.example.smartfridge.domain.model.ItemActionState
import com.example.smartfridge.domain.model.StorageLocation
import kotlinx.coroutines.flow.Flow

class FridgeRepository(
    private val foodItemDao: FoodItemDao,
    private val quickTemplateDao: QuickTemplateDao,
    private val shoppingItemDao: ShoppingItemDao,
    private val storageTipDao: StorageTipDao
) {

    val inStockFoodItems: Flow<List<FoodItemEntity>> = foodItemDao.getFoodItemsByStatus(ItemActionState.IN_FRIDGE)
    val historyFoodItems: Flow<List<FoodItemEntity>> = foodItemDao.getHistoryFoodItems()

    val quickTemplates: Flow<List<QuickTemplateEntity>> = quickTemplateDao.getAllTemplates()
    val shoppingItems: Flow<List<ShoppingItemEntity>> = shoppingItemDao.getAllShoppingItems()

    suspend fun getFoodItemById(id: Long): FoodItemEntity? {
        return foodItemDao.getFoodItemById(id)
    }

    suspend fun insertFoodItem(foodItem: FoodItemEntity): Long {
        return foodItemDao.insertFoodItem(foodItem)
    }

    suspend fun updateFoodItem(foodItem: FoodItemEntity) {
        foodItemDao.updateFoodItem(foodItem)
    }

    suspend fun deleteFoodItem(foodItem: FoodItemEntity) {
        foodItemDao.deleteFoodItem(foodItem)
    }

    // 標記為已食用，若勾選自動補貨則同步加入採買清單
    suspend fun markAsConsumed(foodItem: FoodItemEntity, addToShoppingList: Boolean = true) {
        val now = System.currentTimeMillis()
        val updatedItem = foodItem.copy(
            status = ItemActionState.CONSUMED,
            actionDateMillis = now
        )
        foodItemDao.updateFoodItem(updatedItem)

        if (addToShoppingList) {
            val shoppingItem = ShoppingItemEntity(
                name = foodItem.name,
                quantity = 1,
                isChecked = false
            )
            shoppingItemDao.insertShoppingItem(shoppingItem)
        }
    }

    // 標記為過期丟棄
    suspend fun markAsDiscarded(foodItem: FoodItemEntity) {
        val now = System.currentTimeMillis()
        val updatedItem = foodItem.copy(
            status = ItemActionState.DISCARDED,
            actionDateMillis = now
        )
        foodItemDao.updateFoodItem(updatedItem)
    }

    // 按錯復原回冰箱
    suspend fun restoreToFridge(foodItem: FoodItemEntity) {
        val updatedItem = foodItem.copy(
            status = ItemActionState.IN_FRIDGE,
            actionDateMillis = null
        )
        foodItemDao.updateFoodItem(updatedItem)
    }

    // 再買一次加入採買清單
    suspend fun addAgainToShoppingList(foodItem: FoodItemEntity) {
        val shoppingItem = ShoppingItemEntity(
            name = foodItem.name,
            quantity = foodItem.quantity,
            isChecked = false
        )
        shoppingItemDao.insertShoppingItem(shoppingItem)
    }

    // 採買清單 CRUD
    suspend fun insertShoppingItem(shoppingItem: ShoppingItemEntity): Long {
        return shoppingItemDao.insertShoppingItem(shoppingItem)
    }

    suspend fun updateShoppingItem(shoppingItem: ShoppingItemEntity) {
        shoppingItemDao.updateShoppingItem(shoppingItem)
    }

    suspend fun deleteShoppingItem(shoppingItem: ShoppingItemEntity) {
        shoppingItemDao.deleteShoppingItem(shoppingItem)
    }

    // 保存方式百科檢查
    suspend fun validateStorageLocation(foodName: String, selectedLocation: StorageLocation): String? {
        val tip = storageTipDao.findTipByKeyword(foodName.trim())
        if (tip != null && tip.wrongLocation == selectedLocation) {
            return "貼心提醒：【${foodName}】放在「${selectedLocation.label}」容易變質喔！建議改放「${tip.recommendedLocation.label}」：${tip.reason}"
        }
        return null
    }
}
