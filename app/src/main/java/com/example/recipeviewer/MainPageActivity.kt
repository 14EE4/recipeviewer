package com.example.recipeviewer

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button
import android.widget.Toast
import com.example.recipeviewer.helpers.DatabaseHelper
import com.example.recipeviewer.helpers.VoiceSearchHelper
import com.example.recipeviewer.models.Recipe

class MainPageActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var recipeList: MutableList<Recipe>
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var voiceSearchHelper: VoiceSearchHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // VoiceSearchHelper 초기화
        voiceSearchHelper = VoiceSearchHelper(this) { query ->
            searchRecipes(query)
        }

        // 음성 검색 버튼 설정
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

        // SearchView 초기화 및 설정
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_ingredient -> {
                // 재료 추가 화면으로 이동
                startActivity(Intent(this, AddIngredientActivity::class.java))
                true
            }
            R.id.action_logout -> {
                // 로그아웃 처리
                Toast.makeText(this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // 레시피 목록 읽기
    private fun readRecipes(): MutableList<Recipe> {
        return databaseHelper.readAllData().toMutableList()
    }

    // 레시피 필터링
    private fun filter(query: String?) {
        val filteredList = recipeList.filter { recipe ->
            recipe.title.contains(query ?: "", ignoreCase = true)
        }.toMutableList()

        if (filteredList.isEmpty()) {
            Toast.makeText(this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show()
        }

        recipeAdapter.updateData(filteredList)
    }

    private fun searchRecipes(query: String) {
        val filteredRecipes = recipeList.filter { it.title.contains(query, ignoreCase = true) }.toMutableList()
        recipeAdapter.updateData(filteredRecipes)
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
