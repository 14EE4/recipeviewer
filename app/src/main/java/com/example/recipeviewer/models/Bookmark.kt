package com.example.recipeviewer.models

data class Bookmark(
    var id: String = "", // Firestore 문서 ID를 설정할 수 있도록 var로 변경
    val title: String = "",
    val userId: String = "",
    val description: String = ""
)