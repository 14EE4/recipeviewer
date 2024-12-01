package com.example.recipeviewer.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.recipeviewer.R
import com.example.recipeviewer.mainPage.MainPageActivity
import com.google.firebase.auth.FirebaseAuth
import com.example.recipeviewer.login.RegisterActivity // RegisterActivity import 추가

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var createAccountButton: Button



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 로그인 화면 설정
        setContentView(R.layout.activity_loginpage)

        // Firebase 초기화
        auth = FirebaseAuth.getInstance()


        // XML에 정의된 뷰 참조
        val loginTitle: TextView = findViewById(R.id.textView)
        val welcomeMessage: TextView = findViewById(R.id.textView2)
        emailEditText = findViewById(R.id.editTextText)
        passwordEditText = findViewById(R.id.editTextTextPassword2)
        val passwordLabel: TextView = findViewById(R.id.textView3)
        val usernameLabel: TextView = findViewById(R.id.textView4)
        loginButton = findViewById(R.id.button)
        createAccountButton = findViewById(R.id.button2)

        // 로그인 버튼 클릭 이벤트 설정 (Firebase Authentication 사용)
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "이메일과 비밀번호를 모두 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 회원가입 버튼 클릭 이벤트 설정
        createAccountButton.setOnClickListener {
            // 회원가입 화면으로 이동
            startActivity(Intent(this, RegisterActivity::class.java))
        }

    }




    // 로그인 처리 함수
    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainPageActivity::class.java)) // MainPageActivity로 이동
                    finish() // 현재 MainActivity 종료
                } else {
                    Toast.makeText(this, "로그인 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }


}
