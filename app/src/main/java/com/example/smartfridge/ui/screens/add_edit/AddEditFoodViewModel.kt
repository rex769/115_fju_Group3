package com.example.smartfridge.ui.screens.add_edit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfridge.data.local.AppDatabase
import com.example.smartfridge.data.local.entity.FoodItemEntity
import com.example.smartfridge.data.local.entity.QuickTemplateEntity
import com.example.smartfridge.data.repository.FridgeRepository
import com.example.smartfridge.domain.model.ItemActionState
import com.example.smartfridge.domain.model.StorageLocation
import com.example.smartfridge.domain.usecase.ParseVoiceInputUseCase
import com.example.smartfridge.util.VoiceAssistantManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class AddEditFoodUiState(
    val foodItemId: Long = 0,
    val name: String = "",
    val quantity: Int = 1,
    val unit: String = "份",
    val location: StorageLocation = StorageLocation.COLD,
    val expiryDays: Int = 7,
    val expiryDaysText: String = "7",
    val category: String = "一般",
    val note: String = "",
    val isEditing: Boolean = false,
    val warningMessage: String? = null,
    val isSavedSuccess: Boolean = false,
    val quickTemplates: List<QuickTemplateEntity> = emptyList()
)

class AddEditFoodViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = FridgeRepository(
        foodItemDao = db.foodItemDao(),
        quickTemplateDao = db.quickTemplateDao(),
        shoppingItemDao = db.shoppingItemDao(),
        storageTipDao = db.storageTipDao()
    )
    private val parseVoiceInputUseCase = ParseVoiceInputUseCase()
    private val voiceAssistant = VoiceAssistantManager(application)

    private val _uiState = MutableStateFlow(AddEditFoodUiState())
    val uiState: StateFlow<AddEditFoodUiState> = _uiState

    val templates: StateFlow<List<QuickTemplateEntity>> = repository.quickTemplates.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun loadFoodItemForEdit(foodItemId: Long) {
        if (foodItemId <= 0) return
        viewModelScope.launch {
            val item = repository.getFoodItemById(foodItemId) ?: return@launch
            val days = TimeUnit.MILLISECONDS.toDays(item.expiryDateMillis - System.currentTimeMillis()).toInt().coerceAtLeast(1)
            _uiState.value = _uiState.value.copy(
                foodItemId = item.id,
                name = item.name,
                quantity = item.quantity,
                unit = item.unit,
                location = item.location,
                expiryDays = days,
                expiryDaysText = days.toString(),
                category = item.category,
                note = item.note,
                isEditing = true
            )
        }
    }

    fun onNameChanged(newName: String) {
        _uiState.value = _uiState.value.copy(name = newName)
        validateLocation(newName, _uiState.value.location)
    }

    fun onNoteChanged(newNote: String) {
        _uiState.value = _uiState.value.copy(note = newNote)
    }

    fun onQuantityChanged(delta: Int) {
        val newQty = (_uiState.value.quantity + delta).coerceAtLeast(1)
        _uiState.value = _uiState.value.copy(quantity = newQty)
    }

    fun onUnitChanged(newUnit: String) {
        _uiState.value = _uiState.value.copy(unit = newUnit)
    }

    fun onLocationChanged(newLocation: StorageLocation) {
        _uiState.value = _uiState.value.copy(location = newLocation)
        validateLocation(_uiState.value.name, newLocation)
    }

    fun onExpiryDaysInputChanged(input: String) {
        val filtered = input.filter { it.isDigit() }
        val parsed = filtered.toIntOrNull() ?: 1
        val days = parsed.coerceAtLeast(1)
        _uiState.value = _uiState.value.copy(
            expiryDaysText = filtered,
            expiryDays = days
        )
    }

    fun onExpiryDaysChanged(days: Int) {
        val validDays = days.coerceAtLeast(1)
        _uiState.value = _uiState.value.copy(
            expiryDays = validDays,
            expiryDaysText = validDays.toString()
        )
    }

    fun addExpiryDays(delta: Int) {
        val newDays = (_uiState.value.expiryDays + delta).coerceAtLeast(1)
        _uiState.value = _uiState.value.copy(
            expiryDays = newDays,
            expiryDaysText = newDays.toString()
        )
    }

    fun applyQuickTemplate(template: QuickTemplateEntity) {
        _uiState.value = _uiState.value.copy(
            name = template.name,
            unit = template.defaultUnit,
            location = template.recommendedLocation,
            expiryDays = template.defaultShelfLifeDays,
            expiryDaysText = template.defaultShelfLifeDays.toString(),
            category = template.category
        )
        validateLocation(template.name, template.recommendedLocation)
    }

    fun processVoiceText(spokenText: String) {
        val result = parseVoiceInputUseCase.parse(spokenText) ?: return
        val days = TimeUnit.MILLISECONDS.toDays(result.expiryDateMillis - System.currentTimeMillis()).toInt().coerceAtLeast(1)
        _uiState.value = _uiState.value.copy(
            name = result.foodName,
            expiryDays = days,
            expiryDaysText = days.toString()
        )
        validateLocation(result.foodName, _uiState.value.location)
    }

    private fun validateLocation(foodName: String, location: StorageLocation) {
        if (foodName.isBlank()) return
        viewModelScope.launch {
            val warning = repository.validateStorageLocation(foodName, location)
            _uiState.value = _uiState.value.copy(warningMessage = warning)
            if (warning != null) {
                voiceAssistant.speak(warning)
            }
        }
    }

    fun dismissWarning() {
        _uiState.value = _uiState.value.copy(warningMessage = null)
    }

    fun saveFoodItem() {
        val currentState = _uiState.value
        if (currentState.name.isBlank()) return

        val now = System.currentTimeMillis()
        val expiryMillis = now + TimeUnit.DAYS.toMillis(currentState.expiryDays.toLong())

        val foodItem = FoodItemEntity(
            id = if (currentState.isEditing) currentState.foodItemId else 0,
            name = currentState.name.trim(),
            quantity = currentState.quantity,
            unit = currentState.unit,
            location = currentState.location,
            addedDateMillis = now,
            expiryDateMillis = expiryMillis,
            category = currentState.category,
            status = ItemActionState.IN_FRIDGE,
            note = currentState.note.trim()
        )

        viewModelScope.launch {
            if (currentState.isEditing) {
                repository.updateFoodItem(foodItem)
            } else {
                repository.insertFoodItem(foodItem)
            }
            _uiState.value = _uiState.value.copy(isSavedSuccess = true)
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceAssistant.shutdown()
    }
}
