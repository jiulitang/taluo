package com.example.ui.components

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.example.data.model.DrawnCardSnapshot
import com.example.data.model.TarotCardEntity
import com.example.data.preferences.TarotDeckStyle
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Renders a high-resolution sacred Tarot Divination Scroll Poster [Bitmap] containing:
 * 1. Sacred header & date
 * 2. The user's divination question ("问卜心念") & selected spread name
 * 3. Visual card grid of all drawn cards (with Roman numeral, orientation, position, and keywords)
 * 4. AI Mentor interpretation (or local sacred summary)
 * And saves it to the device photo gallery via [MediaStore] with zero storage permissions.
 */
object SpreadPosterGenerator {

  suspend fun renderSpreadPosterBitmap(
    question: String,
    spreadName: String,
    drawnCards: List<DrawnCardSnapshot>,
    cardsById: Map<Int, TarotCardEntity>,
    aiInterpretation: String,
    mentorName: String,
    deckStyle: TarotDeckStyle,
  ): Bitmap =
    withContext(Dispatchers.Default) {
      val width = 1080
      val pad = 56f
      val contentWidth = (width - pad * 2).toInt()

      val isDarkDeck =
        deckStyle == TarotDeckStyle.ASTRAL_GOLD_FOIL || deckStyle == TarotDeckStyle.MARIAS_GATE
      val bgTop = if (isDarkDeck) 0xFF151126.toInt() else 0xFFFBF8F1.toInt()
      val bgBottom = if (isDarkDeck) 0xFF231C3D.toInt() else 0xFFF1E8D8.toInt()
      val surfaceColor = if (isDarkDeck) 0xFF282145.toInt() else 0xFFFFFDF8.toInt()
      val goldColor = if (isDarkDeck) 0xFFF2D07B.toInt() else 0xFFB58834.toInt()
      val textPrimary = if (isDarkDeck) 0xFFF7F0DF.toInt() else 0xFF2B243D.toInt()
      val textSecondary = if (isDarkDeck) 0xFFC8BBE0.toInt() else 0xFF685F7A.toInt()

      val bodyPaint =
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
          color = textPrimary
          textSize = 28f
          typeface = Typeface.SERIF
        }

      val cleanQuestion = question.ifBlank { "探索当下灵魂轨迹与圣光指引" }
      val questionLayout =
        buildStaticLayout("问卜心念：「$cleanQuestion」", bodyPaint, contentWidth - 64)

      val summaryText =
        if (aiInterpretation.isNotBlank()) {
          aiInterpretation.trim()
        } else {
          drawnCards.joinToString("\n\n") { snap ->
            val entity = cardsById[snap.cardId]
            val orient = if (snap.isReversed) "逆位" else "正位"
            val meaning =
              if (snap.isReversed) entity?.meaningReversed.orEmpty()
              else entity?.meaningUpright.orEmpty()
            "【${snap.positionIndex + 1}. ${snap.positionName}】${snap.cardNameZh}（$orient）\n$meaning"
          }
        }

