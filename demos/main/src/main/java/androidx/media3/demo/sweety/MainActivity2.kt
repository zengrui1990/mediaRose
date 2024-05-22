package androidx.media3.demo.sweety

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.media3.demo.main.R
import androidx.media3.demo.sweety.ui.main.VideoListFragment

class MainActivity2 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_activity2)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, VideoListFragment.newInstance())
                .commitNow()
        }
    }
}