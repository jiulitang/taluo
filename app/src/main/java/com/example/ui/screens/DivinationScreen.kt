package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.ViewCarousel
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BuiltInSpreads
import com.example.data.model.DrawnCardSnapshot
import com.example.data.model.ReadingRecordEntity
import com.example.data.model.TarotCardEntity
import com.example.ui.DivinationPhase
import com.example.ui.DivinationUiState
import com.example.ui.TarotAppViewModel
import com.example.ui.components.CardMeaningBottomSheet
import com.example.ui.components.SlidingTarotDeckFan
import com.example.ui.components.SpreadPosterGenerator
import com.example.ui.components.TarotCard3D
import com.example.ui.components.TarotShuffleRitualStage
import com.example.ui.theme.LocalTarotColors
import kotlinx.coroutines.launch

@Composable
fun DivinationScreen(
  viewModel: TarotAppViewModel,
  onDiscussWithMentor: (ReadingRecordEntity) -> Unit,
  onOpenSettings: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val state by viewModel.divinationState.collectAsStateWithLifecycle()
  val allCards by viewModel.allCards.collectAsStateWithLifecycle()
  val apiSettings by viewModel.apiSettings.collectAsStateWithLifecycle()
  val activePersona by viewModel.activePersona.collectAsStateWithLifecycle()

  val cardsById = remember(allCards) { allCards.associateBy { it.id } }
  var inspectSheet by remember { mutableStateOf<Pair<TarotCardEntity, DrawnCardSnapshot>?>(null) }
  var posterPreviewBitmap by remember { mutableStateOf<Bitmap?>(null) }
  var isGeneratingPoster by remember { mutableStateOf(false) }

  val drawnCount = state.drawnSlots.count { it != null }
  val totalCount = state.selectedSpread.cardCount
  val allDrawn = drawnCount == totalCount && totalCount > 0

  // Allow hardware back press in SHUFFLING or DRAWING phase to return to SETUP screen
  BackHandler(enabled = state.phase != DivinationPhase.SETUP) {
    viewModel.returnToSpreadSetup()
  }

  fun generateAndShowPoster() {
    val finalCards = state.drawnSlots.filterNotNull()
    if (finalCards.isEmpty() || isGeneratingPoster) return
    isGeneratingPoster = true
    scope.launch {
      val bmp =
        SpreadPosterGenerator.renderSpreadPosterBitmap(
          question = state.question.ifBlank { state.currentReading?.question.orEmpty() },
          spreadName = state.selectedSpread.nameZh,
          drawnCards = finalCards,
          cardsById = cardsById,
          aiInterpretation = state.currentReading?.aiInterpretation.orEmpty(),
          mentorName =
            state.currentReading?.mentorName?.ifBlank { activePersona?.name.orEmpty() }.orEmpty(),
          deckStyle = apiSettings.deckStyle,
        )
      isGeneratingPoster = false
      posterPreviewBitmap = bmp
    }
  }

  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    Column(
      modifier = Modifier.widthIn(max = 720.dp).fillMaxSize().padding(horizontal = 16.dp),
    ) {
      Spacer(Modifier.height(10.dp))
      RitualStepHeader(
        currentStep =
          when (state.phase) {
            DivinationPhase.SETUP -> 1
            DivinationPhase.SHUFFLING -> 2
            DivinationPhase.DRAWING -> if (allDrawn) 4 else 3
          },
        drawnCount = drawnCount,
        totalCount = totalCount,
      )
      Spacer(Modifier.height(12.dp))

      AnimatedContent(
        targetState = state.phase,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "divination_phase_transition",
        modifier = Modifier.weight(1f).fillMaxWidth(),
      ) { phase ->
        when (phase) {
          DivinationPhase.SETUP -> {
            SpreadSetupScreenStage(
              state = state,
              isApiConfigured = apiSettings.isConfigured,
              onQuestionChange = viewModel::updateDivinationQuestion,
              onSelectSpread = viewModel::selectSpread,
              onAiSelectSpread = {
                if (!apiSettings.isConfigured) onOpenSettings()
                else viewModel.aiSelectSpreadForQuestion()
              },
              onConfirmSpreadAndEnterDraw = viewModel::startDivinationRitual,
            )
          }

          DivinationPhase.SHUFFLING -> {
            LazyColumn(
              modifier = Modifier.fillMaxSize(),
              contentPadding = PaddingValues(bottom = 28.dp),
              verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
              item {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  TextButton(
                    onClick = viewModel::returnToSpreadSetup,
                    modifier = Modifier.testTag("back_to_setup_from_shuffle"),
                  ) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("返回修改牌阵")
                  }
                  Text(
                    text = "已选定：${state.selectedSpread.nameZh}",
                    style = MaterialTheme.typography.labelLarge,
                    color = LocalTarotColors.current.goldAccent,
                  )
                }
              }
              item {
                TarotShuffleRitualStage(
                  question = state.question,
                  spreadName = state.selectedSpread.nameZh,
                  spreadCardCount = state.selectedSpread.cardCount,
                  onShuffleFinished = viewModel::completeShuffleAndStartDraw,
                )
              }
            }
          }

          DivinationPhase.DRAWING -> {
            DrawingAndAltarScreenStage(
              state = state,
              cardsById = cardsById,
              deckStyle = apiSettings.deckStyle,
              isApiConfigured = apiSettings.isConfigured,
              isGeneratingPoster = isGeneratingPoster,
              onBackToSetup = viewModel::returnToSpreadSetup,
              onReshuffle = viewModel::startDivinationRitual,
              onDrawNextCard = viewModel::drawNextSequentialSlot,
              onInspectCard = { card, snap -> inspectSheet = card to snap },
              onAiInterpret = {
                if (!apiSettings.isConfigured) onOpenSettings()
                else viewModel.interpretCurrentSpreadWithAi()
              },
              onGeneratePoster = { generateAndShowPoster() },
              onDiscussWithMentor = onDiscussWithMentor,
            )
          }
        }
      }
    }
  }

  inspectSheet?.let { (card, snap) ->
    CardMeaningBottomSheet(
      card = card,
      isReversed = snap.isReversed,
      deckStyle = apiSettings.deckStyle,
      positionTitle = "${snap.positionName}（${snap.positionMeaning}）",
      onDismiss = { inspectSheet = null },
    )
  }

  posterPreviewBitmap?.let { bmp ->
    SpreadPosterPreviewDialog(
      bitmap = bmp,
      question = state.question.ifBlank { "探索当下灵魂轨迹与圣光指引" },
      onSaveToAlbum = {
        scope.launch {
          SpreadPosterGenerator.saveBitmapToGallery(context, bmp)
            .onSuccess {
              viewModel.showToast("占卜圣卷图片已保存至系统相册 (Pictures/AureliaTarot)")
            }
            .onFailure { err ->
              viewModel.showToast(err.message ?: "保存图片失败")
            }
        }
      },
      onShare = {
        scope.launch {
          SpreadPosterGenerator.saveBitmapToGallery(context, bmp)
            .onSuccess { uri ->
              SpreadPosterGenerator.sharePosterUri(
                context = context,
                uri = uri,
                question = state.question.ifBlank { "圣光塔罗占卜" },
              )
            }
            .onFailure { err ->
              viewModel.showToast(err.message ?: "分享失败")
            }
        }
      },
      onDismiss = { posterPreviewBitmap = null },
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SpreadSetupScreenStage(
  state: DivinationUiState,
  isApiConfigured: Boolean,
  onQuestionChange: (String) -> Unit,
  onSelectSpread: (com.example.data.model.TarotSpread) -> Unit,
  onAiSelectSpread: () -> Unit,
  onConfirmSpreadAndEnterDraw: () -> Unit,
) {
  val tarotColors = LocalTarotColors.current

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(bottom = 28.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    // 1. Question Input Card
    item {
      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, tarotColors.goldBorder.copy(alpha = 0.55f)),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              Icons.Rounded.Psychology,
              contentDescription = null,
              tint = tarotColors.goldAccent,
              modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text("第一步：凝念输入你的问题", style = MaterialTheme.typography.titleLarge)
          }

          OutlinedTextField(
            value = state.question,
            onValueChange = onQuestionChange,
            label = { Text("输入你想占卜的问题或困惑") },
            placeholder = { Text("例如：近期在换工作与留下之间犹豫不决，想看哪条路更适合我") },
            minLines = 2,
            maxLines = 4,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().testTag("divination_question_input"),
          )

          OutlinedButton(
            onClick = onAiSelectSpread,
            enabled = !state.isSelectingSpread,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().testTag("ai_select_spread_button"),
          ) {
            if (state.isSelectingSpread) {
              CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
            } else {
              Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.width(6.dp))
            Text(if (isApiConfigured) "根据问题让 AI 为我推荐牌阵" else "配置 API 后可使用 AI 推荐牌阵")
          }

          AnimatedVisibility(visible = state.aiSpreadReason.isNotBlank()) {
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = tarotColors.goldSoft.copy(alpha = 0.7f),
              border = BorderStroke(1.dp, tarotColors.goldBorder),
            ) {
              Text(
                text = "✦ AI 选阵启示：${state.aiSpreadReason}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth().padding(12.dp),
              )
            }
          }
        }
      }
    }

    // 2. Choose Spread Grid
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = "第二步：决定本次占卜使用的牌阵 (${BuiltInSpreads.all.size}种)",
          style = MaterialTheme.typography.titleMedium,
          modifier = Modifier.padding(horizontal = 4.dp),
        )
        BuiltInSpreads.all.forEach { spread ->
          val isSelected = spread.id == state.selectedSpread.id
          Surface(
            modifier =
              Modifier.fillMaxWidth()
                .testTag("spread_chip_${spread.id}")
                .clickable { onSelectSpread(spread) },
            shape = RoundedCornerShape(20.dp),
            color =
              if (isSelected) tarotColors.goldSoft
              else MaterialTheme.colorScheme.surface,
            border =
              BorderStroke(
                width = if (isSelected) 2.dp else 1.dp,
                color =
                  if (isSelected) tarotColors.goldAccent
                  else MaterialTheme.colorScheme.outlineVariant,
              ),
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = spread.nameZh,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                  )
                  Spacer(Modifier.width(8.dp))
                  Text(
                    text = spread.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = tarotColors.goldAccent,
                  )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                  ) {
                    Text(
                      text = "${spread.cardCount} 张牌",
                      style = MaterialTheme.typography.labelSmall,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                  }
                  if (isSelected) {
                    Spacer(Modifier.width(6.dp))
                    Icon(
                      Icons.Rounded.CheckCircle,
                      contentDescription = "已选中",
                      tint = tarotColors.goldAccent,
                      modifier = Modifier.size(18.dp),
                    )
                  }
                }
              }

              Text(
                text = spread.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )

              if (isSelected) {
                FlowRow(
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                  verticalArrangement = Arrangement.spacedBy(6.dp),
                  modifier = Modifier.padding(top = 4.dp),
                ) {
                  spread.positions.forEachIndexed { idx, pos ->
                    Surface(
                      shape = RoundedCornerShape(8.dp),
                      color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                      border = BorderStroke(0.8.dp, tarotColors.goldBorder),
                    ) {
                      Text(
                        text = "${idx + 1}. ${pos.title}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
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

    // 3. Primary CTA: Confirm Spread & Enter Shuffle + Draw Screen
    item {
      Button(
        onClick = onConfirmSpreadAndEnterDraw,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = tarotColors.goldAccent),
        modifier =
          Modifier.fillMaxWidth()
            .height(56.dp)
            .testTag("start_ritual_draw_button"),
      ) {
        Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(8.dp))
        Text(
          text = "选定「${state.selectedSpread.nameZh}」· 进入洗牌与抽卡界面",
          style = MaterialTheme.typography.titleMedium,
          color = Color.White,
        )
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DrawingAndAltarScreenStage(
  state: DivinationUiState,
  cardsById: Map<Int, TarotCardEntity>,
  deckStyle: com.example.data.preferences.TarotDeckStyle,
  isApiConfigured: Boolean,
  isGeneratingPoster: Boolean,
  onBackToSetup: () -> Unit,
  onReshuffle: () -> Unit,
  onDrawNextCard: () -> Unit,
  onInspectCard: (TarotCardEntity, DrawnCardSnapshot) -> Unit,
  onAiInterpret: () -> Unit,
  onGeneratePoster: () -> Unit,
  onDiscussWithMentor: (ReadingRecordEntity) -> Unit,
) {
  val tarotColors = LocalTarotColors.current
  val drawnCount = state.drawnSlots.count { it != null }
  val totalCount = state.selectedSpread.cardCount
  val allDrawn = drawnCount == totalCount && totalCount > 0
  val nextSlotIndex = state.drawnSlots.indexOfFirst { it == null }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(bottom = 28.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    // Top Navigation & Current Question Summary Bar
    item {
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, tarotColors.goldBorder.copy(alpha = 0.6f)),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            TextButton(
              onClick = onBackToSetup,
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
              modifier = Modifier.testTag("back_to_spread_setup_button"),
            ) {
              Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(Modifier.width(4.dp))
              Text("重选牌阵")
            }
            OutlinedButton(
              onClick = onReshuffle,
              shape = RoundedCornerShape(12.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
              modifier = Modifier.testTag("reset_divination_button"),
            ) {
              Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
              Spacer(Modifier.width(4.dp))
              Text("重新洗牌")
            }
          }
          Text(
            text = "牌阵：${state.selectedSpread.nameZh} (${state.selectedSpread.cardCount} 张) · 问卜：「${state.question}」",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    }

    // Sliding Curved Tarot Fan Deck (active while drawing cards one by one)
    if (!allDrawn && nextSlotIndex >= 0) {
      val nextPos = state.selectedSpread.positions[nextSlotIndex]
      val remainingDeckSize = (78 - drawnCount).coerceAtLeast(12)
      item {
        SlidingTarotDeckFan(
          currentStepNumber = drawnCount + 1,
          totalSteps = totalCount,
          currentPositionTitle = nextPos.title,
          currentPositionDescription = nextPos.description,
          remainingDeckCount = remainingDeckSize,
          onCardDrawn = onDrawNextCard,
        )
      }
    }

    // Spread Altar with MotionLayout 3D Flip Cards
    item {
      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "${state.selectedSpread.nameZh} · 圣阵牌桌 ($drawnCount/$totalCount)",
                style = MaterialTheme.typography.titleLarge,
              )
              Text(
                text =
                  if (allDrawn) "全部圣牌已揭示 · 可调用 AI 导师解析或保存携问题纪念长图"
                  else "在上方洗好的星轨牌扇中左右滑动，一张张抽出对应牌位",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }

          LinearProgressIndicator(
            progress = { if (totalCount > 0) drawnCount.toFloat() / totalCount else 0f },
            color = tarotColors.goldAccent,
            trackColor = tarotColors.goldSoft,
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
          )

          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(14.dp),
          ) {
            state.selectedSpread.positions.forEachIndexed { index, pos ->
              val drawn = state.drawnSlots.getOrNull(index)
              val entity = drawn?.let { cardsById[it.cardId] }
              val isRevealed = index in state.revealedIndices && entity != null
              val isNextSlot = index == nextSlotIndex

              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier =
                  Modifier.width(if (state.selectedSpread.cardCount <= 3) 138.dp else 110.dp),
              ) {
                TarotCard3D(
                  isRevealed = isRevealed,
                  card = entity,
                  isReversed = drawn?.isReversed ?: false,
                  deckStyle = deckStyle,
                  slotLabel =
                    if (isNextSlot) "待抽取 · ${pos.title}" else "${index + 1}. ${pos.title}",
                  compact = true,
                  testTagName = "spread_card_slot_$index",
                  modifier = Modifier.fillMaxWidth(),
                  onClick = {
                    if (!isRevealed) {
                      onDrawNextCard()
                    } else if (entity != null && drawn != null) {
                      onInspectCard(entity, drawn)
                    }
                  },
                )
                Spacer(Modifier.height(6.dp))
                Text(
                  text = "${index + 1}. ${pos.title}",
                  style = MaterialTheme.typography.labelLarge,
                  color =
                    if (isNextSlot) tarotColors.goldAccent
                    else MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }
            }
          }

          // Completed Spread Dual Actions: AI Mentor Reading + Save Poster Image with Question
          AnimatedVisibility(
            visible = allDrawn,
            enter = fadeIn() + slideInVertically { it / 3 },
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Button(
                onClick = onAiInterpret,
                enabled = !state.isInterpreting,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = tarotColors.goldAccent),
                modifier =
                  Modifier.weight(1f).height(50.dp).testTag("ai_interpret_spread_button"),
              ) {
                if (state.isInterpreting) {
                  CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = Color.White,
                  )
                } else {
                  Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(6.dp))
                Text(
                  if (state.isInterpreting) "导师解析中..."
                  else if (isApiConfigured) "AI 导师解析"
                  else "配置 API 解析"
                )
              }

              OutlinedButton(
                onClick = onGeneratePoster,
                enabled = !isGeneratingPoster,
                shape = RoundedCornerShape(16.dp),
                modifier =
                  Modifier.weight(1f).height(50.dp).testTag("save_spread_image_button"),
              ) {
                if (isGeneratingPoster) {
                  CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                  Icon(Icons.Rounded.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(6.dp))
                Text("保存携问题长图")
              }
            }
          }

          state.errorMessage?.let { err ->
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
            ) {
              Text(
                text = err,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth().padding(12.dp),
              )
            }
          }
        }
      }
    }

    // AI Interpretation Output Card
    val reading = state.currentReading
    if (reading != null && reading.aiInterpretation.isNotBlank()) {
      item {
        Card(
          shape = RoundedCornerShape(24.dp),
          colors =
            CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.42f)
            ),
          border = BorderStroke(1.dp, tarotColors.goldBorder),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = "✦ ${reading.mentorName.ifBlank { "圣光导师" }} · 牌阵全解",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
              )
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                  onClick = onGeneratePoster,
                  shape = RoundedCornerShape(14.dp),
                ) {
                  Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(Modifier.width(4.dp))
                  Text("存图")
                }
                OutlinedButton(
                  onClick = { onDiscussWithMentor(reading) },
                  shape = RoundedCornerShape(14.dp),
                  modifier = Modifier.testTag("discuss_spread_in_chat_button"),
                ) {
                  Icon(Icons.Rounded.Forum, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(Modifier.width(4.dp))
                  Text("追问导师")
                }
              }
            }
            Text(
              text = reading.aiInterpretation,
              style = MaterialTheme.typography.bodyLarge,
              color = MaterialTheme.colorScheme.onSurface,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun RitualStepHeader(
  currentStep: Int,
  drawnCount: Int,
  totalCount: Int,
) {
  val tarotColors = LocalTarotColors.current
  val steps =
    listOf(
      1 to "1. 选定牌阵",
      2 to "2. 洗牌圣仪",
      3 to "3. 抽牌 ($drawnCount/$totalCount)",
      4 to "4. 解析与存图",
    )
  Surface(
    shape = RoundedCornerShape(18.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, tarotColors.goldBorder.copy(alpha = 0.5f)),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceEvenly,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      steps.forEach { (stepNum, label) ->
        val isActive = currentStep == stepNum
        val isDone = currentStep > stepNum
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (isDone) {
            Icon(
              Icons.Rounded.CheckCircle,
              contentDescription = null,
              tint = tarotColors.uprightBadge,
              modifier = Modifier.size(14.dp),
            )
            Spacer(Modifier.width(3.dp))
          }
          Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color =
              when {
                isActive -> tarotColors.goldAccent
                isDone -> tarotColors.uprightBadge
                else -> MaterialTheme.colorScheme.onSurfaceVariant
              },
          )
        }
      }
    }
  }
}

