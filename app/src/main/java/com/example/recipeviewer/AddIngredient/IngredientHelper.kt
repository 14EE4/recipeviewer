package com.example.recipeviewer.helpers

import com.example.recipeviewer.models.Recipe

/**
 * 재료 처리 관련 함수들을 모아둔 헬퍼 클래스
 * extractName = false를 추가하면 분량 단위까지 나옴
 *
 * @author 노평주
 */
object IngredientHelper {
    fun parseAllIngredients(recipe: Recipe, extractName: Boolean = true): List<String> {
        val mainIngredients = parseIngredients(recipe.mainIngredients, extractName)
        val subIngredients = parseIngredients(recipe.subIngredients, extractName)
        val alternativeIngredients = parseIngredients(recipe.alternativeIngredients, extractName)
        val allIngredients = mainIngredients + subIngredients + alternativeIngredients
        return allIngredients
    }

    fun parseIngredients(ingredients: String, extractName: Boolean = true): List<String> {
        val parsedIngredients = ingredients.split(",").map {
            if (extractName) {
                extractIngredientName(it)
            } else {
                it.trim() // extractName이 false면 원본 재료 문자열 그대로 사용
            }
        }
        return parsedIngredients
    }



    private fun extractIngredientName(ingredient: String): String {
        return ingredient.replace(Regex("\\s*\\d+.*"), "").trim()
    }
}