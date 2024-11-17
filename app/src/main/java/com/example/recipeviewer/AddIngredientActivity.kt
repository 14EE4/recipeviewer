package com.example.recipeviewer

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recipeviewer.helpers.DatabaseHelper
import com.example.recipeviewer.models.Ingredient
import java.util.*

class AddIngredientActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var ingredientAdapter: IngredientAdapter
    private lateinit var ingredientList: MutableList<Ingredient>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_ingredient)

        databaseHelper = DatabaseHelper(this)

        val ingredientNameEditText: EditText = findViewById(R.id.editTextIngredientName)
        val ingredientQuantityEditText: EditText = findViewById(R.id.editTextIngredientQuantity)
        val ingredientUnitSpinner: Spinner = findViewById(R.id.spinnerIngredientUnit)
        val ingredientExpiryDateEditText: EditText = findViewById(R.id.editTextIngredientExpiryDate)
        val addButton: Button = findViewById(R.id.buttonAddIngredient)
        val clearButton: Button = findViewById(R.id.buttonClearIngredients)

        // 날짜 선택기 설정
        ingredientExpiryDateEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
                ingredientExpiryDateEditText.setText("$selectedYear-${selectedMonth + 1}-$selectedDay")
            }, year, month, day)
            datePickerDialog.show()
        }

        // RecyclerView 초기화
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 재료 불러오기
        ingredientList = databaseHelper.readIngredients().toMutableList()

        // 어댑터 초기화
        ingredientAdapter = IngredientAdapter(ingredientList)
        recyclerView.adapter = ingredientAdapter

        addButton.setOnClickListener {
            val ingredientName = ingredientNameEditText.text.toString()
            val ingredientQuantity = ingredientQuantityEditText.text.toString().toIntOrNull() ?: 0
            val ingredientUnit = ingredientUnitSpinner.selectedItem.toString()
            val ingredientExpiryDate = ingredientExpiryDateEditText.text.toString()

            if (ingredientName.isNotEmpty() && ingredientUnit.isNotEmpty() && ingredientExpiryDate.isNotEmpty()) {
                // 재료를 데이터베이스에 추가
                val success = databaseHelper.addIngredient(ingredientName, ingredientQuantity, ingredientUnit, ingredientExpiryDate)
                if (success) {
                    Toast.makeText(this, "재료가 추가되었습니다.", Toast.LENGTH_SHORT).show()
                    // 재료 목록 업데이트
                    ingredientList.add(Ingredient(0, ingredientName, ingredientQuantity, ingredientUnit, ingredientExpiryDate))
                    ingredientAdapter.updateData(ingredientList)
                    ingredientNameEditText.text.clear()
                    ingredientQuantityEditText.text.clear()
                    ingredientUnitSpinner.setSelection(0)
                    ingredientExpiryDateEditText.text.clear()
                } else {
                    Toast.makeText(this, "재료 추가에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "모든 필드를 입력하세요.", Toast.LENGTH_SHORT).show()
            }

        }
        clearButton.setOnClickListener {
            // 재료 목록 초기화
            databaseHelper.clearIngredients()
            Toast.makeText(this, "재료 목록이 초기화되었습니다.", Toast.LENGTH_SHORT).show()
            // 재료 목록 업데이트
            ingredientList.clear()
            ingredientAdapter.updateData(ingredientList)
        }
    }
    override fun onResume() {
        super.onResume()
        // 재료 목록을 데이터베이스에서 다시 불러오기
        ingredientList = databaseHelper.readIngredients().toMutableList()
        ingredientAdapter.updateData(ingredientList)
    }
}