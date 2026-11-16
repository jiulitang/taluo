package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.rounded.Swipe
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.R
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.LocalTarotColors
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Interactive Sliding Curved Tarot Fan Deck:
 * Allows the user to slide horizontally across the 78 fanned face-down tarot cards
 * with real-time 3D perspective fan physics, haptic feedback ticks, and an upward
 * levitating draw animation for drawing cards one by one.
 */
@Composable
fun SlidingTarotDeckFan(
  currentStepNumber: Int,
  totalSteps: Int,
  currentPositionTitle: String,
  currentPositionDescription: String,
  remainingDeckCount: Int,
  onCardDrawn: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val tarotColors = LocalTarotColors.current
  val haptics = LocalHapticFeedback.current
  val density = LocalDensity.current.density
  val scope = rememberCoroutineScope()

  // Continuous horizontal scroll index in [0f .. (remainingDeckCount - 1)]
  val maxIndex = (remainingDeckCount - 1).coerceAtLeast(0).toFloat()
  var scrollPosition by remember(remainingDeckCount) {
    mutableFloatStateOf((remainingDeckCount / 2f).coerceIn(0f, maxIndex))
  }
  var lastTickIndex by remember { mutableIntStateOf(scrollPosition.roundToInt()) }

  // Animation state when user triggers a card draw
  var isDrawingAnim by remember { mutableStateOf(false) }
  val drawFlyProgress = remember { Animatable(0f) }

  val focusedCardIndex = scrollPosition.roundToInt().coerceIn(0, remainingDeckCount.coerceAtLeast(1) - 1)

  fun triggerDrawRitual() {
    if (isDrawingAnim) return
    isDrawingAnim = true
    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    scope.launch {
      drawFlyProgress.snapTo(0f)
      drawFlyProgress.animateTo(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 480, easing = FastOutSlowInEasing),
      )
      onCardDrawn()
      drawFlyProgress.snapTo(0f)
      isDrawingAnim = false
    }
  }

  // Snap gently to nearest card index when not dragging
  val animatedCenterPos by
    animateFloatAsState(
      targetValue = scrollPosition,
      animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
      label = "fan_scroll_pos",
    )

  LaunchedEffect(animatedCenterPos.roundToInt()) {
    val currentInt = animatedCenterPos.roundToInt()
    if (currentInt != lastTickIndex) {
      lastTickIndex = currentInt
      haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
  }

  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(26.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.2.dp, tarotColors.goldBorder.copy(alpha = 0.7f)),
    tonalElevation = 3.dp,
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(18.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      // Current Position Prompt Banner
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Surface(
          shape = CircleShape,
          color = tarotColors.goldSoft,
          border = BorderStroke(1.dp, tarotColors.goldBorder),
        ) {
          Text(
            text = "正在抽取 第 $currentStepNumber / $totalSteps 张",
            style = MaterialTheme.typography.labelLarge,
            color = tarotColors.goldAccent,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
          )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            Icons.Rounded.Swipe,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp),
          )
          Spacer(Modifier.width(4.dp))
          Text(
            text = "左右滑动选牌 · 上滑或点击抽出",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = "牌位：$currentPositionTitle",
          style = MaterialTheme.typography.titleLarge,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
          text = currentPositionDescription,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      // Interactive Curved Fan Stage
      Box(
        modifier =
          Modifier.fillMaxWidth()
            .height(235.dp)
            .testTag("sliding_tarot_fan_box")
            .pointerInput(remainingDeckCount, isDrawingAnim) {
              if (isDrawingAnim) return@pointerInput
              var accumulatedVerticalDrag = 0f
              detectDragGestures(
                onDragStart = { accumulatedVerticalDrag = 0f },
                onDragEnd = {
                  if (accumulatedVerticalDrag < -75f) {
                    triggerDrawRitual()
                  } else {
                    scrollPosition = scrollPosition.roundToInt().toFloat().coerceIn(0f, maxIndex)
                  }
                },
                onDragCancel = {
                  scrollPosition = scrollPosition.roundToInt().toFloat().coerceIn(0f, maxIndex)
                },
                onDrag = { change, dragAmount ->
                  change.consume()
                  accumulatedVerticalDrag += dragAmount.y
                  // Horizontal drag moves across the fanned cards smoothly
                  val deltaCards = -dragAmount.x / (38f * density)
                  scrollPosition = (scrollPosition + deltaCards).coerceIn(0f, maxIndex)
                },
              )
            },
        contentAlignment = Alignment.Center,
      ) {
        // Golden Celestial Orbit Arc Track behind the fan
        Canvas(modifier = Modifier.fillMaxSize()) {
          val w = size.width
          val h = size.height
          drawArc(
            color = tarotColors.goldAccent.copy(alpha = 0.32f),
            startAngle = 200f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(-w * 0.15f, h * 0.25f),
            size = androidx.compose.ui.geometry.Size(w * 1.3f, h * 1.4f),
            style =
              Stroke(
                width = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
              ),
          )
        }

        // Render visible window of cards around animatedCenterPos (-5 .. +5)
        val visibleRange =
          ((animatedCenterPos.roundToInt() - 5).coerceAtLeast(0))..((animatedCenterPos.roundToInt() + 5).coerceAtMost(remainingDeckCount - 1))

        for (cardIdx in visibleRange) {
          val offsetFromCenter = cardIdx - animatedCenterPos
          val absOffset = abs(offsetFromCenter)
          val isCenterCard = cardIdx == focusedCardIndex

          // Fly-out progress if this is the center card being drawn
          val fly = if (isCenterCard) drawFlyProgress.value else 0f

          val cardWidthDp = 100.dp
          val spreadSpacingPx = 34f * density
          val archDropPx = (absOffset * absOffset * 5.2f + absOffset * 4f) * density
          val centerLiftPx =
            if (isCenterCard) (-22f - fly * 130f) * density else (-6f * (1f - (absOffset / 5f).coerceIn(0f, 1f))) * density

          val rotZ = offsetFromCenter * 6.5f * (1f - fly)
          val rotY = (-offsetFromCenter * 7f) + (fly * 180f)
          val cardScale =
            (1.06f - (absOffset * 0.055f).coerceAtMost(0.28f)) + (fly * 0.18f)

          Box(
            modifier =
              Modifier.zIndex(if (isCenterCard) 20f else (10f - absOffset))
                .width(cardWidthDp)
                .aspectRatio(0.66f)
                .graphicsLayer {
                  this.translationX = offsetFromCenter * spreadSpacingPx * (1f - fly * 0.6f)
                  this.translationY = archDropPx + centerLiftPx
                  this.rotationZ = rotZ
                  this.rotationY = rotY
                  this.scaleX = cardScale
                  this.scaleY = cardScale
                  this.alpha = (1f - fly * 0.25f).coerceIn(0f, 1f)
                  this.cameraDistance = 16f * density
                }
                .shadow(
                  elevation = if (isCenterCard) 14.dp else 6.dp,
                  shape = RoundedCornerShape(14.dp),
                  spotColor = tarotColors.goldAccent,
                )
                .clip(RoundedCornerShape(14.dp))
                .border(
                  width = if (isCenterCard) 2.dp else 1.dp,
                  brush =
                    Brush.linearGradient(
                      if (isCenterCard) {
                        listOf(Color(0xFFFFE599), tarotColors.goldAccent, Color(0xFFFFE599))
                      } else {
                        listOf(
                          tarotColors.goldBorder.copy(alpha = 0.7f),
                          tarotColors.goldAccent.copy(alpha = 0.4f),
                        )
                      }
                    ),
                  shape = RoundedCornerShape(14.dp),
                )
                .clickable(enabled = !isDrawingAnim) {
                  if (isCenterCard) {
                    triggerDrawRitual()
                  } else {
                    scrollPosition = cardIdx.toFloat()
                  }
                }
          ) {
            Image(
              painter = painterResource(id = R.drawable.img_card_back),
              contentDescription = "星轨圣牌 #${cardIdx + 1}",
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize(),
            )
            Box(
              modifier =
                Modifier.fillMaxSize()
                  .background(
                    Brush.verticalGradient(
                      listOf(
                        Color.Transparent,
                        if (isCenterCard) Color(0x331B1630) else Color(0x77161229),
                      )
                    )
                  )
            )
            // Card serial badge at bottom of focused card
            if (isCenterCard) {
              Surface(
                shape = CircleShape,
                color = Color(0xCC1B1630),
                border = BorderStroke(0.8.dp, Color(0xFFF6DF9F)),
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp),
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Icon(
                    Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFFF6DF9F),
                    modifier = Modifier.size(10.dp),
                  )
                  Spacer(Modifier.width(3.dp))
                  Text(
                    text = "NO.${cardIdx + 1}",
                    fontFamily = CinzelFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFF6DC),
                  )
                }
              }
            }
          }
        }
      }

      // Bottom Draw Action Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "当前感应牌序：第 ${focusedCardIndex + 1} / $remainingDeckCount 张",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(
          onClick = { triggerDrawRitual() },
          enabled = !isDrawingAnim,
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = tarotColors.goldAccent),
          modifier = Modifier.testTag("draw_focused_card_button"),
        ) {
          Icon(Icons.Rounded.TouchApp, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(Modifier.width(6.dp))
          Text("抽取此张圣牌")
        }
      }
    }
  }
}
