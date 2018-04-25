package w_vertex.wings

import android.content.*
import android.os.*
import android.support.v7.app.*

/**
 * Created by root1 on 24/04/2018.
 */
class SplashActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

}