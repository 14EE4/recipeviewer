package com.example.recipeviewer.helpers

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognitionListener 
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog

/**
 * 음성 검색 헬퍼 클래스
 * MainPageActivity에서 음성 인식 검색,
 * AddIngredientActivity에서 재료 추가 시 사용됨
 * 
 * @author 노평주
 */
class VoiceSearchHelper(private val activity: Activity, private val onVoiceResult: (String) -> Unit) {

    private val speechRecognizer: SpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity)
    private val RECORD_AUDIO_REQUEST_CODE = 1

    init {
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    onVoiceResult(matches[0]) // 인식된 텍스트를 콜백으로 전달
                }
            }

            override fun onError(error: Int) {
                Toast.makeText(activity, "음성 인식 오류 발생", Toast.LENGTH_SHORT).show()
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    // 음성 인식 시작 메서드
    fun startVoiceRecognition() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없는 경우 권한 요청
            requestAudioPermission()
        } else {
            // 권한이 있는 경우 음성 인식 시작
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR") // 한국어로 설정
            }
            speechRecognizer.startListening(intent)
        }
    }

    // 권한 요청 결과 처리
    fun handlePermissionsResult(requestCode: Int, grantResults: IntArray) {
        if (requestCode == RECORD_AUDIO_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("VoiceSearchHelper", "오디오 권한이 허용되었습니다.")
            // 권한이 허용된 경우 음성 인식 시작
            startVoiceRecognition()
        } else {
            Log.d("VoiceSearchHelper", "오디오 권한이 필요합니다.")
            Toast.makeText(activity, "오디오 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    fun release() {
        speechRecognizer.destroy()
    }

    // 권한 요청 함수
    private fun requestAudioPermission() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.RECORD_AUDIO)) {
                // 권한 설명 다이얼로그 표시
                AlertDialog.Builder(activity)
                    .setTitle("오디오 권한 필요")
                    .setMessage("음성 인식을 사용하려면 오디오 권한이 필요합니다.")
                    .setPositiveButton("허용") { _, _ ->
                        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_REQUEST_CODE)
                    }
                    .setNegativeButton("거부") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            } else {
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_REQUEST_CODE)
            }
        }
    }
}
