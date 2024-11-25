package com.example.recipeviewer.ExcludedIngredients

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recipeviewer.R
import com.example.recipeviewer.helpers.DatabaseHelper
import android.widget.Button
import androidx.appcompat.widget.SearchView
import android.content.Context



class ExcludedIngredientsActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var excludedRecyclerView: RecyclerView
    private lateinit var excludedAdapter: ExcludedIngredientsAdapter
    private lateinit var excludedIngredientList: MutableList<String>
    private lateinit var sharedPrefs: SharedPreferences






    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_excluded_ingredients)

        excludedAdapter = ExcludedIngredientsAdapter(mutableListOf()) // 초기에는 빈 목록으로 생성
        // ...

        databaseHelper = DatabaseHelper(this)

        sharedPrefs = getSharedPreferences("excluded_ingredients", Context.MODE_PRIVATE)

        // 제외 재료 목록 초기화 (데이터베이스에서 불러오기)
        excludedIngredientList = databaseHelper.getExcludedIngredients().toMutableList()


        // 제외 재료 목록 RecyclerView 초기화
        excludedRecyclerView = findViewById(R.id.recyclerViewExcludedIngredients)
        excludedRecyclerView.layoutManager = LinearLayoutManager(this)

        // 제외 재료 목록 초기화
        excludedIngredientList = mutableListOf()

        // 제외 재료 어댑터 초기화
        excludedAdapter = ExcludedIngredientsAdapter(excludedIngredientList)
        excludedRecyclerView.adapter = excludedAdapter



        val searchView: SearchView = findViewById(R.id.searchViewExcludedIngredients) // SearchView 타입 수정
        val addButton: Button = findViewById(R.id.addExcludedIngredientButton)

        addButton.setOnClickListener {
            val query = searchView.query.toString()
            if (query.isNotBlank()) {
                // 데이터베이스에 제외 재료 추가
                databaseHelper.addExcludedIngredient(query)

                // RecyclerView에 제외 재료 추가 및 업데이트
                excludedIngredientList.add(query)
                excludedAdapter.updateData(excludedIngredientList) // 어댑터 업데이트
                searchView.setQuery("", false) // 검색창 초기화

                // SharedPreferences에 저장
                saveExcludedIngredients()
            }
        }



        // ... (기존 코드) ...
    }

    override fun onResume() {
        super.onResume()
        // SharedPreferences에서 데이터 로드
        excludedIngredientList = loadExcludedIngredients()
        excludedAdapter.updateData(excludedIngredientList)
    }

    // SharedPreferences에 저장
    private fun saveExcludedIngredients() {
        val editor = sharedPrefs.edit()
        editor.putStringSet("excluded_ingredients_set", excludedIngredientList.toSet())
        editor.apply()
    }

    // SharedPreferences에서 로드
    private fun loadExcludedIngredients(): MutableList<String> {
        val excludedIngredientsSet = sharedPrefs.getStringSet("excluded_ingredients_set", setOf())
        return excludedIngredientsSet?.toMutableList() ?: mutableListOf() // 안전 호출 연산자 사용
    }


}