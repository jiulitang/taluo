package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.WorkOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.ExperimentalMotionApi
import androidx.constraintlayout.compose.MotionLayout
import com.example.R
import com.example.data.model.TarotCardEntity
import com.example.data.preferences.TarotDeckStyle
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.LocalTarotColors
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

private data class DeckStylePalette(
  val frameBgTop: Color,
  val frameBgBottom: Color,
  val primaryGold: Color,
  val borderGold: Color,
  val textPrimary: Color,
  val textSecondary: Color,
  val artAlpha: Float,
  val veilBrush: List<Color>,
)

private fun resolveDeckStylePalette(style: TarotDeckStyle): DeckStylePalette =
  when (style) {
    TarotDeckStyle.MORAVIA_MUCHA ->
      DeckStylePalette(
        frameBgTop = Color(0xFFFDFBF6),
        frameBgBottom = Color(0xFFF3ECE0),
        primaryGold = Color(0xFFB58938),
        borderGold = Color(0xFFD8BE82),
        textPrimary = Color(0xFF2B253E),
        textSecondary = Color(0xFF6B617E),
        artAlpha = 0.72f,
        veilBrush = listOf(Color(0x18FFFFFF), Color(0x55FBF7EE), Color(0xCCFDF9F1)),
      )
    TarotDeckStyle.ASTRAL_GOLD_FOIL ->
      DeckStylePalette(
        frameBgTop = Color(0xFF141124),
        frameBgBottom = Color(0xFF1D1836),
        primaryGold = Color(0xFFF3D27E),
        borderGold = Color(0xFFC89F44),
        textPrimary = Color(0xFFF9F1DC),
        textSecondary = Color(0xFFD1C29D),
        artAlpha = 0.34f,
        veilBrush = listOf(Color(0x88130F24), Color(0xAA161229), Color(0xEE120E22)),
      )
    TarotDeckStyle.MARIAS_GATE ->
      DeckStylePalette(
        frameBgTop = Color(0xFF282246),
        frameBgBottom = Color(0xFF39305E),
        primaryGold = Color(0xFFE8D5B5),
        borderGold = Color(0xFFB39DDB),
        textPrimary = Color(0xFFF7F2FF),
        textSecondary = Color(0xFFD1C4E9),
        artAlpha = 0.48f,
        veilBrush = listOf(Color(0x66272044), Color(0x992D254F), Color(0xDD231C3D)),
      )
    TarotDeckStyle.CLASSICAL_SANCTUARY ->
      DeckStylePalette(
        frameBgTop = Color(0xFFF9EED2),
        frameBgBottom = Color(0xFFEEDAB0),
        primaryGold = Color(0xFF9C6B19),
        borderGold = Color(0xFFC9983E),
        textPrimary = Color(0xFF33240C),
        textSecondary = Color(0xFF6E5326),
        artAlpha = 0.62f,
        veilBrush = listOf(Color(0x22FCEFD2), Color(0x66F7E6BE), Color(0xDDF7E7C4)),
      )
  }

/**
 * Ritual 3D Tarot Card combining Jetpack Compose [MotionLayout] constraint transitions
 * (governing the sacred aura ring, levitation scale, and ritual crest badge) with
 * 3D [graphicsLayer] perspective rotation, interactive touch-tilt, and gold-foil specular sweep.
 */
