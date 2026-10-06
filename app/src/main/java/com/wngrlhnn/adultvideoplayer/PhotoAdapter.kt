package com.wngrlhnn.adultvideoplayer

import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PhotoAdapter(private val urls: List<String>) : RecyclerView.Adapter<PhotoAdapter.VH>() {
    class VH(val image: ImageView) : RecyclerView.ViewHolder(image)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val image = ImageView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(180, 220)
            scaleType = ImageView.ScaleType.CENTER_CROP
            setPadding(4, 4, 4, 4)
        }
        return VH(image)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        Glide.with(holder.image)
            .load(urls[position])
            .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
            .into(holder.image)
    }

    override fun getItemCount() = urls.size
}
