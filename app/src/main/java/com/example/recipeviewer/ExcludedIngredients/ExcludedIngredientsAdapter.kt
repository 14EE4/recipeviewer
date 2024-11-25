package com.example.recipeviewer.ExcludedIngredients

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView

import com.example.recipeviewer.R

class ExcludedIngredientsAdapter(private var excludedIngredientsList: MutableList<String>) :
    RecyclerView.Adapter<ExcludedIngredientsAdapter.ViewHolder>() {



    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.excludedIngredientTextView)
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
        notifyDataSetChanged() // 또는 notifyItemRangeInserted() 등을 사용하여 변경된 부분만 업데이트
    }



}