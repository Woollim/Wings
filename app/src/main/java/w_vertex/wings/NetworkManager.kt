package w_vertex.wings

import io.reactivex.*
import io.reactivex.disposables.*
import io.reactivex.schedulers.*
import io.reactivex.subjects.*
import java.io.*
import java.net.*
import java.util.concurrent.*


/**
 * Created by root1 on 24/04/2018.
 */
class NetworkManager(val ip: String, val port: Int = 3000) {

    var socket: Socket? = null

    val dataSubject: PublishSubject<String>
    val stateSubject: BehaviorSubject<Boolean>

    private val disposables by lazy { CompositeDisposable() }

    init {
        dataSubject = PublishSubject.create()
        stateSubject = BehaviorSubject.createDefault(false)
        createSocket()
    }

    fun createSocket() {
        Thread {
            try {
                socket = Socket(ip, port)
                readData(); true
            } catch (e: IOException) {
                false
            }.let { stateSubject.onNext(it) }
        }.start()
    }

    private fun readData() {
        Observable.interval(100, TimeUnit.MILLISECONDS)
                .repeat()
                .subscribeOn(Schedulers.io())
                .map { socket!! }
                .filter { it.isConnected }
                .map { it.getInputStream() }
                .filter { it.available() > 0 }
                .map {
                    val byteArray = ByteArray(it.available())
                    it.read(byteArray)
                    String(byteArray)
                }.subscribe{ dataSubject.onNext(it) }
                .let { disposables.add(it) }
    }

    fun sendData(msg: String) {
        Observable.just(msg)
                .subscribeOn(Schedulers.io())
                .filter { socket != null }
                .subscribe { socket!!.getOutputStream().write(msg.toByteArray()) }
    }

    fun close() {
        socket?.let {
            disposables.clear()
            stateSubject.onNext(false)
            socket = null
        }
    }

}