package com.example.recipeviewer.AddIngredient

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.app.DatePickerDialog
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.app.AlertDialog
import kotlin.text.toIntOrNull
import java.util.Calendar
import com.example.recipeviewer.models.Ingredient
import com.example.recipeviewer.helpers.DatabaseHelper
import com.example.recipeviewer.R
import com.google.firebase.auth.FirebaseAuth

/**
 * AddIngredientActivity에서 재료를 보여주는 RecyclerView의 Adapter
 * 재료 수정, 삭제 기능 구현
 * FirebaseAuth로 사용자별 재료를 수정, 삭제 가능
 * 
 * @author 노평주
 */
class IngredientAdapter(private var ingredientList: MutableList<Ingredient>, private val context: Context) : RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder>() {

    inner class IngredientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ingredientName: TextView = itemView.findViewById(R.id.textViewIngredientName)
        val ingredientQuantity: TextView = itemView.findViewById(R.id.textViewIngredientQuantity)
        val ingredientUnit: TextView = itemView.findViewById(R.id.textViewIngredientUnit)
        val ingredientExpiryDate: TextView = itemView.findViewById(R.id.textViewIngredientExpiryDate)
        val editButton: Button = itemView.findViewById(R.id.editButton)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
        private lateinit var databaseHelper: DatabaseHelper
        private val context: Context = itemView.context
        private lateinit var auth: FirebaseAuth
        private lateinit var userId: String

        init {
            auth = FirebaseAuth.getInstance()
            userId = auth.currentUser?.uid ?: ""
            databaseHelper = DatabaseHelper(context) // context를 사용하여 DatabaseHelper 초기화
            //개별 재료 수정 버튼 클릭 리스너
            editButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val ingredient = ingredientList[position]
                    
                    //수정버튼 누르면 나오는 다이얼로그
                    val builder = AlertDialog.Builder(context)
                    builder.setTitle("재료 수정")

                    val view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_ingredient, null)//재료 수정 다이얼로그
                    val nameEditText = view.findViewById<EditText>(R.id.edit_text_name)//재료이름
                    val quantityEditText = view.findViewById<EditText>(R.id.edit_text_quantity)//재료분량
                    val unitSpinner = view.findViewById<Spinner>(R.id.spinner_unit) //재료단위
                    val expiryDateEditText = view.findViewById<EditText>(R.id.edit_text_expiry_date)//유통기한

                    nameEditText.setText(ingredient.name)
                    quantityEditText.setText(ingredient.quantity.toString())

                    expiryDateEditText.setText(ingredient.expiryDate)

                    // unitSpinner 초기값 설정
                    val adapter = ArrayAdapter.createFromResource(
                        context,
                        R.array.units_array,
                        android.R.layout.simple_spinner_item
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    unitSpinner.adapter = adapter
                    val unitIndex = adapter.getPosition(ingredient.unit)
                    unitSpinner.setSelection(unitIndex)

                    //유통기한 텍스트 클릭 리스너
                    expiryDateEditText.setOnClickListener {
                        val calendar = Calendar.getInstance()
                        val year = calendar.get(Calendar.YEAR)
                        val month = calendar.get(Calendar.MONTH)
                        val day = calendar.get(Calendar.DAY_OF_MONTH)

                        val datePickerDialog = DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val selectedDate = "$year-${month + 1}-$dayOfMonth"
                                expiryDateEditText.setText(selectedDate)
                            },
                            year,
                            month,
                            day
                        )
                        datePickerDialog.show()
                    }
                    builder.setView(view)

                    builder.setPositiveButton("확인") { dialog, which ->
                        val newName = nameEditText.text.toString()
                        val newQuantity = quantityEditText.text.toString().toIntOrNull() ?: 0
                        val newUnit = unitSpinner.selectedItem.toString()
                        val newExpiryDate = expiryDateEditText.text.toString()

                        // 데이터베이스에서 재료 업데이트
                        databaseHelper.updateIngredient(userId, ingredient.id, newName, newQuantity, newUnit, newExpiryDate) // ingredient.id (Firestore 문서 ID) 전달
                        // 리스트에서 재료 업데이트 및 어댑터 알림
                        // 기존 Ingredient 객체를 수정합니다.
                        ingredient.name = newName
                        ingredient.quantity = newQuantity
                        ingredient.unit = newUnit
                        ingredient.expiryDate = newExpiryDate

                        notifyItemChanged(position) // 변경된 항목만 업데이트
                    }
                    builder.setNegativeButton("취소", null)

                    builder.show()
                }
            }
            //삭제버튼
            deleteButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val ingredient = ingredientList[position]

                    val builder = AlertDialog.Builder(context)
                    builder.setTitle("재료 삭제")
                    builder.setMessage("정말로 삭제하시겠습니까?")

                    builder.setPositiveButton("확인") { dialog, which ->
                        // 데이터베이스에서 삭제
                        databaseHelper.deleteIngredient(userId, ingredient.id)

                        // RecyclerView 업데이트
                        ingredientList.removeAt(position)
                        notifyItemRemoved(position)
                    }
                    builder.setNegativeButton("취소", null)

                    builder.show()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ingredient, parent, false)
        return IngredientViewHolder(view)
    }
    //재료 정보 표시
    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        val ingredient = ingredientList[position]
        holder.ingredientName.text = ingredient.name
        holder.ingredientQuantity.text = "분량: ${ingredient.quantity}"
        holder.ingredientUnit.text = "단위: ${ingredient.unit}"
        holder.ingredientExpiryDate.text = "유통기한: ${ingredient.expiryDate}"
    }

    override fun getItemCount(): Int {
        return ingredientList.size
    }

    fun updateData(newIngredientList: MutableList<Ingredient>) {
        ingredientList = newIngredientList
        notifyDataSetChanged()
    }
}