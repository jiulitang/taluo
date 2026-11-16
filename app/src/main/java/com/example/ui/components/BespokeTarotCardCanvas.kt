package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.data.model.TarotCardEntity
import kotlin.math.cos
import kotlin.math.sin

/**
 * Renders bespoke sacred iconography for all 78 Tarot cards (22 Major Arcana + 56 Minor Arcana)
 * so every single card in the 78-card deck has its own distinct, recognizable visual composition.
 */
@Composable
fun BespokeTarotCardCanvas(
  card: TarotCardEntity,
  primaryGold: Color,
  secondaryAccent: Color,
  haloColor: Color,
  modifier: Modifier = Modifier,
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val cx = w / 2f
    val cy = h / 2f
    val r = minOf(w, h) * 0.40f

    // 1. Ornate Art Nouveau Corner Filigree & Cathedral Arch Frame
    drawArtNouveauFrame(w, h, primaryGold, haloColor)

    // 2. Card-specific sacred illustration
    if (card.arcana == "MAJOR") {
      drawMajorArcanaSymbolism(
        cardId = card.id,
        cx = cx,
        cy = cy,
        r = r,
        gold = primaryGold,
        accent = secondaryAccent,
        halo = haloColor,
      )
    } else {
      drawMinorArcanaSymbolism(
        suit = card.suit,
        rank = card.number,
        cx = cx,
        cy = cy,
        r = r,
        gold = primaryGold,
        accent = secondaryAccent,
        halo = haloColor,
      )
    }
  }
}

private fun DrawScope.drawArtNouveauFrame(
  w: Float,
  h: Float,
  gold: Color,
  halo: Color,
) {
  val pad = minOf(w, h) * 0.04f
  // Outer fine border
  drawRoundRect(
    color = gold.copy(alpha = 0.65f),
    topLeft = Offset(pad, pad),
    size = Size(w - pad * 2, h - pad * 2),
    cornerRadius = CornerRadius(18f, 18f),
    style = Stroke(width = 1.6f),
  )
  // Upper celestial arch halo
  val archRadius = (w - pad * 4f) / 2f
  drawCircle(
    color = halo.copy(alpha = 0.22f),
    radius = archRadius * 0.85f,
    center = Offset(w / 2f, h * 0.38f),
  )
  drawCircle(
    color = gold.copy(alpha = 0.72f),
    radius = archRadius * 0.85f,
    center = Offset(w / 2f, h * 0.38f),
    style = Stroke(width = 1.8f),
  )
  drawCircle(
    color = gold.copy(alpha = 0.42f),
    radius = archRadius * 0.74f,
    center = Offset(w / 2f, h * 0.38f),
    style = Stroke(width = 1.1f),
  )

  // Four corner stars
  val corners =
    listOf(
      Offset(pad * 2.4f, pad * 2.4f),
      Offset(w - pad * 2.4f, pad * 2.4f),
      Offset(pad * 2.4f, h - pad * 2.4f),
      Offset(w - pad * 2.4f, h - pad * 2.4f),
    )
  corners.forEach { pt -> drawFourPointStar(pt.x, pt.y, pad * 0.9f, gold.copy(alpha = 0.8f)) }
}

