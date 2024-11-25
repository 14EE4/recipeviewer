package com.example.recipeviewer.models

data class Ingredient(
    var id: String, // String 타입으로 변경
    var name: String,
    var quantity: Int,
    var unit: String,
    var expiryDate: String
)