package com.example.recipeviewer.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.example.recipeviewer.models.Recipe

class BookmarkManager(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    // 북마크 추가
    fun addBookmark(context: Context, recipe: Recipe, userId: String, onComplete: () -> Unit) {
        val bookmark = mapOf(
            "id" to recipe.id,
            "title" to recipe.title,
            "userID" to userId // userId를 매개변수로 전달
        )

        // 사용자의 북마크 컬렉션에 저장
        db.collection("users").document(userId).collection("bookmarks").document(recipe.id.toString()).set(bookmark)
            .addOnSuccessListener {
                Toast.makeText(context, "북마크에 추가되었습니다.", Toast.LENGTH_SHORT).show()
                onComplete()
            }
            .addOnFailureListener { exception ->
                Log.e("BookmarkManager", "Firestore Error: ${exception.message}")
                Toast.makeText(context, "북마크 추가 실패: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // 북마크 삭제
    fun removeBookmark(context: Context, recipeId: String, userId: String, onComplete: () -> Unit) {
        // 사용자의 북마크 컬렉션에서 삭제
        db.collection("users").document(userId).collection("bookmarks").document(recipeId).delete()
            .addOnSuccessListener {
                Toast.makeText(context, "북마크에서 제거되었습니다.", Toast.LENGTH_SHORT).show()
                onComplete()
            }
            .addOnFailureListener {
                Toast.makeText(context, "북마크 제거 실패", Toast.LENGTH_SHORT).show()
            }
    }

    // 북마크 여부 확인
    fun isBookmarked(recipeId: String, userId: String, onResult: (Boolean) -> Unit) {
        // 사용자의 북마크 컬렉션에서 확인
        db.collection("users").document(userId).collection("bookmarks").document(recipeId).get()
            .addOnSuccessListener { document ->
                onResult(document.exists())
            }
            .addOnFailureListener {
                onResult(false)
            }
    }
}

