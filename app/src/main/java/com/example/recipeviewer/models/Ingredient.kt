package com.example.recipeviewer.models

/** 재료 저장을 위한 데이터 클래스
 * 
 * @author 노평주
 */
data class Ingredient(
    var id: String, 
    var name: String,
    var quantity: Int,
    var unit: String,
    var expiryDate: String
)