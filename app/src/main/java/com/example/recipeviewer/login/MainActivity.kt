package com.example.recipeviewer.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.recipeviewer.R
import com.example.recipeviewer.mainPage.MainPageActivity
import com.google.firebase.auth.FirebaseAuth

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
        emailEditText = findViewById(R.id.editTextText)//이메일 입력 필드
        passwordEditText = findViewById(R.id.editTextTextPassword2)//비밀번호 입력 필드
        loginButton = findViewById(R.id.button)//로그인 버튼
        createAccountButton = findViewById(R.id.button2)//회원가입 버튼

        // 로그인 버튼 클릭 이벤트 설정 (Firebase Authentication 사용)
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {//필드가 모두 채워졌을때만 로그인
                loginUser(email, password)
            } else {//필드가 비어있을때
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
                    startActivity(Intent(this, MainPageActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "로그인 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
