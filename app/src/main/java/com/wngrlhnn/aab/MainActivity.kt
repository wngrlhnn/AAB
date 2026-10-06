package com.wngrlhnn.aab

import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import kotlin.math.*
import kotlin.random.Random
import kotlinx.coroutines.delay

private data class Star(var x:Float,var y:Float,var r:Float,var speed:Float)
private data class Rock(var x:Float,var y:Float,var size:Float,var speed:Float,var rot:Float,var spin:Float)
private data class Orb(var x:Float,var y:Float,var type:Int,var speed:Float,var pulse:Float=0f)
private enum class Screen { HOME, PLAY, PAUSE, GAME_OVER }

class MainActivity:ComponentActivity(){
 override fun onCreate(b:Bundle?){super.onCreate(b);setContent{NeonDrift()}}
}

@Composable
private fun NeonDrift(){
 val context=LocalContext.current
 var screen by remember{mutableStateOf(Screen.HOME)}
 var score by remember{mutableIntStateOf(0)}
 var best by remember{mutableIntStateOf(context.getPreferences(0).getInt("best",0))}
 var level by remember{mutableIntStateOf(1)}
 var energy by remember{mutableFloatStateOf(1f)}
 var shield by remember{mutableIntStateOf(0)}
 var px by remember{mutableFloatStateOf(.5f)}
 var py by remember{mutableFloatStateOf(.78f)}
 var shake by remember{mutableFloatStateOf(0f)}
 var flash by remember{mutableFloatStateOf(0f)}
 val stars=remember{mutableStateListOf<Star>()}
 val rocks=remember{mutableStateListOf<Rock>()}
 val orbs=remember{mutableStateListOf<Orb>()}

 fun start(){
  score=0;level=1;energy=1f;shield=0;px=.5f;py=.78f;shake=0f;flash=0f
  stars.clear();rocks.clear();orbs.clear()
  repeat(90){stars.add(Star(Random.nextFloat(),Random.nextFloat(),Random.nextFloat()*2.3f+.5f,Random.nextFloat()*.15f+.02f))}
  screen=Screen.PLAY
 }

 LaunchedEffect(screen){
  var last=System.nanoTime()
  while(screen==Screen.PLAY){
   val now=System.nanoTime();val dt=min(.04,(now-last)/1_000_000_000.0).toFloat();last=now
   stars.forEach{it.y+=it.speed*dt*(1+level*.07f);if(it.y>1.02f){it.y=-.02f;it.x=Random.nextFloat()}}
   rocks.forEach{it.y+=it.speed*dt;it.rot+=it.spin*dt}
   orbs.forEach{it.y+=it.speed*dt;it.pulse+=dt*5}
   rocks.removeAll{it.y>1.12f};orbs.removeAll{it.y>1.12f}
   if(Random.nextFloat()<dt/(.34f/(1+level*.035f)))
    rocks.add(Rock(Random.nextFloat()*.86f+.07f,-.08f,Random.nextFloat()*.035f+.035f,Random.nextFloat()*.20f+.09f,Random.nextFloat()*18+15,Random.nextFloat()*180-90))
   if(Random.nextFloat()<dt*.85f)
    orbs.add(Orb(Random.nextFloat()*.82f+.09f,-.05f,if(Random.nextFloat()<.18)1 else 0,Random.nextFloat()*12+7))
   val hit=rocks.firstOrNull{abs(it.x-px)<it.size*.72f&&abs(it.y-py)<it.size*.72f}
   if(hit!=null){
    rocks.remove(hit)
    if(shield>0)shield-- else{energy-=.26f;shake=1f;flash=1f;vibrate(context,35)}
   }
   val got=orbs.filter{abs(it.x-px)<.065f&&abs(it.y-py)<.065f}
   got.forEach{if(it.type==0){score+=10;energy=min(1f,energy+.045f)}else{score+=40;shield=min(3,shield+1)};flash=1f;vibrate(context,12)}
   orbs.removeAll(got)
   score+=(dt*(6+level*1.4f)).toInt();level=1+score/900;energy=max(0f,energy-dt*.006f)
   if(energy<=0f){
    if(score>best){best=score;context.getPreferences(0).edit().putInt("best",score).apply()}
    screen=Screen.GAME_OVER
   }
   shake=max(0f,shake-dt*3.2f);flash=max(0f,flash-dt*4f);delay(16)
  }
 }

 Box(Modifier.fillMaxSize().background(Color(0xFF03040A))){
  when(screen){
   Screen.HOME->Home(best,::start)
   Screen.PLAY,Screen.PAUSE->Game(px,py,score,best,level,energy,shield,stars,rocks,orbs,shake,flash,screen==Screen.PAUSE,{x,y->px=x.coerceIn(.1f,.9f);py=y.coerceIn(.18f,.9f)},{screen=if(screen==Screen.PLAY)Screen.PAUSE else Screen.PLAY})
   Screen.GAME_OVER->Over(score,best,level,::start){screen=Screen.HOME}
  }
 }
}

