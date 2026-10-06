import java.net.URI

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val offlineVideos = mapOf(
    "romantic_1.mp4" to "https://videos.pexels.com/video-files/5928797/5928797-uhd_4096_2160_25fps.mp4",
    "romantic_2.mp4" to "https://videos.pexels.com/video-files/5928084/5928084-uhd_2160_4096_25fps.mp4",
    "romantic_3.mp4" to "https://videos.pexels.com/video-files/13771336/13771336-uhd_2160_3840_24fps.mp4",
    "romantic_4.mp4" to "https://videos.pexels.com/video-files/6626318/6626318-uhd_2160_3840_25fps.mp4"
)

val offlinePhotos = mapOf(
    "charlie_1.jpg" to "https://commons.wikimedia.org/wiki/Special:Redirect/file/Charlie_Hunnam_by_Gage_Skidmore_4.jpg?width=960",
    "charlie_2.jpg" to "https://commons.wikimedia.org/wiki/Special:Redirect/file/Charlie_Hunnam_%2828611323745%29.jpg?width=960",
    "charlie_3.jpg" to "https://commons.wikimedia.org/wiki/Special:Redirect/file/Charlie_Hunnam_%287607394110%29.jpg?width=960",
    "charlie_4.jpg" to "https://commons.wikimedia.org/wiki/Special:Redirect/file/Charlie_Hunnam_%285984665242%29.jpg?width=960",
    "charlie_5.jpg" to "https://commons.wikimedia.org/wiki/Special:Redirect/file/Charlie_Hunnam_%285984100085%29.jpg?width=960",
    "charlie_6.jpg" to "https://commons.wikimedia.org/wiki/Special:Redirect/file/Charlie_Hunnam_%284843187578%29.jpg?width=960",
    "charlie_7.jpg" to "https://commons.wikimedia.org/wiki/Special:Redirect/file/Charlie_Hunnam_%287607394704%29.jpg?width=960",
    "charlie_8.jpg" to "https://commons.wikimedia.org/wiki/Special:Redirect/file/Charlie_Hunnam_%287607406070%29.jpg?width=960",
    "charlie_9.jpg" to "https://commons.wikimedia.org/wiki/Special:Redirect/file/Charlie_Hunnam_%2828611295255%29.jpg?width=960",
    "charlie_10.jpg" to "https://commons.wikimedia.org/wiki/Special:Redirect/file/Charlie_Hunnam_by_Gage_Skidmore_3.jpg?width=960"
)

val downloadOfflineVideos = tasks.register("downloadOfflineVideos") {
    outputs.files(offlineVideos.keys.map { file("src/main/res/raw/$it") })
    doLast {
        val rawDir = file("src/main/res/raw")
        rawDir.mkdirs()
        offlineVideos.forEach { (name, url) ->
            val target = file("src/main/res/raw/$name")
            if (!target.exists() || target.length() == 0L) {
                URI(url).toURL().openStream().use { input ->
                    target.outputStream().use { output -> input.copyTo(output) }
                }
            }
        }
    }
}

val downloadOfflinePhotos = tasks.register("downloadOfflinePhotos") {
    outputs.files(offlinePhotos.keys.map { file("src/main/res/drawable/$it") })
    doLast {
        val drawableDir = file("src/main/res/drawable")
        drawableDir.mkdirs()
        offlinePhotos.forEach { (name, url) ->
            val target = file("src/main/res/drawable/$name")
            if (!target.exists() || target.length() == 0L) {
                URI(url).toURL().openStream().use { input ->
                    target.outputStream().use { output -> input.copyTo(output) }
                }
            }
        }
    }
}

kotlin { jvmToolchain(17) }

android {
    namespace = "com.wngrlhnn.adultvideoplayer"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.wngrlhnn.adultvideoplayer"
        minSdk = 26
        targetSdk = 36
        versionCode = 6
        versionName = "1.5"
    }
    buildFeatures { viewBinding = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

tasks.named("preBuild") {
    dependsOn(downloadOfflineVideos)
    dependsOn(downloadOfflinePhotos)
}

dependencies {
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.activity:activity-ktx:1.11.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.media3:media3-exoplayer:1.8.0")
    implementation("androidx.media3:media3-ui:1.8.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
}