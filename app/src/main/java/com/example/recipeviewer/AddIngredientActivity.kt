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
    private lateinit var excludedIngredientList: MutableList<Ingredient> // 제외된 재료 목록
    private lateinit var excludedRecyclerView: RecyclerView // 제외 재료 목록을 위한 RecyclerView
    private lateinit var excludedAdapter: IngredientAdapter // 제외 재료용 어댑터

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_ingredient)

        databaseHelper = DatabaseHelper(this)

        val ingredientNameEditText: EditText = findViewById(R.id.editTextIngredientName)
        val ingredientQuantityEditText: EditText = findViewById(R.id.editTextIngredientQuantity)
        val ingredientUnitSpinner: Spinner = findViewById(R.id.spinnerIngredientUnit)
        val ingredientExpiryDateEditText: EditText = findViewById(R.id.editTextIngredientExpiryDate)
        val addButton: Button = findViewById(R.id.buttonAddIngredient)
        val excludeButton: Button = findViewById(R.id.button4) // 제외 추가 버튼
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

        // 제외 재료 목록 RecyclerView 초기화
        excludedRecyclerView = findViewById(R.id.recyclerViewExcluded)
        excludedRecyclerView.layoutManager = LinearLayoutManager(this)

        // 재료 불러오기
        ingredientList = databaseHelper.readIngredients().toMutableList()
        excludedIngredientList = mutableListOf() // 제외된 재료 초기화

        // IngredientAdapter 초기화 및 클릭 리스너 설정
        ingredientAdapter = IngredientAdapter(ingredientList, this)
        // 어댑터 초기화
        ingredientAdapter = IngredientAdapter(ingredientList)
        excludedAdapter = IngredientAdapter(excludedIngredientList) // 제외된 재료 어댑터

        recyclerView.adapter = ingredientAdapter
        excludedRecyclerView.adapter = excludedAdapter // 제외된 재료 목록 어댑터 설정
        ingredientAdapter.setOnIngredientClickListener(object : IngredientAdapter.OnIngredientClickListener {
            override fun onEditIngredient(ingredient: Ingredient) {
                // 재료 수정 다이얼로그 또는 액티비티 표시
                // 예: showEditIngredientDialog(ingredient)
            }

            override fun onDeleteIngredient(ingredient: Ingredient) {
                // 재료 삭제 확인 다이얼로그 표시 후 삭제
                // 예: showDeleteConfirmationDialog(ingredient)
            }
        })

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

                    // 새 재료 추가
                    val newIngredient = Ingredient(0, ingredientName, ingredientQuantity, ingredientUnit, ingredientExpiryDate)
                    ingredientList.add(newIngredient)

                    // RecyclerView 업데이트
                    val position = ingredientList.size - 1
                    ingredientAdapter.notifyItemInserted(position)

                    // RecyclerView 자동 스크롤
                    recyclerView.scrollToPosition(position)

                    // 입력 필드 초기화
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

        excludeButton.setOnClickListener {
            val ingredientName = ingredientNameEditText.text.toString()
            val ingredientQuantity = ingredientQuantityEditText.text.toString().toIntOrNull() ?: 0
            val ingredientUnit = ingredientUnitSpinner.selectedItem.toString()
            val ingredientExpiryDate = ingredientExpiryDateEditText.text.toString()

            if (ingredientName.isNotEmpty() && ingredientUnit.isNotEmpty() && ingredientExpiryDate.isNotEmpty()) {
                // 제외 재료 목록에 추가
                val excludedIngredient = Ingredient(0, ingredientName, ingredientQuantity, ingredientUnit, ingredientExpiryDate)
                excludedIngredientList.add(excludedIngredient)
                excludedAdapter.updateData(excludedIngredientList)
                Toast.makeText(this, "재료가 제외 목록에 추가되었습니다.", Toast.LENGTH_SHORT).show()

                // 입력 필드 초기화
                ingredientNameEditText.text.clear()
                ingredientQuantityEditText.text.clear()
                ingredientUnitSpinner.setSelection(0)
                ingredientExpiryDateEditText.text.clear()
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
            excludedIngredientList.clear()
            excludedAdapter.updateData(excludedIngredientList)
        }
    }

    override fun onResume() {
        super.onResume()
        // 재료 목록을 데이터베이스에서 다시 불러오기
        ingredientList = databaseHelper.readIngredients().toMutableList()
        ingredientAdapter.updateData(ingredientList)

    }
}