private fun vibrate(c:Context,ms:Long){val v=c.getSystemService(Vibrator::class.java)?:return;if(v.hasVibrator())v.vibrate(VibrationEffect.createOneShot(ms,VibrationEffect.DEFAULT_AMPLITUDE))}

@Composable private fun Home(best:Int,play:()->Unit){
 Box(Modifier.fillMaxSize()){
  Canvas(Modifier.fillMaxSize()){backdrop(1f)}
  Column(Modifier.fillMaxSize().padding(28.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){
   Text("NEON",color=Color(0xFF6CF6FF),fontSize=18.sp,fontWeight=FontWeight.Bold,letterSpacing=8.sp)
   Text("DRIFT",color=Color.White,fontSize=58.sp,fontWeight=FontWeight.Black,letterSpacing=2.sp)
   Text("SURVIVE THE VOID",color=Color(0xFF8D92A8),fontSize=12.sp,letterSpacing=3.sp)
   Spacer(Modifier.height(42.dp))
   Button(play,Modifier.fillMaxWidth().height(62.dp),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF7657FF))){Text("PLAY NOW",fontSize=18.sp,fontWeight=FontWeight.Bold,letterSpacing=2.sp)}
   Spacer(Modifier.height(20.dp));Text("BEST  "+best.toString().padStart(6,'0'),color=Color(0xFFB7BBCB),fontWeight=FontWeight.Bold)
   Spacer(Modifier.height(38.dp));Text("OFFLINE • NO ADS • NO ACCOUNT",color=Color(0xFF555A70),fontSize=10.sp,letterSpacing=1.5.sp)
  }
 }
}

@Composable private fun Game(px:Float,py:Float,score:Int,best:Int,level:Int,energy:Float,shield:Int,stars:List<Star>,rocks:List<Rock>,orbs:List<Orb>,shake:Float,flash:Float,paused:Boolean,move:(Float,Float)->Unit,pause:()->Unit){
 Box(Modifier.fillMaxSize().pointerInput(Unit){detectDragGestures{c,_->move(c.position.x/size.width,c.position.y/size.height)}}.pointerInput(Unit){detectTapGestures(onDoubleTap={pause()})}){
  Canvas(Modifier.fillMaxSize()){
   backdrop(level/10f);drawStars(stars);orbs.forEach{orb(it)};rocks.forEach{rock(it)};ship(px,py,shield);hud(score,best,level,energy,shield)
   if(flash>0)drawRect(Color.White.copy(alpha=flash*.10f))
   if(paused)drawRect(Color.Black.copy(alpha=.55f))
  }
  if(paused)Column(Modifier.align(Alignment.Center),horizontalAlignment=Alignment.CenterHorizontally){Text("PAUSED",color=Color.White,fontSize=34.sp,fontWeight=FontWeight.Black,letterSpacing=5.sp);Spacer(Modifier.height(18.dp));Text("DOUBLE TAP TO RESUME",color=Color(0xFF8D92A8),fontSize=11.sp,letterSpacing=2.sp)}
 }
}

