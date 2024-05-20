package androidx.media3.demo.main

import android.util.Log
import java.lang.Thread.sleep

object Utils {
    val TAG = "Utils_dddddd"
    val task1: () -> String = {
        sleep(2000)
        "Hello".also {
            Log.e(TAG, "task1 finished: $it ", )
        }
    }

    val task2: () -> String = {
        sleep(2000)
        "World".also {   Log.e(TAG, "task2 finished: $it") }
    }

    val task3: (String, String) -> String = { p1, p2 ->
        sleep(2000)
        "$p1 $p2".also {  Log.e(TAG, "task3 finished: $it") }
    }
    private fun test_join() {
         var s1: String = "1111"
         var s2: String =" 2222"
        val t1 = Thread { s1 = task1() }
        val t2 = Thread { s2 = task2() }
        t1.start()
        t2.start()

        t1.join()
        t2.join()

        task3(s1, s2)

    }
    fun test(){
        test_join()
    }

}