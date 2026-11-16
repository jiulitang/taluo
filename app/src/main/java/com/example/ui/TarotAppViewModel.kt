package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.TarotDatabase
import com.example.data.model.BuiltInSpreads
import com.example.data.model.ChatMessageEntity
import com.example.data.model.DrawnCardSnapshot
import com.example.data.model.MentorPersonaEntity
import com.example.data.model.ReadingRecordEntity
import com.example.data.model.TarotCardEntity
import com.example.data.model.TarotSpread
import com.example.data.model.WorldbookEntryEntity
import com.example.data.preferences.ApiSettings
import com.example.data.preferences.ApiSettingsRepository
import com.example.data.remote.OpenAiApiService
import com.example.data.repository.TarotRepository
import com.example.ui.theme.SanctuaryThemeMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class DivinationPhase {
  SETUP,
  SHUFFLING,
  DRAWING,
}

data class DivinationUiState(
  val phase: DivinationPhase = DivinationPhase.SETUP,
  val question: String = "",
  val selectedSpread: TarotSpread = BuiltInSpreads.all[1], // Time flow default
  val aiSpreadReason: String = "",
  val isSelectingSpread: Boolean = false,
  val isRitualStarted: Boolean = false,
  val lastDrawnSlotIndex: Int? = null,
  val drawnSlots: List<DrawnCardSnapshot?> = List(BuiltInSpreads.all[1].cardCount) { null },
  val revealedIndices: Set<Int> = emptySet(),
  val currentReading: ReadingRecordEntity? = null,
  val isInterpreting: Boolean = false,
  val errorMessage: String? = null,
)

data class SettingsUiState(
  val baseUrlInput: String = "https://api.openai.com/v1",
  val apiKeyInput: String = "",
  val modelNameInput: String = "gpt-4o-mini",
  val temperatureInput: Float = 0.75f,
  val isFetchingModels: Boolean = false,
  val isTestingConnection: Boolean = false,
  val statusBanner: String? = null,
  val isStatusError: Boolean = false,
)

class TarotAppViewModel(application: Application) : AndroidViewModel(application) {
  private val db = TarotDatabase.getInstance(application)
  private val settingsRepo = ApiSettingsRepository(application)
  private val openAiService = OpenAiApiService()
  val repository = TarotRepository(application, db, settingsRepo, openAiService)

