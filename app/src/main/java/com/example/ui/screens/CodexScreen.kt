package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FlipCameraAndroid
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.RotateRight
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.TarotCardEntity
import com.example.data.model.WorldbookEntryEntity
import com.example.data.preferences.TarotDeckStyle
import com.example.ui.TarotAppViewModel
import com.example.ui.components.CardFrontFace
import com.example.ui.components.TarotCard3D
import com.example.ui.theme.CinzelFontFamily
import com.example.ui.theme.LocalTarotColors

@Composable
fun CodexScreen(
  viewModel: TarotAppViewModel,
  onOpenSettings: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var selectedTab by remember { mutableIntStateOf(0) }

  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    Column(modifier = Modifier.widthIn(max = 760.dp).fillMaxSize()) {
      SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)
      ) {
        SegmentedButton(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
          modifier = Modifier.testTag("codex_tab_gallery"),
        ) {
          Text("唯美卡面全集")
        }
        SegmentedButton(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
          modifier = Modifier.testTag("codex_tab_cards"),
        ) {
          Text("实时牌义库")
        }
        SegmentedButton(
          selected = selectedTab == 2,
          onClick = { selectedTab = 2 },
          shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
          modifier = Modifier.testTag("codex_tab_worldbook"),
        ) {
          Text("世界书资料")
        }
      }

      when (selectedTab) {
        0 -> DeckGalleryStudioPane(viewModel = viewModel, onOpenSettings = onOpenSettings)
        1 -> TarotMeaningsDatabasePane(viewModel = viewModel, onOpenSettings = onOpenSettings)
        else -> WorldbookLibraryPane(viewModel = viewModel)
      }
    }
  }
}

@Composable
private fun DeckGalleryStudioPane(
  viewModel: TarotAppViewModel,
  onOpenSettings: () -> Unit,
) {
  val tarotColors = LocalTarotColors.current
  val allCards by viewModel.allCards.collectAsStateWithLifecycle()
  val apiSettings by viewModel.apiSettings.collectAsStateWithLifecycle()
  val updatingIds by viewModel.updatingCardIds.collectAsStateWithLifecycle()

  var selectedSuit by remember { mutableStateOf("ALL") }
  var showcaseCard by remember { mutableStateOf<TarotCardEntity?>(null) }

  val suitFilters =
    listOf(
      "ALL" to "全套 78 张",
      "MAJOR" to "大阿卡那 (22)",
      "WANDS" to "权杖 · 火 (14)",
      "CUPS" to "圣杯 · 水 (14)",
      "SWORDS" to "宝剑 · 风 (14)",
      "PENTACLES" to "星币 · 土 (14)",
    )

  val filteredCards =
    remember(allCards, selectedSuit) {
      allCards.filter { selectedSuit == "ALL" || it.suit == selectedSuit }
    }

  LazyVerticalGrid(
    columns = GridCells.Adaptive(minSize = 112.dp),
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    // 1. Deck Theme Style Selector Header
    item(span = { GridItemSpan(maxLineSpan) }) {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            Icons.Rounded.Palette,
            contentDescription = null,
            tint = tarotColors.goldAccent,
            modifier = Modifier.size(20.dp),
          )
          Spacer(Modifier.width(8.dp))
          Column {
            Text(
              text = "四套典藏级唯美卡面主题 · 全局实时切换",
              style = MaterialTheme.typography.titleMedium,
            )
            Text(
              text = "当前应用卡面：${apiSettings.deckStyle.titleZh}（全套 78 张独立纹章绘卷）",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          items(TarotDeckStyle.entries, key = { it.name }) { style ->
            val isSelected = apiSettings.deckStyle == style
            Surface(
              shape = RoundedCornerShape(18.dp),
              color =
                if (isSelected) tarotColors.goldSoft
                else MaterialTheme.colorScheme.surface,
              border =
                BorderStroke(
                  width = if (isSelected) 1.8.dp else 1.dp,
                  color =
                    if (isSelected) tarotColors.goldAccent
                    else MaterialTheme.colorScheme.outlineVariant,
                ),
              modifier =
                Modifier.width(215.dp)
                  .testTag("deck_style_${style.name}")
                  .clickable { viewModel.setDeckStyle(style) },
            ) {
              Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                  ) {
                    Text(
                      text = style.badgeZh,
                      style = MaterialTheme.typography.labelSmall,
                      modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                    )
                  }
                  if (isSelected) {
                    Icon(
                      Icons.Rounded.CheckCircle,
                      contentDescription = "已启用",
                      tint = tarotColors.goldAccent,
                      modifier = Modifier.size(16.dp),
                    )
                  }
                }
                Text(
                  text = style.titleZh,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.SemiBold,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                Text(
                  text = style.subtitleZh,
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis,
                )
              }
            }
          }
        }

        // Suit Filter Row
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          items(suitFilters, key = { it.first }) { (key, label) ->
            FilterChip(
              selected = selectedSuit == key,
              onClick = { selectedSuit = key },
              label = { Text(label) },
            )
          }
        }
      }
    }

    // 2. Full 78-Card Illustrated Tarot Deck Grid
    items(filteredCards, key = { it.id }) { card ->
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
          Modifier.fillMaxWidth()
            .testTag("gallery_card_${card.id}")
            .clickable { showcaseCard = card },
      ) {
        Box(
          modifier =
            Modifier.fillMaxWidth()
              .aspectRatio(0.66f)
              .clip(RoundedCornerShape(16.dp))
        ) {
          CardFrontFace(
            card = card,
            isReversed = false,
            deckStyle = apiSettings.deckStyle,
            compact = true,
          )
        }
        Spacer(Modifier.height(4.dp))
        Text(
          text = "${card.romanNumeral} · ${card.nameZh}",
          style = MaterialTheme.typography.labelLarge,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }

  showcaseCard?.let { card ->
    val isUpdating = card.id in updatingIds
    CardArtShowcaseDialog(
      card = card,
      deckStyle = apiSettings.deckStyle,
      isUpdatingAi = isUpdating,
      onSelectStyle = { viewModel.setDeckStyle(it) },
      onAiRefineCard = {
        if (!apiSettings.isConfigured) {
          showcaseCard = null
          onOpenSettings()
        } else {
          viewModel.aiUpdateSingleCardMeaning(card)
        }
      },
      onDismiss = { showcaseCard = null },
    )
  }
}

