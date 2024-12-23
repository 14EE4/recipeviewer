package com.example.recipeviewer.mainPage

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.TableRow
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.recipeviewer.R
import com.example.recipeviewer.helpers.*
import com.example.recipeviewer.models.Ingredient
import com.google.firebase.auth.FirebaseAuth

/**
 * MainPageActivity에서 레시피를 클릭했을 때 레시피 재료와 url버튼으로 레시피를 볼 수 있음(WebViewActivity)
 * 필요한 재료와 보유한 재료를 표로 비교해서 보여줌
 *
 * @author 노평주
 */
class RecipeDetailsActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_details)

        // FirebaseAuth 인스턴스 초기화
        auth = FirebaseAuth.getInstance()

        // 현재 사용자 가져오기
        val userId: String = auth.currentUser?.uid ?: ""

        val recipeId = intent.getIntExtra("recipeId", -1) // 레시피 ID 가져오기

        // DatabaseHelper 초기화
        databaseHelper = DatabaseHelper(this)

        // 레시피 객체 가져오기
        val recipe = databaseHelper.readRecipeById(recipeId)
        if(recipe == null){
            Toast.makeText(this, "레시피를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 사용자 재료 가져오기
        databaseHelper.readIngredients(userId) { userIngredients: List<Ingredient> ->
            // 레시피 재료 파싱
            val mainIngredients = IngredientHelper.parseIngredients(recipe.mainIngredients, extractName = false)
            val subIngredients = IngredientHelper.parseIngredients(recipe.subIngredients, extractName = false)
            val alternativeIngredients = IngredientHelper.parseIngredients(recipe.alternativeIngredients, extractName = false)

            // 각 테이블 생성 및 추가 (파싱된 레시피 재료, userIngredients 비교)
            val mainIngredientTable: TableLayout = findViewById(R.id.mainIngredientTable)
            mainIngredientTable.addView(createIngredientTable(mainIngredients, userIngredients))

            val subIngredientTable: TableLayout = findViewById(R.id.subIngredientTable)
            subIngredientTable.addView(createIngredientTable(subIngredients, userIngredients))

            val alternativeIngredientTable: TableLayout = findViewById(R.id.alternativeIngredientTable)
            alternativeIngredientTable.addView(createIngredientTable(alternativeIngredients, userIngredients))
        }

        val recipeUrl = recipe.recipeUrl

        // TextView 제목
        findViewById<TextView>(R.id.titleTextView).text = recipe.title
        
        // URL 열기 버튼
        findViewById<Button>(R.id.openUrlButton).setOnClickListener {
            val intent = Intent(this, WebViewActivity::class.java).apply {
                putExtra("URL", recipeUrl)
            }
            startActivity(intent)
        }
    }

    // 테이블 생성
    private fun createIngredientTable(
        recipeIngredients: List<String>,
        userIngredients: List<Ingredient>
    ): TableLayout {
        val tableLayout = TableLayout(this)
        tableLayout.layoutParams = LinearLayout.LayoutParams( // LinearLayout.LayoutParams으로 변경
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 0, 0, 16) // bottom margin 설정
        }
        tableLayout.setBackgroundResource(R.drawable.table_border) // 테이블 테두리 설정

        // 행과 열 구분선 추가
        tableLayout.setShowDividers(TableLayout.SHOW_DIVIDER_BEGINNING + TableLayout.SHOW_DIVIDER_MIDDLE + TableLayout.SHOW_DIVIDER_END)
        tableLayout.dividerDrawable = ContextCompat.getDrawable(this, R.drawable.table_divider) // 구분선 드로어블 설정
        // 열 이름 추가
        val headerRow = TableRow(this)
        val recipeIngredientHeader = TextView(this).apply {
            text = "필요한 재료"
            layoutParams = TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,
                1f
            )
        }
        headerRow.addView(recipeIngredientHeader)

        val userIngredientHeader = TextView(this).apply {
            text = "보유 재료"
            layoutParams = TableRow.LayoutParams(
                0,
                TableRow.LayoutParams.WRAP_CONTENT,
                1f
            )
        }
        headerRow.addView(userIngredientHeader)

        tableLayout.addView(headerRow) // 헤더 행 추가

        // 재료 추가
        addIngredientsToTable(tableLayout, recipeIngredients, userIngredients)

        return tableLayout
    }

    // 테이블에 재료 추가
    private fun addIngredientsToTable(
        tableLayout: TableLayout,
        recipeIngredients: List<String>,
        userIngredients: List<Ingredient>
    ) {
        for (recipeIngredient in recipeIngredients) {
            val tableRow = TableRow(this)

            val recipeIngredientTextView = TextView(this).apply {
                text = recipeIngredient
                layoutParams = TableRow.LayoutParams(
                    0,
                    TableRow.LayoutParams.WRAP_CONTENT,
                    1f
                )
                textSize = 20f
            }
            tableRow.addView(recipeIngredientTextView)
            //사용자 재료
            val userIngredientTextView = TextView(this).apply {
                val matchingUserIngredient = userIngredients.find { recipeIngredient.contains(it.name) }
                // 분량과 단위 추가
                text = if (matchingUserIngredient != null) {
                    "${matchingUserIngredient.name} (${matchingUserIngredient.quantity} ${matchingUserIngredient.unit})"
                } else {
                    ""
                }
                layoutParams = TableRow.LayoutParams(
                    0,
                    TableRow.LayoutParams.WRAP_CONTENT,
                    1f
                )
                textSize = 20f // 폰트 크기 설정 (예: 20sp)
            }
            tableRow.addView(userIngredientTextView)

            tableLayout.addView(tableRow)
        }
    }
}
