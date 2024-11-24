package com.example.recipeviewer.login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.recipeviewer.R

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register) // 여기서 activity_register.xml 레이아웃을 사용

        // 뷰 참조
        val usernameEditText: EditText = findViewById(R.id.editTextUsername)
        val passwordEditText: EditText = findViewById(R.id.editTextPassword)
        val registerButton: Button = findViewById(R.id.registerButton)

        // 회원가입 버튼 클릭 시 처리
        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                // 회원가입 처리 (SharedPreferences나 DB에 저장)
                registerUser(username, password)
            } else {
                // 아이디나 비밀번호가 비어있을 경우 경고 메시지 표시
                Toast.makeText(this, "아이디와 비밀번호를 모두 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 회원가입 처리 함수
    private fun registerUser(username: String, password: String) {
        // 예시: SharedPreferences를 사용하여 회원가입 정보 저장
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("username", username)
        editor.putString("password", password)
        editor.apply()

        // 회원가입 완료 후 로그인 화면으로 이동
        Toast.makeText(this, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, MainActivity::class.java)) // 회원가입 후 MainActivity로 이동
        finish() // 현재 RegisterActivity 종료
    }
}
