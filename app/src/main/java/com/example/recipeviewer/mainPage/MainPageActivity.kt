package com.example.recipeviewer.mainPage

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.geometry.isEmpty
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recipeviewer.helpers.DatabaseHelper
import com.example.recipeviewer.helpers.VoiceSearchHelper
import com.example.recipeviewer.models.Recipe
import com.example.recipeviewer.AddIngredient.AddIngredientActivity
import com.example.recipeviewer.login.MainActivity
import com.example.recipeviewer.R
import com.example.recipeviewer.ExcludedIngredients.ExcludedIngredientsActivity
import com.example.recipeviewer.helpers.IngredientHelper
import com.google.firebase.auth.FirebaseAuth
import kotlin.text.contains

/**
 * 로그인 후 나오는 메인 페이지.
 * 재료 검색창, 음성 인식 검색, 재료 검색 버튼
 * recyclerview로 레시피 리스트
 * -클릭하면 RecipeDetailsActivity로 페이지 이동
 *
 * @author 노평주
 */
class MainPageActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var recipeList: MutableList<Recipe>
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var voiceSearchHelper: VoiceSearchHelper
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // FirebaseAuth 인스턴스 초기화
        auth = FirebaseAuth.getInstance()

        // 현재 사용자 가져오기
        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        // VoiceSearchHelper 초기화
        voiceSearchHelper = VoiceSearchHelper(this) { query ->
            searchRecipes(query)
        }

        // RecyclerView 초기화
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // DatabaseHelper 초기화
        databaseHelper = DatabaseHelper(this)

        // 레시피 불러오기
        recipeList = databaseHelper.readAllData()

        // 클릭 리스너와 함께 어댑터 초기화
        val recipes = databaseHelper.readAllData()
        recipeAdapter = RecipeAdapter(recipes) { recipe ->
            val intent = Intent(this, RecipeDetailsActivity::class.java).apply {
                putExtra("recipeId", recipe.id) // 레시피 ID만 전달
            }
            startActivity(intent)
        }
        recyclerView.adapter = recipeAdapter

        // 음성 검색 버튼 설정
        findViewById<Button>(R.id.voiceSearchButton).setOnClickListener {
            voiceSearchHelper.startVoiceRecognition()
        }

        // SearchView 초기화 및 설정
        val searchView: SearchView = findViewById(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchRecipes(query) // 엔터를 쳤을 때 검색 수행
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                // 텍스트가 변경될 때는 아무 작업도 수행하지 않음
                return true
            }
        })
        
        // 재료 검색 버튼 설정
        findViewById<Button>(R.id.ingredientSearchButton).setOnClickListener {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                searchAndSortRecipesByIngredients(userId)
            } else {
                Toast.makeText(this, "사용자 ID를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 모든 레시피 보기 버튼 설정
        findViewById<Button>(R.id.showAllRecipesButton).setOnClickListener {
            recipeAdapter.updateData(recipeList)
        }
    }
    //메뉴 액션바
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    //메뉴 클릭 이벤트
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_ingredient -> {
                // 재료 추가 화면으로 이동
                startActivity(Intent(this, AddIngredientActivity::class.java))
                true
            }
            R.id.action_excluded_ingredients -> {
                // 제외 재료 화면으로 이동
                startActivity(Intent(this, ExcludedIngredientsActivity::class.java))
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

    // 레시피 검색
    private fun searchRecipes(query: String?) {
        val filteredRecipes = recipeList.filter { recipe ->
            recipe.title.contains(query ?: "", ignoreCase = true)
        }.toMutableList()

        if (filteredRecipes.isEmpty() && query != null && query.isNotBlank()) { // query가 null이나 공백이 아닌 경우에만 토스트 메시지 표시
            Toast.makeText(this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show()
        }

        recipeAdapter.updateData(filteredRecipes)
        if (query != null && query.isNotBlank()) { // query가 null이나 공백이 아닌 경우에만 토스트 메시지 표시
            Toast.makeText(this, "검색 결과: $query", Toast.LENGTH_SHORT).show()
        }
    }
    //음성 인식 권한 처리
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        voiceSearchHelper.handlePermissionsResult(requestCode, grantResults)
    }
    //액티비티 소멸시
    override fun onDestroy() {
        super.onDestroy()
        voiceSearchHelper.release()
    }

    private fun searchAndSortRecipesByIngredients(userId: String) {
        databaseHelper.readIngredients(userId) { ingredientList ->
            databaseHelper.getExcludedIngredients(userId) { excludedIngredientList: List<String> -> // SQLite에서 제외 재료 목록 가져오기

                val matchingRecipes = recipeList.map { recipe ->
                    val recipeIngredients =
                        IngredientHelper.parseAllIngredients(recipe) // IngredientHelper 사용
                    val commonIngredientsCount = recipeIngredients.count { recipeIngredient ->
                        ingredientList.any { dbIngredient ->
                            recipeIngredient.contains(dbIngredient.name, ignoreCase = true)
                        }
                    }

                    // 제외 재료가 포함된 레시피는 commonIngredientsCount를 감소시켜 후순위로 보냄
                    val excludedIngredientsCount = recipeIngredients.count { recipeIngredient ->
                        excludedIngredientList.any { excludedIngredient ->
                            recipeIngredient.contains(
                                excludedIngredient,
                                ignoreCase = true
                            ) // excludedIngredient를 직접 사용
                        }
                    }
                    val finalCount = commonIngredientsCount - excludedIngredientsCount

                    // Logcat에 출력
                    Log.d(
                        "RecipeMatch",
                        "Recipe '${recipe.title}' has $finalCount matching ingredients (excluded: $excludedIngredientsCount)."
                    )

                    Pair(recipe, finalCount)
                }.filter { it.second > 0 } // 일치하는 재료가 하나라도 있는 레시피만 필터링
                    .sortedByDescending { it.second } // 일치하는 개수가 많은 순서대로 정렬

                if (matchingRecipes.isEmpty()) {
                    Toast.makeText(this, "비슷한 재료가 있는 레시피가 없습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    val sortedRecipes = matchingRecipes.map { it.first }.toMutableList()
                    recipeAdapter.updateData(sortedRecipes)
                    Toast.makeText(
                        this,
                        "${sortedRecipes.size}개의 레시피가 검색되었습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}