  val apiSettings: StateFlow<ApiSettings> =
    settingsRepo.settingsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ApiSettings())

  val allCards: StateFlow<List<TarotCardEntity>> =
    repository.allCardsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allPersonas: StateFlow<List<MentorPersonaEntity>> =
    repository.allPersonasFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val activePersona: StateFlow<MentorPersonaEntity?> =
    repository.activePersonaFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  val allWorldbookEntries: StateFlow<List<WorldbookEntryEntity>> =
    repository.allWorldbookFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allReadings: StateFlow<List<ReadingRecordEntity>> =
    repository.allReadingsFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val todayDailyReading: StateFlow<ReadingRecordEntity?> =
    repository.observeTodayDailyReading().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  @OptIn(ExperimentalCoroutinesApi::class)
  val chatMessages: StateFlow<List<ChatMessageEntity>> =
    activePersona
      .flatMapLatest { persona ->
        if (persona == null) flowOf(emptyList())
        else repository.observeMessagesForPersona(persona.id)
      }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _divinationState = MutableStateFlow(DivinationUiState())
  val divinationState: StateFlow<DivinationUiState> = _divinationState.asStateFlow()

  private val _settingsUiState = MutableStateFlow(SettingsUiState())
  val settingsUiState: StateFlow<SettingsUiState> = _settingsUiState.asStateFlow()

  private val _isDailyRevealed = MutableStateFlow(false)
  val isDailyRevealed: StateFlow<Boolean> = _isDailyRevealed.asStateFlow()

  private val _isDailyAiLoading = MutableStateFlow(false)
  val isDailyAiLoading: StateFlow<Boolean> = _isDailyAiLoading.asStateFlow()

  private val _isMentorReplying = MutableStateFlow(false)
  val isMentorReplying: StateFlow<Boolean> = _isMentorReplying.asStateFlow()

  private val _attachedReadingForChat = MutableStateFlow<ReadingRecordEntity?>(null)
  val attachedReadingForChat: StateFlow<ReadingRecordEntity?> = _attachedReadingForChat.asStateFlow()

  private val _updatingCardIds = MutableStateFlow<Set<Int>>(emptySet())
  val updatingCardIds: StateFlow<Set<Int>> = _updatingCardIds.asStateFlow()

  private val _batchUpdateProgress = MutableStateFlow<String?>(null)
  val batchUpdateProgress: StateFlow<String?> = _batchUpdateProgress.asStateFlow()

  private val _toastMessage = MutableStateFlow<String?>(null)
  val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

  private var settingsInitialized = false

  init {
    viewModelScope.launch { repository.ensureSeeded() }
    viewModelScope.launch {
      apiSettings.collect { loaded ->
        if (!settingsInitialized && loaded.baseUrl.isNotBlank()) {
          settingsInitialized = true
          _settingsUiState.update {
            it.copy(
              baseUrlInput = loaded.baseUrl,
              apiKeyInput = loaded.apiKey,
              modelNameInput = loaded.modelName,
              temperatureInput = loaded.temperature.toFloat(),
            )
          }
        }
      }
    }
    viewModelScope.launch {
      todayDailyReading.collect { daily ->
        if (daily != null) {
          _isDailyRevealed.value = true
        }
      }
    }
  }

  fun clearToast() {
    _toastMessage.value = null
  }

  fun showToast(msg: String) {
    _toastMessage.value = msg
  }

  // --- Daily Draw Actions ---
  fun performDailyDraw(forceRedraw: Boolean = false) {
    viewModelScope.launch {
      repository.drawDailyCard(forceRedraw = forceRedraw)
      _isDailyRevealed.value = true
    }
  }

  fun interpretDailyWithAi() {
    val record = todayDailyReading.value ?: return
    viewModelScope.launch {
      _isDailyAiLoading.value = true
      repository
        .aiInterpretReading(record)
        .onFailure { err -> _toastMessage.value = err.message ?: "AI 解析失败" }
      _isDailyAiLoading.value = false
    }
  }

  // --- Divination / Spreads Actions ---
  fun updateDivinationQuestion(q: String) {
    _divinationState.update { it.copy(question = q, errorMessage = null) }
  }

  fun selectSpread(spread: TarotSpread, reason: String = "") {
    _divinationState.update {
      DivinationUiState(
        question = it.question,
        selectedSpread = spread,
        aiSpreadReason = reason,
        drawnSlots = List(spread.cardCount) { null },
        revealedIndices = emptySet(),
        currentReading = null,
      )
    }
  }

  fun aiSelectSpreadForQuestion() {
    val q = _divinationState.value.question
    viewModelScope.launch {
      _divinationState.update { it.copy(isSelectingSpread = true, errorMessage = null) }
      repository
        .aiSelectSpread(q)
        .onSuccess { choice ->
          _divinationState.update {
            DivinationUiState(
              question = it.question,
              selectedSpread = choice.spread,
              aiSpreadReason = choice.reason,
              isSelectingSpread = false,
              drawnSlots = List(choice.spread.cardCount) { null },
              revealedIndices = emptySet(),
              currentReading = null,
            )
          }
        }
        .onFailure { err ->
          _divinationState.update {
            it.copy(isSelectingSpread = false, errorMessage = err.message ?: "AI 选阵失败")
          }
        }
    }
  }

  fun startDivinationRitual() {
    _divinationState.update {
      val spread = it.selectedSpread
      it.copy(
        phase = DivinationPhase.SHUFFLING,
        question = it.question.ifBlank { "探索此刻生命流动与圣光指引" },
        isRitualStarted = true,
        lastDrawnSlotIndex = null,
        drawnSlots = List(spread.cardCount) { null },
        revealedIndices = emptySet(),
        currentReading = null,
        errorMessage = null,
      )
    }
  }

  fun completeShuffleAndStartDraw() {
    _divinationState.update {
      it.copy(
        phase = DivinationPhase.DRAWING,
        isRitualStarted = true,
        errorMessage = null,
      )
    }
  }

  fun returnToSpreadSetup() {
    val current = _divinationState.value
    _divinationState.update {
      DivinationUiState(
        phase = DivinationPhase.SETUP,
        question = current.question,
        selectedSpread = current.selectedSpread,
        aiSpreadReason = current.aiSpreadReason,
        drawnSlots = List(current.selectedSpread.cardCount) { null },
      )
    }
  }

  fun drawNextSequentialSlot() {
    val state = _divinationState.value
    val nextIdx = state.drawnSlots.indexOfFirst { it == null }
    if (nextIdx >= 0) {
      drawCardAtSlot(nextIdx)
    }
  }

  fun drawCardAtSlot(slotIndex: Int) {
    val state = _divinationState.value
    if (slotIndex !in state.drawnSlots.indices) return
    if (state.drawnSlots[slotIndex] != null) return

    val cards = allCards.value
    if (cards.isEmpty()) return
    val usedIds = state.drawnSlots.mapNotNull { it?.cardId }.toSet()
    val pool = cards.filter { it.id !in usedIds }.ifEmpty { cards }
    val picked = pool.random()
    val pos = state.selectedSpread.positions[slotIndex]
    val snapshot =
      DrawnCardSnapshot(
        cardId = picked.id,
        cardNameZh = picked.nameZh,
        cardNameEn = picked.nameEn,
        romanNumeral = picked.romanNumeral,
        suit = picked.suit,
        isReversed = (0..99).random() < 30,
        positionIndex = slotIndex,
        positionName = pos.title,
        positionMeaning = pos.description,
      )
    val updatedSlots = state.drawnSlots.toMutableList().apply { set(slotIndex, snapshot) }
    val updatedRevealed = state.revealedIndices + slotIndex
    _divinationState.update {
      it.copy(
        isRitualStarted = true,
        lastDrawnSlotIndex = slotIndex,
        drawnSlots = updatedSlots,
        revealedIndices = updatedRevealed,
        errorMessage = null,
      )
    }
    if (updatedSlots.all { it != null }) {
      persistCompletedSpreadIfNeeded()
    }
  }

  fun drawAllRemainingSlots() {
    val state = _divinationState.value
    val cards = allCards.value
    if (cards.isEmpty()) return
    val usedIds = state.drawnSlots.mapNotNull { it?.cardId }.toMutableSet()
    val updatedSlots = state.drawnSlots.toMutableList()
    val updatedRevealed = state.revealedIndices.toMutableSet()

    for (idx in updatedSlots.indices) {
      if (updatedSlots[idx] == null) {
        val pool = cards.filter { it.id !in usedIds }.ifEmpty { cards }
        val picked = pool.random()
        usedIds.add(picked.id)
        val pos = state.selectedSpread.positions[idx]
        updatedSlots[idx] =
          DrawnCardSnapshot(
            cardId = picked.id,
            cardNameZh = picked.nameZh,
            cardNameEn = picked.nameEn,
            romanNumeral = picked.romanNumeral,
            suit = picked.suit,
            isReversed = (0..99).random() < 30,
            positionIndex = idx,
            positionName = pos.title,
            positionMeaning = pos.description,
          )
        updatedRevealed.add(idx)
      }
    }
    _divinationState.update {
      it.copy(drawnSlots = updatedSlots, revealedIndices = updatedRevealed, errorMessage = null)
    }
    persistCompletedSpreadIfNeeded()
  }

  private fun persistCompletedSpreadIfNeeded() {
    val state = _divinationState.value
    val finalCards = state.drawnSlots.filterNotNull()
    if (finalCards.size != state.selectedSpread.cardCount || state.currentReading != null) return
    viewModelScope.launch {
      val saved =
        repository.saveSpreadReading(
          spread = state.selectedSpread,
          question = state.question,
          drawnCards = finalCards,
          aiSpreadReason = state.aiSpreadReason,
        )
      _divinationState.update { it.copy(currentReading = saved) }
    }
  }

  fun interpretCurrentSpreadWithAi() {
    val state = _divinationState.value
    val finalCards = state.drawnSlots.filterNotNull()
    if (finalCards.size != state.selectedSpread.cardCount) return
    viewModelScope.launch {
      _divinationState.update { it.copy(isInterpreting = true, errorMessage = null) }
      val record =
        state.currentReading
          ?: repository.saveSpreadReading(
            spread = state.selectedSpread,
            question = state.question,
            drawnCards = finalCards,
            aiSpreadReason = state.aiSpreadReason,
          )
      repository
        .aiInterpretReading(record)
        .onSuccess { updated ->
          _divinationState.update { it.copy(currentReading = updated, isInterpreting = false) }
        }
        .onFailure { err ->
          _divinationState.update {
            it.copy(isInterpreting = false, errorMessage = err.message ?: "AI 解读失败")
          }
        }
    }
  }

  fun resetDivinationStage() {
    val current = _divinationState.value.selectedSpread
    _divinationState.update {
      DivinationUiState(
        question = "",
        selectedSpread = current,
        drawnSlots = List(current.cardCount) { null },
      )
    }
  }

  // --- Mentor Chat & Persona Actions ---
  fun attachReadingToChat(record: ReadingRecordEntity?) {
    _attachedReadingForChat.value = record
  }

  fun sendChatMessage(text: String) {
    val persona = activePersona.value ?: return
    if (text.isBlank() || _isMentorReplying.value) return
    val attached = _attachedReadingForChat.value
    viewModelScope.launch {
      _isMentorReplying.value = true
      repository
        .sendMentorChat(persona, text, attached)
        .onSuccess { _attachedReadingForChat.value = null }
        .onFailure { err -> _toastMessage.value = err.message ?: "发送失败" }
      _isMentorReplying.value = false
    }
  }

  fun clearActivePersonaChat() {
    val persona = activePersona.value ?: return
    viewModelScope.launch { repository.clearMentorChat(persona.id) }
  }

  fun selectPersona(personaId: Long) {
    viewModelScope.launch { repository.setActivePersona(personaId) }
  }

  fun savePersona(persona: MentorPersonaEntity) {
    viewModelScope.launch {
      repository.savePersona(persona)
      _toastMessage.value = "人设卡「${persona.name}」已保存"
    }
  }

  fun deletePersona(persona: MentorPersonaEntity) {
    viewModelScope.launch {
      repository.deletePersona(persona)
      _toastMessage.value = "已删除人设卡「${persona.name}」"
    }
  }

  // --- Tarot Meanings DB & Worldbook Actions ---
  fun saveCardEdits(card: TarotCardEntity) {
    viewModelScope.launch {
      repository.saveCard(card.copy(isCustomUpdated = true, aiRefinedAt = System.currentTimeMillis()))
      _toastMessage.value = "「${card.nameZh}」牌义已实时写入数据库"
    }
  }

  fun resetCardDefault(cardId: Int) {
    viewModelScope.launch {
      repository.resetCardToDefault(cardId)
      _toastMessage.value = "已恢复默认牌义"
    }
  }

  fun aiUpdateSingleCardMeaning(card: TarotCardEntity) {
    viewModelScope.launch {
      _updatingCardIds.update { it + card.id }
      repository
        .aiRefineCardMeaning(card)
        .onSuccess { updated ->
          _toastMessage.value = "「${updated.nameZh}」牌义已通过 AI 实时校对更新"
        }
        .onFailure { err -> _toastMessage.value = err.message ?: "AI 更新牌义失败" }
      _updatingCardIds.update { it - card.id }
    }
  }

  fun aiBatchUpdateCards(cards: List<TarotCardEntity>) {
    if (cards.isEmpty() || _batchUpdateProgress.value != null) return
    val subset = cards.take(5) // Safe responsive batch of 5 cards per trigger
    viewModelScope.launch {
      var successCount = 0
      subset.forEachIndexed { idx, card ->
        _batchUpdateProgress.value = "正在实时校对更新 (${idx + 1}/${subset.size})：${card.nameZh}"
        repository.aiRefineCardMeaning(card).onSuccess { successCount++ }.onFailure { err ->
          _toastMessage.value = err.message ?: "批量更新中断"
          _batchUpdateProgress.value = null
          return@launch
        }
      }
      _batchUpdateProgress.value = null
      _toastMessage.value = "已完成 $successCount 张牌义的 AI 实时更新"
    }
  }

  fun saveWorldbookEntry(entry: WorldbookEntryEntity) {
    viewModelScope.launch {
      repository.saveWorldbookEntry(entry)
      _toastMessage.value = "世界书词条「${entry.title}」已保存"
    }
  }

  fun toggleWorldbookEnabled(entry: WorldbookEntryEntity, enabled: Boolean) {
    viewModelScope.launch { repository.saveWorldbookEntry(entry.copy(isEnabled = enabled)) }
  }

  fun toggleWorldbookAlwaysActive(entry: WorldbookEntryEntity, alwaysActive: Boolean) {
    viewModelScope.launch {
      repository.saveWorldbookEntry(entry.copy(isAlwaysActive = alwaysActive))
    }
  }

  fun deleteWorldbookEntry(id: Long) {
    viewModelScope.launch {
      repository.deleteWorldbookEntry(id)
      _toastMessage.value = "已移除世界书词条"
    }
  }

  fun importWorldbookFile(uri: Uri) {
    viewModelScope.launch {
      repository
        .importWorldbookFromUri(uri)
        .onSuccess { count -> _toastMessage.value = "成功导入 $count 条世界书塔罗资料" }
        .onFailure { err -> _toastMessage.value = err.message ?: "导入世界书失败" }
    }
  }

  // --- Settings Actions ---
  fun updateBaseUrlInput(v: String) {
    _settingsUiState.update { it.copy(baseUrlInput = v, statusBanner = null) }
  }

  fun updateApiKeyInput(v: String) {
    _settingsUiState.update { it.copy(apiKeyInput = v, statusBanner = null) }
  }

  fun updateModelNameInput(v: String) {
    _settingsUiState.update { it.copy(modelNameInput = v, statusBanner = null) }
  }

  fun updateTemperatureInput(v: Float) {
    _settingsUiState.update { it.copy(temperatureInput = v) }
  }

  fun saveApiSettings() {
    val s = _settingsUiState.value
    viewModelScope.launch {
      settingsRepo.saveApiConfig(
        baseUrl = s.baseUrlInput,
        apiKey = s.apiKeyInput,
        modelName = s.modelNameInput,
        temperature = s.temperatureInput.toDouble(),
      )
      _settingsUiState.update {
        it.copy(statusBanner = "API 参数已保存并立即生效 (${s.modelNameInput.trim()})", isStatusError = false)
      }
      _toastMessage.value = "设置已保存生效"
    }
  }

  fun fetchModelsFromApi() {
    val s = _settingsUiState.value
    viewModelScope.launch {
      _settingsUiState.update { it.copy(isFetchingModels = true, statusBanner = null) }
      openAiService
        .fetchModels(s.baseUrlInput, s.apiKeyInput)
        .onSuccess { models ->
          settingsRepo.saveFetchedModels(models)
          val selectedModel =
            if (s.modelNameInput.isBlank() || s.modelNameInput !in models) models.first()
            else s.modelNameInput
          _settingsUiState.update {
            it.copy(
              isFetchingModels = false,
              modelNameInput = selectedModel,
              statusBanner = "成功拉取 ${models.size} 个可用模型，点击「保存生效」即可应用",
              isStatusError = false,
            )
          }
        }
        .onFailure { err ->
          _settingsUiState.update {
            it.copy(
              isFetchingModels = false,
              statusBanner = err.message ?: "拉取模型失败",
              isStatusError = true,
            )
          }
        }
    }
  }

  fun testApiConnection() {
    val s = _settingsUiState.value
    viewModelScope.launch {
      _settingsUiState.update { it.copy(isTestingConnection = true, statusBanner = null) }
      openAiService
        .testConnection(s.baseUrlInput, s.apiKeyInput, s.modelNameInput)
        .onSuccess { res ->
          settingsRepo.saveConnectionStatus(res.replyPreview, res.latencyMs)
          _settingsUiState.update {
            it.copy(
              isTestingConnection = false,
              statusBanner = "连接成功 · ${res.latencyMs}ms · 回复：「${res.replyPreview}」",
              isStatusError = false,
            )
          }
        }
        .onFailure { err ->
          val msg = err.message ?: "连接测试失败"
          settingsRepo.saveConnectionStatus(msg, 0L)
          _settingsUiState.update {
            it.copy(isTestingConnection = false, statusBanner = msg, isStatusError = true)
          }
        }
    }
  }

  fun setThemeMode(mode: SanctuaryThemeMode) {
    viewModelScope.launch { settingsRepo.saveThemeMode(mode) }
  }

  fun setDeckStyle(style: com.example.data.preferences.TarotDeckStyle) {
    viewModelScope.launch {
      settingsRepo.saveDeckStyle(style)
      _toastMessage.value = "已切换整套卡面风格：${style.titleZh}"
    }
  }
}