@OptIn(ExperimentalMotionApi::class)
@Composable
fun TarotCard3D(
  isRevealed: Boolean,
  card: TarotCardEntity?,
  isReversed: Boolean,
  deckStyle: TarotDeckStyle = TarotDeckStyle.MORAVIA_MUCHA,
  slotLabel: String = "",
  compact: Boolean = false,
  testTagName: String = "tarot_card_3d",
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val density = LocalDensity.current.density
  val haptics = LocalHapticFeedback.current
  val palette = resolveDeckStylePalette(deckStyle)

  val targetRevealed = isRevealed && card != null

  // MotionLayout progress (0f = sealed mystery, 1f = illuminated revelation)
  val motionProgress by
    animateFloatAsState(
      targetValue = if (targetRevealed) 1f else 0f,
      animationSpec = tween(durationMillis = 920, easing = FastOutSlowInEasing),
      label = "tarot_motion_progress",
    )

  // 3D Y-axis rotation in degrees (0f -> 180f)
  val rotationY = motionProgress * 180f

  // Mid-flip levitation peak (0f at ends, 1f at 90 degrees)
  val midFlipPeak = 1f - abs(rotationY - 90f) / 90f

  // Interactive press tilt for tactile ritual feel
  var pressTiltX by remember { mutableFloatStateOf(0f) }
  var pressTiltY by remember { mutableFloatStateOf(0f) }
  val animatedTiltX by animateFloatAsState(targetValue = pressTiltX, label = "tilt_x")
  val animatedTiltY by animateFloatAsState(targetValue = pressTiltY, label = "tilt_y")

  val infiniteTransition = rememberInfiniteTransition(label = "halo_pulse")
  val haloAlpha by
    infiniteTransition.animateFloat(
      initialValue = 0.25f,
      targetValue = 0.72f,
      animationSpec =
        infiniteRepeatable(
          animation = tween(1900, easing = FastOutSlowInEasing),
          repeatMode = RepeatMode.Reverse,
        ),
      label = "halo_alpha",
    )

  val startSet = remember {
    ConstraintSet {
      val aura = createRefFor("sacred_aura")
      val cardRef = createRefFor("card_surface")
      val crest = createRefFor("ritual_crest")

      constrain(aura) {
        width = Dimension.fillToConstraints
        height = Dimension.fillToConstraints
        centerTo(parent)
        scaleX = 0.88f
        scaleY = 0.88f
        alpha = 0.25f
        rotationZ = 0f
      }
      constrain(cardRef) {
        width = Dimension.fillToConstraints
        height = Dimension.fillToConstraints
        centerTo(parent)
        scaleX = 0.97f
        scaleY = 0.97f
      }
      constrain(crest) {
        top.linkTo(parent.top, margin = 4.dp)
        centerHorizontallyTo(parent)
        alpha = 0f
        scaleX = 0.6f
        scaleY = 0.6f
      }
    }
  }

  val endSet = remember {
    ConstraintSet {
      val aura = createRefFor("sacred_aura")
      val cardRef = createRefFor("card_surface")
      val crest = createRefFor("ritual_crest")

      constrain(aura) {
        width = Dimension.fillToConstraints
        height = Dimension.fillToConstraints
        centerTo(parent)
        scaleX = 1.04f
        scaleY = 1.04f
        alpha = 0.85f
        rotationZ = 90f
      }
      constrain(cardRef) {
        width = Dimension.fillToConstraints
        height = Dimension.fillToConstraints
        centerTo(parent)
        scaleX = 1.0f
        scaleY = 1.0f
      }
      constrain(crest) {
        top.linkTo(parent.top, margin = 0.dp)
        centerHorizontallyTo(parent)
        alpha = 1f
        scaleX = 1f
        scaleY = 1f
      }
    }
  }

  val shape = RoundedCornerShape(if (compact) 16.dp else 22.dp)

  MotionLayout(
    start = startSet,
    end = endSet,
    progress = motionProgress,
    modifier =
      modifier
        .aspectRatio(0.66f)
        .testTag(testTagName)
        .semantics {
          contentDescription =
            if (targetRevealed && card != null) {
              "${card.nameZh} ${if (isReversed) "逆位" else "正位"}"
            } else {
              "未翻开的塔罗牌 $slotLabel"
            }
        },
  ) {
    // 1. MotionLayout-driven Sacred Halo & Starlight Burst Layer
    Canvas(modifier = Modifier.layoutId("sacred_aura")) {
      if (midFlipPeak > 0.05f || targetRevealed) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = maxOf(size.width, size.height) * (0.48f + midFlipPeak * 0.12f)
        drawCircle(
          brush =
            Brush.radialGradient(
              colors =
                listOf(
                  palette.primaryGold.copy(alpha = 0.28f * midFlipPeak + 0.08f),
                  palette.borderGold.copy(alpha = 0.12f * midFlipPeak),
                  Color.Transparent,
                ),
              center = Offset(cx, cy),
              radius = radius,
            ),
          radius = radius,
          center = Offset(cx, cy),
        )
        // Sparkling ritual stars during the 3D flip
        if (midFlipPeak > 0.15f) {
          val starCount = 8
          for (i in 0 until starCount) {
            val angle = Math.toRadians((i * 45.0) + rotationY * 0.6)
            val dist = minOf(size.width, size.height) * (0.42f + midFlipPeak * 0.14f)
            val sx = cx + (dist * cos(angle)).toFloat()
            val sy = cy + (dist * sin(angle)).toFloat()
            val sr = 4.5f * midFlipPeak
            val starPath =
              Path().apply {
                moveTo(sx, sy - sr)
                lineTo(sx + sr * 0.35f, sy - sr * 0.35f)
                lineTo(sx + sr, sy)
                lineTo(sx + sr * 0.35f, sy + sr * 0.35f)
                lineTo(sx, sy + sr)
                lineTo(sx - sr * 0.35f, sy + sr * 0.35f)
                lineTo(sx - sr, sy)
                lineTo(sx - sr * 0.35f, sy - sr * 0.35f)
                close()
              }
            drawPath(starPath, color = palette.primaryGold.copy(alpha = midFlipPeak * 0.9f))
          }
        }
      }
    }

    // 2. 3D Perspective Card Body with Specular Foil Sweep
    Box(
      modifier =
        Modifier.layoutId("card_surface")
          .graphicsLayer {
            this.rotationY = rotationY + animatedTiltY
            this.rotationX = animatedTiltX - (midFlipPeak * 6f)
            val liftScale = 1f + midFlipPeak * 0.08f
            this.scaleX = liftScale
            this.scaleY = liftScale
            this.translationY = -midFlipPeak * 14f * density
            this.cameraDistance = 18f * density
          }
          .shadow(
            elevation = if (targetRevealed) 16.dp else 10.dp,
            shape = shape,
            ambientColor = palette.primaryGold.copy(alpha = 0.38f),
            spotColor = palette.primaryGold.copy(alpha = 0.52f),
          )
          .clip(shape)
          .pointerInput(Unit) {
            detectTapGestures(
              onPress = { offset ->
                val normX = (offset.x / size.width.coerceAtLeast(1)) - 0.5f
                val normY = (offset.y / size.height.coerceAtLeast(1)) - 0.5f
                pressTiltX = -normY * 10f
                pressTiltY = normX * 10f
                tryAwaitRelease()
                pressTiltX = 0f
                pressTiltY = 0f
              },
              onTap = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
              },
            )
          }
          .drawWithContent {
            drawContent()
            // Specular metallic gold sheen sweeping across card during 3D rotation
            if (midFlipPeak > 0.04f) {
              val sheenX = size.width * motionProgress
              drawRect(
                brush =
                  Brush.linearGradient(
                    colors =
                      listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.38f * midFlipPeak),
                        palette.primaryGold.copy(alpha = 0.32f * midFlipPeak),
                        Color.Transparent,
                      ),
                    start = Offset(sheenX - size.width * 0.45f, 0f),
                    end = Offset(sheenX + size.width * 0.45f, size.height),
                  )
              )
            }
          }
    ) {
      if (rotationY <= 90f) {
        CardBackFace(
          slotLabel = slotLabel,
          compact = compact,
          haloAlpha = haloAlpha,
          palette = palette,
        )
      } else if (card != null) {
        Box(
          modifier =
            Modifier.fillMaxSize().graphicsLayer {
              this.rotationY = 180f
            }
        ) {
          CardFrontFace(
            card = card,
            isReversed = isReversed,
            deckStyle = deckStyle,
            slotLabel = slotLabel,
            compact = compact,
          )
        }
      }
    }

    // 3. MotionLayout-animated Top Ritual Star Crest
    Box(modifier = Modifier.layoutId("ritual_crest")) {
      if (!compact && targetRevealed) {
        Icon(
          imageVector = Icons.Rounded.AutoAwesome,
          contentDescription = null,
          tint = palette.primaryGold.copy(alpha = 0.85f),
          modifier = Modifier.size(14.dp),
        )
      }
    }
  }
}

