package com.example.recipeviewer.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.Toast
import com.example.recipeviewer.helpers.DatabaseHelper
import android.widget.EditText
import android.widget.TextView
import com.example.recipeviewer.R
import com.example.recipeviewer.mainPage.MainPageActivity

class MainActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 로그인 화면 설정
        setContentView(R.layout.activity_loginpage) // 수정된 XML 파일 이름으로

        // XML에 정의된 뷰 참조
        val loginTitle: TextView = findViewById(R.id.textView)
        val welcomeMessage: TextView = findViewById(R.id.textView2)
        val usernameEditText: EditText = findViewById(R.id.editTextText)
        val passwordEditText: EditText = findViewById(R.id.editTextTextPassword2)
        val passwordLabel: TextView = findViewById(R.id.textView3)
        val usernameLabel: TextView = findViewById(R.id.textView4)
        val loginButton: Button = findViewById(R.id.button)
        val createAccountButton: Button = findViewById(R.id.button2)

        // DatabaseHelper 초기화
        databaseHelper = DatabaseHelper(this)

        // 로그인 버튼 클릭 이벤트 설정 (데이터베이스 검사 없이 바로 메인 페이지로 이동)
        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            if(username=="admin"&&password=="1111"){
                Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainPageActivity::class.java)) // MainPageActivity로 이동
            }
            else{
                Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            }


        }

        // 회원가입 버튼 클릭 이벤트 설정
        createAccountButton.setOnClickListener {
            // 회원가입 화면으로 이동
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