      val interpPaint =
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
          color = textPrimary
          textSize = 25f
        }
      val interpLayout = buildStaticLayout(summaryText, interpPaint, contentWidth - 64)

      // Calculate card grid rows (2 cards per row)
      val rows = (drawnCards.size + 1) / 2
      val cardBoxHeight = 230f
      val cardGridHeight = rows * cardBoxHeight + (rows - 1).coerceAtLeast(0) * 24f

      val totalHeight =
        (260f +
            (questionLayout.height + 76f) +
            50f +
            cardGridHeight +
            60f +
            (interpLayout.height + 110f) +
            130f)
          .toInt()
          .coerceAtLeast(1480)

      val bitmap = Bitmap.createBitmap(width, totalHeight, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(bitmap)

      // 1. Background Gradient
      val bgPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
          shader =
            LinearGradient(
              0f,
              0f,
              0f,
              totalHeight.toFloat(),
              bgTop,
              bgBottom,
              Shader.TileMode.CLAMP,
            )
        }
      canvas.drawRect(0f, 0f, width.toFloat(), totalHeight.toFloat(), bgPaint)

      // 2. Outer Sacred Gold Frame
      val borderPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
          color = goldColor
          style = Paint.Style.STROKE
          strokeWidth = 3.5f
        }
      canvas.drawRoundRect(
        RectF(28f, 28f, width - 28f, totalHeight - 28f),
        32f,
        32f,
        borderPaint,
      )
      borderPaint.strokeWidth = 1.5f
      canvas.drawRoundRect(
        RectF(38f, 38f, width - 38f, totalHeight - 38f),
        26f,
        26f,
        borderPaint,
      )

      // 3. Header Title
      val titlePaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
          color = goldColor
          textSize = 44f
          textAlign = Paint.Align.CENTER
          typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
      canvas.drawText("✦ 圣 光 塔 罗 · 占 卜 圣 卷 ✦", width / 2f, 118f, titlePaint)

      val subHeaderPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
          color = textSecondary
          textSize = 24f
          textAlign = Paint.Align.CENTER
        }
      val dateStr = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault()).format(Date())
      canvas.drawText(
        "牌阵：$spreadName   |   卡面：${deckStyle.badgeZh}   |   $dateStr",
        width / 2f,
        164f,
        subHeaderPaint,
      )

      var currentY = 205f

      // 4. Question Scroll Box
      val boxFillPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
          color = surfaceColor
          style = Paint.Style.FILL
        }
      val qBoxHeight = questionLayout.height + 56f
      val qRect = RectF(pad, currentY, width - pad, currentY + qBoxHeight)
      canvas.drawRoundRect(qRect, 24f, 24f, boxFillPaint)
      canvas.drawRoundRect(qRect, 24f, 24f, borderPaint)

      canvas.save()
      canvas.translate(pad + 32f, currentY + 28f)
      questionLayout.draw(canvas)
      canvas.restore()

      currentY += qBoxHeight + 36f

      // 5. Drawn Cards Grid (2 columns)
      val colWidth = (contentWidth - 24f) / 2f
      val cardTitlePaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
          color = textPrimary
          textSize = 27f
          typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
      val cardMetaPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
          color = goldColor
          textSize = 22f
          typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
      val cardSubPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
          color = textSecondary
          textSize = 21f
        }

      drawnCards.forEachIndexed { idx, snap ->
        val row = idx / 2
        val col = idx % 2
        val left = pad + col * (colWidth + 24f)
        val top = currentY + row * (cardBoxHeight + 24f)
        val cardRect = RectF(left, top, left + colWidth, top + cardBoxHeight)

        canvas.drawRoundRect(cardRect, 22f, 22f, boxFillPaint)
        canvas.drawRoundRect(cardRect, 22f, 22f, borderPaint)

        // Miniature gold tarot card emblem on the left side of the card cell
        val miniRect = RectF(left + 20f, top + 22f, left + 132f, top + cardBoxHeight - 22f)
        val miniPaint =
          Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isDarkDeck) 0xFF1B1530.toInt() else 0xFFF5ECD8.toInt()
            style = Paint.Style.FILL
          }
        canvas.drawRoundRect(miniRect, 14f, 14f, miniPaint)
        canvas.drawRoundRect(miniRect, 14f, 14f, borderPaint)

        // Draw Roman numeral & diamond inside miniature card
        val numeralPaint =
          Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = goldColor
            textSize = 24f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
          }
        canvas.drawText(snap.romanNumeral, miniRect.centerX(), miniRect.top + 42f, numeralPaint)
        canvas.drawCircle(miniRect.centerX(), miniRect.centerY() + 10f, 28f, borderPaint)

        val orientText = if (snap.isReversed) "逆位" else "正位"
        val textLeft = miniRect.right + 22f
        canvas.drawText(
          "${snap.positionIndex + 1}. ${snap.positionName}",
          textLeft,
          top + 52f,
          cardMetaPaint,
        )
        canvas.drawText(
          "${snap.cardNameZh} · $orientText",
          textLeft,
          top + 94f,
          cardTitlePaint,
        )
        canvas.drawText(
          snap.cardNameEn,
          textLeft,
          top + 128f,
          cardSubPaint,
        )

        val entity = cardsById[snap.cardId]
        val kw =
          (if (snap.isReversed) entity?.keywordsReversed else entity?.keywordsUpright)
            .orEmpty()
            .take(18)
        canvas.drawText(kw, textLeft, top + 168f, cardSubPaint)
      }

      currentY += cardGridHeight + 42f

      // 6. Interpretation Scroll Box
      val interpBoxHeight = interpLayout.height + 96f
      val interpRect = RectF(pad, currentY, width - pad, currentY + interpBoxHeight)
      canvas.drawRoundRect(interpRect, 24f, 24f, boxFillPaint)
      canvas.drawRoundRect(interpRect, 24f, 24f, borderPaint)

      val sectionHeader =
        if (aiInterpretation.isNotBlank()) {
          "✦ ${mentorName.ifBlank { "圣光导师" }} · 深度占卜解析"
        } else {
          "✦ 圣典牌义启示录"
        }
      canvas.drawText(sectionHeader, pad + 32f, currentY + 46f, cardMetaPaint)

      canvas.save()
      canvas.translate(pad + 32f, currentY + 68f)
      interpLayout.draw(canvas)
      canvas.restore()

      // 7. Footer Seal
      canvas.drawText(
        "— 圣光塔罗 AURELIA TAROT SANCTUARY · 愿星辰与圣光常伴你左右 —",
        width / 2f,
        totalHeight - 54f,
        subHeaderPaint,
      )

      bitmap
    }

  suspend fun saveBitmapToGallery(context: Context, bitmap: Bitmap): Result<Uri> =
    withContext(Dispatchers.IO) {
      runCatching {
        val fileName = "AureliaTarot_${System.currentTimeMillis()}.png"
        val resolver = context.contentResolver
        val contentValues =
          ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
              put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + "/AureliaTarot",
              )
              put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
          }

        val uri =
          resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            ?: throw IllegalStateException("无法创建相册图片条目")

        val stream: OutputStream =
          resolver.openOutputStream(uri) ?: throw IllegalStateException("无法写入图片输出流")
        stream.use { out ->
          bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          contentValues.clear()
          contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
          resolver.update(uri, contentValues, null, null)
        }
        uri
      }
    }

  fun sharePosterUri(context: Context, uri: Uri, question: String) {
    val shareIntent =
      Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, "【圣光塔罗占卜】$question")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }
    context.startActivity(Intent.createChooser(shareIntent, "分享塔罗占卜圣卷"))
  }

  private fun buildStaticLayout(text: String, paint: TextPaint, width: Int): StaticLayout {
    return StaticLayout.Builder.obtain(text, 0, text.length, paint, width.coerceAtLeast(100))
      .setAlignment(Layout.Alignment.ALIGN_NORMAL)
      .setLineSpacing(8f, 1.15f)
      .setIncludePad(false)
      .build()
  }
}
