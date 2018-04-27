package w_vertex.wings

import android.content.*
import android.os.*
import android.speech.*
import android.widget.*
import io.reactivex.subjects.*



/**
 * Created by root1 on 24/04/2018.
 */
class MainViewModel(private val context: Context) {

    val buttonSubject: PublishSubject<String>
    val voiceSubject: PublishSubject<String>
    private val recognizer: SpeechRecognizer

    init {
        recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        buttonSubject = PublishSubject.create()
        voiceSubject = PublishSubject.create()
        setListenVoice()
    }

    fun setButtonWithKey(vararg datas: Pair<Button, String>){
        for (data in datas){
            data.first.setOnClickListener { buttonSubject.onNext(data.second) }
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
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
        }
        recognizer.startListening(intent)
    }

}