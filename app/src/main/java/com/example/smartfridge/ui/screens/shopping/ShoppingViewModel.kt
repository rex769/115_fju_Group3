package com.example.smartfridge.ui.screens.shopping

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfridge.data.local.AppDatabase
import com.example.smartfridge.data.local.entity.FoodItemEntity
import com.example.smartfridge.data.local.entity.ShoppingItemEntity
import com.example.smartfridge.data.preferences.UserPreferencesRepo
import com.example.smartfridge.data.repository.FridgeRepository
import com.example.smartfridge.domain.model.ItemActionState
import com.example.smartfridge.domain.model.StorageLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class ShoppingUiState(
    val shoppingItems: List<ShoppingItemEntity> = emptyList(),
    val isSeniorMode: Boolean = false,
    val newItemName: String = ""
)

class ShoppingViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = FridgeRepository(
        foodItemDao = db.foodItemDao(),
        quickTemplateDao = db.quickTemplateDao(),
        shoppingItemDao = db.shoppingItemDao(),
        storageTipDao = db.storageTipDao()
    )
    private val preferencesRepo = UserPreferencesRepo(application)

    private val _newItemName = MutableStateFlow("")

    val uiState: StateFlow<ShoppingUiState> = combine(
        repository.shoppingItems,
        preferencesRepo.isSeniorMode,
        _newItemName
    ) { items, isSenior, name ->
        ShoppingUiState(
            shoppingItems = items,
            isSeniorMode = isSenior,
            newItemName = name
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ShoppingUiState()
    )

    fun onNewItemNameChanged(name: String) {
        _newItemName.value = name
    }

    fun addShoppingItem() {
        val name = _newItemName.value.trim()
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.insertShoppingItem(
                ShoppingItemEntity(name = name, quantity = 1, isChecked = false)
            )
            _newItemName.value = ""
        }
    }

    fun toggleChecked(item: ShoppingItemEntity) {
        viewModelScope.launch {
            repository.updateShoppingItem(item.copy(isChecked = !item.isChecked))
        }
    }

    fun deleteItem(item: ShoppingItemEntity) {
        viewModelScope.launch {
            repository.deleteShoppingItem(item)
        }
    }

    // 採買完成後，將物品一鍵補回冰箱庫存
    fun restockToFridge(item: ShoppingItemEntity) {
        val now = System.currentTimeMillis()
        val defaultExpiry = now + TimeUnit.DAYS.toMillis(7) // 預設 7 天
        val foodItem = FoodItemEntity(
            name = item.name,
            quantity = item.quantity,
            unit = "份",
            location = StorageLocation.COLD,
            addedDateMillis = now,
            expiryDateMillis = defaultExpiry,
            category = "採買補貨",
            status = ItemActionState.IN_FRIDGE
        )

        viewModelScope.launch {
            repository.insertFoodItem(foodItem)
            repository.deleteShoppingItem(item)
        }
    }
}
