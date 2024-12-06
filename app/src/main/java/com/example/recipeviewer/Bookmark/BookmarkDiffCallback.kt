package com.example.recipeviewer.Bookmark

import androidx.recyclerview.widget.DiffUtil
import com.example.recipeviewer.models.Bookmark

class BookmarkDiffCallback(
    private val oldList: List<Bookmark>,
    private val newList: List<Bookmark>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}