@Composable
private fun CardArtShowcaseDialog(
  card: TarotCardEntity,
  deckStyle: TarotDeckStyle,
  isUpdatingAi: Boolean,
  onSelectStyle: (TarotDeckStyle) -> Unit,
  onAiRefineCard: () -> Unit,
  onDismiss: () -> Unit,
) {
  val tarotColors = LocalTarotColors.current
  var isRevealed by remember { mutableStateOf(true) }
  var isReversed by remember { mutableStateOf(false) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(28.dp),
      color = MaterialTheme.colorScheme.surface,
      border = BorderStroke(1.5.dp, tarotColors.goldBorder),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(
        modifier =
          Modifier.fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text(
          text = "${card.romanNumeral} · ${card.nameZh} (${card.nameEn})",
          style = MaterialTheme.typography.titleLarge,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
          text = "${deckStyle.titleZh} · 轻触卡面体验 3D 仪式翻转",
          style = MaterialTheme.typography.labelLarge,
          color = tarotColors.goldAccent,
        )

        // Large 3D Interactive Card Showcase
        TarotCard3D(
          isRevealed = isRevealed,
          card = card,
          isReversed = isReversed,
          deckStyle = deckStyle,
          slotLabel = "轻触翻转至正面",
          compact = false,
          modifier = Modifier.width(230.dp),
          onClick = { isRevealed = !isRevealed },
        )

        // Interactive 3D Controls (Flip Front/Back & Toggle Upright/Reversed)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          OutlinedButton(
            onClick = { isRevealed = !isRevealed },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.weight(1f),
          ) {
            Icon(Icons.Rounded.FlipCameraAndroid, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(if (isRevealed) "查看卡背" else "翻开卡面")
          }
          OutlinedButton(
            onClick = {
              isRevealed = true
              isReversed = !isReversed
            },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.weight(1f),
          ) {
            Icon(Icons.Rounded.RotateRight, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text(if (isReversed) "切换正位" else "切换逆位")
          }
        }

        // Quick Deck Theme Switcher inside Showcase
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          items(TarotDeckStyle.entries, key = { it.name }) { style ->
            FilterChip(
              selected = deckStyle == style,
              onClick = { onSelectStyle(style) },
              label = { Text(style.badgeZh) },
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(16.dp),
          color = tarotColors.goldSoft.copy(alpha = 0.6f),
          border = BorderStroke(1.dp, tarotColors.goldBorder.copy(alpha = 0.6f)),
        ) {
          Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Text(
              text =
                if (isReversed) "逆位核心：${card.keywordsReversed}"
                else "正位核心：${card.keywordsUpright}",
              style = MaterialTheme.typography.labelLarge,
              color = tarotColors.goldAccent,
            )
            Spacer(Modifier.height(4.dp))
            Text(
              text = if (isReversed) card.meaningReversed else card.meaningUpright,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface,
            )
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Button(
            onClick = onAiRefineCard,
            enabled = !isUpdatingAi,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = tarotColors.goldAccent),
            modifier = Modifier.weight(1f),
          ) {
            if (isUpdatingAi) {
              CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = Color.White,
              )
            } else {
              Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.width(6.dp))
            Text("AI 升华此牌释义")
          }
          TextButton(onClick = onDismiss) { Text("关闭") }
        }
      }
    }
  }
}