@Composable
private fun CardBackFace(
  slotLabel: String,
  compact: Boolean,
  haloAlpha: Float,
  palette: DeckStylePalette,
) {
  Box(
    modifier =
      Modifier.fillMaxSize()
        .background(palette.frameBgTop)
        .border(
          width = if (compact) 1.5.dp else 2.2.dp,
          brush =
            Brush.linearGradient(
              listOf(
                palette.primaryGold.copy(alpha = 0.9f),
                palette.borderGold.copy(alpha = 0.55f),
                palette.primaryGold.copy(alpha = 0.95f),
              )
            ),
          shape = RoundedCornerShape(if (compact) 16.dp else 22.dp),
        )
  ) {
    Image(
      painter = painterResource(id = R.drawable.img_card_back),
      contentDescription = null,
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
                Color(0x5518142B),
                Color(0xB818142B),
              )
            )
          )
    )
    Column(
      modifier = Modifier.align(Alignment.BottomCenter).padding(if (compact) 8.dp else 14.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Icon(
        imageVector = Icons.Rounded.AutoAwesome,
        contentDescription = null,
        tint = Color(0xFFF6DF9F).copy(alpha = haloAlpha + 0.3f),
        modifier = Modifier.size(if (compact) 16.dp else 22.dp),
      )
      Spacer(Modifier.height(4.dp))
      Text(
        text = slotLabel.ifBlank { "轻触启封圣牌" },
        color = Color(0xFFFFF8E8),
        style =
          if (compact) MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp)
          else MaterialTheme.typography.labelLarge,
        textAlign = TextAlign.Center,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
fun CardFrontFace(
  card: TarotCardEntity,
  isReversed: Boolean,
  deckStyle: TarotDeckStyle = TarotDeckStyle.MORAVIA_MUCHA,
  slotLabel: String = "",
  compact: Boolean = false,
) {
  val tarotColors = LocalTarotColors.current
  val palette = resolveDeckStylePalette(deckStyle)

  val suitAccent =
    when (card.suit) {
      "WANDS" -> Color(0xFFD6893E)
      "CUPS" -> Color(0xFF5688B8)
      "SWORDS" -> Color(0xFF8C7CC2)
      "PENTACLES" -> Color(0xFF5C9672)
      else -> palette.primaryGold
    }

  val artDrawableRes =
    when (card.suit) {
      "WANDS" -> R.drawable.img_deck_wands
      "CUPS" -> R.drawable.img_deck_cups
      "SWORDS" -> R.drawable.img_deck_swords
      "PENTACLES" -> R.drawable.img_deck_pentacles
      else -> if (card.id % 2 == 0) R.drawable.img_deck_mucha_major else R.drawable.img_arcana_sacred
    }

  Box(
    modifier =
      Modifier.fillMaxSize()
        .background(
          Brush.verticalGradient(
            listOf(
              palette.frameBgTop,
              palette.frameBgBottom,
              palette.frameBgTop,
            )
          )
        )
        .border(
          width = if (compact) 1.5.dp else 2.2.dp,
          brush =
            Brush.linearGradient(
              listOf(palette.primaryGold, palette.borderGold, palette.primaryGold)
            ),
          shape = RoundedCornerShape(if (compact) 16.dp else 22.dp),
        )
        .padding(if (compact) 5.dp else 9.dp)
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      // Top Roman Numeral & Orientation Pill
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = card.romanNumeral,
          fontFamily = CinzelFontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = if (compact) 10.sp else 13.sp,
          color = palette.primaryGold,
        )
        Surface(
          shape = CircleShape,
          color =
            if (isReversed) tarotColors.reversedBadge.copy(alpha = 0.20f)
            else tarotColors.uprightBadge.copy(alpha = 0.20f),
        ) {
          Text(
            text = if (isReversed) "逆位" else "正位",
            fontSize = if (compact) 8.sp else 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isReversed) tarotColors.reversedBadge else tarotColors.uprightBadge,
            modifier = Modifier.padding(horizontal = if (compact) 5.dp else 8.dp, vertical = 1.5.dp),
          )
        }
      }

      // Central Art Nouveau Window (rotates 180° when reversed)
      val archShape =
        RoundedCornerShape(
          topStart = if (compact) 48.dp else 82.dp,
          topEnd = if (compact) 48.dp else 82.dp,
          bottomStart = 12.dp,
          bottomEnd = 12.dp,
        )

      Box(
        modifier =
          Modifier.weight(1f)
            .fillMaxWidth()
            .padding(vertical = if (compact) 3.dp else 6.dp)
            .clip(archShape)
            .border(
              width = if (compact) 1.dp else 1.4.dp,
              color = palette.primaryGold.copy(alpha = 0.8f),
              shape = archShape,
            )
            .graphicsLayer {
              rotationZ = if (isReversed) 180f else 0f
            },
        contentAlignment = Alignment.Center,
      ) {
        Image(
          painter = painterResource(id = artDrawableRes),
          contentDescription = null,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize().alpha(palette.artAlpha),
        )

        Box(
          modifier =
            Modifier.fillMaxSize()
              .background(Brush.verticalGradient(palette.veilBrush))
        )

        BespokeTarotCardCanvas(
          card = card,
          primaryGold = palette.primaryGold,
          secondaryAccent = suitAccent,
          haloColor = palette.borderGold,
          modifier = Modifier.fillMaxSize().padding(if (compact) 4.dp else 8.dp),
        )
      }

      // Bottom Cartouche Nameplate
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(bottom = 1.dp),
      ) {
        Text(
          text = card.nameZh,
          style =
            if (compact) MaterialTheme.typography.titleMedium.copy(fontSize = 11.5.sp)
            else MaterialTheme.typography.titleLarge.copy(fontSize = 17.sp),
          fontWeight = FontWeight.SemiBold,
          color = palette.textPrimary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        if (!compact) {
          Text(
            text = card.nameEn.uppercase(),
            fontFamily = CinzelFontFamily,
            fontSize = 9.5.sp,
            letterSpacing = 1.0.sp,
            color = palette.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        } else if (slotLabel.isNotBlank()) {
          Text(
            text = slotLabel,
            fontSize = 8.5.sp,
            color = palette.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardMeaningBottomSheet(
  card: TarotCardEntity,
  isReversed: Boolean,
  deckStyle: TarotDeckStyle = TarotDeckStyle.MORAVIA_MUCHA,
  positionTitle: String? = null,
  onDismiss: () -> Unit,
) {
  val tarotColors = LocalTarotColors.current
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = MaterialTheme.colorScheme.surface,
  ) {
    Column(
      modifier =
        Modifier.fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 24.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(
          modifier =
            Modifier.width(96.dp)
              .aspectRatio(0.66f)
              .clip(RoundedCornerShape(14.dp))
        ) {
          CardFrontFace(
            card = card,
            isReversed = isReversed,
            deckStyle = deckStyle,
            compact = true,
          )
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
          if (!positionTitle.isNullOrBlank()) {
            Text(
              text = "牌位 · $positionTitle",
              style = MaterialTheme.typography.labelLarge,
              color = tarotColors.goldAccent,
            )
          }
          Text(
            text = "${card.romanNumeral} · ${card.nameZh}",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = "${card.nameEn} · ${card.element} · ${card.astrology}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Surface(
            shape = RoundedCornerShape(20.dp),
            color =
              if (isReversed) tarotColors.reversedBadge.copy(alpha = 0.16f)
              else tarotColors.uprightBadge.copy(alpha = 0.16f),
            border =
              BorderStroke(
                1.dp,
                if (isReversed) tarotColors.reversedBadge.copy(alpha = 0.4f)
                else tarotColors.uprightBadge.copy(alpha = 0.4f),
              ),
          ) {
            Text(
              text = if (isReversed) "逆位能量" else "正位能量",
              style = MaterialTheme.typography.labelLarge,
              color = if (isReversed) tarotColors.reversedBadge else tarotColors.uprightBadge,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            )
          }
        }
      }

      Surface(
        shape = RoundedCornerShape(16.dp),
        color = tarotColors.goldSoft.copy(alpha = 0.65f),
        border = BorderStroke(1.dp, tarotColors.goldBorder.copy(alpha = 0.6f)),
      ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
          Text(
            text = "核心关键词",
            style = MaterialTheme.typography.labelLarge,
            color = tarotColors.goldAccent,
          )
          Spacer(Modifier.height(4.dp))
          Text(
            text = if (isReversed) card.keywordsReversed else card.keywordsUpright,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
          )
        }
      }

      Text(
        text = if (isReversed) "逆位详细解析" else "正位详细解析",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Text(
        text = if (isReversed) card.meaningReversed else card.meaningUpright,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

      AdviceRow(Icons.Rounded.FavoriteBorder, "情感与关系", card.loveAdvice)
      AdviceRow(Icons.Rounded.WorkOutline, "事业与学业", card.careerAdvice)
      AdviceRow(Icons.Rounded.SelfImprovement, "灵性成长", card.spiritualAdvice)

      if (card.customNotes.isNotBlank()) {
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              Icons.Rounded.Refresh,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.secondary,
              modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
              text = "卡面铭文与笔记：${card.customNotes}",
              style = MaterialTheme.typography.bodyMedium,
            )
          }
        }
      }

      Spacer(Modifier.height(24.dp))
    }
  }
}

@Composable
private fun AdviceRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  content: String,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.Top,
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = LocalTarotColors.current.goldAccent,
      modifier = Modifier.padding(top = 2.dp).size(18.dp),
    )
    Spacer(Modifier.width(10.dp))
    Column {
      Text(text = title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
      Text(text = content, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}
