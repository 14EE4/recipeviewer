
package com.example.recipeviewer.models
/**
 * 레시피 모델
 *
 * @author 노평주
 */
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
    val recipeUrl: String,

    var isBookmarked: Boolean = false  // 북마크 상태
)





