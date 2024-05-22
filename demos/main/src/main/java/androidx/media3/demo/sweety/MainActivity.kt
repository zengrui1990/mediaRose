package androidx.media3.demo.sweety

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ExpandableListView
import android.widget.ExpandableListView.OnChildClickListener
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.ClippingConfiguration
import androidx.media3.common.MediaItem.DrmConfiguration
import androidx.media3.common.MediaItem.SubtitleConfiguration
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.demo.main.DemoUtil
import androidx.media3.demo.main.IntentUtil
import androidx.media3.demo.main.MediaStoreUtil
import androidx.media3.demo.main.MediaStoreUtil.getVideoList
import androidx.media3.demo.main.PlayerActivity
import androidx.media3.demo.main.R
import com.google.common.base.Objects
import com.google.common.base.Preconditions
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import java.util.UUID
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity(), OnChildClickListener {
    private var useExtensionRenderers = false
    private var sampleAdapter: SampleAdapter? = null
    private lateinit var preferExtensionDecodersMenuItem: MenuItem
    private lateinit var sampleListView: ExpandableListView
    private var notificationPermissionToastShown = false

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sample_chooser_activity)
        sampleAdapter = SampleAdapter()
        sampleListView = findViewById(R.id.sample_list)

        sampleListView.setAdapter(sampleAdapter)
        sampleListView.setOnChildClickListener(this)

        useExtensionRenderers = DemoUtil.useExtensionRenderers()
        loadSample()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.sample_chooser_menu, menu)
        preferExtensionDecodersMenuItem = menu.findItem(R.id.prefer_extension_decoders)
        preferExtensionDecodersMenuItem.setVisible(useExtensionRenderers)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.setChecked(!item.isChecked)
        return true
    }

    public override fun onStart() {
        super.onStart()
        sampleAdapter!!.notifyDataSetChanged()
    }

    public override fun onStop() {
        super.onStop()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (!notificationPermissionToastShown
            && (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED)
        ) {
            Toast.makeText(
                applicationContext, R.string.post_notification_not_granted, Toast.LENGTH_LONG
            )
                .show()
            notificationPermissionToastShown = true
        }
    }

    private fun loadSample() {
        SampleListLoader().execute()
    }

    private fun onPlaylistGroups(groups: List<PlaylistGroup>, sawError: Boolean) {
        if (sawError) {
            Toast.makeText(applicationContext, R.string.sample_list_load_error, Toast.LENGTH_LONG)
                .show()
        }
        sampleAdapter!!.setPlaylistGroups(groups)

        val preferences = getPreferences(MODE_PRIVATE)
        val groupPosition = preferences.getInt(GROUP_POSITION_PREFERENCE_KEY,  /* defValue= */-1)
        val childPosition = preferences.getInt(CHILD_POSITION_PREFERENCE_KEY,  /* defValue= */-1)
        // Clear the group and child position if either are unset or if either are out of bounds.
        if (groupPosition != -1 && childPosition != -1 && groupPosition < groups.size && childPosition < groups[groupPosition].playlists.size) {
            sampleListView.expandGroup(groupPosition) // shouldExpandGroup does not work without this.
            sampleListView.setSelectedChild(
                groupPosition,
                childPosition,  /* shouldExpandGroup= */
                true
            )
        }
    }

    override fun onChildClick(
        parent: ExpandableListView, view: View, groupPosition: Int, childPosition: Int, id: Long
    ): Boolean {
        // Save the selected item first to be able to restore it if the tested code crashes.
        val prefEditor = getPreferences(MODE_PRIVATE).edit()
        prefEditor.putInt(GROUP_POSITION_PREFERENCE_KEY, groupPosition)
        prefEditor.putInt(CHILD_POSITION_PREFERENCE_KEY, childPosition)
        prefEditor.apply()

        val playlistHolder = view.tag as PlaylistHolder
        val intent = Intent(this, PlayerActivity::class.java)
        intent.putExtra(
            IntentUtil.PREFER_EXTENSION_DECODERS_EXTRA,
            isNonNullAndChecked(preferExtensionDecodersMenuItem)
        )
        IntentUtil.addToIntent(playlistHolder.mediaItems, intent)
        startActivity(intent)
        return true
    }

    private inner class SampleListLoader {
        private val executorService: ExecutorService = Executors.newSingleThreadExecutor()

        private val sawError = false

        @OptIn(markerClass = [UnstableApi::class])
        fun execute() {
            executorService.execute {
                val result: MutableList<PlaylistGroup> = ArrayList()
                readPlaylistGroup(result)
                Handler(Looper.getMainLooper())
                    .post { onPlaylistGroups(result, sawError) }
            }
        }

        private fun readPlaylistGroup(groups: MutableList<PlaylistGroup>) {
            val groupName = ""
            val playlistHolders = ArrayList<PlaylistHolder>()
            playlistHolders.add(readEntry())
            val group = getGroup(groupName, groups)
            group.playlists.addAll(playlistHolders)
        }


        private fun readEntry(): PlaylistHolder {
            var uri: Uri? = null
            val subtitleUri: Uri? = null
            val subtitleMimeType: String? = null
            val subtitleLanguage: String? = null
            val drmUuid: UUID? = null
            val drmLicenseUri: String? = null
            val drmLicenseRequestHeaders = ImmutableMap.of<String, String>()
            val drmSessionForClearContent = false
            val drmMultiSession = false
            val drmForceDefaultLicenseUri = false
            val clippingConfiguration =
                ClippingConfiguration.Builder()

            val mediaItem = MediaItem.Builder()

            val title = "米奇"
            val videoUrls = getVideoList(
                applicationContext
            )
            var videoItem: MediaStoreUtil.Video? = null
            if (videoUrls.isNotEmpty()) {
                videoItem = videoUrls[0]
                uri = Uri.parse(videoItem.uri.toString())
            }
            Log.e(TAG, "readEntry: dddddd url=$uri")

            val adaptiveMimeType = videoItem?.mimeType ?: ""
            Log.e(TAG, "dddddd readEntry: adaptiveMimeType=$adaptiveMimeType")
            mediaItem
                .setUri(uri)
                .setMediaMetadata(MediaMetadata.Builder().setTitle(title).build())
                .setMimeType(adaptiveMimeType)
                .setClippingConfiguration(clippingConfiguration.build())
            if (drmUuid != null) {
                mediaItem.setDrmConfiguration(
                    DrmConfiguration.Builder(drmUuid)
                        .setLicenseUri(drmLicenseUri)
                        .setLicenseRequestHeaders(drmLicenseRequestHeaders)
                        .setForceSessionsForAudioAndVideoTracks(drmSessionForClearContent)
                        .setMultiSession(drmMultiSession)
                        .setForceDefaultLicenseUri(drmForceDefaultLicenseUri)
                        .build()
                )
            } else {
                Preconditions.checkState(
                    drmLicenseUri == null,
                    "drm_uuid is required if drm_license_uri is set."
                )
                Preconditions.checkState(
                    drmLicenseRequestHeaders.isEmpty(),
                    "drm_uuid is required if drm_key_request_properties is set."
                )
                Preconditions.checkState(
                    !drmSessionForClearContent,
                    "drm_uuid is required if drm_session_for_clear_content is set."
                )
                Preconditions.checkState(
                    !drmMultiSession,
                    "drm_uuid is required if drm_multi_session is set."
                )
                Preconditions.checkState(
                    !drmForceDefaultLicenseUri,
                    "drm_uuid is required if drm_force_default_license_uri is set."
                )
            }
            if (subtitleUri != null) {
                val subtitleConfiguration =
                    SubtitleConfiguration.Builder(subtitleUri)
                        .setMimeType(
                            Preconditions.checkNotNull(
                                subtitleMimeType,
                                "subtitle_mime_type is required if subtitle_uri is set."
                            )
                        )
                        .setLanguage(subtitleLanguage)
                        .build()
                mediaItem.setSubtitleConfigurations(ImmutableList.of(subtitleConfiguration))
            }
            return PlaylistHolder(title, listOf(mediaItem.build()))
        }

        private fun getGroup(groupName: String, groups: MutableList<PlaylistGroup>): PlaylistGroup {
            for (i in groups.indices) {
                if (Objects.equal(groupName, groups[i].title)) {
                    return groups[i]
                }
            }
            val group = PlaylistGroup(groupName)
            groups.add(group)
            return group
        }
    }

    private inner class SampleAdapter : BaseExpandableListAdapter(), View.OnClickListener {
        private var playlistGroups: List<PlaylistGroup>

        init {
            playlistGroups = emptyList()
        }

        fun setPlaylistGroups(playlistGroups: List<PlaylistGroup>) {
            this.playlistGroups = playlistGroups
            notifyDataSetChanged()
        }

        override fun getChild(groupPosition: Int, childPosition: Int): PlaylistHolder {
            return getGroup(groupPosition).playlists[childPosition]
        }

        override fun getChildId(groupPosition: Int, childPosition: Int): Long {
            return childPosition.toLong()
        }

        override fun getChildView(
            groupPosition: Int,
            childPosition: Int,
            isLastChild: Boolean,
            convertView: View?,
            parent: ViewGroup
        ): View {
            var view = convertView
            if (view == null) {
                view = layoutInflater.inflate(R.layout.sample_list_item, parent, false)
                val downloadButton = view.findViewById<View>(R.id.download_button)
                downloadButton.setOnClickListener(this)
                downloadButton.isFocusable = false
            }
            initializeChildView(view!!, getChild(groupPosition, childPosition))
            return view
        }

        override fun getChildrenCount(groupPosition: Int): Int {
            return getGroup(groupPosition).playlists.size
        }

        override fun getGroup(groupPosition: Int): PlaylistGroup {
            return playlistGroups[groupPosition]
        }

        override fun getGroupId(groupPosition: Int): Long {
            return groupPosition.toLong()
        }

        override fun getGroupView(
            groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup
        ): View {
            var view = convertView
            if (view == null) {
                view =
                    layoutInflater
                        .inflate(android.R.layout.simple_expandable_list_item_1, parent, false)
            }
            (view as TextView).text = getGroup(groupPosition).title
            return view
        }

        override fun getGroupCount(): Int {
            return playlistGroups.size
        }

        override fun hasStableIds(): Boolean {
            return false
        }

        override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
            return true
        }

        override fun onClick(view: View) {
        }

        private fun initializeChildView(view: View, playlistHolder: PlaylistHolder) {
            view.tag = playlistHolder
            val sampleTitle = view.findViewById<TextView>(R.id.sample_title)
            sampleTitle.text = playlistHolder.title
        }
    }

    private class PlaylistHolder(title: String, mediaItems: List<MediaItem>) {
        val title: String
        val mediaItems: List<MediaItem>

        init {
            Preconditions.checkArgument(mediaItems.isNotEmpty())
            this.title = title
            this.mediaItems = java.util.List.copyOf(mediaItems)
        }
    }

    private class PlaylistGroup(val title: String) {
        val playlists: MutableList<PlaylistHolder> =
            ArrayList()
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val GROUP_POSITION_PREFERENCE_KEY = "sample_chooser_group_position"
        private const val CHILD_POSITION_PREFERENCE_KEY = "sample_chooser_child_position"

        private fun isNonNullAndChecked(menuItem: MenuItem?): Boolean {
            // Temporary workaround for layouts that do not inflate the options menu.
            return menuItem != null && menuItem.isChecked
        }
    }
}
