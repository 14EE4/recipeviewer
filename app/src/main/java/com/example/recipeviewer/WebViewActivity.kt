package com.example.recipeviewer

// WebViewActivity.kt
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebSettings
import androidx.appcompat.app.AppCompatActivity
import android.webkit.WebResourceRequest
import android.webkit.WebResourceError

import android.widget.Toast



class WebViewActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview) // WebView 레이아웃으로 변경하세요

        webView = findViewById(R.id.webView) // 중복 제거
        webView.settings.javaScriptEnabled = true
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE // 캐시 비활성화
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // 페이지 로드가 끝났을 때 수행할 작업
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                // 오류 발생 시 처리할 코드
                Toast.makeText(this@WebViewActivity, "페이지 로드 중 오류 발생", Toast.LENGTH_SHORT).show()
            }
        }

        val url = intent.getStringExtra("URL") // Intent에서 URL을 가져옵니다.
        webView.loadUrl(url ?: "https://www.example.com") // URL을 로드합니다.
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack() // 이전 페이지로 이동
        } else {
            super.onBackPressed() // 기본 동작 수행
        }
    }
}



