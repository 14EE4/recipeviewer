package com.example.recipeviewer.helpers

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.recipeviewer.Recipe
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class DatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "recipes.db" // 데이터베이스 이름
        private const val DATABASE_VERSION = 1 // 데이터베이스 버전
        private const val ASSET_DB_PATH = "databases/$DATABASE_NAME"

        // Users Table
        private const val TABLE_USERS = "users"
        private const val COLUMN_USER_ID = "id"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PASSWORD = "password"

        // Ingredients Table
        private const val TABLE_INGREDIENTS = "ingredients"
        private const val COLUMN_INGREDIENT_ID = "id"
        private const val COLUMN_INGREDIENT_NAME = "name"
    }

    private val dbPath: String = context.getDatabasePath(DATABASE_NAME).absolutePath

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
        val createUsersTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_USERS (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_EMAIL TEXT NOT NULL UNIQUE,
                $COLUMN_PASSWORD TEXT NOT NULL
            )
        """
        db.execSQL(createUsersTable)

        val createIngredientsTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_INGREDIENTS (
                $COLUMN_INGREDIENT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_INGREDIENT_NAME TEXT NOT NULL
            )
        """
        db.execSQL(createIngredientsTable)
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

    // 회원가입 기능
    fun registerUser(email: String, password: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_EMAIL, email)
            put(COLUMN_PASSWORD, password)
        }
        val result = db.insert(TABLE_USERS, null, values)
        db.close()
        return result != -1L
    }

    // 로그인 기능
    fun authenticateUser(email: String, password: String): Boolean {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_EMAIL = ? AND $COLUMN_PASSWORD = ?"
        val cursor = db.rawQuery(query, arrayOf(email, password))
        val isAuthenticated = cursor.count > 0
        cursor.close()
        db.close()
        return isAuthenticated
    }

    // 재료 추가 기능
    fun addIngredient(name: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_INGREDIENT_NAME, name)
        }
        val result = db.insert(TABLE_INGREDIENTS, null, values)
        db.close()
        return result != -1L
    }
}
