package androidx.media3.demo.main

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import androidx.core.content.ContextCompat
import java.io.File
import java.util.concurrent.TimeUnit

object MediaStoreUtil {

    // Need the READ_EXTERNAL_STORAGE permission if accessing video files that your app didn't create.
    // Container for information about each video.
    data class Video(val uri: Uri,
                     val name: String,
                     val duration: Int,
                     val size: Int,
                     val thumbnail: Bitmap,
                     val albums: String = "",
                     val data:String = "",
                     val relativePath:String = "",
                     val mimeType:String = "",

    )
    fun getVideoList(context: Context):List<Video>{
        val videoList = mutableListOf<Video>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }
        val projection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.ALBUM,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.RELATIVE_PATH,
                MediaStore.Video.Media.MIME_TYPE,
        )

        // Show only videos that are at least 5 minutes in duration.
        val selection = "${MediaStore.Video.Media.DURATION} >= ? and ${MediaStore.Video.Media.RELATIVE_PATH} like ?"
        val selectionArgs = arrayOf(
            TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES).toString(),
            "susie%"
        )
        Log.e("dddddd", "getVideoList: selectionArgs=$selectionArgs", )

        // Display videos in alphabetical order based on their display name.
        val sortOrder = "${MediaStore.Video.Media.DISPLAY_NAME} ASC"

        val query = context.contentResolver.query(
                collection,
                null/*projection*/,
                selection,
                selectionArgs,
                sortOrder
        )
        query?.use { cursor ->
            Log.e("dddddd", "getVideoList: cursor=${cursor.count}", )
            // Cache column indices.
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
            val albumsColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ALBUM)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)
            val relativePathColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.RELATIVE_PATH)
            val AUTHOR = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.AUTHOR)

            while (cursor.moveToNext()) {
                // Get values of columns for a given video.
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val duration = cursor.getInt(durationColumn)
                val size = cursor.getInt(sizeColumn)
                val contentUri: Uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                val albums = cursor.getString(albumsColumn)
                val data = cursor.getString(dataColumn)
                val relativePath = cursor.getString(relativePathColumn)
                val mimeType = cursor.getString(mimeTypeColumn)

                // Stores column values and the contentUri in a local object
                // that represents the media file.
                val thumbnail: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    context.contentResolver.loadThumbnail(contentUri, Size(640, 480), null)
                } else {
                    MediaStore.Video.Thumbnails.getThumbnail(context.contentResolver, id, MediaStore.Video.Thumbnails.MINI_KIND, null)
                }
                Log.e("dddddd", "getVideoList:AUTHOR ${cursor.getString(AUTHOR)}")
                extractVideoLocationInfo(context,contentUri)
                Video(contentUri, name, duration, size,thumbnail,albums,data,relativePath,mimeType).let {
                    Log.e("dddddd", "getVideoList: Video=$it", )
                    videoList.add(it) }
                videoList += Video(contentUri, name, duration, size,thumbnail)
            }
        }
        return videoList
    }


    //要访问视频元数据中的位置信息
    private fun extractVideoLocationInfo(context: Context,videoUri: Uri) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, videoUri)
        } catch (e: RuntimeException) {
            Log.e("dddddd", "Cannot retrieve video file", e)
        }
        // Metadata uses a standardized format.
        val locationMetadata: String? =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_LOCATION)
        Log.e("dddddd", "extractVideoLocationInfo: $locationMetadata", )
    }


    // Checks if a volume containing external storage is available
    // for read and write.
    fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    // Checks if a volume containing external storage is available to at least read.
    fun isExternalStorageReadable(): Boolean {
        return Environment.getExternalStorageState() in
                setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
    }
    fun test(context: Context){
        val externalStorageVolumes: Array<out File> =
                ContextCompat.getExternalFilesDirs(context, null)
        val primaryExternalStorage = externalStorageVolumes[0]
    }
    fun getAppSpecificAlbumStorageDir(context: Context, albumName: String): File? {
        // Get the pictures directory that's inside the app-specific directory on
        // external storage.
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), albumName)
        if (!file.mkdirs()) {
            Log.e("LOG_TAG", "Directory not created")
        }
        return file
    }
    fun openVideo(context: Context, uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setDataAndType(uri, "video/*")
        context.startActivity(intent)
    }

}