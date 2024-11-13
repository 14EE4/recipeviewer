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

        setContentView(R.layout.acyivity_loginpage)

        // XML에 정의된 뷰 참조
        val loginTitle: TextView = findViewById(R.id.textView)
        val welcomeMessage: TextView = findViewById(R.id.textView2)
        val emailEditText: EditText = findViewById(R.id.editTextText)
        val passwordEditText: EditText = findViewById(R.id.editTextTextPassword2)
        val passwordLabel: TextView = findViewById(R.id.textView3)
        val emailLabel: TextView = findViewById(R.id.textView4)
        val loginButton: Button = findViewById(R.id.button)
        val createAccountButton: Button = findViewById(R.id.button2)

        // 텍스트 또는 속성을 설정할 수 있습니다.
        loginTitle.text = "Login"
        welcomeMessage.text = "Welcome back to the app"
        emailEditText.setText("Hello@email.com")

        // 버튼 클릭 이벤트 설정
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()




            // 테스트용으로 바로 initializeMainPage() 호출
            initializeMainPage()
        }


        createAccountButton.setOnClickListener {
            // 계정 생성 화면으로 이동
        }




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

    private fun initializeMainPage() {
        // 메인 레이아웃으로 변경
        setContentView(R.layout.activity_main)

        // VoiceSearchHelper 초기화
        voiceSearchHelper = VoiceSearchHelper(this) { query ->
            searchRecipes(query) // 검색 메서드 호출
        }

        // 음성 검색 버튼 설정
        findViewById<Button>(R.id.voiceSearchButton).setOnClickListener {
            voiceSearchHelper.startVoiceRecognition()
        }

        // RecyclerView 초기화
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // DatabaseHelper 초기화 및 레시피 데이터 가져오기
        databaseHelper = DatabaseHelper(this)
        recipeList = databaseHelper.readAllData().toMutableList() // DB에서 레시피 불러오기

        // RecipeAdapter 초기화 및 설정
        recipeAdapter = RecipeAdapter(recipeList) { recipe ->
            // 각 레시피 아이템 클릭 시 상세 정보 화면으로 이동
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

