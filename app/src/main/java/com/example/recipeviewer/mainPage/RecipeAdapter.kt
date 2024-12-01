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
 * 레시피 이름 클릭하면 RecipeDetailsActivity로 이동
 *
 * @author 노평주
 */
class RecipeAdapter(
    private var recipes: List<Recipe>,
    private val itemClick: (Recipe) -> Unit // 레시피 클릭 시 호출되는 함수(RecipeDetailsActivity)
) : RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>() {

    // RecyclerView의 각 항목을 나타내는 뷰 홀더
    inner class RecipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.recipeTitle)

        fun bind(recipe: Recipe) {
            titleTextView.text = recipe.title // 레시피 제목 설정

            itemView.setOnClickListener {
                itemClick(recipe) // 클릭 시 레시피 정보 전달
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recipe_item, parent, false) // 레이아웃 파일 사용
        return RecipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.bind(recipe) // 레시피 바인딩
    }
    
    override fun getItemCount(): Int {
        return recipes.size // 레시피 목록 크기 반환
    }

    // 재료 기반 검색 시 레시피 목록 업데이트
    fun updateData(newRecipes: MutableList<Recipe>) {
        recipes = newRecipes
        notifyDataSetChanged() // 데이터가 변경되었음을 어댑터에 알림
    }
}






