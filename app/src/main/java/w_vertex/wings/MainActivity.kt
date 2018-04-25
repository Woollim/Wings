package w_vertex.wings

import android.os.*
import android.support.v7.app.*
import android.widget.*
import com.google.firebase.iid.*
import com.gun0912.tedpermission.*
import io.reactivex.android.schedulers.*
import io.reactivex.disposables.*
import io.reactivex.schedulers.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*



class MainActivity : AppCompatActivity() {

    lateinit var networkManager: NetworkManager
    lateinit var vm: MainViewModel
    val disposables by lazy { CompositeDisposable() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        vm = MainViewModel(this)
        networkManager = NetworkManager("192.168.1.47")

        bt_main_voice.setOnClickListener { checkPermissionVoice() }
        ib_main_state.setOnClickListener {
            if(networkManager.socket == null) { networkManager.createSocket() }
            else { networkManager.close() }
        }
        bt_main_fcm.setOnClickListener {
            FirebaseInstanceId.getInstance().token?.let { networkManager.sendData("fcm : $it") }
        }

        readNetworkData()
        readVoiceData()
        readConnectState()
        setButton()
    }

    override fun onStart() {
        super.onStart()
        checkPermissionVoice()
    }

    override fun onDestroy() {
        super.onDestroy()
        networkManager.close()
        disposables.clear()
    }

    private fun readConnectState(){
        networkManager.stateSubject
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val data = when(it){
                        true -> R.drawable.main_state_line_back to "연결 성공"
                        false -> R.drawable.main_state_line_fail_back to "연결 실패"
                    }
                    ib_main_state.background = getDrawable(data.first)
                    tv_main_state.text = data.second
                    showToast(data.second)
                }
    }

    private fun setButton(){
        vm.buttonSubject
                .subscribeOn(Schedulers.io())
                .subscribe {
                    networkManager.sendData(it)
        }.let { disposables.add(it) }
    }

    private fun readVoiceData(){
        vm.voiceSubject.subscribe {
            when{
                it.contains("꺼", true) -> "OFF"
                it.contains("약", true) -> "WEAK"
                it.contains("강", true) -> "STRONG"
                else -> { tv_main_voice_state.text = it; null }
            }.let { it?.let { networkManager.sendData(it) } }
        }.let { disposables.add(it) }
    }

    private fun readNetworkData(){
        networkManager.dataSubject
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe{
                    "현재 상태 : " + when(it){
                        "OFF" -> "OFF"
                        "WEAK" -> "파워 - 약"
                        "STRONG" -> "파워 - 강"
                        else -> "상태 오류.."
                    }.let { tv_main_state.text = it }
                }.let { disposables.add(it) }
    }

    private fun checkPermissionVoice(){
        TedPermission.with(this)
                .setPermissions(android.Manifest.permission.RECORD_AUDIO)
                .setPermissionListener(object: PermissionListener{
                    override fun onPermissionGranted() {
                        vm.listenVoice()
                    }

                    override fun onPermissionDenied(deniedPermissions: ArrayList<String>?) {
                        showToast("음성인식을 사용할 수 없습니다.")
                    }
                }).check()
    }

    private fun showToast(msg: String){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

}