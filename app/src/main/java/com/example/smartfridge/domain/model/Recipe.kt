package com.example.smartfridge.domain.model

data class RecipeIngredient(
    val name: String,
    val amount: String
)

data class Recipe(
    val id: Long,
    val title: String,
    val description: String,
    val category: String = "家常料理",
    val seniorTag: String = "好咀嚼",
    val ingredients: List<RecipeIngredient>,
    val steps: List<String>
)
