package androidx.media3.demo.sweety.ui.main

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.demo.main.MediaStoreUtil
import androidx.media3.demo.main.R
import androidx.media3.demo.main.databinding.FragmentMainBinding
import androidx.media3.demo.main.databinding.VideoItemBinding
import androidx.media3.demo.main.databinding.VideoItemTitleBinding
import androidx.media3.demo.sweety.repository.VipRepository
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freeme.fragment.app.viewBinding
import kotlinx.coroutines.launch

class VideoListFragment : Fragment(R.layout.fragment_main) {
    private val binding: FragmentMainBinding by viewBinding()
    companion object {
        fun newInstance() = VideoListFragment()
    }

    private val viewModel: MainViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = GestureAdapter()
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        viewModel.videos.observe(getViewLifecycleOwner()) { data ->
            Log.e("dddddd", "onViewCreated:observer: ${data}" )
            val list =  data.distinctBy { it.relativePath }

            adapter.clear()
            adapter.addItems(list)
            adapter.notifyDataSetChanged()
        }

    }

    class GestureAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val VIEW_TYPE_TITLE = 0
        private val VIEW_TYPE_AUDIO = 1
        private val VIEW_TYPE_VIDEO = 2
        private val data = mutableListOf<Any>()
        fun addItems(list: List<Any>){
            data.addAll(list)
        }

        fun clear(){
            data.clear()
        }
        override fun getItemViewType(position: Int): Int {
           val item = data[position]
            if (item is String){
                return VIEW_TYPE_TITLE
            }
            if (item is MediaStoreUtil.Video){
                return VIEW_TYPE_VIDEO
            }
            return -1
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when(viewType) {
                VIEW_TYPE_TITLE -> TitleViewHolder(VideoItemTitleBinding.inflate(LayoutInflater.from(parent.context), parent, false))
                else -> VideoViewHolder(VideoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is VideoViewHolder) {
                val video = data[position]
                if (video is MediaStoreUtil.Video){
                    holder.bind(video.name, video.thumbnail)
                }
            } else if (holder is TitleViewHolder){
                val category = data[position].toString()
                holder.bind(category)
            }
        }

        override fun getItemCount(): Int {
            return data.size
        }
    }

    class TitleViewHolder(private val binding:VideoItemTitleBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(text : String) {
            binding.title.text = text
        }
    }
    class VideoViewHolder(private val binding: VideoItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(text : String, image : Bitmap) {
            binding.title.text = text
            binding.icon.background = BitmapDrawable(binding.root.context.resources, image)
        }
    }
}