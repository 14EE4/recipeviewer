package com.example.recipeviewer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import android.widget.Button
import android.widget.Toast
import com.example.recipeviewer.helpers.DatabaseHelper
import com.example.recipeviewer.helpers.VoiceSearchHelper
class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var recipeList: MutableList<Recipe>
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var voiceSearchHelper: VoiceSearchHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        voiceSearchHelper = VoiceSearchHelper(this) { query ->
            searchRecipes(query) // 검색 메서드 호출
        }

        findViewById<Button>(R.id.voiceSearchButton).setOnClickListener {
            voiceSearchHelper.startVoiceRecognition()
        }

        // RecyclerView 초기화
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // DatabaseHelper 초기화
        databaseHelper = DatabaseHelper(this)

        // 레시피 불러오기
        recipeList = readRecipes().toMutableList() // List를 MutableList로 변환

        // 클릭 리스너와 함께 어댑터 초기화
        val recipes = databaseHelper.readAllData().toMutableList() // List를 MutableList로 변환

        recipeAdapter = RecipeAdapter(recipes) { recipe ->
            val intent = Intent(this, RecipeDetailsActivity::class.java).apply {
                putExtra("title", recipe.title)
                putExtra("mainIngredients", recipe.mainIngredients)
                putExtra("subIngredients", recipe.subIngredients)          // 부재료 전달
                putExtra("alternativeIngredients", recipe.alternativeIngredients) // 대체재료 전달
                putExtra("recipeUrl", recipe.recipeUrl)
            }
            startActivity(intent)

        }
        recyclerView.adapter = recipeAdapter

        // SearchView 초기화
        val searchView: SearchView = findViewById(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filter(newText)
                return true
            }
        })
    }

    // 레시피 목록 읽기
    private fun readRecipes(): MutableList<Recipe> {
        return databaseHelper.readAllData().toMutableList() // DatabaseHelper에서 데이터 가져오기
    }


    // 레시피 필터링
    private fun filter(query: String?) {
        val filteredList = recipeList.filter { recipe ->
            recipe.title.contains(query ?: "", ignoreCase = true)
        }.toMutableList() // List<Recipe>를 MutableList<Recipe>로 변환

        if (filteredList.isEmpty()) {
            Toast.makeText(this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show()
        }

        recipeAdapter.updateData(filteredList) // 어댑터 데이터 업데이트
    }



    // 레시피 상세 정보 표시
    private fun showRecipeDetails(recipe: Recipe) {
        val intent = Intent(this, RecipeDetailsActivity::class.java)
        intent.putExtra("RECIPE_TITLE", recipe.title)
        intent.putExtra("MAIN_INGREDIENTS", recipe.mainIngredients)
        intent.putExtra("RECIPE_URL", recipe.recipeUrl) // URL 전달
        startActivity(intent)
    }
    private fun searchRecipes(query: String) {
        // 검색어를 기준으로 필터링
        val filteredRecipes = recipeList.filter { it.title.contains(query, ignoreCase = true) }.toMutableList()

        // 어댑터의 데이터 업데이트
        recipeAdapter.updateData(filteredRecipes)

        // 검색된 결과에 대한 Toast 메시지
        Toast.makeText(this, "검색 결과: $query", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        voiceSearchHelper.handlePermissionsResult(requestCode, grantResults)
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceSearchHelper.release()
    }

}
