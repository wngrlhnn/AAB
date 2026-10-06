package com.wngrlhnn.aab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade

data class GifItem(val title: String, val url: String)

private val demoGifs = listOf(
    GifItem("Hello", "https://media.giphy.com/media/ASd0Ukj0y3qMM/giphy.gif"),
    GifItem("Excited", "https://media.giphy.com/media/5GoVLqeAOo6PK/giphy.gif"),
    GifItem("Wow", "https://media.giphy.com/media/26ufdipQqU2lhNA4g/giphy.gif"),
    GifItem("Happy", "https://media.giphy.com/media/111ebonMs90YLu/giphy.gif"),
    GifItem("Dance", "https://media.giphy.com/media/l0MYt5jPR6QX5pnqM/giphy.gif"),
    GifItem("Laugh", "https://media.giphy.com/media/10JhviFuU2gWD6/giphy.gif")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { GifVaultApp() }
    }
}

@Composable
fun GifVaultApp() {
    val context = LocalContext.current
    var menuOpen by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<GifItem?>(null) }

    val filtered = demoGifs.filter {
        query.isBlank() || it.title.contains(query, ignoreCase = true)
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Color(0xFF09090B),
            surface = Color(0xFF151518),
            primary = Color(0xFF8B5CF6)
        )
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .pointerInput(Unit) {
                    detectDragGestures { _, drag ->
                        if (drag.x > 120 && kotlin.math.abs(drag.y) < 80) {
                            menuOpen = true
                        }
                    }
                }
        ) {
            Column(Modifier.fillMaxSize()) {
                Text(
                    "GIF Vault",
                    fontSize = 28.sp,
                    color = Color.White,
                    modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 12.dp)
                )
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("חיפוש GIFים") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(12.dp))
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(150.dp),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered) { gif ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clickable { selected = gif }
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(gif.url)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = gif.title,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            if (menuOpen) {
                Surface(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(280.dp)
                        .align(Alignment.CenterStart),
                    color = Color(0xFF151518),
                    shadowElevation = 12.dp
                ) {
                    Column(Modifier.padding(22.dp)) {
                        Text("תפריט", color = Color.White, fontSize = 24.sp)
                        Spacer(Modifier.height(20.dp))
                        Text("קטגוריות", color = Color.LightGray)
                        Spacer(Modifier.height(12.dp))
                        Text("🔥 פופולרי", color = Color.White, modifier = Modifier.clickable { menuOpen = false }.padding(8.dp))
                        Text("😂 מצחיק", color = Color.White, modifier = Modifier.clickable { menuOpen = false }.padding(8.dp))
                        Text("❤️ תגובות", color = Color.White, modifier = Modifier.clickable { menuOpen = false }.padding(8.dp))
                    }
                }
            }

            selected?.let { gif ->
                AlertDialog(
                    onDismissRequest = { selected = null },
                    title = { Text(gif.title) },
                    text = {
                        AsyncImage(
                            model = gif.url,
                            contentDescription = gif.title,
                            modifier = Modifier.fillMaxWidth().height(260.dp)
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { selected = null }) { Text("סגור") }
                    }
                )
            }
        }
    }
}
