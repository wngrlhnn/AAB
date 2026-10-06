plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val offlineVideos = mapOf(
    "romantic_1.mp4" to "https://videos.pexels.com/video-files/5304017/5304017-uhd_4096_2160_30fps.mp4",
    "romantic_2.mp4" to "https://videos.pexels.com/video-files/8451945/8451945-uhd_2160_3840_25fps.mp4",
    "romantic_3.mp4" to "https://videos.pexels.com/video-files/6718243/6718243-uhd_4096_2160_25fps.mp4",
    "romantic_4.mp4" to "https://videos.pexels.com/video-files/9500161/9500161-uhd_4096_2160_30fps.mp4"
)

val downloadOfflineVideos = tasks.register("downloadOfflineVideos") {
    outputs.files(offlineVideos.keys.map { file("src/main/res/raw/$it") })
    doLast {
        val rawDir = file("src/main/res/raw")
        rawDir.mkdirs()
        offlineVideos.forEach { (name, url) ->
            val target = file("src/main/res/raw/$name")
            if (!target.exists() || target.length() == 0L) {
                println("Downloading $name for offline playback...")
                java.net.URI(url).toURL().openStream().use { input ->
                    target.outputStream().use { output -> input.copyTo(output) }
                }
            }
        }
    }
}

android {
    namespace = "com.wngrlhnn.adultvideoplayer"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.wngrlhnn.adultvideoplayer"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "1.1"
    }
    buildFeatures { viewBinding = true }
}

tasks.named("preBuild") {
    dependsOn(downloadOfflineVideos)
}

dependencies {
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.activity:activity-ktx:1.11.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.media3:media3-exoplayer:1.8.0")
    implementation("androidx.media3:media3-ui:1.8.0")
}
