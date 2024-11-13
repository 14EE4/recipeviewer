package com.example.recipeviewer.helpers



import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognitionListener 
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.os.Bundle

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

            // 필요시 다른 RecognitionListener 메서드를 구현할 수 있습니다.
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
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_REQUEST_CODE)
        } else {
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
            startVoiceRecognition()
        } else {
            Toast.makeText(activity, "오디오 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    fun release() {
        speechRecognizer.destroy()
    }
}