private fun DrawScope.backdrop(i:Float){
 drawRect(Brush.verticalGradient(listOf(Color(0xFF070A18),Color(0xFF02030A))))
 drawCircle(Color(0xFF4637B5).copy(alpha=.10f+i*.025f),size.minDimension*.55f,Offset(size.width*.72f,size.height*.18f))
 drawCircle(Color(0xFF00C8FF).copy(alpha=.055f),size.minDimension*.42f,Offset(size.width*.18f,size.height*.72f))
 for(n in 1..9){val y=size.height*n/10f;drawLine(Color.White.copy(alpha=.018f),Offset(0f,y),Offset(size.width,y),1f)}
}
private fun DrawScope.drawStars(s:List<Star>){s.forEach{drawCircle(Color.White.copy(alpha=(.25f+it.r/5f).coerceAtMost(.8f)),it.r,Offset(it.x*size.width,it.y*size.height))}}
private fun DrawScope.orb(o:Orb){
 val c=if(o.type==0)Color(0xFF62F5FF)else Color(0xFFFFC857);val x=o.x*size.width;val y=o.y*size.height;val p=1f+sin(o.pulse).toFloat()*.18f
 drawCircle(c.copy(alpha=.12f),25*p,Offset(x,y));drawCircle(c.copy(alpha=.25f),14*p,Offset(x,y));drawCircle(c,5*p,Offset(x,y));drawCircle(Color.White.copy(alpha=.8f),2f,Offset(x-1,y-2))
}
private fun DrawScope.rock(r:Rock){
 val x=r.x*size.width;val y=r.y*size.height;val s=r.size*size.minDimension
 rotate(r.rot,Offset(x,y)){val p=Path().apply{moveTo(x,y-s);lineTo(x+s*.72f,y-s*.35f);lineTo(x+s*.62f,y+s*.72f);lineTo(x-s*.42f,y+s);lineTo(x-s*.85f,y+s*.12f);close()}
 drawPath(p,Brush.linearGradient(listOf(Color(0xFF50576E),Color(0xFF151927))));drawPath(p,Color(0xFF9AA2B8).copy(alpha=.35f),style=androidx.compose.ui.graphics.drawscope.Stroke(1.5f))
 }
}
private fun DrawScope.ship(px:Float,py:Float,shield:Int){
 val x=px*size.width;val y=py*size.height;val s=size.minDimension*.055f
 val flame=Path().apply{moveTo(x-s*.22f,y+s*.62f);lineTo(x,y+s*(1.15f+Random.nextFloat()*.3f));lineTo(x+s*.22f,y+s*.62f);close()};drawPath(flame,Brush.verticalGradient(listOf(Color(0xFFFFE66D),Color(0xFFFF4D9D),Color.Transparent)))
 val body=Path().apply{moveTo(x,y-s);lineTo(x+s*.62f,y+s*.65f);lineTo(x,y+s*.40f);lineTo(x-s*.62f,y+s*.65f);close()};drawPath(body,Brush.linearGradient(listOf(Color.White,Color(0xFF75D9FF),Color(0xFF6655FF))));drawPath(body,Color(0xFFBDF7FF),style=androidx.compose.ui.graphics.drawscope.Stroke(2f))
 drawCircle(Color(0xFF081020),s*.23f,Offset(x,y-s*.12f));drawCircle(Color(0xFF75F6FF),s*.11f,Offset(x,y-s*.12f))
 if(shield>0){drawCircle(Color(0xFF6CF6FF).copy(alpha=.12f),s*1.55f,Offset(x,y));drawCircle(Color(0xFF6CF6FF).copy(alpha=.5f),s*1.35f,Offset(x,y),style=androidx.compose.ui.graphics.drawscope.Stroke(2f))}
}
private fun DrawScope.hud(score:Int,best:Int,level:Int,energy:Float,shield:Int){
 val pad=22f;drawContext.canvas.nativeCanvas.apply{val p=android.graphics.Paint(1);p.typeface=android.graphics.Typeface.DEFAULT_BOLD;p.color=android.graphics.Color.WHITE;p.textSize=34f;drawText(score.toString().padStart(6,'0'),pad,48f,p);p.color=android.graphics.Color.LTGRAY;p.textSize=18f;drawText("LV $level",pad,76f,p);p.textSize=13f;drawText("BEST $best",size.width-110f,42f,p)}
 drawRoundRect(Color.White.copy(alpha=.10f),Offset(pad,size.height-30f),Offset(size.width-pad,size.height-18f),6f,6f)
 drawRoundRect(Brush.horizontalGradient(listOf(Color(0xFF6CF6FF),Color(0xFF7657FF))),Offset(pad,size.height-30f),Offset(pad+(size.width-2*pad)*energy,size.height-18f),6f,6f)
 if(shield>0)drawContext.canvas.nativeCanvas.apply{val p=android.graphics.Paint(1);p.color=android.graphics.Color.WHITE;p.textSize=13f;p.typeface=android.graphics.Typeface.DEFAULT_BOLD;drawText("SHIELD x$shield",size.width-100f,size.height-46f,p)}
}

@Composable private fun Over(score:Int,best:Int,level:Int,again:()->Unit,home:()->Unit){
 Box(Modifier.fillMaxSize()){Canvas(Modifier.fillMaxSize()){backdrop(1f)}
  Column(Modifier.align(Alignment.Center).padding(28.dp),horizontalAlignment=Alignment.CenterHorizontally){
   Text("RUN OVER",color=Color.White,fontSize=40.sp,fontWeight=FontWeight.Black,letterSpacing=4.sp);Spacer(Modifier.height(28.dp))
   Text(score.toString().padStart(6,'0'),color=Color(0xFF6CF6FF),fontSize=48.sp,fontWeight=FontWeight.Black);Text("SCORE",color=Color(0xFF7E849B),fontSize=11.sp,letterSpacing=3.sp)
   Spacer(Modifier.height(8.dp));Text("LEVEL $level   •   BEST $best",color=Color(0xFFB7BBCB),fontSize=12.sp);Spacer(Modifier.height(38.dp))
   Button(again,Modifier.fillMaxWidth().height(58.dp),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF7657FF))){Text("RUN AGAIN",fontWeight=FontWeight.Bold,letterSpacing=2.sp)}
   Spacer(Modifier.height(10.dp));OutlinedButton(home,Modifier.fillMaxWidth().height(54.dp)){Text("MAIN MENU")}
  }
 }
}
