package com.example.recipeviewer.mainPage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.recipeviewer.R
import com.example.recipeviewer.models.Recipe

/**
 * mainpageactivity에서 recyclerview에 레시피 목록을 표시하는 클래스
 * 
 * @author 노평주
 */
class RecipeAdapter(
    private var recipes: List<Recipe>,
    private val itemClick: (Recipe) -> Unit // 레시피 클릭 시 호출되는 함수
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    // ViewHolder 클래스
    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.recipeTitle)

        fun bind(recipe: Recipe) {
            titleTextView.text = recipe.title // 레시피 제목 설정

            itemView.setOnClickListener {
                itemClick(recipe) // 클릭 시 레시피 정보 전달
            }
        }
    }

    // onCreateViewHolder 메소드
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recipe_item, parent, false) // 레이아웃 파일 사용
        return RecipeViewHolder(view)
    }

    // onBindViewHolder 메소드
    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.bind(recipe) // 레시피 바인딩
    }

    // getItemCount 메소드
    override fun getItemCount(): Int {
        return recipes.size // 레시피 목록 크기 반환
    }

    // 레시피 목록 업데이트 메소드
    fun updateData(newRecipes: MutableList<Recipe>) {
        recipes = newRecipes
        notifyDataSetChanged() // 데이터가 변경되었음을 어댑터에 알림
    }
}