@Composable
private fun TarotMeaningsDatabasePane(
  viewModel: TarotAppViewModel,
  onOpenSettings: () -> Unit,
) {
  val tarotColors = LocalTarotColors.current
  val allCards by viewModel.allCards.collectAsStateWithLifecycle()
  val updatingIds by viewModel.updatingCardIds.collectAsStateWithLifecycle()
  val batchProgress by viewModel.batchUpdateProgress.collectAsStateWithLifecycle()
  val apiSettings by viewModel.apiSettings.collectAsStateWithLifecycle()

  var searchQuery by remember { mutableStateOf("") }
  var selectedSuit by remember { mutableStateOf("ALL") }
  var editingCard by remember { mutableStateOf<TarotCardEntity?>(null) }

  val suitFilters =
    listOf(
      "ALL" to "全部 78 张",
      "MAJOR" to "大阿卡那",
      "WANDS" to "权杖",
      "CUPS" to "圣杯",
      "SWORDS" to "宝剑",
      "PENTACLES" to "星币",
    )

  val filteredCards =
    remember(allCards, searchQuery, selectedSuit) {
      allCards.filter { card ->
        val suitMatch = selectedSuit == "ALL" || card.suit == selectedSuit
        val q = searchQuery.trim()
        val textMatch =
          q.isEmpty() ||
            card.nameZh.contains(q, ignoreCase = true) ||
            card.nameEn.contains(q, ignoreCase = true) ||
            card.keywordsUpright.contains(q, ignoreCase = true) ||
            card.meaningUpright.contains(q, ignoreCase = true)
        suitMatch && textMatch
      }
    }

  val updatedCount = remember(allCards) { allCards.count { it.isCustomUpdated } }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    // Live Sync Banner
    item {
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = tarotColors.goldSoft.copy(alpha = 0.65f),
        border = BorderStroke(1.dp, tarotColors.goldBorder),
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
            Column(modifier = Modifier.weight(1f)) {
              Text("实时牌义数据库 · 已校准 $updatedCount/${allCards.size} 张", style = MaterialTheme.typography.titleMedium)
              Text(
                "所有更新会实时写入本地圣典并即刻用于抽牌与 AI 解读",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            Button(
              onClick = {
                if (!apiSettings.isConfigured) onOpenSettings()
                else viewModel.aiBatchUpdateCards(filteredCards)
              },
              enabled = batchProgress == null && filteredCards.isNotEmpty(),
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(containerColor = tarotColors.goldAccent),
              modifier = Modifier.testTag("batch_ai_update_cards_button"),
            ) {
              Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(Modifier.width(4.dp))
              Text("AI 批量校准")
            }
          }
          batchProgress?.let { msg ->
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Text(msg, style = MaterialTheme.typography.labelLarge, color = tarotColors.goldAccent)
          }
        }
      }
    }

    item {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
        placeholder = { Text("搜索牌名、关键词或牌义内容...") },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().testTag("codex_search_input"),
      )
    }

    item {
      LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(suitFilters, key = { it.first }) { (key, label) ->
          FilterChip(
            selected = selectedSuit == key,
            onClick = { selectedSuit = key },
            label = { Text(label) },
          )
        }
      }
    }

    items(filteredCards, key = { it.id }) { card ->
      val isUpdating = card.id in updatingIds
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border =
          BorderStroke(
            1.dp,
            if (card.isCustomUpdated) tarotColors.goldAccent.copy(alpha = 0.65f)
            else MaterialTheme.colorScheme.outlineVariant,
          ),
        modifier = Modifier.fillMaxWidth().clickable { editingCard = card },
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          // Miniature illustrated card thumbnail
          Box(
            modifier =
              Modifier.width(64.dp)
                .aspectRatio(0.66f)
                .clip(RoundedCornerShape(10.dp))
          ) {
            CardFrontFace(
              card = card,
              isReversed = false,
              deckStyle = apiSettings.deckStyle,
              compact = true,
            )
          }

          Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = card.romanNumeral,
                  fontFamily = CinzelFontFamily,
                  fontWeight = FontWeight.SemiBold,
                  color = tarotColors.goldAccent,
                )
                Spacer(Modifier.width(6.dp))
                Text("${card.nameZh} · ${card.nameEn}", style = MaterialTheme.typography.titleMedium)
              }
              if (card.isCustomUpdated) {
                Surface(shape = RoundedCornerShape(10.dp), color = tarotColors.goldSoft) {
                  Text(
                    "已校准",
                    style = MaterialTheme.typography.labelSmall,
                    color = tarotColors.goldAccent,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                  )
                }
              }
            }

            Text(
              text = "正位：${card.keywordsUpright}",
              style = MaterialTheme.typography.labelLarge,
              color = tarotColors.uprightBadge,
            )
            Text(
              text = card.meaningUpright,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.End,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              TextButton(onClick = { editingCard = card }) {
                Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("编辑")
              }
              OutlinedButton(
                onClick = {
                  if (!apiSettings.isConfigured) onOpenSettings()
                  else viewModel.aiUpdateSingleCardMeaning(card)
                },
                enabled = !isUpdating,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("ai_update_card_${card.id}"),
              ) {
                if (isUpdating) {
                  CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                } else {
                  Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(15.dp))
                }
                Spacer(Modifier.width(4.dp))
                Text("AI 实时更新")
              }
            }
          }
        }
      }
    }
  }

  editingCard?.let { card ->
    CardEditorDialog(
      card = card,
      onDismiss = { editingCard = null },
      onSave = { updated ->
        viewModel.saveCardEdits(updated)
        editingCard = null
      },
      onReset = {
        viewModel.resetCardDefault(card.id)
        editingCard = null
      },
    )
  }
}

