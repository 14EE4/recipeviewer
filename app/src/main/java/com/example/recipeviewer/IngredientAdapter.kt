package com.example.recipeviewer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.recipeviewer.models.Ingredient

class IngredientAdapter(private var ingredientList: MutableList<Ingredient>) : RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder>() {

    class IngredientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ingredientName: TextView = itemView.findViewById(R.id.ingredientName)
        val ingredientQuantity: TextView = itemView.findViewById(R.id.ingredientQuantity)
        val ingredientUnit: TextView = itemView.findViewById(R.id.ingredientUnit)
        val ingredientExpiryDate: TextView = itemView.findViewById(R.id.ingredientExpiryDate)
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