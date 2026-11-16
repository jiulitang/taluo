package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.ReadingRecordEntity
import com.example.data.model.TarotCardEntity
import com.example.data.preferences.TarotDeckStyle
import com.example.ui.TarotAppViewModel
import com.example.ui.components.CardMeaningBottomSheet
import com.example.ui.components.TarotCard3D
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.LocalTarotColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SanctuaryScreen(
  viewModel: TarotAppViewModel,
  onNavigateToDivination: () -> Unit,
  onDiscussWithMentor: (ReadingRecordEntity) -> Unit,
  onOpenSettings: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val tarotColors = LocalTarotColors.current
  val allCards by viewModel.allCards.collectAsStateWithLifecycle()
  val todayReading by viewModel.todayDailyReading.collectAsStateWithLifecycle()
  val allReadings by viewModel.allReadings.collectAsStateWithLifecycle()
  val isAiLoading by viewModel.isDailyAiLoading.collectAsStateWithLifecycle()
  val apiSettings by viewModel.apiSettings.collectAsStateWithLifecycle()

  var cardFlippedLocally by remember(todayReading?.id) { mutableStateOf(todayReading != null) }
  var inspectCardSheet by remember { mutableStateOf<Pair<TarotCardEntity, Boolean>?>(null) }
  var inspectPastReading by remember { mutableStateOf<ReadingRecordEntity?>(null) }

  val cardsById = remember(allCards) { allCards.associateBy { it.id } }
  val dailySnapshot =
    remember(todayReading) {
      todayReading?.let { viewModel.repository.decodeDrawnCards(it.drawnCardsJson).firstOrNull() }
    }
  val dailyCardEntity = dailySnapshot?.let { cardsById[it.cardId] }

  val dateLabel =
    remember { SimpleDateFormat("yyyy年MM月dd日 · EEEE", Locale.SIMPLIFIED_CHINESE).format(Date()) }

  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    LazyColumn(
      modifier = Modifier.widthIn(max = 680.dp).fillMaxSize(),
      contentPadding = PaddingValues(bottom = 28.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // 1. Ethereal Hero Banner
      item {
        Box(
          modifier =
            Modifier.fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 8.dp)
              .height(176.dp)
              .clip(RoundedCornerShape(26.dp))
        ) {
          Image(
            painter = painterResource(id = R.drawable.img_sanctuary_banner),
            contentDescription = "圣光塔罗神殿",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
          )
          Box(
            modifier =
              Modifier.fillMaxSize()
                .background(
                  Brush.verticalGradient(
                    listOf(Color(0x2218142C), Color(0x881A1530), Color(0xCC18132B))
                  )
                )
          )
          Column(
            modifier = Modifier.align(Alignment.BottomStart).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.Rounded.NightsStay,
                contentDescription = null,
                tint = Color(0xFFF6DF9F),
                modifier = Modifier.size(16.dp),
              )
              Spacer(Modifier.width(6.dp))
              Text(
                text = dateLabel,
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFFF6DF9F),
              )
            }
            Text(
              text = "AURELIA SANCTUARY",
              fontFamily = CinzelFontFamily,
              fontWeight = FontWeight.SemiBold,
              style = MaterialTheme.typography.headlineLarge,
              color = Color.White,
            )
            Text(
              text = "静心澄意 · 倾听今日圣光启示",
              style = MaterialTheme.typography.bodyMedium,
              color = Color(0xFFEAE4F5),
            )
          }
        }
      }

      // 2. Daily Draw Ritual Card
      item {
        Card(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
          shape = RoundedCornerShape(26.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, tarotColors.goldBorder.copy(alpha = 0.55f)),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column {
                Text(
                  text = "每日圣光抽牌",
                  style = MaterialTheme.typography.titleLarge,
                  color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                  text = if (dailyCardEntity == null) "轻触牌背开启今日 3D 翻牌仪式" else "点击牌面可查看完整牌义圣典",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
              if (dailyCardEntity != null) {
                Surface(
                  shape = CircleShape,
                  color = tarotColors.goldSoft,
                  modifier =
                    Modifier.testTag("redraw_daily_button").clickable {
                      cardFlippedLocally = false
                      viewModel.performDailyDraw(forceRedraw = true)
                      cardFlippedLocally = true
                    },
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Icon(
                      Icons.Rounded.Refresh,
                      contentDescription = "重新抽牌",
                      tint = tarotColors.goldAccent,
                      modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("重抽", style = MaterialTheme.typography.labelLarge, color = tarotColors.goldAccent)
                  }
                }
              }
            }

            // Deck Art Theme Quick Selector
            LazyRow(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              items(TarotDeckStyle.entries, key = { it.name }) { style ->
                FilterChip(
                  selected = apiSettings.deckStyle == style,
                  onClick = { viewModel.setDeckStyle(style) },
                  label = { Text(style.badgeZh) },
                )
              }
            }

            // 3D Card Stage
            TarotCard3D(
              isRevealed = cardFlippedLocally && dailyCardEntity != null,
              card = dailyCardEntity,
              isReversed = dailySnapshot?.isReversed ?: false,
              deckStyle = apiSettings.deckStyle,
              slotLabel = "轻触开启今日启示",
              testTagName = "daily_draw_card_3d",
              modifier = Modifier.width(208.dp),
              onClick = {
                if (dailyCardEntity == null) {
                  viewModel.performDailyDraw(forceRedraw = false)
                  cardFlippedLocally = true
                } else if (!cardFlippedLocally) {
                  cardFlippedLocally = true
                } else {
                  inspectCardSheet = dailyCardEntity to (dailySnapshot?.isReversed ?: false)
                }
              },
            )

            // Detailed Daily Interpretation once revealed
            AnimatedVisibility(
              visible = cardFlippedLocally && dailyCardEntity != null && dailySnapshot != null,
              enter = fadeIn() + slideInVertically { it / 4 },
            ) {
              if (dailyCardEntity != null && dailySnapshot != null) {
                val isRev = dailySnapshot.isReversed
                Column(
                  modifier = Modifier.fillMaxWidth(),
                  verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                  Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = tarotColors.goldSoft.copy(alpha = 0.55f),
                    border = BorderStroke(1.dp, tarotColors.goldBorder.copy(alpha = 0.45f)),
                  ) {
                    Column(
                      modifier = Modifier.fillMaxWidth().padding(14.dp),
                      verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                      Text(
                        text =
                          "关键词 · " +
                            if (isRev) dailyCardEntity.keywordsReversed else dailyCardEntity.keywordsUpright,
                        style = MaterialTheme.typography.labelLarge,
                        color = tarotColors.goldAccent,
                      )
                      Text(
                        text =
                          if (isRev) dailyCardEntity.meaningReversed else dailyCardEntity.meaningUpright,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                      )
                    }
                  }

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                  ) {
                    OutlinedButton(
                      onClick = { inspectCardSheet = dailyCardEntity to isRev },
                      modifier = Modifier.weight(1f).testTag("daily_detail_button"),
                      shape = RoundedCornerShape(14.dp),
                    ) {
                      Icon(Icons.Rounded.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                      Spacer(Modifier.width(6.dp))
                      Text("详尽牌义")
                    }
                    Button(
                      onClick = {
                        if (!apiSettings.isConfigured) {
                          onOpenSettings()
                        } else {
                          viewModel.interpretDailyWithAi()
                        }
                      },
                      enabled = !isAiLoading,
                      modifier = Modifier.weight(1f).testTag("daily_ai_interpret_button"),
                      shape = RoundedCornerShape(14.dp),
                      colors = ButtonDefaults.buttonColors(containerColor = tarotColors.goldAccent),
                    ) {
                      if (isAiLoading) {
                        CircularProgressIndicator(
                          modifier = Modifier.size(18.dp),
                          strokeWidth = 2.dp,
                          color = Color.White,
                        )
                      } else {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                      }
                      Spacer(Modifier.width(6.dp))
                      Text(if (apiSettings.isConfigured) "AI 深度解析" else "配置 API 解析")
                    }
                  }

                  val aiText = todayReading?.aiInterpretation.orEmpty()
                  if (aiText.isNotBlank()) {
                    Surface(
                      shape = RoundedCornerShape(18.dp),
                      color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                      border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)),
                    ) {
                      Column(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                      ) {
                        Row(
                          modifier = Modifier.fillMaxWidth(),
                          horizontalArrangement = Arrangement.SpaceBetween,
                          verticalAlignment = Alignment.CenterVertically,
                        ) {
                          Text(
                            text = "导师圣光解读 · ${todayReading?.mentorName.orEmpty().ifBlank { "AI 导师" }}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary,
                          )
                          todayReading?.let { rec ->
                            Text(
                              text = "进入对话探讨 →",
                              style = MaterialTheme.typography.labelLarge,
                              color = tarotColors.goldAccent,
                              modifier = Modifier.clickable { onDiscussWithMentor(rec) },
                            )
                          }
                        }
                        Text(
                          text = aiText,
                          style = MaterialTheme.typography.bodyMedium,
                          color = MaterialTheme.colorScheme.onSurface,
                        )
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }

      // 3. Quick CTA to Multi-Spread Divination
      item {
        Surface(
          modifier =
            Modifier.fillMaxWidth()
              .padding(horizontal = 16.dp)
              .testTag("goto_divination_card")
              .clickable { onNavigateToDivination() },
          shape = RoundedCornerShape(22.dp),
          color = tarotColors.sanctuaryMist.copy(alpha = 0.72f),
          border = BorderStroke(1.dp, tarotColors.celestialGlow.copy(alpha = 0.3f)),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "AI 协同多牌阵占卜",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Spacer(Modifier.height(2.dp))
              Text(
                text = "时间流、维纳斯之镜、抉择之门、凯尔特十字 · 支持 AI 选阵与实时解读",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            Spacer(Modifier.width(12.dp))
            Surface(shape = CircleShape, color = tarotColors.celestialGlow.copy(alpha = 0.18f)) {
              Icon(
                Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = tarotColors.celestialGlow,
                modifier = Modifier.padding(12.dp).size(22.dp),
              )
            }
          }
        }
      }

      // 4. Recent Divination Journal
      item {
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(
            Icons.Rounded.History,
            contentDescription = null,
            tint = tarotColors.goldAccent,
            modifier = Modifier.size(18.dp),
          )
          Spacer(Modifier.width(8.dp))
          Text(
            text = "近期圣殿占卜记录",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
          )
        }
      }

      if (allReadings.isEmpty()) {
        item {
          Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
          ) {
            Text(
              text = "尚无占卜记录。在上方抽取今日第一张圣光牌，将你的灵魂轨迹留存于此。",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(18.dp),
            )
          }
        }
      } else {
        items(allReadings.take(8), key = { it.id }) { rec ->
          val snapshots = remember(rec.drawnCardsJson) { viewModel.repository.decodeDrawnCards(rec.drawnCardsJson) }
          val cardSummary =
            snapshots.joinToString(" · ") {
              "${it.cardNameZh}(${if (it.isReversed) "逆" else "正"})"
            }
          Surface(
            modifier =
              Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable { inspectPastReading = rec },
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
          ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Text(
                  text = rec.spreadName,
                  style = MaterialTheme.typography.labelLarge,
                  color = tarotColors.goldAccent,
                )
                Text(
                  text = rec.dateKey,
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
              Text(
                text = rec.question,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Text(
                text = cardSummary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }
      }
    }
  }

  inspectCardSheet?.let { (card, isRev) ->
    CardMeaningBottomSheet(
      card = card,
      isReversed = isRev,
      deckStyle = apiSettings.deckStyle,
      onDismiss = { inspectCardSheet = null },
    )
  }

  inspectPastReading?.let { rec ->
    PastReadingDialog(
      record = rec,
      summaryCards = viewModel.repository.decodeDrawnCards(rec.drawnCardsJson),
      onDismiss = { inspectPastReading = null },
      onDiscussWithMentor = {
        inspectPastReading = null
        onDiscussWithMentor(rec)
      },
    )
  }
}

@Composable
private fun PastReadingDialog(
  record: ReadingRecordEntity,
  summaryCards: List<com.example.data.model.DrawnCardSnapshot>,
  onDismiss: () -> Unit,
  onDiscussWithMentor: () -> Unit,
) {
  androidx.compose.material3.AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("${record.spreadName} · ${record.dateKey}") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("问题：${record.question}", style = MaterialTheme.typography.titleMedium)
        HorizontalDivider()
        summaryCards.forEach { snap ->
          Text(
            text = "• ${snap.positionName}：${snap.cardNameZh}（${if (snap.isReversed) "逆位" else "正位"}）",
            style = MaterialTheme.typography.bodyMedium,
          )
        }
        if (record.aiInterpretation.isNotBlank()) {
          HorizontalDivider()
          Text(
            text = record.aiInterpretation,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 10,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    },
    confirmButton = {
      Button(onClick = onDiscussWithMentor) {
        Icon(Icons.Rounded.Forum, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text("与导师续聊")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) { Text("关闭") }
    },
  )
}