@Composable
private fun SpreadPosterPreviewDialog(
  bitmap: Bitmap,
  question: String,
  onSaveToAlbum: () -> Unit,
  onShare: () -> Unit,
  onDismiss: () -> Unit,
) {
  val tarotColors = LocalTarotColors.current
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(26.dp),
      color = MaterialTheme.colorScheme.surface,
      border = BorderStroke(1.5.dp, tarotColors.goldBorder),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text("占卜圣卷长图预览（含问题与牌阵）", style = MaterialTheme.typography.titleLarge)
        Text(
          text = "问卜：「$question」",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )

        Box(
          modifier =
            Modifier.fillMaxWidth()
              .heightIn(max = 420.dp)
              .clip(RoundedCornerShape(16.dp))
              .verticalScroll(rememberScrollState())
        ) {
          Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "塔罗占卜长图预览",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth(),
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Button(
            onClick = onSaveToAlbum,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = tarotColors.goldAccent),
            modifier = Modifier.weight(1f).testTag("confirm_save_poster_button"),
          ) {
            Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("保存至相册")
          }
          OutlinedButton(
            onClick = onShare,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.weight(1f).testTag("share_poster_button"),
          ) {
            Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("分享长图")
          }
        }

        TextButton(onClick = onDismiss) { Text("关闭预览") }
      }
    }
  }
}
