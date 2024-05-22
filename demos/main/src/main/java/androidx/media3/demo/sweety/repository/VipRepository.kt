package androidx.media3.demo.sweety.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class VipRepository private constructor(
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    private val _captchaCode : MutableLiveData<String> = MutableLiveData()
    val captchaCode:LiveData<String> = _captchaCode



    companion object {
        // For Singleton instantiation
        @Volatile
        private var instance: VipRepository? = null

    }


}