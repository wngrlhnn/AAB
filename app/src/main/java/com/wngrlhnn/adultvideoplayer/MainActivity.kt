package com.wngrlhnn.adultvideoplayer

import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowInsets
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
    private var rotationMode = 0

    private val sampleVideos = listOf(
        VideoEntry("קטע 1 — זוג יחד במיטה", Uri.parse("android.resource://com.wngrlhnn.adultvideoplayer/raw/romantic_1"), "Pexels"),
        VideoEntry("קטע 2 — רגע אינטימי", Uri.parse("android.resource://com.wngrlhnn.adultvideoplayer/raw/romantic_2"), "Pexels"),
        VideoEntry("קטע 3 — שוכבים יחד", Uri.parse("android.resource://com.wngrlhnn.adultvideoplayer/raw/romantic_3"), "Pexels"),
        VideoEntry("קטע 4 — רגע רומנטי", Uri.parse("android.resource://com.wngrlhnn.adultvideoplayer/raw/romantic_4"), "Pexels")
    )

    private val charliePhotos = listOf(
        "android.resource://com.wngrlhnn.adultvideoplayer/drawable/charlie_1",
        "android.resource://com.wngrlhnn.adultvideoplayer/drawable/charlie_2",
        "android.resource://com.wngrlhnn.adultvideoplayer/drawable/charlie_3",
        "android.resource://com.wngrlhnn.adultvideoplayer/drawable/charlie_4"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        player = ExoPlayer.Builder(this).build()
        binding.playerView.player = player
        binding.fullscreenButton.setOnClickListener { toggleFullscreen() }
        binding.rotateButton.setOnClickListener { rotateScreen() }
        videos.addAll(sampleVideos)
        binding.photoGallery.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.photoGallery.adapter = PhotoAdapter(charliePhotos) { showPhotoFullscreen(it) }
        binding.playlist.layoutManager = LinearLayoutManager(this)
        binding.playlist.adapter = VideoAdapter(videos) { play(it) }
        refreshPlaylist()
        if (videos.isNotEmpty()) play(0)
    }

    private fun play(index: Int) {
        if (index !in videos.indices) return
        player.setMediaItem(MediaItem.fromUri(videos[index].uri))
        player.prepare()
        player.play()
    }

    private fun rotateScreen() {
        rotationMode = (rotationMode + 1) % 3
        when (rotationMode) {
            0 -> { requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED; binding.rotateButton.text = "↻ אוטו" }
            1 -> { requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT; binding.rotateButton.text = "↕ לאורך" }
            else -> { requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE; binding.rotateButton.text = "↔ לרוחב" }
        }
    }

    private fun toggleFullscreen() {
        fullscreen = !fullscreen
        val visible = if (fullscreen) View.GONE else View.VISIBLE
        binding.toolbar.visibility = visible
        binding.emptyText.visibility = if (fullscreen) View.GONE else if (videos.isEmpty()) View.VISIBLE else View.GONE
        binding.photoTitle.visibility = visible
        binding.photoGallery.visibility = visible
        binding.playlist.visibility = visible
        if (fullscreen) {
            window.insetsController?.hide(WindowInsets.Type.systemBars())
        } else {
            window.insetsController?.show(WindowInsets.Type.systemBars())
        }
    }

    private fun showPhotoFullscreen(url: String) {
        val dialog = android.app.Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val image = ImageView(this).apply {
            setBackgroundColor(android.graphics.Color.BLACK)
            scaleType = ImageView.ScaleType.FIT_CENTER
            isClickable = true
            setOnClickListener { dialog.dismiss() }
        }
        dialog.setContentView(image)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setOnShowListener {
            dialog.window?.apply {
                setBackgroundDrawableResource(android.R.color.black)
                setLayout(-1, -1)
                decorView.systemUiVisibility = 5894
            }
        }
        Glide.with(this).load(url).error(android.R.drawable.ic_menu_report_image).fitCenter().into(image)
        dialog.show()
    }

    private fun refreshPlaylist() {
        binding.playlist.adapter?.notifyDataSetChanged()
        binding.emptyText.visibility = if (videos.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroy() {
        player.release()
        super.onDestroy()
    }
}