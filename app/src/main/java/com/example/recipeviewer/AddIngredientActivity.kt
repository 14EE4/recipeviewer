package com.example.recipeviewer

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recipeviewer.helpers.DatabaseHelper

class AddIngredientActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var ingredientAdapter: IngredientAdapter
    private lateinit var ingredientList: MutableList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_ingredient)

        databaseHelper = DatabaseHelper(this)

        val ingredientNameEditText: EditText = findViewById(R.id.editTextIngredientName)
        val addButton: Button = findViewById(R.id.buttonAddIngredient)

        // RecyclerView 초기화
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 재료 불러오기
        ingredientList = readIngredients().toMutableList()

        // 어댑터 초기화
        ingredientAdapter = IngredientAdapter(ingredientList)
        recyclerView.adapter = ingredientAdapter

        addButton.setOnClickListener {
            val ingredientName = ingredientNameEditText.text.toString()
            if (ingredientName.isNotEmpty()) {
                // 재료를 데이터베이스에 추가
                databaseHelper.addIngredient(ingredientName)
                Toast.makeText(this, "재료가 추가되었습니다.", Toast.LENGTH_SHORT).show()
                // 재료 목록 업데이트
                ingredientList.add(ingredientName)
                ingredientAdapter.updateData(ingredientList)
                ingredientNameEditText.text.clear()
            } else {
                Toast.makeText(this, "재료 이름을 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 재료 목록 읽기
    private fun readIngredients(): List<String> {
        val db = databaseHelper.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT name FROM ingredients", null)
        val ingredients = mutableListOf<String>()

        if (cursor.moveToFirst()) {
            do {
                ingredients.add(cursor.getString(cursor.getColumnIndex("name")))
            } while (cursor.moveToNext())
        }

        cursor.close()
        return ingredients
    }
}
