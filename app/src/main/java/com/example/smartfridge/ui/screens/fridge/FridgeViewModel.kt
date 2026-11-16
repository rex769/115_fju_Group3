package com.example.smartfridge.ui.screens.fridge

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfridge.data.local.AppDatabase
import com.example.smartfridge.data.local.entity.FoodItemEntity
import com.example.smartfridge.data.preferences.UserPreferencesRepo
import com.example.smartfridge.data.repository.FridgeRepository
import com.example.smartfridge.domain.model.ExpiryStatus
import com.example.smartfridge.domain.model.FoodSortField
import com.example.smartfridge.domain.model.FoodSortOrder
import com.example.smartfridge.domain.model.StorageLocation
import com.example.smartfridge.util.VoiceAssistantManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class FridgeUiState(
    val foodItems: List<FoodItemEntity> = emptyList(),
    val filteredItems: List<FoodItemEntity> = emptyList(),
    val isSeniorMode: Boolean = false,
    val redCount: Int = 0,
    val yellowCount: Int = 0,
    val greenCount: Int = 0,
    val selectedLocation: StorageLocation? = null,
    val searchQuery: String = "",
    val sortField: FoodSortField = FoodSortField.EXPIRY_DATE,
    val sortOrder: FoodSortOrder = FoodSortOrder.ASCENDING
)

class FridgeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = FridgeRepository(
        foodItemDao = db.foodItemDao(),
        quickTemplateDao = db.quickTemplateDao(),
        shoppingItemDao = db.shoppingItemDao(),
        storageTipDao = db.storageTipDao()
    )
    private val preferencesRepo = UserPreferencesRepo(application)
    private val voiceAssistant = VoiceAssistantManager(application)

    private val _searchQuery = MutableStateFlow("")
    private val _selectedLocation = MutableStateFlow<StorageLocation?>(null)
    private val _sortField = MutableStateFlow(FoodSortField.EXPIRY_DATE)
    private val _sortOrder = MutableStateFlow(FoodSortOrder.ASCENDING)

    val uiState: StateFlow<FridgeUiState> = combine(
        repository.inStockFoodItems,
        preferencesRepo.isSeniorMode,
        _searchQuery,
        _selectedLocation,
        _sortField,
        _sortOrder
    ) { flows: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val items = flows[0] as List<FoodItemEntity>
        val isSenior = flows[1] as Boolean
        val query = flows[2] as String
        val location = flows[3] as StorageLocation?
        val field = flows[4] as FoodSortField
        val order = flows[5] as FoodSortOrder

        val red = items.count { it.getExpiryStatus() == ExpiryStatus.RED }
        val yellow = items.count { it.getExpiryStatus() == ExpiryStatus.YELLOW }
        val green = items.count { it.getExpiryStatus() == ExpiryStatus.GREEN }

        val filtered = items.filter { item ->
            val matchesQuery = query.isBlank() || item.name.contains(query, ignoreCase = true)
            val matchesLocation = location == null || item.location == location
            matchesQuery && matchesLocation
        }.sortedWith { item1, item2 ->
            val time1 = if (field == FoodSortField.EXPIRY_DATE) item1.expiryDateMillis else item1.addedDateMillis
            val time2 = if (field == FoodSortField.EXPIRY_DATE) item2.expiryDateMillis else item2.addedDateMillis

            if (order == FoodSortOrder.ASCENDING) {
                time1.compareTo(time2)
            } else {
                time2.compareTo(time1)
            }
        }

        FridgeUiState(
            foodItems = items,
            filteredItems = filtered,
            isSeniorMode = isSenior,
            redCount = red,
            yellowCount = yellow,
            greenCount = green,
            selectedLocation = location,
            searchQuery = query,
            sortField = field,
            sortOrder = order
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FridgeUiState()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onLocationFilterSelected(location: StorageLocation?) {
        _selectedLocation.value = location
    }

    fun onSortFieldSelected(field: FoodSortField) {
        _sortField.value = field
    }

    fun onSortOrderSelected(order: FoodSortOrder) {
        _sortOrder.value = order
    }

    fun toggleSeniorMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepo.setSeniorMode(enabled)
        }
    }

    fun markAsConsumed(foodItem: FoodItemEntity) {
        viewModelScope.launch {
            repository.markAsConsumed(foodItem, addToShoppingList = true)
        }
    }

    fun markAsDiscarded(foodItem: FoodItemEntity) {
        viewModelScope.launch {
            repository.markAsDiscarded(foodItem)
        }
    }

    fun permanentlyDeleteItem(foodItem: FoodItemEntity) {
        viewModelScope.launch {
            repository.deleteFoodItem(foodItem)
        }
    }

    fun speakTodayExpiringSummary() {
        val currentItems = uiState.value.foodItems
        val redItems = currentItems.filter { it.getExpiryStatus() == ExpiryStatus.RED }
        if (redItems.isEmpty()) {
            voiceAssistant.speak("提醒您，目前冰箱裡的食材都很新鮮，沒有快過期的食材喔！")
        } else {
            val names = redItems.joinToString("、") { "${it.name}剩餘${it.getRemainingDays()}天" }
            voiceAssistant.speak("提醒您，冰箱裡的${names}快要到期了，記得優先料理喔！")
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceAssistant.shutdown()
    }
}
