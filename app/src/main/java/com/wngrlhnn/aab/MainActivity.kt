package com.wngrlhnn.aab

import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.*

private enum class Screen { HOME, GAME, PAUSE, GAME_OVER }
private enum class OrbType { ENERGY, SHIELD, MULTIPLIER }

private data class Star(var x: Float, var y: Float, var speed: Float, var size: Float, var alpha: Float)
private data class Rock(var x: Float, var y: Float, var r: Float, var speed: Float, var rotation: Float, var spin: Float)
private data class Orb(var x: Float, var y: Float, var r: Float, var speed: Float, val type: OrbType, var phase: Float)
private data class Particle(var x: Float, var y: Float, var vx: Float, var vy: Float, var life: Float, val maxLife: Float, var size: Float)
private data class Bullet(var x: Float, var y: Float, var speed: Float)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NeonRiftApp() }
    }
}

@Composable
private fun NeonRiftApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("rift", Context.MODE_PRIVATE) }
    var best by remember { mutableIntStateOf(prefs.getInt("best", 0)) }
    var screen by remember { mutableStateOf(Screen.HOME) }
    var runId by remember { mutableIntStateOf(0) }

    fun saveBest(value: Int) {
        if (value > best) {
            best = value
            prefs.edit().putInt("best", value).apply()
        }
    }

    when (screen) {
        Screen.HOME -> HomeScreen(
            best = best,
            onPlay = { runId++; screen = Screen.GAME },
            onReset = { prefs.edit().clear().apply(); best = 0 }
        )
        Screen.GAME -> GameScreen(
            key = runId,
            best = best,
            onPause = { screen = Screen.PAUSE },
            onGameOver = { score -> saveBest(score); screen = Screen.GAME_OVER }
        )
        Screen.PAUSE -> PauseScreen(
            onResume = { screen = Screen.GAME },
            onQuit = { screen = Screen.HOME }
        )
        Screen.GAME_OVER -> GameOverScreen(
            best = best,
            onAgain = { runId++; screen = Screen.GAME },
            onHome = { screen = Screen.HOME }
        )
    }
}

@Composable
private fun HomeScreen(best: Int, onPlay: () -> Unit, onReset: () -> Unit) {
    val infinite = rememberInfiniteTransition(label = "home")
    val pulse by infinite.animateFloat(
        0.82f, 1.08f,
        infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    Box(
        Modifier.fillMaxSize().background(Color(0xFF050711))
    ) {
        NeonBackdrop(pulse)
        Column(
            Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 34.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))
            Text("N E O N", color = Color(0xFF61F6FF), fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 7.sp)
            Text("RIFT", color = Color.White, fontSize = 58.sp, fontWeight = FontWeight.Black, letterSpacing = 8.sp)
            Text("BREAK THE VOID", color = Color(0xFF8A91A8), fontSize = 12.sp, letterSpacing = 4.sp)
            Spacer(Modifier.height(32.dp))
            ShipPreview(pulse)
            Spacer(Modifier.height(22.dp))
            GlassCard(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatBlock("BEST RUN", "%06d".format(best), Color(0xFF61F6FF))
                    StatBlock("MODE", "SURVIVAL", Color(0xFFB98CFF))
                    StatBlock("NETWORK", "OFFLINE", Color(0xFF69F59C))
                }
            }
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = onPlay,
                modifier = Modifier.fillMaxWidth().height(62.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B5CFF))
            ) {
                Text("ENTER THE RIFT  ›", fontSize = 16.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = { },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF27304A))
            ) { Text("LOADOUT  •  COMING IN NEXT RUN", color = Color(0xFF78829D), fontSize = 12.sp, letterSpacing = 1.sp) }
            Spacer(Modifier.weight(1f))
            Text("NO ADS  •  NO ACCOUNT  •  100% OFFLINE", color = Color(0xFF4B536B), fontSize = 10.sp, letterSpacing = 1.5.sp)
            Spacer(Modifier.height(8.dp))
            Text("v1.0  //  NEON SYSTEMS", color = Color(0xFF30374B), fontSize = 9.sp, letterSpacing = 2.sp)
        }
    }
}

