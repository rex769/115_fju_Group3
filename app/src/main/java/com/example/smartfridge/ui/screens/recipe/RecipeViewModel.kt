package com.example.smartfridge.ui.screens.recipe

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartfridge.data.local.AppDatabase
import com.example.smartfridge.data.local.entity.ShoppingItemEntity
import com.example.smartfridge.data.preferences.UserPreferencesRepo
import com.example.smartfridge.data.repository.FridgeRepository
import com.example.smartfridge.data.repository.RecipeRecommendation
import com.example.smartfridge.data.repository.RecipeRepository
import com.example.smartfridge.util.VoiceAssistantManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RecipeUiState(
    val recommendations: List<RecipeRecommendation> = emptyList(),
    val isSeniorMode: Boolean = false,
    val addedToShoppingSuccessMessage: String? = null
)

class RecipeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val fridgeRepository = FridgeRepository(
        foodItemDao = db.foodItemDao(),
        quickTemplateDao = db.quickTemplateDao(),
        shoppingItemDao = db.shoppingItemDao(),
        storageTipDao = db.storageTipDao()
    )
    private val recipeRepository = RecipeRepository()
    private val preferencesRepo = UserPreferencesRepo(application)
    private val voiceAssistant = VoiceAssistantManager(application)

    val uiState: StateFlow<RecipeUiState> = combine(
        fridgeRepository.inStockFoodItems,
        preferencesRepo.isSeniorMode
    ) { inStock, isSenior ->
        val recs = recipeRepository.getRecommendedRecipes(inStock)
        RecipeUiState(
            recommendations = recs,
            isSeniorMode = isSenior
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RecipeUiState()
    )

    fun addMissingIngredientsToShopping(missingList: List<String>) {
        if (missingList.isEmpty()) return
        viewModelScope.launch {
            missingList.forEach { name ->
                fridgeRepository.insertShoppingItem(
                    ShoppingItemEntity(name = name, quantity = 1, isChecked = false)
                )
            }
            val msg = "已將【${missingList.joinToString("、")}】加入採買清單！"
            voiceAssistant.speak(msg)
        }
    }

    fun speakRecipeSteps(title: String, steps: List<String>) {
        val speechText = "${title}的做法：${steps.joinToString("。")}"
        voiceAssistant.speak(speechText)
    }

    override fun onCleared() {
        super.onCleared()
        voiceAssistant.shutdown()
    }
}
