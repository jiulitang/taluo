package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Api
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.NetworkCheck
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.TarotAppViewModel
import com.example.ui.theme.LocalTarotColors
import com.example.ui.theme.SanctuaryThemeMode
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
  viewModel: TarotAppViewModel,
  modifier: Modifier = Modifier,
) {
  val tarotColors = LocalTarotColors.current
  val uiState by viewModel.settingsUiState.collectAsStateWithLifecycle()
  val savedSettings by viewModel.apiSettings.collectAsStateWithLifecycle()
  val allCards by viewModel.allCards.collectAsStateWithLifecycle()
  val activePersona by viewModel.activePersona.collectAsStateWithLifecycle()
  val worldbook by viewModel.allWorldbookEntries.collectAsStateWithLifecycle()

  var showApiKey by remember { mutableStateOf(false) }

  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    LazyColumn(
      modifier = Modifier.widthIn(max = 680.dp).fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // 1. OpenAI Format API Configuration Card
      item {
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, tarotColors.goldBorder.copy(alpha = 0.6f)),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Rounded.Api, contentDescription = null, tint = tarotColors.goldAccent)
              Spacer(Modifier.width(8.dp))
              Column {
                Text("OpenAI 格式接口设置", style = MaterialTheme.typography.titleLarge)
                Text(
                  "配置 Base URL、API Key 与 Model Name，点击保存即刻生效",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }

            // Quick Base URL presets
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              listOf(
                  "OpenAI 官方" to "https://api.openai.com/v1",
                  "DeepSeek" to "https://api.deepseek.com/v1",
                  "SiliconFlow" to "https://api.siliconflow.cn/v1",
                  "本地 Ollama" to "http://127.0.0.1:11434/v1",
                )
                .forEach { (label, url) ->
                  AssistChip(
                    onClick = { viewModel.updateBaseUrlInput(url) },
                    label = { Text(label) },
                  )
                }
            }

            OutlinedTextField(
              value = uiState.baseUrlInput,
              onValueChange = viewModel::updateBaseUrlInput,
              label = { Text("Base URL") },
              placeholder = { Text("https://api.openai.com/v1") },
              singleLine = true,
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier.fillMaxWidth().testTag("settings_base_url_input"),
            )

            OutlinedTextField(
              value = uiState.apiKeyInput,
              onValueChange = viewModel::updateApiKeyInput,
              label = { Text("API Key") },
              placeholder = { Text("sk-...") },
              singleLine = true,
              visualTransformation =
                if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
              trailingIcon = {
                IconButton(onClick = { showApiKey = !showApiKey }) {
                  Icon(
                    imageVector =
                      if (showApiKey) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                    contentDescription = if (showApiKey) "隐藏密钥" else "显示密钥",
                  )
                }
              },
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier.fillMaxWidth().testTag("settings_api_key_input"),
            )

            OutlinedTextField(
              value = uiState.modelNameInput,
              onValueChange = viewModel::updateModelNameInput,
              label = { Text("Model Name") },
              placeholder = { Text("例如 gpt-4o-mini / deepseek-chat") },
              singleLine = true,
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier.fillMaxWidth().testTag("settings_model_name_input"),
            )

            // Fetched Models Picker Chips
            if (savedSettings.fetchedModels.isNotEmpty()) {
              Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                  text = "已拉取模型列表 (${savedSettings.fetchedModels.size}) · 点击填入：",
                  style = MaterialTheme.typography.labelLarge,
                  color = tarotColors.goldAccent,
                )
                FlowRow(
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                  savedSettings.fetchedModels.take(24).forEach { modelId ->
                    FilterChip(
                      selected = uiState.modelNameInput == modelId,
                      onClick = { viewModel.updateModelNameInput(modelId) },
                      label = { Text(modelId) },
                      leadingIcon =
                        if (uiState.modelNameInput == modelId) {
                          { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null,
                    )
                  }
                }
              }
            }

            // Temperature Slider
            Column {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Text("灵性创造力 Temperature", style = MaterialTheme.typography.labelLarge)
                Text(
                  text = String.format(Locale.US, "%.2f", uiState.temperatureInput),
                  style = MaterialTheme.typography.labelLarge,
                  color = tarotColors.goldAccent,
                )
              }
              Slider(
                value = uiState.temperatureInput,
                onValueChange = viewModel::updateTemperatureInput,
                valueRange = 0f..1.4f,
              )
            }

            // Fetch Models + Test Connection Row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              OutlinedButton(
                onClick = viewModel::fetchModelsFromApi,
                enabled = !uiState.isFetchingModels && !uiState.isTestingConnection,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f).testTag("fetch_models_button"),
              ) {
                if (uiState.isFetchingModels) {
                  CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                  Icon(Icons.Rounded.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(6.dp))
                Text("拉取模型")
              }

              OutlinedButton(
                onClick = viewModel::testApiConnection,
                enabled = !uiState.isTestingConnection && !uiState.isFetchingModels,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f).testTag("test_connection_button"),
              ) {
                if (uiState.isTestingConnection) {
                  CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                  Icon(Icons.Rounded.NetworkCheck, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(6.dp))
                Text("测试连接")
              }
            }

            // Save & Apply Button
            Button(
              onClick = viewModel::saveApiSettings,
              shape = RoundedCornerShape(16.dp),
              colors = ButtonDefaults.buttonColors(containerColor = tarotColors.goldAccent),
              modifier = Modifier.fillMaxWidth().testTag("save_api_settings_button"),
            ) {
              Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(Modifier.width(8.dp))
              Text("保存生效")
            }

            // Status / Connection Banner
            AnimatedVisibility(visible = !uiState.statusBanner.isNullOrBlank()) {
              Surface(
                shape = RoundedCornerShape(14.dp),
                color =
                  if (uiState.isStatusError) MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                  else tarotColors.uprightBadge.copy(alpha = 0.14f),
                border =
                  BorderStroke(
                    1.dp,
                    if (uiState.isStatusError) MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                    else tarotColors.uprightBadge.copy(alpha = 0.4f),
                  ),
              ) {
                Text(
                  text = uiState.statusBanner.orEmpty(),
                  style = MaterialTheme.typography.bodyMedium,
                  color =
                    if (uiState.isStatusError) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.fillMaxWidth().padding(12.dp),
                )
              }
            }
          }
        }
      }

      // 2. Sanctuary Theme Appearance
      item {
        Card(
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Rounded.Palette, contentDescription = null, tint = tarotColors.goldAccent)
              Spacer(Modifier.width(8.dp))
              Text("圣殿光影外观", style = MaterialTheme.typography.titleMedium)
            }
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
              val modes =
                listOf(
                  SanctuaryThemeMode.LIGHT to "圣洁昼光",
                  SanctuaryThemeMode.DARK to "星夜圣殿",
                  SanctuaryThemeMode.SYSTEM to "跟随系统",
                )
              modes.forEachIndexed { idx, (mode, label) ->
                SegmentedButton(
                  selected = savedSettings.themeMode == mode,
                  onClick = { viewModel.setThemeMode(mode) },
                  shape = SegmentedButtonDefaults.itemShape(index = idx, count = modes.size),
                ) {
                  Text(label)
                }
              }
            }
          }
        }
      }

      // 3. Active Sanctuary Data Summary
      item {
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Text("当前圣殿运行状态", style = MaterialTheme.typography.titleMedium)
            Text(
              "• 当前生效模型：${savedSettings.modelName.ifBlank { "未设置" }} (${if (savedSettings.isConfigured) "已配置" else "待配置"})",
              style = MaterialTheme.typography.bodyMedium,
            )
            Text(
              "• 牌义数据库：${allCards.size} 张完整塔罗（已校准更新 ${allCards.count { it.isCustomUpdated }} 张）",
              style = MaterialTheme.typography.bodyMedium,
            )
            Text(
              "• 当前塔罗导师：${activePersona?.name ?: "未选择"}",
              style = MaterialTheme.typography.bodyMedium,
            )
            Text(
              "• 世界书资料库：${worldbook.count { it.isEnabled }}/${worldbook.size} 条已激活",
              style = MaterialTheme.typography.bodyMedium,
            )
          }
        }
      }
    }
  }
}
