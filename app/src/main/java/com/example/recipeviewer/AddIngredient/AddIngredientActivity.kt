package com.example.recipeviewer.AddIngredient

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recipeviewer.R
import com.example.recipeviewer.helpers.DatabaseHelper
import com.example.recipeviewer.models.Ingredient
import com.example.recipeviewer.helpers.VoiceSearchHelper
import java.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference

class AddIngredientActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var ingredientAdapter: IngredientAdapter
    private lateinit var ingredientList: MutableList<Ingredient>

    private lateinit var voiceSearchHelper: VoiceSearchHelper // 음성인식
    private lateinit var ingredientNameEditText: EditText
    private lateinit var ingredientQuantityEditText: EditText
    private lateinit var ingredientUnitSpinner: Spinner
    private lateinit var auth: FirebaseAuth
    private lateinit var userId: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_ingredient)

        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser?.uid ?: ""
        databaseHelper = DatabaseHelper(this)

        // ingredientList 초기화
        ingredientList = mutableListOf()

        // 뷰 초기화
        ingredientNameEditText = findViewById(R.id.editTextIngredientName)
        ingredientQuantityEditText = findViewById(R.id.editTextIngredientQuantity)
        ingredientUnitSpinner = findViewById(R.id.spinnerIngredientUnit)
        val ingredientExpiryDateEditText: EditText = findViewById(R.id.editTextIngredientExpiryDate)
        val addButton: Button = findViewById(R.id.buttonAddIngredient)

        val clearButton: Button = findViewById(R.id.buttonClearIngredients)

        // Spinner에 사용할 데이터 리스트 생성
        val unitList = mutableListOf<String>()
        unitList.addAll(resources.getStringArray(R.array.units_array).toList()) // 기존 단위 추가

        // ArrayAdapter 생성 (수정 가능한 리스트 사용)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, unitList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Spinner에 ArrayAdapter 설정
        ingredientUnitSpinner.adapter = adapter

        // VoiceSearchHelper 초기화
        voiceSearchHelper = VoiceSearchHelper(this) { spokenText ->
            parseAndFillIngredientFields(spokenText, ingredientNameEditText, ingredientQuantityEditText, ingredientUnitSpinner)
        }

        // 음성 검색 버튼 클릭 리스너
        findViewById<Button>(R.id.buttonVoiceInput).setOnClickListener {
            voiceSearchHelper.startVoiceRecognition()
        }

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
        databaseHelper.readIngredients(userId) { ingredients ->
            ingredientList = ingredients.toMutableList()
            ingredientAdapter = IngredientAdapter(ingredientList, this)
            recyclerView.adapter = ingredientAdapter
        }

        // IngredientAdapter 초기화 및 클릭 리스너 설정
        ingredientAdapter = IngredientAdapter(ingredientList, this)



        recyclerView.adapter = ingredientAdapter



        addButton.setOnClickListener {
            val ingredientName = ingredientNameEditText.text.toString()
            val ingredientQuantity = ingredientQuantityEditText.text.toString().toIntOrNull() ?: 0
            val ingredientUnit = ingredientUnitSpinner.selectedItem.toString()
            val ingredientExpiryDate = ingredientExpiryDateEditText.text.toString()

            if (ingredientName.isNotEmpty() && ingredientUnit.isNotEmpty() && ingredientExpiryDate.isNotEmpty()) {
                // Firestore에 데이터 추가 시도 로그
                Log.d("AddIngredientActivity", "Attempting to add ingredient to Firestore")

                // 재료를 Firestore에 추가
                databaseHelper.addIngredient(userId, ingredientName, ingredientQuantity, ingredientUnit, ingredientExpiryDate)
                    .addOnSuccessListener { documentReference ->
                        // Firestore에 데이터 추가 성공 로그
                        Log.d("AddIngredientActivity", "Ingredient added to Firestore with ID: ${documentReference.id}")

                        Toast.makeText(this, "재료가 추가되었습니다.", Toast.LENGTH_SHORT).show()

                        // 새 재료 추가
                        val newIngredient = Ingredient(
                            id = documentReference.id,
                            name = ingredientName,
                            quantity = ingredientQuantity,
                            unit = ingredientUnit,
                            expiryDate = ingredientExpiryDate
                        )

                        // 재료 리스트에 추가 시도 로그
                        Log.d("AddIngredientActivity", "Attempting to add ingredient to list")

                        ingredientList.add(newIngredient)

                        // 리스트에 추가된 후 크기 로그
                        Log.d("AddIngredientActivity", "Ingredient list size after addition: ${ingredientList.size}")

                        // UI 스레드에서 RecyclerView 업데이트
                        runOnUiThread {
                            ingredientAdapter.notifyItemInserted(ingredientList.size - 1)
                            recyclerView.scrollToPosition(ingredientList.size - 1)

                            // 입력 필드 초기화
                            ingredientNameEditText.text.clear()
                            ingredientQuantityEditText.text.clear()
                            ingredientUnitSpinner.setSelection(0)
                            ingredientExpiryDateEditText.text.clear()
                        }
                    }
                    .addOnFailureListener { e ->
                        // Firestore에 데이터 추가 실패 로그
                        Log.e("AddIngredientActivity", "Failed to add ingredient to Firestore", e)
                        Toast.makeText(this, "재료 추가에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "모든 필드를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }


        clearButton.setOnClickListener {
            // 재료 목록 초기화
            databaseHelper.clearIngredients(userId)
            Toast.makeText(this, "재료 목록이 초기화되었습니다.", Toast.LENGTH_SHORT).show()
            // 재료 목록 업데이트
            ingredientList.clear()
            ingredientAdapter.updateData(ingredientList)

        }
    }



    override fun onResume() {
        super.onResume()
        // 재료 목록을 Firestore에서 다시 불러오기
        databaseHelper.readIngredients(userId) { ingredients ->
            ingredientList = ingredients.toMutableList()
            ingredientAdapter.updateData(ingredientList)
        }
    }

    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        voiceSearchHelper.handlePermissionsResult(requestCode, grantResults)
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceSearchHelper.release()
    }

    // 음성 인식 결과 파싱 및 입력 필드 설정
    private fun parseAndFillIngredientFields(spokenText: String, nameEditText: EditText, quantityEditText: EditText, unitSpinner: Spinner) {
        val regex = Regex("(.*?)\\s*(\\d+)(.*)") // 이름, 분량, 단위를 추출하는 정규식 (공백 없는 경우)
        val matchResult = regex.find(spokenText)


        if (matchResult != null) {
            val (name, quantity, unit) = matchResult.destructured

            nameEditText.setText(name.trim())
            quantityEditText.setText(quantity.trim())

            // 단위 설정 (Spinner에서 해당 단위를 찾아 선택)
            val adapter = ingredientUnitSpinner.adapter as ArrayAdapter<String> // ArrayAdapter로 캐스팅
            var unitFound = false
            for (i in 0 until adapter.count) {
                if (adapter.getItem(i) == unit.trim()) {
                    ingredientUnitSpinner.setSelection(i)
                    unitFound = true
                    break
                }
            }

            // 단위를 찾지 못한 경우 Spinner에 단위 추가
            if (!unitFound) {
                adapter.add(unit.trim())
                ingredientUnitSpinner.setSelection(adapter.count - 1) // 새로 추가된 단위 선택
            }
        } else {
            // 음성 인식 원본 텍스트를 포함한 오류 메시지 출력
            val errorMessage = "음성 인식 결과를 파싱할 수 없습니다.\n원본 텍스트: $spokenText"
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }
}