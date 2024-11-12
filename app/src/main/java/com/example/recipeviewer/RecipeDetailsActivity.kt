// RecipeDetailsActivity.kt
package com.example.recipeviewer

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.recipeviewer.R
import com.example.recipeviewer.WebViewActivity

class RecipeDetailsActivity : AppCompatActivity() {

    private lateinit var titleTextView: TextView
    private lateinit var ingredientsTextView: TextView
    private lateinit var urlButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_details) // activity_recipe_details.xml을 사용



        // Intent로 전달된 데이터를 수신
        val title = intent.getStringExtra("title")
        val mainIngredients = intent.getStringExtra("mainIngredients")
        val subIngredients = intent.getStringExtra("subIngredients")
        val alternativeIngredients = intent.getStringExtra("alternativeIngredients")
        val recipeUrl = intent.getStringExtra("recipeUrl")

        // TextView에 데이터 설정
        findViewById<TextView>(R.id.titleTextView).text = title
        findViewById<TextView>(R.id.mainIngredientsTextView).text = "주재료: $mainIngredients"
        findViewById<TextView>(R.id.subIngredientsTextView).text = "부재료: $subIngredients"
        findViewById<TextView>(R.id.alternativeIngredientsTextView).text = "대체재료: $alternativeIngredients"

        // URL 열기 버튼을 설정
        findViewById<Button>(R.id.openUrlButton).setOnClickListener {
            val intent = Intent(this, WebViewActivity::class.java).apply {
                putExtra("URL", recipeUrl)
            }
            startActivity(intent)
        }
    }
}
