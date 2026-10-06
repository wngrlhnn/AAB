package com.wngrlhnn.adultvideoplayer

import android.net.Uri

data class VideoEntry(val title: String, val uri: Uri, val source: String = "local")