package com.wngrlhnn.adultvideoplayer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.wngrlhnn.adultvideoplayer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var player: ExoPlayer
    private val videos = mutableListOf<VideoEntry>()

    private val sampleVideos = listOf(
        VideoEntry("דוגמה 1 — נשיקה רומנטית", Uri.parse("https://videos.pexels.com/video-files/5304017/5304017-uhd_4096_2160_30fps.mp4"), "Pexels"),
        VideoEntry("דוגמה 2 — רגע רומנטי", Uri.parse("https://videos.pexels.com/video-files/8451945/8451945-uhd_2160_3840_25fps.mp4"), "Pexels"),
        VideoEntry("דוגמה 3 — זוג בצללית", Uri.parse("https://videos.pexels.com/video-files/6718243/6718243-uhd_4096_2160_25fps.mp4"), "Pexels"),
        VideoEntry("דוגמה 4 — נשיקה במרפסת", Uri.parse("https://videos.pexels.com/video-files/9500161/9500161-uhd_4096_2160_25fps.mp4"), "Pexels")
    )

    private val pickVideos = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        uris.forEach { uri ->
            if (videos.none { it.uri == uri }) {
                try { contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
                val localNumber = videos.count { it.source == "local" } + 1
                videos.add(VideoEntry("הסרטון שלי $localNumber", uri))
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
        binding.addSamples.setOnClickListener {
            val existing = videos.map { it.uri }.toSet()
            sampleVideos.filter { it.uri !in existing }.forEach { videos.add(it) }
            refreshPlaylist()
            if (videos.isNotEmpty() && player.currentMediaItem == null) play(0)
        }
        binding.playlist.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        binding.playlist.adapter = VideoAdapter(videos) { index -> play(index) }
        refreshPlaylist()
    }

    private fun play(index: Int) {
        if (index !in videos.indices) return
        player.setMediaItem(MediaItem.fromUri(videos[index].uri))
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