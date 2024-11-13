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

import android.widget.EditText
import android.widget.TextView




class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var recipeAdapter: RecipeAdapter
    private lateinit var recipeList: MutableList<Recipe>
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var voiceSearchHelper: VoiceSearchHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 로그인 화면 설정
        setContentView(R.layout.activity_loginpage) // 수정된 XML 파일 이름으로


        // XML에 정의된 뷰 참조
        val loginTitle: TextView = findViewById(R.id.textView)
        val welcomeMessage: TextView = findViewById(R.id.textView2)
        val emailEditText: EditText = findViewById(R.id.editTextText)
        val passwordEditText: EditText = findViewById(R.id.editTextTextPassword2)
        val passwordLabel: TextView = findViewById(R.id.textView3)
        val emailLabel: TextView = findViewById(R.id.textView4)
        val loginButton: Button = findViewById(R.id.button)
        val createAccountButton: Button = findViewById(R.id.button2)

        // DatabaseHelper 초기화
        databaseHelper = DatabaseHelper(this)

        // 로그인 버튼 클릭 이벤트 설정 (데이터베이스 검사 없이 바로 메인 페이지로 이동)
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            // 로그인 검증 생략하고 바로 메인 페이지로 이동
            Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()
            initializeMainPage() // 메인 페이지 초기화
        }

        // 회원가입 버튼 클릭 이벤트 설정
        createAccountButton.setOnClickListener {
            // 회원가입 화면으로 이동
            startActivity(Intent(this, RegisterActivity::class.java))
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

    // 레시피 상세 정보 표시
    private fun showRecipeDetails(recipe: Recipe) {
        val intent = Intent(this, RecipeDetailsActivity::class.java).apply {
            putExtra("RECIPE_TITLE", recipe.title)
            putExtra("MAIN_INGREDIENTS", recipe.mainIngredients)
            putExtra("RECIPE_URL", recipe.recipeUrl)
        }
        startActivity(intent)
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

    private fun initializeMainPage() {
        // 메인 레이아웃으로 변경
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
}
