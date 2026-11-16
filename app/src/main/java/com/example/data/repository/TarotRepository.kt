package com.example.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.data.local.TarotDatabase
import com.example.data.local.TarotSeedData
import com.example.data.model.BuiltInSpreads
import com.example.data.model.ChatMessageEntity
import com.example.data.model.DrawnCardSnapshot
import com.example.data.model.MentorPersonaEntity
import com.example.data.model.ReadingRecordEntity
import com.example.data.model.TarotCardEntity
import com.example.data.model.TarotSpread
import com.example.data.model.WorldbookEntryEntity
import com.example.data.preferences.ApiSettingsRepository
import com.example.data.remote.OpenAiApiService
import com.example.data.remote.OpenAiChatMessage
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class AiSpreadChoice(
  val spread: TarotSpread,
  val reason: String,
)

class TarotRepository(
  private val context: Context,
  private val db: TarotDatabase,
  private val settingsRepo: ApiSettingsRepository,
  val openAiService: OpenAiApiService,
) {
  private val cardDao = db.tarotCardDao()
  private val personaDao = db.mentorPersonaDao()
  private val worldbookDao = db.worldbookDao()
  private val readingDao = db.readingRecordDao()
  private val chatDao = db.chatMessageDao()

  private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
  private val drawnListType =
    Types.newParameterizedType(List::class.java, DrawnCardSnapshot::class.java)
  private val drawnListAdapter = moshi.adapter<List<DrawnCardSnapshot>>(drawnListType)

  val allCardsFlow: Flow<List<TarotCardEntity>> = cardDao.observeAllCards()
  val allPersonasFlow: Flow<List<MentorPersonaEntity>> = personaDao.observeAllPersonas()
  val activePersonaFlow: Flow<MentorPersonaEntity?> = personaDao.observeActivePersona()
  val allWorldbookFlow: Flow<List<WorldbookEntryEntity>> = worldbookDao.observeAllEntries()
  val allReadingsFlow: Flow<List<ReadingRecordEntity>> = readingDao.observeAllReadings()

  fun todayDateKey(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

  fun observeTodayDailyReading(): Flow<ReadingRecordEntity?> =
    readingDao.observeDailyReading(todayDateKey())

  fun observeMessagesForPersona(personaId: Long): Flow<List<ChatMessageEntity>> =
    chatDao.observeMessagesForPersona(personaId)

  fun decodeDrawnCards(json: String): List<DrawnCardSnapshot> =
    runCatching { drawnListAdapter.fromJson(json).orEmpty() }.getOrDefault(emptyList())

  fun encodeDrawnCards(cards: List<DrawnCardSnapshot>): String =
    drawnListAdapter.toJson(cards)

  suspend fun ensureSeeded() {
    withContext(Dispatchers.IO) {
      if (cardDao.getCardCount() < 78) {
        cardDao.insertAll(TarotSeedData.createAll78Cards())
      }
      if (personaDao.getPersonaCount() == 0) {
        personaDao.insertAll(TarotSeedData.createDefaultPersonas())
      } else if (personaDao.getActivePersonaOnce() == null) {
        // Guarantee one active persona
        val first = db.mentorPersonaDao().let { dao ->
          // fallback if none active
          dao
        }
        val list = TarotSeedData.createDefaultPersonas()
        if (personaDao.getPersonaCount() == 0) {
          personaDao.insertAll(list)
        }
      }
      if (worldbookDao.getEntryCount() == 0) {
        worldbookDao.insertAll(TarotSeedData.createDefaultWorldbookEntries())
      }
    }
  }

  suspend fun saveCard(card: TarotCardEntity) {
    cardDao.updateCard(card)
  }

  suspend fun resetCardToDefault(cardId: Int) {
    val defaultCard = TarotSeedData.createAll78Cards().firstOrNull { it.id == cardId } ?: return
    cardDao.updateCard(defaultCard)
  }

  suspend fun setActivePersona(personaId: Long) {
    personaDao.clearActiveFlags()
    personaDao.setActiveFlag(personaId)
  }

  suspend fun savePersona(persona: MentorPersonaEntity) {
    if (persona.id == 0L) {
      val newId = personaDao.insertPersona(persona.copy(updatedAt = System.currentTimeMillis()))
      if (persona.isActive) {
        personaDao.clearActiveFlags()
        personaDao.setActiveFlag(newId)
      }
    } else {
      personaDao.updatePersona(persona.copy(updatedAt = System.currentTimeMillis()))
      if (persona.isActive) {
        personaDao.clearActiveFlags()
        personaDao.setActiveFlag(persona.id)
      }
    }
  }

  suspend fun deletePersona(persona: MentorPersonaEntity) {
    if (persona.isDefault) return
    personaDao.deletePersonaById(persona.id)
    if (personaDao.getActivePersonaOnce() == null) {
      personaDao.setActiveFlag(1L)
    }
  }

  suspend fun saveWorldbookEntry(entry: WorldbookEntryEntity) {
    val stamped = entry.copy(updatedAt = System.currentTimeMillis())
    if (entry.id == 0L) {
      worldbookDao.insertEntry(stamped)
    } else {
      worldbookDao.updateEntry(stamped)
    }
  }

  suspend fun deleteWorldbookEntry(id: Long) {
    worldbookDao.deleteEntryById(id)
  }

  suspend fun importWorldbookFromUri(uri: Uri): Result<Int> =
    withContext(Dispatchers.IO) {
      runCatching {
        val fileName = resolveFileName(uri)
        val rawText =
          context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.bufferedReader(Charsets.UTF_8).readText()
          } ?: throw IllegalStateException("无法读取所选文件内容")

        val trimmed = rawText.trim()
        require(trimmed.isNotBlank()) { "所选文件内容为空" }

        val parsedEntries = mutableListOf<WorldbookEntryEntity>()
        if (fileName.endsWith(".json", ignoreCase = true) || trimmed.startsWith("{") || trimmed.startsWith("[")) {
          parseJsonLorebook(trimmed, fileName, parsedEntries)
        }

        if (parsedEntries.isEmpty()) {
          // Parse Markdown / TXT sections separated by headings or dividers
          val sections =
            trimmed
              .split(Regex("(?m)^#{1,3}\\s+|^---+$"))
              .map { it.trim() }
              .filter { it.isNotBlank() }

          if (sections.size > 1) {
            sections.forEachIndexed { index, block ->
              val lines = block.lines()
              val firstLine = lines.first().take(48).trim()
              val body = if (lines.size > 1) lines.drop(1).joinToString("\n").trim() else block
              parsedEntries.add(
                WorldbookEntryEntity(
                  title = firstLine.ifBlank { "${fileName.substringBeforeLast('.')} · 节段 ${index + 1}" },
                  keywords = extractAutoKeywords(firstLine, body),
                  content = body.ifBlank { firstLine },
                  sourceName = fileName,
                  isAlwaysActive = false,
                  isEnabled = true,
                )
              )
            }
          } else {
            // Chunk long single text cleanly by ~1500 chars so prompt budget stays healthy
            val chunks = trimmed.chunked(1800)
            chunks.forEachIndexed { idx, chunk ->
              val suffix = if (chunks.size > 1) " (${idx + 1}/${chunks.size})" else ""
              parsedEntries.add(
                WorldbookEntryEntity(
                  title = fileName.substringBeforeLast('.') + suffix,
                  keywords = extractAutoKeywords(fileName, chunk),
                  content = chunk,
                  sourceName = fileName,
                  isAlwaysActive = idx == 0,
                  isEnabled = true,
                )
              )
            }
          }
        }

        worldbookDao.insertAll(parsedEntries)
        parsedEntries.size
      }
    }

  private fun parseJsonLorebook(
    trimmed: String,
    fileName: String,
    out: MutableList<WorldbookEntryEntity>,
  ) {
    runCatching {
      if (trimmed.startsWith("[")) {
        val arr = JSONArray(trimmed)
        for (i in 0 until arr.length()) {
          val obj = arr.optJSONObject(i) ?: continue
          extractJsonEntry(obj, fileName, i, out)
        }
      } else {
        val root = JSONObject(trimmed)
        val entriesObj = root.optJSONObject("entries")
        if (entriesObj != null) {
          val keys = entriesObj.keys()
          var idx = 0
          while (keys.hasNext()) {
            val k = keys.next()
            val obj = entriesObj.optJSONObject(k) ?: continue
            extractJsonEntry(obj, fileName, idx++, out)
          }
        } else if (root.has("content") || root.has("comment") || root.has("title")) {
          extractJsonEntry(root, fileName, 0, out)
        }
      }
    }
  }

  private fun extractJsonEntry(
    obj: JSONObject,
    fileName: String,
    index: Int,
    out: MutableList<WorldbookEntryEntity>,
  ) {
    val content = obj.optString("content").ifBlank { obj.optString("description") }
    if (content.isBlank()) return
    val title =
      obj.optString("title")
        .ifBlank { obj.optString("comment") }
        .ifBlank { obj.optString("name") }
        .ifBlank { "${fileName.substringBeforeLast('.')} #${index + 1}" }
    val keyArray = obj.optJSONArray("key") ?: obj.optJSONArray("keys")
    val keywords =
      if (keyArray != null) {
        (0 until keyArray.length()).mapNotNull { keyArray.optString(it).takeIf { s -> s.isNotBlank() } }
          .joinToString(",")
      } else {
        obj.optString("keywords").ifBlank { extractAutoKeywords(title, content) }
      }
    val alwaysActive = obj.optBoolean("constant", false) || obj.optBoolean("isAlwaysActive", false)
    val enabled = !obj.optBoolean("disable", false) && obj.optBoolean("isEnabled", true)
    out.add(
      WorldbookEntryEntity(
        title = title,
        keywords = keywords,
        content = content,
        sourceName = fileName,
        isAlwaysActive = alwaysActive,
        isEnabled = enabled,
      )
    )
  }

  private fun extractAutoKeywords(title: String, body: String): String {
    val candidates = listOf("大阿卡那", "小阿卡那", "权杖", "圣杯", "宝剑", "星币", "正位", "逆位", "爱情", "事业", "灵性", "愚者", "女祭司", "太阳", "月亮", "星星", "命运之轮")
    val matched = candidates.filter { title.contains(it) || body.contains(it) }
    return (listOf(title.take(12)) + matched).distinct().take(6).joinToString(",")
  }

  private fun resolveFileName(uri: Uri): String {
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
      val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
      if (idx >= 0 && cursor.moveToFirst()) {
        return cursor.getString(idx)
      }
    }
    return uri.lastPathSegment?.substringAfterLast('/') ?: "塔罗资料.txt"
  }

  private suspend fun buildWorldbookContext(triggerText: String): String {
    val entries = worldbookDao.getEnabledEntriesOnce()
    if (entries.isEmpty()) return ""
    val lowerTrigger = triggerText.lowercase(Locale.getDefault())
    val matched =
      entries.filter { entry ->
        if (entry.isAlwaysActive) return@filter true
        val keys =
          entry.keywords
            .split(",", "，", ";", "；", " ")
            .map { it.trim().lowercase(Locale.getDefault()) }
            .filter { it.isNotEmpty() }
        keys.any { k -> lowerTrigger.contains(k) } ||
          lowerTrigger.contains(entry.title.lowercase(Locale.getDefault()))
      }
    if (matched.isEmpty()) return ""
    return matched.joinToString("\n\n") { "【世界书 · ${it.title}】\n${it.content}" }
  }

  suspend fun drawDailyCard(forceRedraw: Boolean = false): ReadingRecordEntity {
    val today = todayDateKey()
    if (!forceRedraw) {
      val existing = readingDao.getLatestReadingOnce()
      if (existing != null && existing.isDailyDraw && existing.dateKey == today) {
        return existing
      }
    }
    val allCards = cardDao.getAllCardsOnce().ifEmpty { TarotSeedData.createAll78Cards() }
    val picked = allCards.random()
    val isReversed = (0..99).random() < 32
    val spread = BuiltInSpreads.findById("single_sanctuary")
    val pos = spread.positions.first()
    val snapshot =
      DrawnCardSnapshot(
        cardId = picked.id,
        cardNameZh = picked.nameZh,
        cardNameEn = picked.nameEn,
        romanNumeral = picked.romanNumeral,
        suit = picked.suit,
        isReversed = isReversed,
        positionIndex = 0,
        positionName = pos.title,
        positionMeaning = pos.description,
      )
    val record =
      ReadingRecordEntity(
        dateKey = today,
        isDailyDraw = true,
        spreadId = spread.id,
        spreadName = "每日圣光一牌",
        question = "今日灵魂与能量指引 ($today)",
        drawnCardsJson = encodeDrawnCards(listOf(snapshot)),
      )
    val id = readingDao.insertReading(record)
    return record.copy(id = id)
  }

  suspend fun saveSpreadReading(
    spread: TarotSpread,
    question: String,
    drawnCards: List<DrawnCardSnapshot>,
    aiSpreadReason: String,
  ): ReadingRecordEntity {
    val record =
      ReadingRecordEntity(
        dateKey = todayDateKey(),
        isDailyDraw = false,
        spreadId = spread.id,
        spreadName = spread.nameZh,
        question = question.ifBlank { "探索此刻生命流动与圣光启示" },
        drawnCardsJson = encodeDrawnCards(drawnCards),
        aiSpreadSelectionReason = aiSpreadReason,
      )
    val id = readingDao.insertReading(record)
    return record.copy(id = id)
  }

  suspend fun aiSelectSpread(question: String): Result<AiSpreadChoice> {
    val settings = settingsRepo.getSettingsOnce()
    val persona = personaDao.getActivePersonaOnce()
    val worldbook = buildWorldbookContext(question)
    val spreadSummary =
      BuiltInSpreads.all.joinToString("\n") { s ->
        "- id: \"${s.id}\" | 名称: ${s.nameZh} (${s.cardCount}张) | 类别: ${s.category} | 适用: ${s.bestFor}"
      }

    val systemPrompt =
      buildString {
        appendLine(persona?.systemPrompt ?: "你是一位温柔圣洁的塔罗占卜导师。")
        if (worldbook.isNotBlank()) {
          appendLine("\n参考世界书资料：\n$worldbook")
        }
        appendLine(
          """
          
          请根据问卜者的问题，从下方可选牌阵中选出最契合的一个，并严格返回如下 JSON（不要附加多余 markdown 文字）：
          {"spreadId": "选中的id", "reason": "用温柔圣洁的语气说明为何此牌阵最契合当前问题（60字以内）"}
          可选牌阵列表：
          $spreadSummary
          """.trimIndent()
        )
      }

    return openAiService
      .chatCompletion(
        baseUrl = settings.baseUrl,
        apiKey = settings.apiKey,
        modelName = settings.modelName,
        messages =
          listOf(
            OpenAiChatMessage("system", systemPrompt),
            OpenAiChatMessage("user", "我的问题是：${question.ifBlank { "我想看看近期整体运势与心灵状态" }}"),
          ),
        temperature = 0.4,
        maxTokens = 300,
      )
      .map { raw ->
        val jsonText = extractJsonObject(raw)
        val obj = runCatching { JSONObject(jsonText) }.getOrNull()
        val chosenId = obj?.optString("spreadId").orEmpty()
        val reason =
          obj?.optString("reason").orEmpty().ifBlank {
            raw.replace(Regex("[{}\"`]"), "").trim().take(100)
          }
        val spread = BuiltInSpreads.findById(chosenId)
        AiSpreadChoice(spread = spread, reason = reason)
      }
  }

  suspend fun aiInterpretReading(record: ReadingRecordEntity): Result<ReadingRecordEntity> {
    val settings = settingsRepo.getSettingsOnce()
    val persona = personaDao.getActivePersonaOnce()
    val snapshots = decodeDrawnCards(record.drawnCardsJson)
    val allCards = cardDao.getAllCardsOnce().associateBy { it.id }

    val cardDetailsText =
      snapshots.joinToString("\n\n") { snap ->
        val dbCard = allCards[snap.cardId]
        val orient = if (snap.isReversed) "逆位 (Reversed)" else "正位 (Upright)"
        val meaning =
          if (snap.isReversed) dbCard?.meaningReversed ?: "" else dbCard?.meaningUpright ?: ""
        val keywords =
          if (snap.isReversed) dbCard?.keywordsReversed ?: "" else dbCard?.keywordsUpright ?: ""
        val customNote = dbCard?.customNotes?.takeIf { it.isNotBlank() }?.let { "\n  自定义/校准牌义备注：$it" } ?: ""
        """
        位置 ${snap.positionIndex + 1}【${snap.positionName}】(${snap.positionMeaning})
        - 抽到牌面：${snap.cardNameZh} (${snap.cardNameEn}) · $orient
        - 实时牌义关键词：$keywords
        - 当前牌义库释义：$meaning$customNote
        """.trimIndent()
      }

    val worldbook = buildWorldbookContext(record.question + " " + snapshots.joinToString(" ") { it.cardNameZh })

    val systemPrompt =
      buildString {
        appendLine(persona?.systemPrompt ?: "你是一位温柔圣洁、富有洞见的塔罗占卜导师。")
        if (worldbook.isNotBlank()) {
          appendLine("\n【世界书资料库（请融入解读）】\n$worldbook")
        }
        appendLine(
          """
          
          请严格依据问卜者所抽出的牌面、正逆位状态与上述最新牌义库信息，以你的人设口吻提供深度且温暖的塔罗解读。
          建议结构：
          1. 圣殿总览：整体能量、元素流动与核心主题
          2. 逐牌微光解析：结合牌位意义与正逆位深入剖析
          3. 圣光指引与行动建议：给出2-3条温暖切实的行动方向
          """.trimIndent()
        )
      }

    val userPrompt =
      """
      牌阵：${record.spreadName}
      问卜主题：${record.question}
      
      已揭示牌面：
      $cardDetailsText
      """.trimIndent()

    return openAiService
      .chatCompletion(
        baseUrl = settings.baseUrl,
        apiKey = settings.apiKey,
        modelName = settings.modelName,
        messages =
          listOf(
            OpenAiChatMessage("system", systemPrompt),
            OpenAiChatMessage("user", userPrompt),
          ),
        temperature = settings.temperature,
        maxTokens = 2000,
      )
      .map { interpretation ->
        val updated =
          record.copy(
            aiInterpretation = interpretation,
            mentorName = persona?.name ?: "圣光导师",
          )
        readingDao.updateReading(updated)
        updated
      }
  }

  suspend fun aiRefineCardMeaning(card: TarotCardEntity): Result<TarotCardEntity> {
    val settings = settingsRepo.getSettingsOnce()
    val worldbook = buildWorldbookContext("${card.nameZh} ${card.nameEn} ${card.suit} 牌义")

    val systemPrompt =
      buildString {
        appendLine("你是精通古典维特塔罗、赫尔墨斯象征学与心理占卜的牌义编纂专家。")
        if (worldbook.isNotBlank()) {
          appendLine("请结合以下世界书资料更新并深化此牌的牌义：\n$worldbook")
        }
        appendLine(
          """
          请为指定塔罗牌生成更精准、现代、唯美且具预测洞察力的牌义数据，并严格仅返回如下 JSON 对象（无多余说明）：
          {
            "keywordsUpright": "4-5个中文关键词，顿号分隔",
            "keywordsReversed": "4-5个中文关键词，顿号分隔",
            "meaningUpright": "100-140字正位深度释义",
            "meaningReversed": "100-140字逆位深度释义（温和而有建设性）",
            "loveAdvice": "50-80字情感建议",
            "careerAdvice": "50-80字事业学业建议",
            "spiritualAdvice": "40-70字灵性启示"
          }
          """.trimIndent()
        )
      }

    val userPrompt =
      """
      请实时更新校准以下塔罗牌的牌义数据库条目：
      - 名称：${card.nameZh} (${card.nameEn})
      - 编号/属性：${card.romanNumeral} · ${card.element} · ${card.astrology}
      - 原正位释义：${card.meaningUpright}
      - 原逆位释义：${card.meaningReversed}
      """.trimIndent()

    return openAiService
      .chatCompletion(
        baseUrl = settings.baseUrl,
        apiKey = settings.apiKey,
        modelName = settings.modelName,
        messages =
          listOf(
            OpenAiChatMessage("system", systemPrompt),
            OpenAiChatMessage("user", userPrompt),
          ),
        temperature = 0.5,
        maxTokens = 1000,
      )
      .map { raw ->
        val obj = JSONObject(extractJsonObject(raw))
        val updated =
          card.copy(
            keywordsUpright = obj.optString("keywordsUpright").ifBlank { card.keywordsUpright },
            keywordsReversed = obj.optString("keywordsReversed").ifBlank { card.keywordsReversed },
            meaningUpright = obj.optString("meaningUpright").ifBlank { card.meaningUpright },
            meaningReversed = obj.optString("meaningReversed").ifBlank { card.meaningReversed },
            loveAdvice = obj.optString("loveAdvice").ifBlank { card.loveAdvice },
            careerAdvice = obj.optString("careerAdvice").ifBlank { card.careerAdvice },
            spiritualAdvice = obj.optString("spiritualAdvice").ifBlank { card.spiritualAdvice },
            aiRefinedAt = System.currentTimeMillis(),
            isCustomUpdated = true,
          )
        cardDao.updateCard(updated)
        updated
      }
  }

  suspend fun sendMentorChat(
    persona: MentorPersonaEntity,
    userMessage: String,
    attachedReading: ReadingRecordEntity?,
  ): Result<String> {
    val settings = settingsRepo.getSettingsOnce()
    val readingSummary =
      if (attachedReading != null) {
        val snaps = decodeDrawnCards(attachedReading.drawnCardsJson)
        val cardsStr =
          snaps.joinToString("、") {
            "${it.positionName}:${it.cardNameZh}(${if (it.isReversed) "逆位" else "正位"})"
          }
        "【附加牌面：${attachedReading.spreadName}】问题「${attachedReading.question}」-> $cardsStr"
      } else {
        ""
      }

    // Persist user message immediately
    chatDao.insertMessage(
      ChatMessageEntity(
        personaId = persona.id,
        role = "user",
        content = userMessage.trim(),
        attachedReadingSummary = readingSummary,
      )
    )

    val history = chatDao.getMessagesForPersonaOnce(persona.id).takeLast(16)
    val triggerText = "$userMessage $readingSummary"
    val worldbook = buildWorldbookContext(triggerText)

    val allCards = cardDao.getAllCardsOnce().associateBy { it.id }
    val readingPromptContext =
      if (attachedReading != null) {
        val snaps = decodeDrawnCards(attachedReading.drawnCardsJson)
        val detail =
          snaps.joinToString("\n") { s ->
            val dbCard = allCards[s.cardId]
            val meaning =
              if (s.isReversed) dbCard?.meaningReversed ?: "" else dbCard?.meaningUpright ?: ""
            "- ${s.positionName}: ${s.cardNameZh} (${if (s.isReversed) "逆位" else "正位"}) — $meaning"
          }
        "\n【问卜者当前携带来的占卜记录】\n牌阵：${attachedReading.spreadName}\n问题：${attachedReading.question}\n$detail"
      } else {
        ""
      }

    val systemPrompt =
      buildString {
        appendLine(persona.systemPrompt)
        appendLine("人物性格：${persona.personality}")
        appendLine("解牌风格：${persona.readingStyle}")
        if (worldbook.isNotBlank()) {
          appendLine("\n【激活的世界书资料】\n$worldbook")
        }
        if (readingPromptContext.isNotBlank()) {
          appendLine(readingPromptContext)
        }
      }

    val apiMessages = buildList {
      add(OpenAiChatMessage("system", systemPrompt))
      history.forEach { msg ->
        val content =
          if (msg.attachedReadingSummary.isNotBlank() && msg.role == "user") {
            "${msg.attachedReadingSummary}\n${msg.content}"
          } else {
            msg.content
          }
        add(OpenAiChatMessage(role = msg.role, content = content))
      }
    }

    return openAiService
      .chatCompletion(
        baseUrl = settings.baseUrl,
        apiKey = settings.apiKey,
        modelName = settings.modelName,
        messages = apiMessages,
        temperature = settings.temperature,
        maxTokens = 1800,
      )
      .onSuccess { reply ->
        chatDao.insertMessage(
          ChatMessageEntity(
            personaId = persona.id,
            role = "assistant",
            content = reply,
          )
        )
      }
  }

  suspend fun clearMentorChat(personaId: Long) {
    chatDao.clearMessagesForPersona(personaId)
  }

  private fun extractJsonObject(raw: String): String {
    val start = raw.indexOf('{')
    val end = raw.lastIndexOf('}')
    return if (start >= 0 && end > start) raw.substring(start, end + 1) else raw
  }
}
