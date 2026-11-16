package com.example.smartfridge.ui.screens.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfridge.data.local.AppDatabase
import com.example.smartfridge.data.local.entity.FoodItemEntity
import com.example.smartfridge.data.preferences.UserPreferencesRepo
import com.example.smartfridge.data.repository.FridgeRepository
import com.example.smartfridge.domain.model.ItemActionState
import com.example.smartfridge.util.VoiceAssistantManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HistoryUiState(
    val historyItems: List<FoodItemEntity> = emptyList(),
    val filteredItems: List<FoodItemEntity> = emptyList(),
    val isSeniorMode: Boolean = false,
    val consumedCount: Int = 0,
    val discardedCount: Int = 0,
    val wastePercentage: Float = 0f,
    val selectedStatusFilter: ItemActionState? = null
)

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = FridgeRepository(
        foodItemDao = db.foodItemDao(),
        quickTemplateDao = db.quickTemplateDao(),
        shoppingItemDao = db.shoppingItemDao(),
        storageTipDao = db.storageTipDao()
    )
    private val preferencesRepo = UserPreferencesRepo(application)
    private val voiceAssistant = VoiceAssistantManager(application)

    private val _selectedStatusFilter = MutableStateFlow<ItemActionState?>(null)

    val uiState: StateFlow<HistoryUiState> = combine(
        repository.historyFoodItems,
        preferencesRepo.isSeniorMode,
        _selectedStatusFilter
    ) { items, isSenior, filter ->
        val consumed = items.count { it.status == ItemActionState.CONSUMED }
        val discarded = items.count { it.status == ItemActionState.DISCARDED }
        val total = consumed + discarded
        val wastePct = if (total > 0) (discarded.toFloat() / total) * 100f else 0f

        val filtered = items.filter { item ->
            filter == null || item.status == filter
        }

        HistoryUiState(
            historyItems = items,
            filteredItems = filtered,
            isSeniorMode = isSenior,
            consumedCount = consumed,
            discardedCount = discarded,
            wastePercentage = wastePct,
            selectedStatusFilter = filter
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState()
    )

    fun onFilterSelected(filter: ItemActionState?) {
        _selectedStatusFilter.value = filter
    }

    fun restoreToFridge(item: FoodItemEntity) {
        viewModelScope.launch {
            repository.restoreToFridge(item)
            voiceAssistant.speak("已將【${item.name}】復原加回冰箱庫存！")
        }
    }

    fun addAgainToShoppingList(item: FoodItemEntity) {
        viewModelScope.launch {
            repository.addAgainToShoppingList(item)
            voiceAssistant.speak("已將【${item.name}】再次加入採買清單！")
        }
    }

    fun permanentlyDeleteItem(item: FoodItemEntity) {
        viewModelScope.launch {
            repository.deleteFoodItem(item)
            voiceAssistant.speak("已徹底刪除【${item.name}】紀錄！")
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceAssistant.shutdown()
    }
}
