package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.SanctuaryThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

enum class TarotDeckStyle(
  val titleZh: String,
  val subtitleZh: String,
  val badgeZh: String,
) {
  MORAVIA_MUCHA(
    titleZh = "莫拉维亚 · 穆夏圣百合",
    subtitleZh = "新艺术派花卉拱门、象牙羊皮卷与鼠尾草绿描金",
    badgeZh = "穆夏新艺术",
  ),
  ASTRAL_GOLD_FOIL(
    titleZh = "星穹黑金 · 鎏金星轨",
    subtitleZh = "深邃曜石黑绒底、24K 璀璨烫金日月星辰纹章",
    badgeZh = "曜石烫金",
  ),
  MARIAS_GATE(
    titleZh = "玛利亚之门 · 彩窗圣咏",
    subtitleZh = "雾霭紫罗兰哥特花窗、天使羽翼与琉璃光晕",
    badgeZh = "琉璃花窗",
  ),
  CLASSICAL_SANCTUARY(
    titleZh = "古典圣殿 · 黄金黎明",
    subtitleZh = "温暖古典羊皮金、精细版画线描与晨曦圣环",
    badgeZh = "古典金刻",
  ),
}

private val Context.apiSettingsDataStore: DataStore<Preferences> by
  preferencesDataStore(name = "aurelia_api_settings")

data class ApiSettings(
  val baseUrl: String = "https://api.openai.com/v1",
  val apiKey: String = "",
  val modelName: String = "gpt-4o-mini",
  val fetchedModels: List<String> = emptyList(),
  val temperature: Double = 0.75,
  val themeMode: SanctuaryThemeMode = SanctuaryThemeMode.LIGHT,
  val deckStyle: TarotDeckStyle = TarotDeckStyle.MORAVIA_MUCHA,
  val lastConnectionMessage: String = "",
  val lastLatencyMs: Long = 0L,
  val savedAt: Long = 0L,
) {
  val isConfigured: Boolean
    get() = baseUrl.isNotBlank() && apiKey.isNotBlank() && modelName.isNotBlank()
}

class ApiSettingsRepository(private val context: Context) {
  private object Keys {
    val BASE_URL = stringPreferencesKey("base_url")
    val API_KEY = stringPreferencesKey("api_key")
    val MODEL_NAME = stringPreferencesKey("model_name")
    val FETCHED_MODELS = stringPreferencesKey("fetched_models")
    val TEMPERATURE = doublePreferencesKey("temperature")
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val DECK_STYLE = stringPreferencesKey("deck_style")
    val LAST_CONNECTION_MSG = stringPreferencesKey("last_connection_msg")
    val LAST_LATENCY_MS = longPreferencesKey("last_latency_ms")
    val SAVED_AT = longPreferencesKey("saved_at")
  }

  val settingsFlow: Flow<ApiSettings> =
    context.apiSettingsDataStore.data.map { prefs ->
      val rawModels = prefs[Keys.FETCHED_MODELS].orEmpty()
      val models =
        if (rawModels.isBlank()) emptyList()
        else rawModels.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
      val themeStr = prefs[Keys.THEME_MODE] ?: SanctuaryThemeMode.LIGHT.name
      val theme =
        runCatching { SanctuaryThemeMode.valueOf(themeStr) }.getOrDefault(SanctuaryThemeMode.LIGHT)
      val deckStr = prefs[Keys.DECK_STYLE] ?: TarotDeckStyle.MORAVIA_MUCHA.name
      val deckStyle =
        runCatching { TarotDeckStyle.valueOf(deckStr) }.getOrDefault(TarotDeckStyle.MORAVIA_MUCHA)

      ApiSettings(
        baseUrl = prefs[Keys.BASE_URL] ?: "https://api.openai.com/v1",
        apiKey = prefs[Keys.API_KEY] ?: "",
        modelName = prefs[Keys.MODEL_NAME] ?: "gpt-4o-mini",
        fetchedModels = models,
        temperature = prefs[Keys.TEMPERATURE] ?: 0.75,
        themeMode = theme,
        deckStyle = deckStyle,
        lastConnectionMessage = prefs[Keys.LAST_CONNECTION_MSG] ?: "",
        lastLatencyMs = prefs[Keys.LAST_LATENCY_MS] ?: 0L,
        savedAt = prefs[Keys.SAVED_AT] ?: 0L,
      )
    }

  suspend fun getSettingsOnce(): ApiSettings = settingsFlow.first()

  suspend fun saveApiConfig(
    baseUrl: String,
    apiKey: String,
    modelName: String,
    temperature: Double,
  ) {
    context.apiSettingsDataStore.edit { prefs ->
      prefs[Keys.BASE_URL] = baseUrl.trim()
      prefs[Keys.API_KEY] = apiKey.trim()
      prefs[Keys.MODEL_NAME] = modelName.trim()
      prefs[Keys.TEMPERATURE] = temperature.coerceIn(0.0, 1.5)
      prefs[Keys.SAVED_AT] = System.currentTimeMillis()
    }
  }

  suspend fun saveFetchedModels(models: List<String>) {
    context.apiSettingsDataStore.edit { prefs ->
      prefs[Keys.FETCHED_MODELS] = models.distinct().joinToString("\n")
    }
  }

  suspend fun saveConnectionStatus(message: String, latencyMs: Long) {
    context.apiSettingsDataStore.edit { prefs ->
      prefs[Keys.LAST_CONNECTION_MSG] = message
      prefs[Keys.LAST_LATENCY_MS] = latencyMs
    }
  }

  suspend fun saveThemeMode(mode: SanctuaryThemeMode) {
    context.apiSettingsDataStore.edit { prefs -> prefs[Keys.THEME_MODE] = mode.name }
  }

  suspend fun saveDeckStyle(style: TarotDeckStyle) {
    context.apiSettingsDataStore.edit { prefs -> prefs[Keys.DECK_STYLE] = style.name }
  }
}
