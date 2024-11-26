package com.example.recipeviewer.mainPage

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebSettings
import android.webkit.WebResourceRequest
import android.webkit.WebResourceError
import android.widget.Toast
import android.widget.Button
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.recipeviewer.R
import java.util.Locale





class WebViewActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var tts: TextToSpeech
    private lateinit var ttsButton: Button
    private lateinit var stopTtsButton: Button
    private lateinit var webAppInterface: WebAppInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview) // WebView 레이아웃으로 변경하세요

        webView = findViewById(R.id.webView) // 중복 제거
        ttsButton = findViewById(R.id.ttsButton)
        stopTtsButton = findViewById(R.id.stopTtsButton)

        webView.settings.javaScriptEnabled = true
        webView.settings.cacheMode = WebSettings.LOAD_NO_CACHE // 캐시 비활성화

        // WebAppInterface 객체 생성 및 초기화
        webAppInterface = WebAppInterface(this)
        webView.addJavascriptInterface(webAppInterface, "Android")


        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                webView.loadUrl("javascript:(function() { " +
                        "var textContent = document.body.innerText; " +
                        "Android.getTextContent(textContent); " +
                        "})()")
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                Toast.makeText(this@WebViewActivity, "페이지 로드 중 오류 발생", Toast.LENGTH_SHORT).show()
            }
        }

        //tts 출력 버튼
        ttsButton.setOnClickListener {
            webView.loadUrl("javascript:(function() { " +
                    "var textContent = document.body.innerText; " +
                    "Android.getTextContent(textContent); " +
                    "})()")
            Toast.makeText(this, "음성 출력을 시작합니다.", Toast.LENGTH_SHORT).show() // Toast 메시지 추가
        }

        //tts 멈춤 버튼
        stopTtsButton.setOnClickListener {
            Log.d("WebViewActivity", "stopTtsButton clicked")
            if (::webAppInterface.isInitialized) {
                webAppInterface.stopTts()
                Toast.makeText(this, "음성 출력을 멈춥니다.", Toast.LENGTH_SHORT).show() // Toast 메시지 추가
            } else {
                Log.e("WebViewActivity", "webAppInterface is not initialized")
            }
        }

        val url = intent.getStringExtra("URL") // Intent에서 URL을 가져옵니다.
        webView.loadUrl(url ?: "https://www.example.com") // URL을 로드합니다.

        webView.webViewClient = object : WebViewClient() {
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                Toast.makeText(this@WebViewActivity, "페이지 로드 중 오류 발생", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack() // 이전 페이지로 이동
        } else {
            super.onBackPressed() // 기본 동작 수행
        }
    }

    // JavaScript 인터페이스 클래스
    class WebAppInterface(private val context: Context) {
        private lateinit var tts: TextToSpeech // tts 변수를 멤버 변수로 선언

        init {
            tts = TextToSpeech(context, TextToSpeech.OnInitListener { status ->
                if (status == TextToSpeech.SUCCESS) {
                    // TTS 엔진 초기화 성공
                    // 원하는 언어 설정 등 추가 작업 수행 가능
                } else {
                    // TTS 엔진 초기화 실패
                    Log.e("WebAppInterface", "TTS initialization failed")
                }
            })
        }

        @JavascriptInterface
        fun getTextContent(textContent: String) {
            // TTS 초기화 (tts 변수가 이미 초기화되었는지 확인)
            if (!::tts.isInitialized) {
                tts = TextToSpeech(context) { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        tts.language = Locale.getDefault()
                    }
                }
            }

            tts.speak(textContent, TextToSpeech.QUEUE_FLUSH, null, null)
        }

        fun stopTts() {
            Log.d("WebAppInterface", "stopTts called") // 로그 추가
            if (::tts.isInitialized && tts.isSpeaking) { // TextToSpeech 객체 상태 확인
                tts.stop()
                Log.d("WebAppInterface", "tts.stop() called") // 로그 추가
            } else {
                Log.e("WebAppInterface", "tts is not initialized or not speaking") // 로그 추가
            }
        }

        fun shutdownTts() { // shutdown() 메서드 추가
            if (::tts.isInitialized) {
                tts.stop()
                tts.shutdown()
            }
        }
    }
    // WebViewActivity의 onDestroy() 메서드에서 shutdownTts() 호출
    override fun onDestroy() {
        super.onDestroy()
        webAppInterface.shutdownTts()
    }
}