@Composable
private fun WorldbookLibraryPane(viewModel: TarotAppViewModel) {
  val tarotColors = LocalTarotColors.current
  val entries by viewModel.allWorldbookEntries.collectAsStateWithLifecycle()
  var editingEntry by remember { mutableStateOf<WorldbookEntryEntity?>(null) }

  val filePickerLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
      if (uri != null) {
        viewModel.importWorldbookFile(uri)
      }
    }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Box(
        modifier = Modifier.fillMaxWidth().height(136.dp).clip(RoundedCornerShape(22.dp))
      ) {
        Image(
          painter = painterResource(id = R.drawable.img_worldbook_codex),
          contentDescription = "塔罗世界书资料库",
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize(),
        )
        Box(
          modifier =
            Modifier.fillMaxSize()
              .background(Brush.verticalGradient(listOf(Color(0x2218142C), Color(0xBB18132B))))
        )
        Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
          Text(
            "塔罗世界书与密卷资料库",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
          )
          Text(
            "上传 .txt / .md / .json 资料，AI 占卜与导师对话将实时检索注入",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFF0E8D8),
          )
        }
      }
    }

    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Button(
          onClick = {
            filePickerLauncher.launch(arrayOf("text/*", "application/json", "*/*"))
          },
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = tarotColors.goldAccent),
          modifier = Modifier.weight(1f).testTag("upload_worldbook_button"),
        ) {
          Icon(Icons.Rounded.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(Modifier.width(6.dp))
          Text("上传塔罗资料")
        }
        OutlinedButton(
          onClick = {
            editingEntry =
              WorldbookEntryEntity(
                title = "",
                keywords = "",
                content = "",
                sourceName = "手动录入",
                isAlwaysActive = false,
                isEnabled = true,
              )
          },
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.testTag("create_worldbook_button"),
        ) {
          Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(Modifier.width(4.dp))
          Text("新建词条")
        }
      }
    }

    items(entries, key = { it.id }) { entry ->
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border =
          BorderStroke(
            1.dp,
            if (entry.isEnabled) tarotColors.goldBorder.copy(alpha = 0.7f)
            else MaterialTheme.colorScheme.outlineVariant,
          ),
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(entry.title, style = MaterialTheme.typography.titleMedium)
              Text(
                "来源：${entry.sourceName} · 触发词：${entry.keywords.ifBlank { "全局匹配" }}",
                style = MaterialTheme.typography.labelSmall,
                color = tarotColors.goldAccent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
            Switch(
              checked = entry.isEnabled,
              onCheckedChange = { viewModel.toggleWorldbookEnabled(entry, it) },
            )
          }

          Text(
            text = entry.content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            FilterChip(
              selected = entry.isAlwaysActive,
              onClick = { viewModel.toggleWorldbookAlwaysActive(entry, !entry.isAlwaysActive) },
              label = { Text(if (entry.isAlwaysActive) "常驻注入" else "关键词触发") },
            )
            Row {
              IconButton(onClick = { editingEntry = entry }) {
                Icon(Icons.Rounded.Edit, contentDescription = "编辑词条")
              }
              IconButton(onClick = { viewModel.deleteWorldbookEntry(entry.id) }) {
                Icon(Icons.Rounded.DeleteOutline, contentDescription = "删除词条")
              }
            }
          }
        }
      }
    }
  }

  editingEntry?.let { entry ->
    WorldbookEditorDialog(
      initial = entry,
      onDismiss = { editingEntry = null },
      onSave = { updated ->
        viewModel.saveWorldbookEntry(updated)
        editingEntry = null
      },
    )
  }
}

