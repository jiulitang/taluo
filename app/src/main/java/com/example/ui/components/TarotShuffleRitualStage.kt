package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.LocalTarotColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay

/**
 * Multi-phase 3D Sacred Tarot Shuffling Animation Stage:
 * Phase 1 (0.00..0.32): Riffle Split & Interleave Weave (交错切牌)
 * Phase 2 (0.32..0.78): Cosmic Astral Vortex Wash (星璇回环洗牌 · 随机正逆位洗牌)
 * Phase 3 (0.78..1.00): Sacred Convergence & Fan Unfold (收拢切牌 · 展开星轨牌扇)
 */
@Composable
fun TarotShuffleRitualStage(
  question: String,
  spreadName: String,
  spreadCardCount: Int,
  onShuffleFinished: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val tarotColors = LocalTarotColors.current
  val density = LocalDensity.current.density
  val haptics = LocalHapticFeedback.current

  var shuffleCycleKey by remember { mutableIntStateOf(0) }
  val shuffleProgress = remember(shuffleCycleKey) { Animatable(0f) }

  val infiniteTransition = rememberInfiniteTransition(label = "magic_circle_spin")
  val circleRotation by
    infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = 360f,
      animationSpec =
        infiniteRepeatable(
          animation = tween(durationMillis = 12000, easing = LinearEasing),
          repeatMode = RepeatMode.Restart,
        ),
      label = "circle_rot",
    )

  LaunchedEffect(shuffleCycleKey) {
    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    shuffleProgress.snapTo(0f)
    // Run the 3-stage shuffle choreography over 3.1 seconds
    shuffleProgress.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 3100, easing = FastOutSlowInEasing),
    )
    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    delay(280)
    onShuffleFinished()
  }

  val p = shuffleProgress.value
  val stageCaption =
    when {
      p < 0.32f -> "第一式 · 圣殿双叠交错切牌，涤净过往杂念..."
      p < 0.78f -> "第二式 · 星璇回环洗牌，注入问卜心念与正逆位天机..."
      else -> "第三式 · 圣牌归位合十，即将为你展开星轨牌扇..."
    }

  Surface(
    modifier = modifier.fillMaxWidth().testTag("tarot_shuffle_stage"),
    shape = RoundedCornerShape(28.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.5.dp, tarotColors.goldBorder),
    tonalElevation = 4.dp,
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(22.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // Top Pill
      Surface(
        shape = CircleShape,
        color = tarotColors.goldSoft,
        border = BorderStroke(1.dp, tarotColors.goldBorder),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(
            Icons.Rounded.AutoAwesome,
            contentDescription = null,
            tint = tarotColors.goldAccent,
            modifier = Modifier.size(16.dp),
          )
          Spacer(Modifier.width(6.dp))
          Text(
            text = "洗牌圣仪进行中 · $spreadName ($spreadCardCount 张)",
            style = MaterialTheme.typography.labelLarge,
            color = tarotColors.goldAccent,
          )
        }
      }

      Text(
        text = "请默念你的问题：「${question.ifBlank { "探索此刻生命流动与圣光指引" }}」",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
      )

      // 3D Shuffling Animation Arena
      Box(
        modifier = Modifier.fillMaxWidth().height(310.dp),
        contentAlignment = Alignment.Center,
      ) {
        // Rotating Sacred Geometry Magic Circle Backdrop
        Canvas(
          modifier =
            Modifier.size(270.dp).graphicsLayer {
              rotationZ = circleRotation + p * 180f
            }
        ) {
          val cx = size.width / 2f
          val cy = size.height / 2f
          val r = size.minDimension * 0.46f

          drawCircle(
            brush =
              Brush.radialGradient(
                listOf(
                  tarotColors.goldAccent.copy(alpha = 0.20f),
                  tarotColors.celestialGlow.copy(alpha = 0.08f),
                  Color.Transparent,
                )
              ),
            radius = r * 1.05f,
            center = Offset(cx, cy),
          )

          drawCircle(
            color = tarotColors.goldAccent.copy(alpha = 0.55f),
            radius = r,
            center = Offset(cx, cy),
            style = Stroke(width = 2f),
          )
          drawCircle(
            color = tarotColors.goldBorder.copy(alpha = 0.45f),
            radius = r * 0.78f,
            center = Offset(cx, cy),
            style =
              Stroke(
                width = 1.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f),
              ),
          )

          // Inscribed Octagram inside the magic circle
          for (i in 0 until 8) {
            val a1 = (i * 45.0) * PI / 180.0
            val a2 = ((i + 3) * 45.0) * PI / 180.0
            drawLine(
              color = tarotColors.goldAccent.copy(alpha = 0.35f),
              start = Offset(cx + (r * 0.78f * cos(a1)).toFloat(), cy + (r * 0.78f * sin(a1)).toFloat()),
              end = Offset(cx + (r * 0.78f * cos(a2)).toFloat(), cy + (r * 0.78f * sin(a2)).toFloat()),
              strokeWidth = 1.2f,
            )
          }
        }

        // 12 Animated Tarot Cards performing Riffle -> Astral Vortex -> Fan Convergence
        val cardCount = 12
        for (i in 0 until cardCount) {
          val normIdx = i.toFloat() / cardCount
          val side = if (i % 2 == 0) -1f else 1f

          var tx = 0f
          var ty = 0f
          var rotZ = 0f
          var rotY = 0f
          var scale = 1f

          if (p < 0.32f) {
            // Phase 1: Riffle Split & Interleave Weave
            val localP = (p / 0.32f).coerceIn(0f, 1f)
            val splitWave = sin(localP * PI * 2).toFloat()
            tx = side * 78f * density * splitWave
            ty = (i - cardCount / 2f) * 2.5f * density * (1f - abs(splitWave))
            rotZ = side * 14f * splitWave
            rotY = side * 28f * splitWave
            scale = 0.96f + 0.06f * abs(splitWave)
          } else if (p < 0.78f) {
            // Phase 2: Cosmic Astral Vortex Orbit (360° swirling ring)
            val localP = ((p - 0.32f) / 0.46f).coerceIn(0f, 1f)
            val radiusEnvelope = sin(localP * PI).toFloat()
            val orbitRadius = 86f * density * radiusEnvelope
            val angle = (normIdx * 2.0 * PI) + (localP * 3.2 * PI)
            tx = (orbitRadius * cos(angle)).toFloat()
            ty = (orbitRadius * 0.72f * sin(angle)).toFloat()
            rotZ = Math.toDegrees(angle).toFloat() + 90f
            rotY = sin(localP * PI * 2 + i).toFloat() * 35f
            scale = 0.90f + 0.12f * radiusEnvelope
          } else {
            // Phase 3: Gather into deck & unfold slightly into initial fan
            val localP = ((p - 0.78f) / 0.22f).coerceIn(0f, 1f)
            val fanOffset = (i - (cardCount - 1) / 2f)
            tx = fanOffset * 14f * density * localP
            ty = abs(fanOffset) * 2.5f * density * localP
            rotZ = fanOffset * 4f * localP
            rotY = (1f - localP) * 20f * side
            scale = 0.95f + 0.05f * localP
          }

          Box(
            modifier =
              Modifier.width(96.dp)
                .aspectRatio(0.66f)
                .graphicsLayer {
                  this.translationX = tx
                  this.translationY = ty
                  this.rotationZ = rotZ
                  this.rotationY = rotY
                  this.scaleX = scale
                  this.scaleY = scale
                  this.cameraDistance = 16f * density
                }
                .shadow(8.dp, RoundedCornerShape(14.dp), spotColor = tarotColors.goldAccent)
                .clip(RoundedCornerShape(14.dp))
                .border(
                  width = 1.4.dp,
                  brush =
                    Brush.linearGradient(
                      listOf(Color(0xFFFFE599), tarotColors.goldAccent, Color(0xFFFFE599))
                    ),
                  shape = RoundedCornerShape(14.dp),
                )
          ) {
            Image(
              painter = painterResource(id = R.drawable.img_card_back),
              contentDescription = null,
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize(),
            )
          }
        }
      }

      Text(
        text = stageCaption,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        color = tarotColors.goldAccent,
        textAlign = TextAlign.Center,
      )

      LinearProgressIndicator(
        progress = { p },
        color = tarotColors.goldAccent,
        trackColor = tarotColors.goldSoft,
        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        OutlinedButton(
          onClick = { shuffleCycleKey++ },
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.weight(1f).testTag("reshuffle_button"),
        ) {
          Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(Modifier.width(6.dp))
          Text("再次洗牌")
        }
        Button(
          onClick = onShuffleFinished,
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = tarotColors.goldAccent),
          modifier = Modifier.weight(1.2f).testTag("finish_shuffle_start_draw_button"),
        ) {
          Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(Modifier.width(6.dp))
          Text("完成洗牌 · 开始抽牌")
        }
      }
    }
  }
}

private fun abs(v: Float): Float = kotlin.math.abs(v)
