package com.wngrlhnn.adultvideoplayer

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.wngrlhnn.adultvideoplayer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var player: ExoPlayer
    private val videos = mutableListOf<VideoEntry>()

    private val sampleVideos = listOf(
        VideoEntry("קטע 1 — נשיקה רומנטית", Uri.parse("android.resource://com.wngrlhnn.adultvideoplayer/raw/romantic_1"), "Pexels"),
        VideoEntry("קטע 2 — רגע רומנטי", Uri.parse("android.resource://com.wngrlhnn.adultvideoplayer/raw/romantic_2"), "Pexels"),
        VideoEntry("קטע 3 — זוג בצללית", Uri.parse("android.resource://com.wngrlhnn.adultvideoplayer/raw/romantic_3"), "Pexels"),
        VideoEntry("קטע 4 — נשיקה במרפסת", Uri.parse("android.resource://com.wngrlhnn.adultvideoplayer/raw/romantic_4"), "Pexels")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        player = ExoPlayer.Builder(this).build()
        binding.playerView.player = player
        videos.addAll(sampleVideos)
        binding.playlist.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        binding.playlist.adapter = VideoAdapter(videos) { index -> play(index) }
        refreshPlaylist()
        if (videos.isNotEmpty()) play(0)
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
