package com.example.recipeviewer.models

data class Ingredient(
    val id: Int,
    val name: String,
    val quantity: Int,
    val unit: String,
    val expiryDate: String
)