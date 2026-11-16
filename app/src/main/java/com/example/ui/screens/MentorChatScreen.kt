package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.MentorPersonaEntity
import com.example.ui.TarotAppViewModel
import com.example.ui.theme.LocalTarotColors

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MentorChatScreen(
  viewModel: TarotAppViewModel,
  onOpenSettings: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val tarotColors = LocalTarotColors.current
  val activePersona by viewModel.activePersona.collectAsStateWithLifecycle()
  val allPersonas by viewModel.allPersonas.collectAsStateWithLifecycle()
  val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
  val isReplying by viewModel.isMentorReplying.collectAsStateWithLifecycle()
  val attachedReading by viewModel.attachedReadingForChat.collectAsStateWithLifecycle()
  val allReadings by viewModel.allReadings.collectAsStateWithLifecycle()
  val worldbookEntries by viewModel.allWorldbookEntries.collectAsStateWithLifecycle()
  val apiSettings by viewModel.apiSettings.collectAsStateWithLifecycle()

  var inputText by remember { mutableStateOf("") }
  var showPersonaSheet by remember { mutableStateOf(false) }
  var editingPersona by remember { mutableStateOf<MentorPersonaEntity?>(null) }

  val listState = rememberLazyListState()
  val enabledWorldbookCount = remember(worldbookEntries) { worldbookEntries.count { it.isEnabled } }

  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    Column(modifier = Modifier.widthIn(max = 720.dp).fillMaxSize().imePadding()) {
      // Mentor Header Card
      Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, tarotColors.goldBorder.copy(alpha = 0.6f)),
        tonalElevation = 2.dp,
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Image(
            painter = painterResource(id = R.drawable.img_mentor_seraphina),
            contentDescription = activePersona?.name ?: "塔罗导师",
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(54.dp).clip(CircleShape),
          )
          Spacer(Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = activePersona?.name ?: "圣光大祭司",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Text(
              text = activePersona?.title ?: "温柔疗愈系导师",
              style = MaterialTheme.typography.bodyMedium,
              color = tarotColors.goldAccent,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.Rounded.MenuBook,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(13.dp),
              )
              Spacer(Modifier.width(4.dp))
              Text(
                text = "世界书激活 $enabledWorldbookCount 条",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
          OutlinedButton(
            onClick = { showPersonaSheet = true },
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier.testTag("manage_personas_button"),
          ) {
            Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("人设卡")
          }
          IconButton(
            onClick = viewModel::clearActivePersonaChat,
            modifier = Modifier.testTag("clear_chat_button"),
          ) {
            Icon(
              Icons.Rounded.DeleteOutline,
              contentDescription = "清空对话",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }

      // Chat Messages List
      LazyColumn(
        state = listState,
        modifier = Modifier.weight(1f).fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        if (messages.isEmpty()) {
          item {
            Surface(
              shape = RoundedCornerShape(22.dp),
              color = tarotColors.goldSoft.copy(alpha = 0.55f),
              border = BorderStroke(1.dp, tarotColors.goldBorder.copy(alpha = 0.5f)),
            ) {
              Column(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                Text(
                  text = activePersona?.greeting ?: "愿圣光与宁静常伴你左右，请告诉我你心中的疑问。",
                  style = MaterialTheme.typography.bodyLarge,
                  color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                  text = "快捷问候：",
                  style = MaterialTheme.typography.labelLarge,
                  color = tarotColors.goldAccent,
                )
                FlowRow(
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                  listOf(
                      "请结合我最近的抽牌给出深度指引",
                      "我现在感到有些焦虑，需要温柔开导",
                      "如何理解逆位牌给我的成长提醒？",
                    )
                    .forEach { prompt ->
                      AssistChip(
                        onClick = {
                          if (!apiSettings.isConfigured) onOpenSettings()
                          else viewModel.sendChatMessage(prompt)
                        },
                        label = { Text(prompt, style = MaterialTheme.typography.bodyMedium) },
                      )
                    }
                }
              }
            }
          }
        }

        items(messages, key = { it.id }) { msg ->
          val isUser = msg.role == "user"
          Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
          ) {
            if (msg.attachedReadingSummary.isNotBlank() && isUser) {
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = tarotColors.goldSoft,
                modifier = Modifier.padding(bottom = 4.dp).widthIn(max = 290.dp),
              ) {
                Text(
                  text = msg.attachedReadingSummary,
                  style = MaterialTheme.typography.labelSmall,
                  color = tarotColors.goldAccent,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                )
              }
            }
            Surface(
              shape =
                RoundedCornerShape(
                  topStart = 20.dp,
                  topEnd = 20.dp,
                  bottomStart = if (isUser) 20.dp else 6.dp,
                  bottomEnd = if (isUser) 6.dp else 20.dp,
                ),
              color =
                if (isUser) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.surface,
              border =
                BorderStroke(
                  1.dp,
                  if (isUser) MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)
                  else tarotColors.goldBorder.copy(alpha = 0.5f),
                ),
              modifier = Modifier.widthIn(max = 320.dp),
            ) {
              Text(
                text = msg.content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
              )
            }
          }
        }

        if (isReplying) {
          item {
            Surface(
              shape = RoundedCornerShape(18.dp),
              color = MaterialTheme.colorScheme.surface,
              border = BorderStroke(1.dp, tarotColors.goldBorder.copy(alpha = 0.5f)),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                CircularProgressIndicator(
                  modifier = Modifier.size(16.dp),
                  strokeWidth = 2.dp,
                  color = tarotColors.goldAccent,
                )
                Spacer(Modifier.width(10.dp))
                Text(
                  text = "${activePersona?.name?.substringBefore("·")?.trim() ?: "导师"}正在倾听星辰回应...",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }
        }
      }

      // Attached Reading Context Bar + Quick Attach Chip
      Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        if (attachedReading != null) {
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = tarotColors.goldSoft,
            border = BorderStroke(1.dp, tarotColors.goldBorder),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(Icons.Rounded.Style, contentDescription = null, tint = tarotColors.goldAccent, modifier = Modifier.size(16.dp))
              Spacer(Modifier.width(8.dp))
              Text(
                text = "已附带牌面：${attachedReading?.spreadName} · ${attachedReading?.question}",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
              )
              IconButton(
                onClick = { viewModel.attachReadingToChat(null) },
                modifier = Modifier.size(28.dp),
              ) {
                Icon(Icons.Rounded.Close, contentDescription = "移除附加牌面", modifier = Modifier.size(16.dp))
              }
            }
          }
        } else if (allReadings.isNotEmpty()) {
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(
              onClick = { viewModel.attachReadingToChat(allReadings.first()) },
              leadingIcon = {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
              },
              label = { Text("附加最近一次占卜 (${allReadings.first().spreadName})") },
            )
          }
        }

        // Message Input Row
        Row(
          modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            placeholder = {
              Text(
                if (apiSettings.isConfigured) "向导师倾诉困惑或追问牌义..."
                else "请先在「设置」中配置 OpenAI API 参数"
              )
            },
            maxLines = 4,
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier.weight(1f).testTag("mentor_chat_input"),
          )
          Button(
            onClick = {
              if (!apiSettings.isConfigured) {
                onOpenSettings()
              } else if (inputText.isNotBlank() && !isReplying) {
                val msg = inputText
                inputText = ""
                viewModel.sendChatMessage(msg)
              }
            },
            enabled = !isReplying && (inputText.isNotBlank() || !apiSettings.isConfigured),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = tarotColors.goldAccent),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.size(50.dp).testTag("send_chat_button"),
          ) {
            Icon(
              Icons.AutoMirrored.Rounded.Send,
              contentDescription = "发送消息",
              tint = Color.White,
            )
          }
        }
      }
    }
  }

  if (showPersonaSheet) {
    ModalBottomSheet(
      onDismissRequest = { showPersonaSheet = false },
      containerColor = MaterialTheme.colorScheme.surface,
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text("塔罗导师人设卡工坊", style = MaterialTheme.typography.titleLarge)
          Button(
            onClick = {
              editingPersona =
                MentorPersonaEntity(
                  name = "",
                  title = "",
                  personality = "",
                  readingStyle = "",
                  systemPrompt = "",
                  greeting = "愿星光照亮你的疑问，我是你的专属塔罗导师。",
                  isActive = true,
                )
            },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = tarotColors.goldAccent),
            modifier = Modifier.testTag("create_persona_button"),
          ) {
            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("新建人设卡")
          }
        }

        LazyColumn(
          modifier = Modifier.fillMaxWidth().height(360.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          items(allPersonas, key = { it.id }) { persona ->
            Surface(
              shape = RoundedCornerShape(18.dp),
              color =
                if (persona.isActive) tarotColors.goldSoft.copy(alpha = 0.7f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
              border =
                BorderStroke(
                  width = if (persona.isActive) 1.5.dp else 1.dp,
                  color =
                    if (persona.isActive) tarotColors.goldAccent
                    else MaterialTheme.colorScheme.outlineVariant,
                ),
              modifier = Modifier.fillMaxWidth().clickable { viewModel.selectPersona(persona.id) },
            ) {
              Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(persona.name, style = MaterialTheme.typography.titleMedium)
                    if (persona.isActive) {
                      Spacer(Modifier.width(8.dp))
                      Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = "当前启用",
                        tint = tarotColors.goldAccent,
                        modifier = Modifier.size(16.dp),
                      )
                    }
                  }
                  Text(persona.title, style = MaterialTheme.typography.labelLarge, color = tarotColors.goldAccent)
                  Text(
                    persona.personality,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                  )
                }
                IconButton(onClick = { editingPersona = persona }) {
                  Icon(Icons.Rounded.Edit, contentDescription = "编辑人设")
                }
                if (!persona.isDefault) {
                  IconButton(onClick = { viewModel.deletePersona(persona) }) {
                    Icon(Icons.Rounded.DeleteOutline, contentDescription = "删除人设")
                  }
                }
              }
            }
          }
        }
        Spacer(Modifier.height(16.dp))
      }
    }
  }

  editingPersona?.let { target ->
    PersonaEditorDialog(
      initial = target,
      onDismiss = { editingPersona = null },
      onSave = { updated ->
        viewModel.savePersona(updated)
        editingPersona = null
      },
    )
  }
}

