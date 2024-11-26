package com.example.recipeviewer.ExcludedIngredients

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.recipeviewer.R
import com.example.recipeviewer.helpers.DatabaseHelper
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast



class ExcludedIngredientsAdapter(private var excludedIngredientsList: MutableList<String>) :
    RecyclerView.Adapter<ExcludedIngredientsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.excludedIngredientTextView)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
        private lateinit var databaseHelper: DatabaseHelper
        private lateinit var auth: FirebaseAuth
        private lateinit var userId: String

        init {
            auth = FirebaseAuth.getInstance()
            userId = auth.currentUser?.uid ?: ""
            databaseHelper = DatabaseHelper(itemView.context)
            deleteButton.setOnClickListener {
                val position = bindingAdapterPosition // bindingAdapterPosition 사용
                if (position != RecyclerView.NO_POSITION) {
                    val ingredient = (bindingAdapter as ExcludedIngredientsAdapter).excludedIngredientsList[position]

                    val builder = AlertDialog.Builder(itemView.context)
                    builder.setTitle("제외 재료 삭제")
                    builder.setMessage("정말로 '${ingredient}'를 삭제하시겠습니까?")

                    builder.setPositiveButton("확인") { dialog, which ->
                        // 데이터베이스에서 삭제
                        val isDeleted = databaseHelper.deleteExcludedIngredient(userId, ingredient)

                        if (isDeleted) {
                            // RecyclerView 업데이트
                            (bindingAdapter as ExcludedIngredientsAdapter).excludedIngredientsList.removeAt(position)
                            (bindingAdapter as ExcludedIngredientsAdapter).notifyItemRemoved(position) // notifyItemRemoved 호출
                        } else {
                            // 삭제 실패 처리 (예: Toast 메시지 표시)
                            Toast.makeText(itemView.context, "삭제 실패", Toast.LENGTH_SHORT).show()
                        }
                    }

                    builder.setNegativeButton("취소", null)

                    builder.show()


                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_excluded_ingredient, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = excludedIngredientsList[position]
    }

    override fun getItemCount(): Int {
        return excludedIngredientsList.size
    }

    fun updateData(newExcludedIngredientsList: MutableList<String>) {
        excludedIngredientsList.clear()
        excludedIngredientsList.addAll(newExcludedIngredientsList)
        notifyDataSetChanged()
    }
}