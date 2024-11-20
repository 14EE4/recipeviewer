package com.example.recipeviewer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.app.DatePickerDialog
import androidx.compose.ui.semantics.setText
import androidx.compose.ui.semantics.text
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.app.AlertDialog
import com.example.recipeviewer.models.Ingredient
import kotlin.text.toIntOrNull
import com.example.recipeviewer.helpers.DatabaseHelper
import android.widget.*
import androidx.compose.material3.DatePickerDialog
import java.util.Calendar

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

        init {
            databaseHelper = DatabaseHelper(context) // context를 사용하여 DatabaseHelper 초기화
            editButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val ingredient = ingredientList[position]

                    val builder = AlertDialog.Builder(context)
                    builder.setTitle("재료 수정")

                    val view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_ingredient, null)
                    val nameEditText = view.findViewById<EditText>(R.id.edit_text_name)
                    val quantityEditText = view.findViewById<EditText>(R.id.edit_text_quantity)
                    val unitSpinner = view.findViewById<Spinner>(R.id.spinner_unit) // Spinner로 변경
                    val expiryDateEditText = view.findViewById<EditText>(R.id.edit_text_expiry_date)

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
                        databaseHelper.updateIngredient(ingredient.id, newName, newQuantity, newUnit, newExpiryDate)

                        // 리스트에서 재료 업데이트 및 어댑터 알림
                        val updatedIngredient = ingredient.copy(
                            name = newName,
                            quantity = newQuantity,
                            unit = newUnit,
                            expiryDate = newExpiryDate
                        )
                        ingredientList[position] = updatedIngredient
                        notifyItemChanged(position)
                    }

                    builder.setNegativeButton("취소", null)

                    builder.show()



                }
            }

            deleteButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val ingredient = ingredientList[position]

                    val builder = AlertDialog.Builder(context)
                    builder.setTitle("재료 삭제")
                    builder.setMessage("정말로 삭제하시겠습니까?")

                    builder.setPositiveButton("확인") { dialog, which ->
                        // 데이터베이스에서 삭제
                        databaseHelper.deleteIngredient(ingredient.id)

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
    interface OnIngredientClickListener {
        fun onEditIngredient(ingredient: Ingredient)
        fun onDeleteIngredient(ingredient: Ingredient)
    }

    private lateinit var listener: OnIngredientClickListener

    fun setOnIngredientClickListener(listener: OnIngredientClickListener) {
        this.listener = listener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ingredient, parent, false)
        return IngredientViewHolder(view)
    }

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