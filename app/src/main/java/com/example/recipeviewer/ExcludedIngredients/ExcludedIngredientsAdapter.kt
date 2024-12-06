package com.example.recipeviewer.ExcludedIngredients

import android.content.Context
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



class ExcludedIngredientsAdapter(
    //제외 재료 목록
    private var excludedIngredientsList: MutableList<String>,
    private val context: Context // Context 매개변수 추가
) : RecyclerView.Adapter<ExcludedIngredientsAdapter.ViewHolder>(){

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // 제외 재료 이름 텍스트뷰
        val textView: TextView = itemView.findViewById(R.id.excludedIngredientTextView)
        // 제외 재료 삭제 버튼
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
        private lateinit var databaseHelper: DatabaseHelper
        // Firebase 선언
        private lateinit var auth: FirebaseAuth
        // 현재 로그임 사용자 ID 저장
        private lateinit var userId: String

        init {
            auth = FirebaseAuth.getInstance()
            userId = auth.currentUser?.uid ?: ""
            databaseHelper = DatabaseHelper(itemView.context)

            // 삭제 버튼 기능
            deleteButton.setOnClickListener {
                // 현재 항목의 위치 가져오기
                val position = bindingAdapterPosition
                // 항목 위치가 유효하면
                if (position != RecyclerView.NO_POSITION) {
                    // 삭제할 재료 이름 가져오기
                    val ingredient = (bindingAdapter as ExcludedIngredientsAdapter).excludedIngredientsList[position]

                    val builder = AlertDialog.Builder(itemView.context)
                    builder.setTitle("제외 재료 삭제")
                    builder.setMessage("정말로 '${ingredient}'를 삭제하시겠습니까?")

                    // 확인 버튼
                    builder.setPositiveButton("확인") { dialog, which ->
                        // DatabaseHelper를 사용하여 제외 재료 삭제
                        databaseHelper.deleteExcludedIngredient(userId, ingredient) { isSuccessful -> // 콜백 함수 사용
                            // 삭제 성공 시
                            if (isSuccessful) {
                                // 제외 재료 목록에서 삭제
                                (bindingAdapter as ExcludedIngredientsAdapter).excludedIngredientsList.removeAt(position)
                                (bindingAdapter as ExcludedIngredientsAdapter).notifyItemRemoved(position)
                            } else {
                                Toast.makeText(itemView.context, "삭제 실패", Toast.LENGTH_SHORT).show()
                            }
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