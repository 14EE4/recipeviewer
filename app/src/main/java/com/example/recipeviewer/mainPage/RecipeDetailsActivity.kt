
package com.example.recipeviewer.mainPage

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.TableRow
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.text
import androidx.core.view.setMargins
import com.example.recipeviewer.R
import com.example.recipeviewer.helpers.*
import com.example.recipeviewer.models.Ingredient
import com.google.firebase.auth.FirebaseAuth


/**
 * MainPageActivity에서 레시피를 클릭했을 때 레시피 재료와 url버튼으로 레시피를 볼 수 있음(WebViewActivity)
 *
 * @author 노평주
 */
class RecipeDetailsActivity : AppCompatActivity() {

    private lateinit var titleTextView: TextView
    private lateinit var ingredientsTextView: TextView
    private lateinit var urlButton: Button
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var auth: FirebaseAuth
    private lateinit var ingredientTable: TableLayout
    private lateinit var recipeIngredients: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_details) // activity_recipe_details.xml을 사용


        val recipeId = intent.getIntExtra("recipeId", -1) // 레시피 ID 가져오기

        // DatabaseHelper 초기화
        databaseHelper = DatabaseHelper(this)

        // 레시피 객체 가져오기
        val recipe = databaseHelper.readRecipeById(recipeId) ?: run {
            // 레시피를 찾을 수 없는 경우 처리
            // 예: 오류 메시지 표시 후 액티비티 종료
            Toast.makeText(this, "레시피를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val recipeUrl = recipe.recipeUrl

        // TextView에 데이터 설정
        findViewById<TextView>(R.id.titleTextView).text = recipe.title
        findViewById<TextView>(R.id.mainIngredientsTextView).text = "주재료: ${recipe.mainIngredients}"
        findViewById<TextView>(R.id.subIngredientsTextView).text = "부재료: ${recipe.subIngredients}"
        findViewById<TextView>(R.id.alternativeIngredientsTextView).text = "대체재료: ${recipe.alternativeIngredients}"


        // FirebaseAuth 인스턴스 초기화
        auth = FirebaseAuth.getInstance()

        // 현재 사용자 가져오기

        val userId: String = auth.currentUser?.uid ?: ""

        // 레시피 재료 가져오기
        val recipeIngredients = IngredientHelper.parseAllIngredients(recipe) // recipe는 Intent에서 전달된 레시피 객체


        // 표 생성 및 데이터 채우기
        val ingredientTable: LinearLayout = findViewById(R.id.ingredientTable) // LinearLayout으로 변경

        databaseHelper.readIngredients(userId) { userIngredients: List<Ingredient> ->
            // 레시피 재료 가져오기 (콜백 내부로 이동)
            val mainIngredients = IngredientHelper.parseIngredients(recipe.mainIngredients, extractName = false)
            val subIngredients = IngredientHelper.parseIngredients(recipe.subIngredients, extractName = false)
            val alternativeIngredients = IngredientHelper.parseIngredients(recipe.alternativeIngredients, extractName = false)

            // 각 테이블 생성 및 추가
            ingredientTable.addView(createIngredientTable("주재료", mainIngredients, userIngredients))
            ingredientTable.addView(createIngredientTable("부재료", subIngredients, userIngredients))
            ingredientTable.addView(createIngredientTable("대체재료", alternativeIngredients, userIngredients))
        }

        // URL 열기 버튼을 설정
        findViewById<Button>(R.id.openUrlButton).setOnClickListener {
            val intent = Intent(this, WebViewActivity::class.java).apply {
                putExtra("URL", recipeUrl)
            }
            startActivity(intent)
        }
    }




    // 테이블 생성 함수 (수정)
    private fun createIngredientTable(
        title: String,
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

        // 테이블 제목 추가
        val titleTextView = TextView(this)
        titleTextView.text = title
        titleTextView.setPadding(16, 16, 16, 16)

        // titleTextView를 TableRow에 추가
        val titleRow = TableRow(this)
        titleRow.addView(titleTextView)
        tableLayout.addView(titleRow) // titleRow를 tableLayout에 추가

        // 재료 추가
        addIngredientsToTable(tableLayout, recipeIngredients, userIngredients)

        return tableLayout
    }

    // 테이블에 재료 추가 함수 (수정)
    // addIngredientsToTable 함수 수정
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
            }
            tableRow.addView(recipeIngredientTextView)

            val userIngredientTextView = TextView(this).apply {
                val matchingUserIngredient = userIngredients.find { recipeIngredient.contains(it.name) }
                text = matchingUserIngredient?.name ?: ""
                layoutParams = TableRow.LayoutParams(
                    0,
                    TableRow.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }
            tableRow.addView(userIngredientTextView)

            tableLayout.addView(tableRow)
        }
    }
}
