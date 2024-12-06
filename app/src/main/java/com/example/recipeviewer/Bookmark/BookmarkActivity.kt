package com.example.recipeviewer.Bookmark

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.recipeviewer.R
import com.example.recipeviewer.models.Bookmark
import com.example.recipeviewer.mainPage.RecipeDetailsActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BookmarkActivity : AppCompatActivity() {

    private lateinit var bookmarkRecyclerView: RecyclerView
    private lateinit var bookmarkAdapter: BookmarkAdapter
    private val bookmarkList: MutableList<Bookmark> = mutableListOf()
    private val filteredList: MutableList<Bookmark> = mutableListOf() // 필터링된 리스트
    private lateinit var auth: FirebaseAuth
    private lateinit var userId: String
    private lateinit var firestore: FirebaseFirestore
    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmark)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        val currentUser = auth.currentUser

        // 로그인 여부 확인
        if (currentUser == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        userId = currentUser.uid

        // 북마크 리사이클러뷰 설정
        bookmarkRecyclerView = findViewById(R.id.recyclerViewBookmark)
        bookmarkRecyclerView.layoutManager = LinearLayoutManager(this)
        bookmarkAdapter = BookmarkAdapter(filteredList, firestore, userId)
        bookmarkRecyclerView.adapter = bookmarkAdapter

        // 검색 뷰 초기화
        searchView = findViewById(R.id.searchViewBookmark)

        // 검색 뷰 리스너 설정
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterBookmarks(newText)
                return true
            }
        })

        loadBookmarks()
    }

    // 북마크 로딩
    private fun loadBookmarks() {
        firestore.collection("users").document(userId)
            .collection("bookmarks")
            .get() // 실시간 리스너 대신 단일 데이터 로드
            .addOnSuccessListener { snapshots ->
                val newList = snapshots.map { document ->
                    Bookmark(
                        id = document.id,
                        title = document.getString("title") ?: "",
                        description = document.getString("description") ?: ""
                    )
                }

                val diffCallback = BookmarkDiffCallback(bookmarkList, newList)
                val diffResult = DiffUtil.calculateDiff(diffCallback)

                bookmarkList.clear()
                bookmarkList.addAll(newList)
                filteredList.clear()
                filteredList.addAll(newList) // 필터링된 리스트 초기화
                diffResult.dispatchUpdatesTo(bookmarkAdapter)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "북마크를 불러오는 데 실패했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // 검색어에 맞게 북마크 필터링
    private fun filterBookmarks(query: String?) {
        filteredList.clear()

        if (query.isNullOrEmpty()) {
            filteredList.addAll(bookmarkList) // 검색어가 비어 있으면 전체 목록 표시
        } else {
            val filtered = bookmarkList.filter { bookmark ->
                bookmark.title.contains(query, ignoreCase = true) ||
                        bookmark.description.contains(query, ignoreCase = true)
            }
            filteredList.addAll(filtered)
        }

        bookmarkAdapter.notifyDataSetChanged()
    }

    // 레시피 아이템 클릭 시 RecipeDetailsActivity로 이동
    fun onRecipeClicked(bookmark: Bookmark) {
        val intent = Intent(this, RecipeDetailsActivity::class.java)
        intent.putExtra("RECIPE_ID", bookmark.id) // 북마크의 ID를 전달
        startActivity(intent)
    }

    // 북마크 상태 변경 시 호출되는 메서드
    private fun onBookmarkChanged() {
        val intent = Intent()
        setResult(RESULT_OK, intent) // 결과 코드 설정
        finish() // Activity 종료
    }
}
