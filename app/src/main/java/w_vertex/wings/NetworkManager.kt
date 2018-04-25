package w_vertex.wings

import io.reactivex.subjects.*
import java.io.*
import java.net.*



/**
 * Created by root1 on 24/04/2018.
 */
class NetworkManager(val ip: String, val port: Int = 3000) {

    var socket: Socket? = null
    private var thread: Thread? = null

    val dataSubject: PublishSubject<String>
    val stateSubject: BehaviorSubject<Boolean>

    init {
        createSocket()
        dataSubject = PublishSubject.create()
        stateSubject = BehaviorSubject.createDefault(false)
    }

    fun createSocket(){
        Thread{
            try { socket = Socket(ip, port); readData(); true }
            catch (e: IOException){ false }
                    .let { stateSubject.onNext(it) }
        }.start()
    }

    private fun readData(){
        socket?.apply {
            thread = Thread{
                while (true){
                    val data = ByteArray(1024)
                    try { this.getInputStream().read(data) }
                    catch (e: IOException){ break }
                    if (data.size == 1024){ break }
                    else{ dataSubject.onNext(String(data)) }
                }
                stateSubject.onNext(false)
            }.apply { start() }
        }
    }

    fun sendData(msg: String){
        socket?.let {
            Thread{ it.getOutputStream().write(msg.toByteArray()) }.start()
        }
    }

    fun close(){
        socket?.close()
        socket = null
    }

}