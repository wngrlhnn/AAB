package com.wngrlhnn.adultvideoplayer

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.wngrlhnn.adultvideoplayer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var player: ExoPlayer
    private val videos = mutableListOf<VideoEntry>()
    private var fullscreen = false

    private val sampleVideos = listOf(
        VideoEntry("קטע 1 — נשיקה רומנטית", Uri.parse("android.resource://com.wngrlhnn.adultvideoplayer/raw/romantic_1"), "Pexels"),
        VideoEntry("קטע 2 — רגע רומנטי", Uri.parse("android.resource://com.wngrlhnn.adultvideoplayer/raw/romantic_2"), "Pexels"),
        VideoEntry("קטע 3 — זוג בצללית", Uri.parse("android.resource://com.wngrlhnn.adultvideoplayer/raw/romantic_3"), "Pexels"),
        VideoEntry("קטע 4 — נשיקה במרפסת", Uri.parse("android.resource://com.wngrlhnn.adultvideoplayer/raw/romantic_4"), "Pexels")
    )

    private val charliePhotos = listOf(
        "https://upload.wikimedia.org/wikipedia/commons/1/10/Charlie_Hunnam_by_Gage_Skidmore_4.jpg",
        "https://upload.wikimedia.org/wikipedia/commons/6/6a/Charlie_Hunnam_%2828611323745%29.jpg",
        "https://upload.wikimedia.org/wikipedia/commons/2/2e/Charlie_Hunnam_%287607394110%29.jpg",
        "https://upload.wikimedia.org/wikipedia/commons/5/51/Charlie_Hunnam_%285984665242%29.jpg"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        player = ExoPlayer.Builder(this).build()
        binding.playerView.player = player

        binding.fullscreenButton.setOnClickListener { toggleFullscreen() }

        videos.addAll(sampleVideos)
        binding.photoGallery.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.photoGallery.adapter = PhotoAdapter(charliePhotos) { url -> showPhotoFullscreen(url) }

        binding.playlist.layoutManager = LinearLayoutManager(this)
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

    private fun toggleFullscreen() {
        fullscreen = !fullscreen
        val visible = if (fullscreen) View.GONE else View.VISIBLE
        binding.toolbar.visibility = visible
        binding.emptyText.visibility = if (fullscreen) View.GONE else
            if (videos.isEmpty()) View.VISIBLE else View.GONE
        binding.photoTitle.visibility = visible
        binding.photoGallery.visibility = visible
        binding.playlist.visibility = visible

        binding.fullscreenButton.text = if (fullscreen) "⛶" else "⛶"

        if (fullscreen) {
            window.insetsController?.let {
                it.hide(WindowInsets.Type.systemBars())
                it.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            window.insetsController?.show(WindowInsets.Type.systemBars())
        }
        binding.playerView.requestLayout()
    }

    private fun showPhotoFullscreen(url: String) {
        val dialog = android.app.Dialog(this, android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen)
        val image = ImageView(this).apply {
            setBackgroundColor(android.graphics.Color.BLACK)
            scaleType = ImageView.ScaleType.FIT_CENTER
            setOnClickListener { dialog.dismiss() }
        }
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(image)
        dialog.window?.setBackgroundDrawableResource(android.R.color.black)
        dialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT
        )
        Glide.with(this)
            .load(url)
            .fitCenter()
            .into(image)
        dialog.show()
        dialog.window?.setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    private fun refreshPlaylist() {
        binding.playlist.adapter?.notifyDataSetChanged()
        binding.emptyText.visibility =
            if (videos.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroy() {
        player.release()
        super.onDestroy()
    }
}