@Composable
private fun CardEditorDialog(
  card: TarotCardEntity,
  onDismiss: () -> Unit,
  onSave: (TarotCardEntity) -> Unit,
  onReset: () -> Unit,
) {
  var kwUp by remember { mutableStateOf(card.keywordsUpright) }
  var kwRev by remember { mutableStateOf(card.keywordsReversed) }
  var meanUp by remember { mutableStateOf(card.meaningUpright) }
  var meanRev by remember { mutableStateOf(card.meaningReversed) }
  var love by remember { mutableStateOf(card.loveAdvice) }
  var career by remember { mutableStateOf(card.careerAdvice) }
  var notes by remember { mutableStateOf(card.customNotes) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("编辑牌义 · ${card.nameZh}") },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        OutlinedTextField(value = kwUp, onValueChange = { kwUp = it }, label = { Text("正位关键词") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = meanUp, onValueChange = { meanUp = it }, label = { Text("正位释义") }, minLines = 2, maxLines = 4, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = kwRev, onValueChange = { kwRev = it }, label = { Text("逆位关键词") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = meanRev, onValueChange = { meanRev = it }, label = { Text("逆位释义") }, minLines = 2, maxLines = 4, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = love, onValueChange = { love = it }, label = { Text("情感建议") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = career, onValueChange = { career = it }, label = { Text("事业建议") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("个人经验 / 占卜校准备注") }, minLines = 2, modifier = Modifier.fillMaxWidth())
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSave(
            card.copy(
              keywordsUpright = kwUp.trim(),
              keywordsReversed = kwRev.trim(),
              meaningUpright = meanUp.trim(),
              meaningReversed = meanRev.trim(),
              loveAdvice = love.trim(),
              careerAdvice = career.trim(),
              customNotes = notes.trim(),
            )
          )
        }
      ) {
        Text("保存更新")
      }
    },
    dismissButton = {
      Row {
        TextButton(onClick = onReset) {
          Icon(Icons.Rounded.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(Modifier.width(4.dp))
          Text("恢复默认")
        }
        TextButton(onClick = onDismiss) { Text("取消") }
      }
    },
  )
}

@Composable
private fun WorldbookEditorDialog(
  initial: WorldbookEntryEntity,
  onDismiss: () -> Unit,
  onSave: (WorldbookEntryEntity) -> Unit,
) {
  var title by remember { mutableStateOf(initial.title) }
  var keywords by remember { mutableStateOf(initial.keywords) }
  var content by remember { mutableStateOf(initial.content) }
  var alwaysActive by remember { mutableStateOf(initial.isAlwaysActive) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(if (initial.id == 0L) "新建世界书词条" else "编辑世界书词条") },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("词条标题") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = keywords, onValueChange = { keywords = it }, label = { Text("触发关键词（逗号分隔）") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("塔罗资料 / 设定正文") }, minLines = 4, maxLines = 8, modifier = Modifier.fillMaxWidth())
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text("常驻注入所有对话与占卜", style = MaterialTheme.typography.bodyMedium)
          Switch(checked = alwaysActive, onCheckedChange = { alwaysActive = it })
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isNotBlank() && content.isNotBlank()) {
            onSave(
              initial.copy(
                title = title.trim(),
                keywords = keywords.trim(),
                content = content.trim(),
                isAlwaysActive = alwaysActive,
              )
            )
          }
        },
        enabled = title.isNotBlank() && content.isNotBlank(),
      ) {
        Text("保存词条")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("取消") }
    },
  )
}
