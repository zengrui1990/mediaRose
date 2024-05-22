package androidx.media3.demo.sweety.ui.main

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.media3.demo.main.MediaStoreUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val _videos = MutableLiveData<List<MediaStoreUtil.Video>>()
    val videos: LiveData<List<MediaStoreUtil.Video>> = _videos
    private val _videosEntry: MutableLiveData<Map<EntryKey, List<MediaStoreUtil.Video>>> =
        MutableLiveData()
    val videosEntry: LiveData<Map<EntryKey, List<MediaStoreUtil.Video>>> = _videosEntry

    init {
        getVideos()
    }

    private fun getVideos() {
        viewModelScope.launch(Dispatchers.IO) {
            val videos = MediaStoreUtil.getVideoList(getApplication())
            Log.e("dddddd", "getVideos:${videos.size}== $videos")
//            val entryList: MutableMap<EntryKey, List<MediaStoreUtil.Video>> = mutableMapOf()

//            videos.groupBy { it.relativePath }
//                .filter { entry -> entry.value.isNotEmpty() }
//                .forEach{ entry ->
//                    entryList[EntryKey(entry.key, entry.value[0].thumbnail)] = entry.value
//                }
//            val entry: Map<String, List<MediaStoreUtil.Video>> = videos.groupBy { it.relativePath }
//            Log.e("dddddd", "getVideos entry: $entry")
//            _videosEntry.postValue(entryList)
           _videos.postValue(videos)
        }
    }

    class EntryKey(val key: String, val bitmap: Bitmap)
}