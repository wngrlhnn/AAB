package com.wngrlhnn.adultvideoplayer

import android.net.Uri
import android.os.Bundle
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.wngrlhnn.adultvideoplayer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var player: ExoPlayer
    private val videos = mutableListOf<Uri>()

    private val pickVideos = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        uris.forEach { uri ->
            if (!videos.contains(uri)) {
                try { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
                videos.add(uri)
            }
        }
        refreshPlaylist()
        if (videos.isNotEmpty() && player.currentMediaItem == null) play(0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        player = ExoPlayer.Builder(this).build()
        binding.playerView.player = player
        binding.addVideos.setOnClickListener { pickVideos.launch(arrayOf("video/*")) }
        binding.playlist.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        binding.playlist.adapter = VideoAdapter(videos) { index -> play(index) }
        refreshPlaylist()
    }

    private fun play(index: Int) {
        if (index !in videos.indices) return
        player.setMediaItem(MediaItem.fromUri(videos[index]))
        player.prepare()
        player.play()
    }

    private fun refreshPlaylist() {
        binding.playlist.adapter?.notifyDataSetChanged()
        binding.emptyText.visibility = if (videos.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
    }

    override fun onDestroy() {
        player.release()
        super.onDestroy()
    }
}
