// Recipe.kt
package com.example.recipeviewer

data class Recipe(
    val id: Int,  // id 매개변수 추가
    val title: String,
    val mainIngredients: String,
    val subIngredients: String,
    val alternativeIngredients: String,
    val cookingTime: String,
    val calories: String,
    val portions: String,
    val description: String,
    val recipeUrl: String
)