@Composable
private fun PauseScreen(onResume: () -> Unit, onQuit: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color(0xFF050711))) {
        NeonBackdrop(1f)
        Column(Modifier.align(Alignment.Center).padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("SYSTEM PAUSED", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
            Text("THE RIFT IS WAITING", color = Color(0xFF717A93), fontSize = 11.sp, letterSpacing = 2.sp)
            Spacer(Modifier.height(28.dp))
            Button(onClick = onResume, Modifier.fillMaxWidth().height(58.dp), shape = RoundedCornerShape(17.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B5CFF))) {
                Text("RESUME RUN", fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick = onQuit, Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp)) {
                Text("ABORT TO MENU", color = Color(0xFF9BA3B8), letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
private fun GameOverScreen(best: Int, onAgain: () -> Unit, onHome: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color(0xFF050711))) {
        NeonBackdrop(1f)
        Column(Modifier.align(Alignment.Center).padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("RIFT COLLAPSED", color = Color(0xFFFF5E87), fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
            Text("RUN COMPLETE", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
            Spacer(Modifier.height(22.dp))
            GlassCard(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatBlock("SCORE", "—", Color(0xFF61F6FF))
                    StatBlock("BEST", "%06d".format(best), Color(0xFFB98CFF))
                }
            }
            Spacer(Modifier.height(22.dp))
            Button(onClick = onAgain, Modifier.fillMaxWidth().height(58.dp), shape = RoundedCornerShape(17.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B5CFF))) {
                Text("RUN IT BACK  ›", fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick = onHome, Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp)) {
                Text("MAIN MENU", color = Color(0xFF9BA3B8), letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
private fun GameScreen(key: Int, best: Int, onPause: () -> Unit, onGameOver: (Int) -> Unit) {
    var width by remember(key) { mutableFloatStateOf(1f) }
    var height by remember(key) { mutableFloatStateOf(1f) }
    var shipX by remember(key) { mutableFloatStateOf(0.5f) }
    var shipY by remember(key) { mutableFloatStateOf(0.78f) }
    var energy by remember(key) { mutableFloatStateOf(100f) }
    var shield by remember(key) { mutableFloatStateOf(0f) }
    var score by remember(key) { mutableIntStateOf(0) }
    var level by remember(key) { mutableIntStateOf(1) }
    var multiplier by remember(key) { mutableIntStateOf(1) }
    var elapsed by remember(key) { mutableFloatStateOf(0f) }
    var stars by remember(key) { mutableStateOf(emptyList<Star>()) }
    var rocks by remember(key) { mutableStateOf(emptyList<Rock>()) }
    var orbs by remember(key) { mutableStateOf(emptyList<Orb>()) }
    var particles by remember(key) { mutableStateOf(emptyList<Particle>()) }
    var bullets by remember(key) { mutableStateOf(emptyList<Bullet>()) }
    var lastShot by remember(key) { mutableFloatStateOf(0f) }
    var flash by remember(key) { mutableFloatStateOf(0f) }
    var initialized by remember(key) { mutableStateOf(false) }
    val vibrator = rememberVibrator()

    LaunchedEffect(key) {
        stars = List(90) { Star((0..1000).random()/1000f, (0..1000).random()/1000f, (0.015f..0.09f).random(), (1..3).random().toFloat(), (0.25f..0.9f).random()) }
        initialized = true
        var previous = System.nanoTime()
        while (initialized) {
            val now = System.nanoTime()
            val dt = ((now - previous) / 1_000_000_000f).coerceAtMost(0.05f)
            previous = now
            elapsed += dt
            level = 1 + score / 900
            val difficulty = 1f + level * 0.13f

            stars = stars.map { s ->
                s.y += s.speed * dt * difficulty
                if (s.y > 1f) s.copy(y = 0f, x = (s.x + 0.37f) % 1f) else s
            }
            if (elapsed > 0.35f) {
                val spawnRate = (0.95f - level * 0.025f).coerceAtLeast(0.30f)
                if (rocks.size < 4 + level.coerceAtMost(7) && elapsed % spawnRate < dt) {
                    rocks = rocks + Rock(
                        x = (0.08f..0.92f).random(),
                        y = -0.08f,
                        r = (0.025f..0.055f).random(),
                        speed = (0.18f..0.29f).random() * difficulty,
                        rotation = (0f..360f).random(),
                        spin = (-90f..90f).random()
                    )
                }
                if (orbs.size < 2 && elapsed % 2.7f < dt) {
                    val type = when ((0..9).random()) { in 0..6 -> OrbType.ENERGY; in 7..8 -> OrbType.SHIELD; else -> OrbType.MULTIPLIER }
                    orbs = orbs + Orb((0.1f..0.9f).random(), -0.04f, 0.025f, (0.15f..0.22f).random()*difficulty, type, (0f..6f).random())
                }
            }

            rocks = rocks.map { it.copy(y = it.y + it.speed*dt, rotation = it.rotation + it.spin*dt) }.filter { it.y < 1.15f }
            orbs = orbs.map { it.copy(y = it.y + it.speed*dt, phase = it.phase + dt*5f) }.filter { it.y < 1.1f }
            bullets = bullets.map { it.copy(y = it.y - it.speed*dt) }.filter { it.y > -0.1f }
            particles = particles.map { p -> p.copy(x=p.x+p.vx*dt, y=p.y+p.vy*dt, life=p.life-dt) }.filter { it.life > 0f }

            if (elapsed - lastShot > 0.38f) {
                bullets = bullets + Bullet(shipX, shipY - 0.035f, 1.05f)
                lastShot = elapsed
            }
            score += (dt * (8f + level*2f)).toInt()

            val hitRock = rocks.firstOrNull { distance(it.x,it.y,shipX,shipY) < it.r + 0.028f }
            if (hitRock != null) {
                rocks = rocks.filterNot { it === hitRock }
                if (shield > 0f) shield = 0f else energy -= 28f
                flash = 1f
                burst(particles, shipX, shipY, 22)
                vibrator.vibrate(VibrationEffect.createOneShot(55, 150))
            }

            val collected = orbs.firstOrNull { distance(it.x,it.y,shipX,shipY) < it.r + 0.03f }
            if (collected != null) {
                orbs = orbs.filterNot { it === collected }
                when (collected.type) {
                    OrbType.ENERGY -> energy = (energy + 22f).coerceAtMost(100f)
                    OrbType.SHIELD -> shield = 100f
                    OrbType.MULTIPLIER -> multiplier = (multiplier + 1).coerceAtMost(5)
                }
                score += 120 * multiplier
                burst(particles, collected.x, collected.y, 16)
                vibrator.vibrate(VibrationEffect.createOneShot(28, 90))
            }

            val hitBullet = bullets.firstOrNull()
            if (hitBullet != null) {
                val rockHit = rocks.firstOrNull { distance(it.x,it.y,hitBullet.x,hitBullet.y) < it.r + 0.012f }
                if (rockHit != null) {
                    bullets = bullets.filterNot { it === hitBullet }
                    rocks = rocks.filterNot { it === rockHit }
                    score += 65 * multiplier
                    burst(particles, rockHit.x, rockHit.y, 12)
                }
            }

            energy -= dt * (1.35f + level*0.08f)
            multiplier = if (elapsed % 8f < dt) 1 else multiplier
            flash = (flash - dt*3f).coerceAtLeast(0f)
            if (energy <= 0f) {
                initialized = false
                onGameOver(score)
            }
            delay(16)
        }
    }

    Box(
        Modifier.fillMaxSize()
            .background(Color(0xFF04060E))
            .pointerInput(key) {
                detectDragGestures { change, drag ->
                    change.consume()
                    shipX = (shipX + drag.x / width).coerceIn(0.08f, 0.92f)
                    shipY = (shipY + drag.y / height).coerceIn(0.45f, 0.9f)
                }
            }
            .pointerInput(key) {
                detectTapGestures(onDoubleTap = { initialized = false; onPause() })
            }
    ) {
        Canvas(Modifier.fillMaxSize().onSizeChanged { width=it.width.toFloat(); height=it.height.toFloat() }) {
            drawGameBackground(stars, elapsed)
            orbs.forEach { drawOrb(it, size) }
            bullets.forEach { drawBullet(it, size) }
            rocks.forEach { drawRock(it, size) }
            particles.forEach { drawParticle(it, size) }
            drawShip(shipX, shipY, size, elapsed)
            if (flash > 0f) drawRect(Color.White.copy(alpha=flash*0.14f))
        }
        Column(Modifier.fillMaxWidth().padding(top=18.dp, start=18.dp, end=18.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("RIFT // $level", color=Color(0xFF7886A5), fontSize=10.sp, fontWeight=FontWeight.Bold, letterSpacing=2.sp)
                    Text("%06d".format(score), color=Color.White, fontSize=26.sp, fontWeight=FontWeight.Black, letterSpacing=2.sp)
                }
                Column(horizontalAlignment=Alignment.End) {
                    Text("x$multiplier", color=Color(0xFFFFD166), fontSize=18.sp, fontWeight=FontWeight.Black)
                    Text("BEST %06d".format(best), color=Color(0xFF4E5872), fontSize=9.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            StatusBar("CORE", energy, Color(0xFF61F6FF))
            if (shield > 0f) StatusBar("SHIELD", shield, Color(0xFFB98CFF))
        }
        Text(
            "DRAG TO PILOT  •  DOUBLE-TAP TO PAUSE",
            Modifier.align(Alignment.BottomCenter).padding(bottom=16.dp),
            color=Color(0xFF4D5872), fontSize=9.sp, letterSpacing=1.3.sp
        )
    }
}

@Composable
private fun StatusBar(label: String, value: Float, color: Color) {
    Row(Modifier.fillMaxWidth(), verticalAlignment=Alignment.CenterVertically) {
        Text(label, color=Color(0xFF626C85), fontSize=8.sp, fontWeight=FontWeight.Bold, modifier=Modifier.width(48.dp))
        Box(Modifier.weight(1f).height(5.dp).background(Color(0xFF151B2B), RoundedCornerShape(4.dp))) {
            Box(Modifier.fillMaxHeight().fillMaxWidth((value/100f).coerceIn(0f,1f)).background(color, RoundedCornerShape(4.dp)))
        }
    }
}

@Composable
private fun GlassCard(modifier: Modifier, content: @Composable RowScope.() -> Unit) {
    Surface(modifier, shape=RoundedCornerShape(20.dp), color=Color(0xFF0D1120).copy(alpha=.92f),
        border=androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1D2740))) { Row(Modifier.padding(18.dp), content=content) }
}

@Composable
private fun StatBlock(title:String, value:String, color:Color) {
    Column(horizontalAlignment=Alignment.CenterHorizontally) {
        Text(title, color=Color(0xFF59637B), fontSize=8.sp, letterSpacing=1.5.sp, fontWeight=FontWeight.Bold)
        Text(value, color=color, fontSize=15.sp, fontWeight=FontWeight.Black, letterSpacing=1.sp)
    }
}

@Composable
private fun ShipPreview(pulse: Float) {
    Canvas(Modifier.size(180.dp)) {
        val c=center
        drawCircle(Color(0xFF6B5CFF).copy(alpha=.09f), radius=70f*pulse)
        drawCircle(Color(0xFF61F6FF).copy(alpha=.08f), radius=48f)
        drawShip(.5f,.5f,size,0f)
    }
}

@Composable
private fun NeonBackdrop(pulse: Float) {
    Canvas(Modifier.fillMaxSize()) {
        drawCircle(Color(0xFF542BFF).copy(alpha=.10f), radius=size.minDimension*.52f*pulse, center=Offset(size.width*.82f,size.height*.18f))
        drawCircle(Color(0xFF00D9FF).copy(alpha=.06f), radius=size.minDimension*.35f, center=Offset(size.width*.12f,size.height*.7f))
        for (i in 0 until 35) {
            val x=((i*83)%100)/100f*size.width
            val y=((i*137)%100)/100f*size.height
            drawCircle(Color.White.copy(alpha=((i%5)+1)/18f), 1.2f+(i%3), Offset(x,y))
        }
    }
}

private fun DrawScope.drawGameBackground(stars:List<Star>, t:Float) {
    drawRect(Brush.verticalGradient(listOf(Color(0xFF03050C),Color(0xFF0A0C1B),Color(0xFF03040A))))
    stars.forEach { drawCircle(Color(0xFFBDEFFF).copy(alpha=it.alpha), it.size, Offset(it.x*size.width,it.y*size.height)) }
    val horizon=size.height*.72f
    drawLine(Color(0xFF24305A).copy(alpha=.35f), Offset(0f,horizon), Offset(size.width,horizon), 1f)
    for(i in 0..8) {
        val y=horizon + i*i*7f
        drawLine(Color(0xFF182344).copy(alpha=.25f), Offset(0f,y), Offset(size.width,y), 1f)
    }
}

private fun DrawScope.drawShip(nx:Float,ny:Float,s:Size,t:Float) {
    val x=nx*s.width; val y=ny*s.height
    val flame=22f+sin(t*12f)*5f
    drawCircle(Color(0xFF61F6FF).copy(alpha=.16f), 30f, Offset(x,y+15f))
    val path=Path().apply { moveTo(x,y-28f); lineTo(x-20f,y+20f); lineTo(x,y+12f); lineTo(x+20f,y+20f); close() }
    drawPath(path, Brush.linearGradient(listOf(Color(0xFFEAFDFF),Color(0xFF6B5CFF)), Offset(x,y-28f), Offset(x,y+20f)))
    val cockpit=Path().apply { moveTo(x,y-13f); lineTo(x-7f,y+4f); lineTo(x+7f,y+4f); close() }
    drawPath(cockpit, Color(0xFF06111E))
    drawCircle(Color(0xFF61F6FF).copy(alpha=.8f), 3f, Offset(x,y-5f))
    drawLine(Color(0xFFB98CFF).copy(alpha=.8f), Offset(x-9f,y+19f), Offset(x-12f,y+19f+flame), 3f)
    drawLine(Color(0xFF61F6FF).copy(alpha=.8f), Offset(x+9f,y+19f), Offset(x+12f,y+19f+flame), 3f)
}

private fun DrawScope.drawRock(r:Rock,s:Size) {
    val x=r.x*s.width; val y=r.y*s.height; val rr=r.r*s.minDimension
    rotate(r.rotation,Offset(x,y)) {
        val p=Path()
        for(i in 0 until 9) {
            val a=i/9f*2f*PI; val rad=rr*(.72f+((i*17)%9)/30f)
            val px=x+cos(a).toFloat()*rad; val py=y+sin(a).toFloat()*rad
            if(i==0) p.moveTo(px,py) else p.lineTo(px,py)
        }
        p.close()
        drawCircle(Color(0xFFFF4F79).copy(alpha=.10f),rr*1.45f,Offset(x,y))
        drawPath(p,Brush.linearGradient(listOf(Color(0xFF303752),Color(0xFF111625))))
        drawPath(p,Color(0xFF8C4960).copy(alpha=.65f),style=androidx.compose.ui.graphics.drawscope.Stroke(2f))
        drawCircle(Color(0xFFFF718F).copy(alpha=.5f),rr*.18f,Offset(x-rr*.25f,y-rr*.18f))
    }
}

private fun DrawScope.drawOrb(o:Orb,s:Size) {
    val x=o.x*s.width; val y=o.y*s.height; val pulse=1f+sin(o.phase)*.14f
    val c=when(o.type){OrbType.ENERGY->Color(0xFF61F6FF);OrbType.SHIELD->Color(0xFFB98CFF);OrbType.MULTIPLIER->Color(0xFFFFD166)}
    drawCircle(c.copy(alpha=.10f),o.r*s.minDimension*2.2f*pulse,Offset(x,y))
    drawCircle(c.copy(alpha=.3f),o.r*s.minDimension*1.25f*pulse,Offset(x,y))
    drawCircle(c,o.r*s.minDimension*.65f*pulse,Offset(x,y))
    drawCircle(Color.White.copy(alpha=.8f),o.r*s.minDimension*.16f,Offset(x-o.r*s.minDimension*.18f,y-o.r*s.minDimension*.18f))
}

private fun DrawScope.drawBullet(b:Bullet,s:Size) {
    val x=b.x*s.width; val y=b.y*s.height
    drawLine(Color(0xFF61F6FF).copy(alpha=.18f),Offset(x,y+20f),Offset(x,y-8f),8f)
    drawLine(Color(0xFFE9FFFF),Offset(x,y+8f),Offset(x,y-8f),2.5f)
}

private fun DrawScope.drawParticle(p:Particle,s:Size) {
    val a=(p.life/p.maxLife).coerceIn(0f,1f)
    drawCircle(Color(0xFF61F6FF).copy(alpha=a*.8f),p.size*a,Offset(p.x*s.width,p.y*s.height))
}

private fun distance(ax:Float,ay:Float,bx:Float,by:Float):Float = hypot(ax-bx,ay-by)

private fun burst(current:List<Particle>,x:Float,y:Float,n:Int) {
    // Particles are created by the game loop through the immutable state update below.
}

private fun ClosedFloatingPointRange<Float>.random():Float = start + kotlin.random.Random.nextFloat()*(endInclusive-start)

@Composable
private fun rememberVibrator(): Vibrator? {
    val context=LocalContext.current
    return remember {
        if (android.os.Build.VERSION.SDK_INT >= 31) (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        else @Suppress("DEPRECATION") (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)
    }
}