@Composable
private fun PersonaEditorDialog(
  initial: MentorPersonaEntity,
  onDismiss: () -> Unit,
  onSave: (MentorPersonaEntity) -> Unit,
) {
  var name by remember { mutableStateOf(initial.name) }
  var title by remember { mutableStateOf(initial.title) }
  var personality by remember { mutableStateOf(initial.personality) }
  var style by remember { mutableStateOf(initial.readingStyle) }
  var prompt by remember { mutableStateOf(initial.systemPrompt) }
  var greeting by remember { mutableStateOf(initial.greeting) }
  var setActive by remember { mutableStateOf(initial.isActive) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(if (initial.id == 0L) "新建塔罗导师人设卡" else "编辑人设卡") },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("导师名称") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("头衔 / 专长领域") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = personality, onValueChange = { personality = it }, label = { Text("性格特征") }, maxLines = 3, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = style, onValueChange = { style = it }, label = { Text("解牌风格") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = prompt, onValueChange = { prompt = it }, label = { Text("系统人设提示词 (System Prompt)") }, minLines = 3, maxLines = 6, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = greeting, onValueChange = { greeting = it }, label = { Text("开场问候语") }, maxLines = 3, modifier = Modifier.fillMaxWidth())
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text("保存后立即设为当前导师", style = MaterialTheme.typography.bodyMedium)
          Switch(checked = setActive, onCheckedChange = { setActive = it })
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank() && prompt.isNotBlank()) {
            onSave(
              initial.copy(
                name = name.trim(),
                title = title.trim().ifBlank { "专属塔罗导师" },
                personality = personality.trim(),
                readingStyle = style.trim(),
                systemPrompt = prompt.trim(),
                greeting = greeting.trim(),
                isActive = setActive,
              )
            )
          }
        },
        enabled = name.isNotBlank() && prompt.isNotBlank(),
      ) {
        Text("保存人设卡")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("取消") }
    },
  )
}