private fun DrawScope.drawMajorArcanaSymbolism(
  cardId: Int,
  cx: Float,
  cy: Float,
  r: Float,
  gold: Color,
  accent: Color,
  halo: Color,
) {
  // Subtle sacred sun/starburst backdrop behind central emblem
  drawRadialRays(cx, cy, r * 0.35f, r * 0.95f, 12, gold.copy(alpha = 0.32f))

  when (cardId) {
    0 -> {
      // 0 The Fool: Rising dawn sun at top-right, blooming white rose in center, cliff arc below
      drawCircle(gold.copy(alpha = 0.85f), radius = r * 0.24f, center = Offset(cx + r * 0.42f, cy - r * 0.45f))
      drawRadialRays(cx + r * 0.42f, cy - r * 0.45f, r * 0.28f, r * 0.46f, 8, gold)
      drawRoseBlossom(cx, cy, r * 0.42f, gold, accent)
    }
    1 -> {
      // I The Magician: Lemniscate infinity above + 4 suit relics around central wand
      drawInfinitySymbol(cx, cy - r * 0.56f, r * 0.34f, gold)
      drawSuitRelic("WANDS", cx - r * 0.42f, cy + r * 0.15f, r * 0.24f, gold, accent)
      drawSuitRelic("CUPS", cx + r * 0.42f, cy + r * 0.15f, r * 0.24f, gold, accent)
      drawSuitRelic("SWORDS", cx - r * 0.22f, cy + r * 0.52f, r * 0.24f, gold, accent)
      drawSuitRelic("PENTACLES", cx + r * 0.22f, cy + r * 0.52f, r * 0.24f, gold, accent)
    }
    2 -> {
      // II High Priestess: Twin Pillars & Triple Moon Crown + Crescent at feet
      drawLine(gold, Offset(cx - r * 0.58f, cy - r * 0.65f), Offset(cx - r * 0.58f, cy + r * 0.65f), strokeWidth = 3.5f)
      drawLine(gold, Offset(cx + r * 0.58f, cy - r * 0.65f), Offset(cx + r * 0.58f, cy + r * 0.65f), strokeWidth = 3.5f)
      drawCircle(gold, radius = r * 0.22f, center = Offset(cx, cy - r * 0.12f), style = Stroke(2.4f))
      drawCrescentMoon(cx, cy + r * 0.36f, r * 0.32f, gold, halo)
    }
    3 -> {
      // III The Empress: 12-star diadem arc + Venus symbol + wheat stalks
      for (i in 0 until 12) {
        val a = Math.toRadians((-165 + i * 13.5))
        drawFourPointStar(
          cx + (r * 0.66f * cos(a)).toFloat(),
          cy - r * 0.1f + (r * 0.66f * sin(a)).toFloat(),
          r * 0.08f,
          gold,
        )
      }
      drawCircle(gold, radius = r * 0.26f, center = Offset(cx, cy - r * 0.08f), style = Stroke(2.6f))
      drawLine(gold, Offset(cx, cy + r * 0.18f), Offset(cx, cy + r * 0.56f), strokeWidth = 2.6f)
      drawLine(gold, Offset(cx - r * 0.18f, cy + r * 0.36f), Offset(cx + r * 0.18f, cy + r * 0.36f), strokeWidth = 2.6f)
    }
    4 -> {
      // IV The Emperor: Ankh / Solar Cross + Geometric Throne Diamond
      drawPolygonOutline(cx, cy, r * 0.56f, 4, 45.0, gold, 2.5f)
      drawCircle(gold, radius = r * 0.20f, center = Offset(cx, cy - r * 0.18f), style = Stroke(2.8f))
      drawLine(gold, Offset(cx, cy + r * 0.02f), Offset(cx, cy + r * 0.48f), strokeWidth = 2.8f)
      drawLine(gold, Offset(cx - r * 0.22f, cy + r * 0.18f), Offset(cx + r * 0.22f, cy + r * 0.18f), strokeWidth = 2.8f)
    }
    5 -> {
      // V The Hierophant: Crossed Sacred Keys & Triple Crown
      drawLine(gold, Offset(cx - r * 0.38f, cy + r * 0.42f), Offset(cx + r * 0.38f, cy - r * 0.34f), strokeWidth = 3f)
      drawLine(gold, Offset(cx + r * 0.38f, cy + r * 0.42f), Offset(cx - r * 0.38f, cy - r * 0.34f), strokeWidth = 3f)
      drawCircle(gold, radius = r * 0.13f, center = Offset(cx - r * 0.38f, cy + r * 0.42f), style = Stroke(2.4f))
      drawCircle(gold, radius = r * 0.13f, center = Offset(cx + r * 0.38f, cy + r * 0.42f), style = Stroke(2.4f))
      drawFourPointStar(cx, cy - r * 0.54f, r * 0.18f, gold)
    }
    6 -> {
      // VI The Lovers: Intertwined Vesica Piscis rings + Radiant Winged Sun above
      drawCircle(gold, radius = r * 0.34f, center = Offset(cx - r * 0.18f, cy + r * 0.12f), style = Stroke(2.4f))
      drawCircle(accent, radius = r * 0.34f, center = Offset(cx + r * 0.18f, cy + r * 0.12f), style = Stroke(2.4f))
      drawFourPointStar(cx, cy - r * 0.48f, r * 0.24f, gold)
      drawAngelWings(cx, cy - r * 0.46f, r * 0.62f, gold)
    }
    7 -> {
      // VII The Chariot: Winged Solar Disk + Star Chariot Shield
      drawAngelWings(cx, cy - r * 0.28f, r * 0.72f, gold)
      drawPolygonOutline(cx, cy + r * 0.16f, r * 0.38f, 6, 30.0, gold, 2.5f)
      drawEightPointStar(cx, cy + r * 0.16f, r * 0.28f, gold)
    }
    8 -> {
      // VIII Strength: Large Lemniscate Infinity + Solar Rose Heart
      drawInfinitySymbol(cx, cy - r * 0.40f, r * 0.46f, gold)
      drawRoseBlossom(cx, cy + r * 0.14f, r * 0.42f, gold, accent)
    }
    9 -> {
      // IX The Hermit: Blazing 6-pointed Star of Solomon inside a raised Sanctuary Lantern
      drawPolygonOutline(cx, cy, r * 0.46f, 6, 0.0, gold.copy(alpha = 0.6f), 2f)
      drawHexagramStar(cx, cy, r * 0.36f, gold)
      drawFourPointStar(cx, cy, r * 0.16f, halo)
    }
    10 -> {
      // X Wheel of Fortune: 8-spoked Alchemical Wheel with inner & outer rings
      drawCircle(gold, radius = r * 0.64f, center = Offset(cx, cy), style = Stroke(3f))
      drawCircle(gold, radius = r * 0.48f, center = Offset(cx, cy), style = Stroke(1.8f))
      drawCircle(gold, radius = r * 0.16f, center = Offset(cx, cy), style = Stroke(2.4f))
      drawRadialRays(cx, cy, r * 0.16f, r * 0.64f, 8, gold)
    }
    11 -> {
      // XI Justice: Upright Sword of Truth + Balanced Golden Scales
      drawLine(gold, Offset(cx, cy - r * 0.68f), Offset(cx, cy + r * 0.64f), strokeWidth = 3.2f)
      drawLine(gold, Offset(cx - r * 0.54f, cy - r * 0.18f), Offset(cx + r * 0.54f, cy - r * 0.18f), strokeWidth = 2.6f)
      drawCircle(gold, radius = r * 0.18f, center = Offset(cx - r * 0.54f, cy + r * 0.18f), style = Stroke(2.2f))
      drawCircle(gold, radius = r * 0.18f, center = Offset(cx + r * 0.54f, cy + r * 0.18f), style = Stroke(2.2f))
    }
    12 -> {
      // XII The Hanged Man: Sacred T-Cross at top + Luminous Halo at bottom
      drawLine(gold, Offset(cx - r * 0.48f, cy - r * 0.55f), Offset(cx + r * 0.48f, cy - r * 0.55f), strokeWidth = 3.2f)
      drawLine(gold, Offset(cx, cy - r * 0.55f), Offset(cx, cy + r * 0.25f), strokeWidth = 2.5f)
      drawCircle(halo.copy(alpha = 0.45f), radius = r * 0.32f, center = Offset(cx, cy + r * 0.34f))
      drawCircle(gold, radius = r * 0.32f, center = Offset(cx, cy + r * 0.34f), style = Stroke(2.4f))
      drawEightPointStar(cx, cy + r * 0.34f, r * 0.24f, gold)
    }
    13 -> {
      // XIII Death: Mystic 5-petalled White Rose Banner + Rising Sun on horizon
      drawRoseBlossom(cx, cy - r * 0.08f, r * 0.50f, gold, halo)
      drawLine(gold, Offset(cx - r * 0.64f, cy + r * 0.52f), Offset(cx + r * 0.64f, cy + r * 0.52f), strokeWidth = 2.2f)
      drawRadialRays(cx, cy + r * 0.52f, r * 0.12f, r * 0.42f, 9, gold)
    }
    14 -> {
      // XIV Temperance: Angel Wings + Two Chalices connected by a diagonal stream of light
      drawAngelWings(cx, cy - r * 0.22f, r * 0.76f, gold)
      drawSuitRelic("CUPS", cx - r * 0.36f, cy - r * 0.05f, r * 0.26f, gold, accent)
      drawSuitRelic("CUPS", cx + r * 0.36f, cy + r * 0.38f, r * 0.26f, gold, accent)
      drawLine(halo, Offset(cx - r * 0.28f, cy + r * 0.02f), Offset(cx + r * 0.28f, cy + r * 0.30f), strokeWidth = 3f)
    }
    15 -> {
      // XV The Devil: Pentagram Star within Ring + Twin Torch Sparks
      drawPentagramRing(cx, cy, r * 0.52f, gold, accent, inverted = true)
    }
    16 -> {
      // XVI The Tower: Crowned Spire + Celestial Lightning Bolt + Falling Golden Sparks
      val tower =
        Path().apply {
          moveTo(cx - r * 0.24f, cy + r * 0.64f)
          lineTo(cx - r * 0.16f, cy - r * 0.24f)
          lineTo(cx + r * 0.16f, cy - r * 0.24f)
          lineTo(cx + r * 0.24f, cy + r * 0.64f)
          close()
        }
      drawPath(tower, color = gold, style = Stroke(2.5f))
      val bolt =
        Path().apply {
          moveTo(cx + r * 0.48f, cy - r * 0.72f)
          lineTo(cx, cy - r * 0.28f)
          lineTo(cx + r * 0.18f, cy - r * 0.22f)
          lineTo(cx - r * 0.36f, cy + r * 0.12f)
        }
      drawPath(bolt, color = gold, style = Stroke(3.2f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
    17 -> {
      // XVII The Star: Great 8-pointed Star surrounded by 7 smaller stars
      drawEightPointStar(cx, cy - r * 0.14f, r * 0.48f, gold)
      for (i in 0 until 7) {
        val a = Math.toRadians((-165 + i * 25).toDouble())
        val sx = cx + (r * 0.68f * cos(a)).toFloat()
        val sy = cy - r * 0.14f + (r * 0.68f * sin(a)).toFloat()
        drawFourPointStar(sx, sy, r * 0.11f, gold)
      }
    }
    18 -> {
      // XVIII The Moon: Luminous Crescent within Solar-Lunar Disk + Twin Pillars below
      drawCrescentMoon(cx, cy - r * 0.14f, r * 0.46f, gold, halo)
      drawLine(gold, Offset(cx - r * 0.46f, cy + r * 0.22f), Offset(cx - r * 0.46f, cy + r * 0.66f), strokeWidth = 4f)
      drawLine(gold, Offset(cx + r * 0.46f, cy + r * 0.22f), Offset(cx + r * 0.46f, cy + r * 0.66f), strokeWidth = 4f)
    }
    19 -> {
      // XIX The Sun: Blazing 16-Rayed Solar Disk
      drawRadialRays(cx, cy, r * 0.30f, r * 0.74f, 16, gold)
      drawCircle(halo.copy(alpha = 0.5f), radius = r * 0.32f, center = Offset(cx, cy))
      drawCircle(gold, radius = r * 0.32f, center = Offset(cx, cy), style = Stroke(2.8f))
    }
    20 -> {
      // XX Judgement: Angel Wings + Herald Horn + Rising Cross Banner
      drawAngelWings(cx, cy - r * 0.25f, r * 0.78f, gold)
      drawFourPointStar(cx, cy + r * 0.24f, r * 0.36f, gold)
    }
    else -> {
      // XXI The World: Sacred Oval Laurel Wreath + Four Elemental Stars
      drawOval(
        color = gold,
        topLeft = Offset(cx - r * 0.46f, cy - r * 0.66f),
        size = Size(r * 0.92f, r * 1.32f),
        style = Stroke(width = 3.2f),
      )
      drawOval(
        color = accent,
        topLeft = Offset(cx - r * 0.38f, cy - r * 0.56f),
        size = Size(r * 0.76f, r * 1.12f),
        style = Stroke(width = 1.6f),
      )
      drawEightPointStar(cx, cy, r * 0.34f, gold)
    }
  }
}

private fun DrawScope.drawMinorArcanaSymbolism(
  suit: String,
  rank: Int,
  cx: Float,
  cy: Float,
  r: Float,
  gold: Color,
  accent: Color,
  halo: Color,
) {
  if (rank in 1..10) {
    val positions = getPipCoordinates(rank, cx, cy, r)
    val relicRadius =
      when {
        rank == 1 -> r * 0.54f
        rank <= 3 -> r * 0.28f
        rank <= 6 -> r * 0.22f
        else -> r * 0.18f
      }
    if (rank == 1) {
      // Ace gets a radiant halo crown
      drawRadialRays(cx, cy, relicRadius * 0.6f, relicRadius * 1.35f, 12, gold.copy(alpha = 0.55f))
    }
    positions.forEach { pt ->
      drawSuitRelic(suit, pt.x, pt.y, relicRadius, gold, accent)
    }
  } else {
    // Court Cards: 11 Page, 12 Knight, 13 Queen, 14 King
    drawCourtCrest(rank, cx, cy - r * 0.44f, r * 0.36f, gold, halo)
    drawRadialRays(cx, cy + r * 0.12f, r * 0.25f, r * 0.64f, 10, gold.copy(alpha = 0.4f))
    drawSuitRelic(suit, cx, cy + r * 0.12f, r * 0.44f, gold, accent)
  }
}

private fun getPipCoordinates(rank: Int, cx: Float, cy: Float, r: Float): List<Offset> {
  val dx = r * 0.44f
  val dy = r * 0.52f
  return when (rank) {
    1 -> listOf(Offset(cx, cy))
    2 -> listOf(Offset(cx, cy - dy * 0.65f), Offset(cx, cy + dy * 0.65f))
    3 ->
      listOf(
        Offset(cx, cy - dy * 0.75f),
        Offset(cx - dx * 0.75f, cy + dy * 0.55f),
        Offset(cx + dx * 0.75f, cy + dy * 0.55f),
      )
    4 ->
      listOf(
        Offset(cx - dx * 0.7f, cy - dy * 0.6f),
        Offset(cx + dx * 0.7f, cy - dy * 0.6f),
        Offset(cx - dx * 0.7f, cy + dy * 0.6f),
        Offset(cx + dx * 0.7f, cy + dy * 0.6f),
      )
    5 ->
      listOf(
        Offset(cx - dx * 0.75f, cy - dy * 0.65f),
        Offset(cx + dx * 0.75f, cy - dy * 0.65f),
        Offset(cx, cy),
        Offset(cx - dx * 0.75f, cy + dy * 0.65f),
        Offset(cx + dx * 0.75f, cy + dy * 0.65f),
      )
    6 ->
      listOf(
        Offset(cx - dx * 0.7f, cy - dy * 0.75f),
        Offset(cx + dx * 0.7f, cy - dy * 0.75f),
        Offset(cx - dx * 0.7f, cy),
        Offset(cx + dx * 0.7f, cy),
        Offset(cx - dx * 0.7f, cy + dy * 0.75f),
        Offset(cx + dx * 0.7f, cy + dy * 0.75f),
      )
    7 ->
      listOf(
        Offset(cx, cy - dy * 0.92f),
        Offset(cx - dx * 0.72f, cy - dy * 0.35f),
        Offset(cx + dx * 0.72f, cy - dy * 0.35f),
        Offset(cx - dx * 0.72f, cy + dy * 0.25f),
        Offset(cx + dx * 0.72f, cy + dy * 0.25f),
        Offset(cx - dx * 0.72f, cy + dy * 0.85f),
        Offset(cx + dx * 0.72f, cy + dy * 0.85f),
      )
    8 ->
      (-1..2).flatMap { row ->
        val y = cy + (row - 0.5f) * (dy * 0.56f)
        listOf(Offset(cx - dx * 0.72f, y), Offset(cx + dx * 0.72f, y))
      }
    9 ->
      listOf(
        Offset(cx - dx * 0.75f, cy - dy * 0.82f),
        Offset(cx + dx * 0.75f, cy - dy * 0.82f),
        Offset(cx - dx * 0.75f, cy - dy * 0.28f),
        Offset(cx + dx * 0.75f, cy - dy * 0.28f),
        Offset(cx, cy),
        Offset(cx - dx * 0.75f, cy + dy * 0.28f),
        Offset(cx + dx * 0.75f, cy + dy * 0.28f),
        Offset(cx - dx * 0.75f, cy + dy * 0.82f),
        Offset(cx + dx * 0.75f, cy + dy * 0.82f),
      )
    else ->
      // 10: Sephiroth Tree of Life sacred symmetry
      listOf(
        Offset(cx, cy - dy * 0.96f),
        Offset(cx - dx * 0.75f, cy - dy * 0.62f),
        Offset(cx + dx * 0.75f, cy - dy * 0.62f),
        Offset(cx - dx * 0.75f, cy - dy * 0.14f),
        Offset(cx + dx * 0.75f, cy - dy * 0.14f),
        Offset(cx, cy + dy * 0.08f),
        Offset(cx - dx * 0.75f, cy + dy * 0.36f),
        Offset(cx + dx * 0.75f, cy + dy * 0.36f),
        Offset(cx, cy + dy * 0.64f),
        Offset(cx, cy + dy * 0.96f),
      )
  }
}

private fun DrawScope.drawSuitRelic(
  suit: String,
  x: Float,
  y: Float,
  size: Float,
  gold: Color,
  accent: Color,
) {
  when (suit) {
    "WANDS" -> {
      // Blossoming Sacred Staff with Lily Leaves
      drawLine(
        color = gold,
        start = Offset(x, y + size * 0.85f),
        end = Offset(x, y - size * 0.75f),
        strokeWidth = 3.2f,
        cap = StrokeCap.Round,
      )
      drawFourPointStar(x, y - size * 0.78f, size * 0.38f, gold)
      // Sprouting leaves
      val leafLeft =
        Path().apply {
          moveTo(x, y)
          quadraticTo(x - size * 0.55f, y - size * 0.35f, x - size * 0.42f, y - size * 0.55f)
          quadraticTo(x - size * 0.10f, y - size * 0.30f, x, y)
        }
      val leafRight =
        Path().apply {
          moveTo(x, y + size * 0.2f)
          quadraticTo(x + size * 0.55f, y - size * 0.15f, x + size * 0.42f, y - size * 0.35f)
          quadraticTo(x + size * 0.10f, y - size * 0.10f, x, y + size * 0.2f)
        }
      drawPath(leafLeft, color = accent.copy(alpha = 0.85f))
      drawPath(leafRight, color = accent.copy(alpha = 0.85f))
    }
    "CUPS" -> {
      // Sacred Golden Chalice with Pearl Halo
      val bowl =
        Path().apply {
          moveTo(x - size * 0.62f, y - size * 0.45f)
          quadraticTo(x, y + size * 0.38f, x + size * 0.62f, y - size * 0.45f)
          close()
        }
      drawPath(bowl, color = accent.copy(alpha = 0.42f))
      drawPath(bowl, color = gold, style = Stroke(width = 2.2f))
      drawLine(gold, Offset(x, y - size * 0.02f), Offset(x, y + size * 0.62f), strokeWidth = 2.6f)
      drawLine(gold, Offset(x - size * 0.42f, y + size * 0.62f), Offset(x + size * 0.42f, y + size * 0.62f), strokeWidth = 2.6f)
      drawCircle(gold, radius = size * 0.14f, center = Offset(x, y - size * 0.64f))
    }
    "SWORDS" -> {
      // Ceremonial Winged Sword
      drawLine(
        color = gold,
        start = Offset(x, y - size * 0.90f),
        end = Offset(x, y + size * 0.85f),
        strokeWidth = 2.8f,
        cap = StrokeCap.Round,
      )
      // Curved guard
      val guard =
        Path().apply {
          moveTo(x - size * 0.56f, y + size * 0.24f)
          quadraticTo(x, y + size * 0.42f, x + size * 0.56f, y + size * 0.24f)
        }
      drawPath(guard, color = gold, style = Stroke(width = 2.8f, cap = StrokeCap.Round))
      drawCircle(accent, radius = size * 0.12f, center = Offset(x, y + size * 0.32f))
    }
    else -> {
      // PENTACLES: Golden Coin with Inscribed 5-Pointed Star
      drawCircle(accent.copy(alpha = 0.28f), radius = size * 0.76f, center = Offset(x, y))
      drawCircle(gold, radius = size * 0.76f, center = Offset(x, y), style = Stroke(width = 2.2f))
      drawPentagramRing(x, y, size * 0.68f, gold, accent, inverted = false)
    }
  }
}

private fun DrawScope.drawCourtCrest(
  rank: Int,
  cx: Float,
  cy: Float,
  r: Float,
  gold: Color,
  halo: Color,
) {
  when (rank) {
    11 -> {
      // Page: Lotus Blossom Crown
      drawRoseBlossom(cx, cy, r * 0.65f, gold, halo)
    }
    12 -> {
      // Knight: Winged Helm Crest
      drawAngelWings(cx, cy, r * 1.15f, gold)
      drawFourPointStar(cx, cy, r * 0.42f, gold)
    }
    13 -> {
      // Queen: Crescent Moon Tiara
      drawCrescentMoon(cx, cy, r * 0.62f, gold, halo)
      drawFourPointStar(cx, cy - r * 0.42f, r * 0.30f, gold)
    }
    else -> {
      // King: Sovereign 5-Spire Crown
      val crown =
        Path().apply {
          moveTo(cx - r * 0.75f, cy + r * 0.28f)
          lineTo(cx - r * 0.85f, cy - r * 0.28f)
          lineTo(cx - r * 0.42f, cy + r * 0.05f)
          lineTo(cx, cy - r * 0.45f)
          lineTo(cx + r * 0.42f, cy + r * 0.05f)
          lineTo(cx + r * 0.85f, cy - r * 0.28f)
          lineTo(cx + r * 0.75f, cy + r * 0.28f)
          close()
        }
      drawPath(crown, color = halo.copy(alpha = 0.35f))
      drawPath(crown, color = gold, style = Stroke(width = 2.4f, join = StrokeJoin.Round))
    }
  }
}

private fun DrawScope.drawFourPointStar(cx: Float, cy: Float, r: Float, color: Color) {
  val p =
    Path().apply {
      moveTo(cx, cy - r)
      quadraticTo(cx, cy, cx + r, cy)
      quadraticTo(cx, cy, cx, cy + r)
      quadraticTo(cx, cy, cx - r, cy)
      quadraticTo(cx, cy, cx, cy - r)
      close()
    }
  drawPath(p, color = color)
}

private fun DrawScope.drawEightPointStar(cx: Float, cy: Float, r: Float, color: Color) {
  drawFourPointStar(cx, cy, r, color)
  val rDiag = r * 0.68f
  drawLine(color, Offset(cx - rDiag, cy - rDiag), Offset(cx + rDiag, cy + rDiag), strokeWidth = 1.8f)
  drawLine(color, Offset(cx + rDiag, cy - rDiag), Offset(cx - rDiag, cy + rDiag), strokeWidth = 1.8f)
}

private fun DrawScope.drawRadialRays(
  cx: Float,
  cy: Float,
  innerR: Float,
  outerR: Float,
  count: Int,
  color: Color,
) {
  for (i in 0 until count) {
    val a = (2.0 * Math.PI * i / count) - Math.PI / 2.0
    val rEnd = if (i % 2 == 0) outerR else outerR * 0.78f
    drawLine(
      color = color,
      start = Offset(cx + (innerR * cos(a)).toFloat(), cy + (innerR * sin(a)).toFloat()),
      end = Offset(cx + (rEnd * cos(a)).toFloat(), cy + (rEnd * sin(a)).toFloat()),
      strokeWidth = 1.4f,
      cap = StrokeCap.Round,
    )
  }
}

private fun DrawScope.drawInfinitySymbol(cx: Float, cy: Float, w: Float, color: Color) {
  val h = w * 0.48f
  val path =
    Path().apply {
      moveTo(cx, cy)
      cubicTo(cx - w * 0.5f, cy - h, cx - w, cy - h * 0.4f, cx - w * 0.75f, cy + h * 0.3f)
      cubicTo(cx - w * 0.5f, cy + h * 0.8f, cx - w * 0.15f, cy + h * 0.2f, cx, cy)
      cubicTo(cx + w * 0.5f, cy - h, cx + w, cy - h * 0.4f, cx + w * 0.75f, cy + h * 0.3f)
      cubicTo(cx + w * 0.5f, cy + h * 0.8f, cx + w * 0.15f, cy + h * 0.2f, cx, cy)
    }
  drawPath(path, color = color, style = Stroke(width = 2.6f, cap = StrokeCap.Round))
}

private fun DrawScope.drawCrescentMoon(
  cx: Float,
  cy: Float,
  r: Float,
  gold: Color,
  fill: Color,
) {
  drawCircle(fill.copy(alpha = 0.35f), radius = r, center = Offset(cx, cy))
  drawCircle(gold, radius = r, center = Offset(cx, cy), style = Stroke(width = 2.4f))
  drawCircle(gold, radius = r * 0.76f, center = Offset(cx + r * 0.28f, cy - r * 0.14f), style = Stroke(width = 1.6f))
}

private fun DrawScope.drawRoseBlossom(
  cx: Float,
  cy: Float,
  r: Float,
  gold: Color,
  petalColor: Color,
) {
  for (i in 0 until 5) {
    val a = Math.toRadians((-90 + i * 72).toDouble())
    val px = cx + (r * 0.44f * cos(a)).toFloat()
    val py = cy + (r * 0.44f * sin(a)).toFloat()
    drawCircle(petalColor.copy(alpha = 0.35f), radius = r * 0.38f, center = Offset(px, py))
    drawCircle(gold, radius = r * 0.38f, center = Offset(px, py), style = Stroke(width = 1.8f))
  }
  drawCircle(gold, radius = r * 0.18f, center = Offset(cx, cy))
}

private fun DrawScope.drawAngelWings(cx: Float, cy: Float, span: Float, gold: Color) {
  val leftWing =
    Path().apply {
      moveTo(cx, cy)
      quadraticTo(cx - span * 0.55f, cy - span * 0.42f, cx - span * 0.85f, cy - span * 0.05f)
      quadraticTo(cx - span * 0.45f, cy + span * 0.26f, cx, cy + span * 0.08f)
      close()
    }
  val rightWing =
    Path().apply {
      moveTo(cx, cy)
      quadraticTo(cx + span * 0.55f, cy - span * 0.42f, cx + span * 0.85f, cy - span * 0.05f)
      quadraticTo(cx + span * 0.45f, cy + span * 0.26f, cx, cy + span * 0.08f)
      close()
    }
  drawPath(leftWing, color = gold.copy(alpha = 0.25f))
  drawPath(rightWing, color = gold.copy(alpha = 0.25f))
  drawPath(leftWing, color = gold, style = Stroke(width = 2f))
  drawPath(rightWing, color = gold, style = Stroke(width = 2f))
}

private fun DrawScope.drawHexagramStar(cx: Float, cy: Float, r: Float, gold: Color) {
  drawPolygonOutline(cx, cy, r, 3, -90.0, gold, 2.2f)
  drawPolygonOutline(cx, cy, r, 3, 90.0, gold, 2.2f)
}

private fun DrawScope.drawPentagramRing(
  cx: Float,
  cy: Float,
  r: Float,
  gold: Color,
  accent: Color,
  inverted: Boolean,
) {
  val startDeg = if (inverted) 90 else -90
  val star = Path()
  for (i in 0 until 5) {
    val a = Math.toRadians((startDeg + i * 144).toDouble())
    val px = cx + (r * cos(a)).toFloat()
    val py = cy + (r * sin(a)).toFloat()
    if (i == 0) star.moveTo(px, py) else star.lineTo(px, py)
  }
  star.close()
  drawPath(star, color = accent.copy(alpha = 0.25f))
  drawPath(star, color = gold, style = Stroke(width = 1.9f, join = StrokeJoin.Round))
}

private fun DrawScope.drawPolygonOutline(
  cx: Float,
  cy: Float,
  r: Float,
  sides: Int,
  rotationDeg: Double,
  color: Color,
  strokeWidth: Float,
) {
  val p = Path()
  for (i in 0 until sides) {
    val a = Math.toRadians(rotationDeg + i * (360.0 / sides))
    val px = cx + (r * cos(a)).toFloat()
    val py = cy + (r * sin(a)).toFloat()
    if (i == 0) p.moveTo(px, py) else p.lineTo(px, py)
  }
  p.close()
  drawPath(p, color = color, style = Stroke(width = strokeWidth, join = StrokeJoin.Round))
}
