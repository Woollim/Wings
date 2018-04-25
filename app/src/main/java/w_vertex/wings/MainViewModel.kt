package w_vertex.wings

import android.content.*
import android.os.*
import android.speech.*
import io.reactivex.subjects.*
import kotlinx.android.synthetic.main.activity_main.*



/**
 * Created by root1 on 24/04/2018.
 */
class MainViewModel(val ac: MainActivity) {

    val buttonSubject: PublishSubject<String>
    val voiceSubject: PublishSubject<String>
    val recognizer: SpeechRecognizer

    init {
        recognizer = SpeechRecognizer.createSpeechRecognizer(ac)
        buttonSubject = PublishSubject.create()
        voiceSubject = PublishSubject.create()
        setButton()
        setListenVoice()
    }

    private fun setButton(){
        ac.apply {
            bt_main_off.setOnClickListener { buttonSubject.onNext("OFF") }
            bt_main_strong.setOnClickListener { buttonSubject.onNext("STRONG") }
            bt_main_weak.setOnClickListener { buttonSubject.onNext("WEAK") }
        }
    }

    private fun setListenVoice(){
        recognizer.setRecognitionListener(object: RecognitionListener{
            override fun onReadyForSpeech(params: Bundle?) { voiceSubject.onNext("듣고 있습니다...") }
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onEndOfSpeech() { voiceSubject.onNext("음성을 분석 중입니다.") }
            override fun onError(error: Int) { voiceSubject.onNext("오류가 발생했습니다.") }
            override fun onResults(results: Bundle) {
                val key = SpeechRecognizer.RESULTS_RECOGNITION
                val result = results.getStringArrayList(key)
                voiceSubject.onNext(result[0])
            }
        })
    }

    fun listenVoice(){
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, ac.packageName)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
        }
        recognizer.startListening(intent)
    }

}