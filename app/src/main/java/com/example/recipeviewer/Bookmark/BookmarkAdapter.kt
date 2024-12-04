package com.example.recipeviewer.Bookmark

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.recipeviewer.R
import com.example.recipeviewer.models.Bookmark
import com.example.recipeviewer.mainPage.RecipeDetailsActivity
import com.google.firebase.firestore.FirebaseFirestore

class BookmarkAdapter(
    private val bookmarks: MutableList<Bookmark>,
    private val db: FirebaseFirestore,
    private val userId: String
) : RecyclerView.Adapter<BookmarkAdapter.BookmarkViewHolder>() {

    inner class BookmarkViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleText: TextView = view.findViewById(R.id.bookmarkTitle)
        val starButton: Button = view.findViewById(R.id.bookmarkButton)

        fun bind(bookmark: Bookmark) {
            titleText.text = bookmark.title
            updateButtonState(starButton, bookmark)

            // 북마크 버튼 클릭 이벤트
            starButton.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    toggleBookmark(bookmark, starButton)
                }
            }

            // 레시피 클릭 이벤트 처리
            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, RecipeDetailsActivity::class.java)
                intent.putExtra("recipeId", bookmark.id.toInt()) // 북마크 ID 전달
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.bookmark_item, parent, false)
        return BookmarkViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
        holder.bind(bookmarks[position])
    }

    override fun getItemCount(): Int = bookmarks.size

    // 북마크 상태 전환
    private fun toggleBookmark(bookmark: Bookmark, button: Button) {
        val bookmarkRef = db.collection("users").document(userId)
            .collection("bookmarks").document(bookmark.id)

        bookmarkRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Firestore에서 북마크 삭제
                bookmarkRef.delete().addOnSuccessListener {
                    removeBookmarkFromList(bookmark)
                    Toast.makeText(button.context, "북마크에서 제거되었습니다.", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(button.context, "북마크 삭제 실패", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Firestore에서 북마크 추가
                bookmarkRef.set(bookmark).addOnSuccessListener {
                    Toast.makeText(button.context, "북마크에 추가되었습니다.", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(button.context, "북마크 추가 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 북마크 삭제 후 리스트 갱신
    private fun removeBookmarkFromList(bookmark: Bookmark) {
        val position = bookmarks.indexOfFirst { it.id == bookmark.id }
        if (position != -1) {
            bookmarks.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    // 버튼 상태 업데이트 (별 색상 변경)
    private fun updateButtonState(button: Button, bookmark: Bookmark) {
        val bookmarkRef = db.collection("users").document(userId)
            .collection("bookmarks").document(bookmark.id)

        bookmarkRef.get().addOnSuccessListener { document ->
            val isBookmarked = document.exists()
            button.setTextColor(
                button.context.getColor(
                    if (isBookmarked) R.color.yellow else android.R.color.black
                )
            )
        }
    }
}
