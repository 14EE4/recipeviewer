package com.example.recipeviewer.helpers

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.widget.Toast
import com.example.recipeviewer.models.Ingredient
import com.example.recipeviewer.models.Recipe
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class DatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "recipes.db" // 데이터베이스 이름
        private const val DATABASE_VERSION = 1 // 데이터베이스 버전
        private const val ASSET_DB_PATH = "databases/$DATABASE_NAME"

        //제외 재료 테이블
        private const val TABLE_EXCLUDED_INGREDIENTS = "excluded_ingredients"
        private const val COLUMN_EXCLUDED_INGREDIENT_ID = "id"
        private const val COLUMN_EXCLUDED_INGREDIENT_NAME = "name"
    }

    private val dbPath: String = context.getDatabasePath(DATABASE_NAME).absolutePath
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val userId: String = auth.currentUser?.uid ?: ""

    init {
        createDatabase()
    }

    private fun createDatabase() {
        val dbExist = checkDatabaseExists()

        if (!dbExist) {
            this.readableDatabase.close()
            try {
                copyDatabase()
                Log.d("DatabaseHelper", "데이터베이스가 성공적으로 복사되었습니다.")
            } catch (e: IOException) {
                Log.e("DatabaseHelper", "데이터베이스 복사 중 오류 발생", e)
                throw RuntimeException("Error copying database")
            }
        } else {
            Log.d("DatabaseHelper", "데이터베이스가 이미 존재합니다.")
        }

        // 데이터베이스가 존재하든 존재하지 않든 테이블을 생성합니다.
        val db = writableDatabase
        createTables(db)
        db.close()

    }

    private fun checkDatabaseExists(): Boolean {
        val dbFile = File(dbPath)
        val exists = dbFile.exists() && dbFile.length() > 0
        Log.d("DatabaseHelper", "데이터베이스 존재 여부: $exists, 경로: $dbPath, 파일 크기: ${dbFile.length()}")
        return exists
    }

    private fun copyDatabase() {
        val dbPath = context.getDatabasePath(DATABASE_NAME).absolutePath

        try {
            val inputStream = context.assets.open(ASSET_DB_PATH)
            val outputStream = FileOutputStream(dbPath)

            // 데이터 복사
            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            Log.d("DatabaseHelper", "데이터베이스 복사가 완료되었습니다.")

        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("DatabaseHelper", "데이터베이스 복사 중 오류 발생: ${e.message}")
        }
    }

    private fun createTables(db: SQLiteDatabase) {


        val createExcludedIngredientsTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_EXCLUDED_INGREDIENTS (
                $COLUMN_EXCLUDED_INGREDIENT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_EXCLUDED_INGREDIENT_NAME TEXT NOT NULL UNIQUE
            )
        """
        db.execSQL(createExcludedIngredientsTable)


    }


    override fun onCreate(db: SQLiteDatabase?) {
        // onCreate 메서드는 비워둡니다.
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // 데이터베이스 버전 업그레이드 시 필요한 작업
    }

    fun readAllData(): MutableList<Recipe> {
        val recipeList = mutableListOf<Recipe>()
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM recipes", null)

        // 열 인덱스 확인
        val idIndex = cursor.getColumnIndex("id")
        val titleIndex = cursor.getColumnIndex("title")
        val mainIngredientsIndex = cursor.getColumnIndex("main_ingredients")
        val subIngredientsIndex = cursor.getColumnIndex("sub_ingredients")
        val alternativeIngredientsIndex = cursor.getColumnIndex("alternative_ingredients")
        val cookingTimeIndex = cursor.getColumnIndex("cooking_time")
        val caloriesIndex = cursor.getColumnIndex("calories")
        val portionsIndex = cursor.getColumnIndex("portions")
        val descriptionIndex = cursor.getColumnIndex("description")
        val recipeUrlIndex = cursor.getColumnIndex("recipe_url")

        // 각 열의 인덱스가 -1이 아닌지 확인
        if (idIndex != -1 && titleIndex != -1 && mainIngredientsIndex != -1 &&
            subIngredientsIndex != -1 && alternativeIngredientsIndex != -1 &&
            cookingTimeIndex != -1 && caloriesIndex != -1 &&
            portionsIndex != -1 && descriptionIndex != -1 &&
            recipeUrlIndex != -1) {

            if (cursor.moveToFirst()) {
                do {
                    val recipe = Recipe(
                        id = cursor.getInt(idIndex),
                        title = cursor.getString(titleIndex),
                        mainIngredients = cursor.getString(mainIngredientsIndex),
                        subIngredients = cursor.getString(subIngredientsIndex),
                        alternativeIngredients = cursor.getString(alternativeIngredientsIndex),
                        cookingTime = cursor.getString(cookingTimeIndex),
                        calories = cursor.getString(caloriesIndex),
                        portions = cursor.getString(portionsIndex),
                        description = cursor.getString(descriptionIndex),
                        recipeUrl = cursor.getString(recipeUrlIndex)
                    )
                    recipeList.add(recipe)
                } while (cursor.moveToNext())
            }
        } else {
            Log.e("Database", "One or more column names are incorrect.")
        }

        cursor.close()
        return recipeList
    }



    fun addIngredient(
        userId: String,
        name: String,
        quantity: Int,
        unit: String,
        expiryDate: String
    ): Task<DocumentReference> {
        val ingredient = hashMapOf(
            "name" to name,
            "quantity" to quantity,
            "unit" to unit,
            "expiryDate" to expiryDate
        )
        return firestore.collection("users").document(userId).collection("ingredients").add(ingredient)
    }

    // 재료 수정
    fun updateIngredient(userId: String, documentId: String, name: String, quantity: Int, unit: String, expiryDate: String): Task<Void> {
        val ingredient = hashMapOf(
            "name" to name,
            "quantity" to quantity,
            "unit" to unit,
            "expiryDate" to expiryDate
        )
        return firestore.collection("users").document(userId).collection("ingredients").document(documentId).set(ingredient) // documentId 사용
            .addOnSuccessListener {
                Log.d("DatabaseHelper", "Ingredient updated with ID: $documentId")
            }.addOnFailureListener { e ->
                Log.e("DatabaseHelper", "Error updating ingredient", e)
            }
    }


    // 재료 목록 초기화
    fun clearIngredients(userId: String): Task<QuerySnapshot> {
        return firestore.collection("users").document(userId).collection("ingredients").get().addOnSuccessListener { result ->
            for (document in result) {
                firestore.collection("users").document(userId).collection("ingredients").document(document.id).delete()
            }
            Log.d("DatabaseHelper", "All ingredients cleared")
        }.addOnFailureListener { e ->
            Log.e("DatabaseHelper", "Error clearing ingredients", e)
        }
    }

    // 재료 목록 읽기
    fun readIngredients(userId: String, callback: (List<Ingredient>) -> Unit) {
        firestore.collection("users").document(userId).collection("ingredients").get().addOnSuccessListener { result ->
            val ingredients = mutableListOf<Ingredient>()
            for (document in result) {
                val ingredient = Ingredient(
                    id = document.id, // Firestore 문서 ID를 직접 사용
                    name = document.getString("name") ?: "",
                    quantity = document.getLong("quantity")?.toInt() ?: 0,
                    unit = document.getString("unit") ?: "",
                    expiryDate = document.getString("expiryDate") ?: ""
                )
                ingredients.add(ingredient)
            }
            callback(ingredients)
            Log.d("DatabaseHelper", "Ingredients: $ingredients")
        }.addOnFailureListener { e ->
            Log.e("DatabaseHelper", "Error getting ingredients", e)
        }
    }

    // 재료 삭제
    fun deleteIngredient(userId: String, documentId: String): Task<Void> { // documentId 매개변수 사용
        return firestore.collection("users").document(userId).collection("ingredients").document(documentId).delete() // documentId 사용
            .addOnSuccessListener {
                Log.d("DatabaseHelper", "Ingredient deleted with ID: $documentId")
            }.addOnFailureListener { e ->
                Log.e("DatabaseHelper", "Error deleting ingredient", e)
            }
    }

    fun addExcludedIngredient(name: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_EXCLUDED_INGREDIENT_NAME, name)
        }
        val result = db.insert(TABLE_EXCLUDED_INGREDIENTS, null, values)
        db.close()
        return result != -1L
    }

    // 제외 재료 목록 불러오기
    fun getExcludedIngredients(): List<String> {
        val excludedIngredientsList = mutableListOf<String>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_EXCLUDED_INGREDIENTS", null)

        if (cursor.moveToFirst()) {
            do {
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXCLUDED_INGREDIENT_NAME))
                excludedIngredientsList.add(name)
            } while (cursor.moveToNext())
        }

        cursor.close()
        // db.close() // SQLiteOpenHelper에서 관리하므로 직접 close() 호출 필요 없음
        return excludedIngredientsList
    }

    fun deleteExcludedIngredient(userId: String, ingredient: String): Boolean {
        val db = writableDatabase
        try {
            db.beginTransaction()
            val whereClause = "$COLUMN_EXCLUDED_INGREDIENT_NAME = ?"
            val whereArgs = arrayOf(ingredient)
            val result = db.delete(TABLE_EXCLUDED_INGREDIENTS, whereClause, whereArgs)
            db.setTransactionSuccessful()
            // 데이터베이스를 닫기 전에 삭제 결과를 반환합니다.
            return result > 0
        } finally {
            db.endTransaction()
            db.close()
        }
    }
}