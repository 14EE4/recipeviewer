package com.example.recipeviewer.ExcludedIngredients

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.size
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recipeviewer.R
import com.example.recipeviewer.helpers.DatabaseHelper
import com.google.firebase.auth.FirebaseAuth // Firebase Authentication import
import com.google.firebase.firestore.FirebaseFirestore // Firebase Firestore import

class ExcludedIngredientsActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var excludedRecyclerView: RecyclerView
    private lateinit var excludedAdapter: ExcludedIngredientsAdapter
    private lateinit var excludedIngredientList: MutableList<String>
    private lateinit var auth: FirebaseAuth
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_excluded_ingredients)

        // Firebase Authentication 초기화
        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: ""

        // DatabaseHelper 초기화
        databaseHelper = DatabaseHelper(this)

        // 제외 재료 목록 초기화
        excludedIngredientList = mutableListOf()

        // 제외 재료 어댑터 초기화
        excludedAdapter = ExcludedIngredientsAdapter(excludedIngredientList, this)
        excludedRecyclerView = findViewById(R.id.recyclerViewExcludedIngredients)
        excludedRecyclerView.layoutManager = LinearLayoutManager(this)
        excludedRecyclerView.adapter = excludedAdapter

        val searchView = findViewById<androidx.appcompat.widget.SearchView>(R.id.searchViewExcludedIngredients)
        val addButton = findViewById<Button>(R.id.addExcludedIngredientButton)

        // ExcludedIngredientsActivity.kt
// ...

        addButton.setOnClickListener {
            val query = searchView.query.toString()
            if (query.isNotBlank()) {
                databaseHelper.addExcludedIngredient(userId, query) {
                    runOnUiThread { // 메인 스레드에서 UI 업데이트
                        excludedIngredientList.add(query)
                        excludedAdapter.notifyItemInserted(excludedIngredientList.size - 1)
                        excludedRecyclerView.scrollToPosition(excludedIngredientList.size - 1) // RecyclerView 스크롤
                        searchView.setQuery("", false)
                        Toast.makeText(this@ExcludedIngredientsActivity, "제외 재료가 추가되었습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "제외할 재료를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }

// ...

        // Firebase Firestore에서 제외 재료 로드
        loadExcludedIngredients()
    }

    private fun loadExcludedIngredients() {
        databaseHelper.getExcludedIngredients(userId) { ingredients ->
            excludedIngredientList = ingredients.toMutableList()
            excludedAdapter.updateData(excludedIngredientList)
        }
    }

    override fun onResume() {
        super.onResume()
        // Firebase Firestore에서 제외 재료 로드
        loadExcludedIngredients()
    }
}