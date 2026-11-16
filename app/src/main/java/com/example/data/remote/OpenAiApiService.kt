package com.example.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

data class ConnectionTestResult(
  val success: Boolean,
  val latencyMs: Long,
  val replyPreview: String,
)

class OpenAiApiService {
  private val moshi: Moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
  private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

  private val client: OkHttpClient =
    OkHttpClient.Builder()
      .connectTimeout(25, TimeUnit.SECONDS)
      .readTimeout(90, TimeUnit.SECONDS)
      .writeTimeout(30, TimeUnit.SECONDS)
      .build()

  private val modelsAdapter = moshi.adapter(OpenAiModelsResponse::class.java)
  private val chatReqAdapter = moshi.adapter(OpenAiChatRequest::class.java)
  private val chatRespAdapter = moshi.adapter(OpenAiChatResponse::class.java)
  private val errorAdapter = moshi.adapter(OpenAiErrorEnvelope::class.java)

  fun normalizeBaseUrl(rawUrl: String): String {
    var cleaned = rawUrl.trim().trimEnd('/')
    if (cleaned.endsWith("/chat/completions")) {
      cleaned = cleaned.removeSuffix("/chat/completions")
    } else if (cleaned.endsWith("/models")) {
      cleaned = cleaned.removeSuffix("/models")
    }
    if (!cleaned.startsWith("http://") && !cleaned.startsWith("https://")) {
      cleaned = "https://$cleaned"
    }
    // Append /v1 only if URL has no explicit version path segment like /v1, /v2, /api/v3
    val hasVersionPath = Regex(".*/v\\d+($|/.*)").matches(cleaned)
    return if (hasVersionPath) cleaned else "$cleaned/v1"
  }

  suspend fun fetchModels(baseUrl: String, apiKey: String): Result<List<String>> =
    withContext(Dispatchers.IO) {
      runCatching {
        require(baseUrl.isNotBlank()) { "请先填写 Base URL" }
        val normalized = normalizeBaseUrl(baseUrl)
        val requestBuilder = Request.Builder().url("$normalized/models").get()
        if (apiKey.isNotBlank()) {
          requestBuilder.addHeader("Authorization", "Bearer ${apiKey.trim()}")
        }
        client.newCall(requestBuilder.build()).execute().use { response ->
          val bodyText = response.body?.string().orEmpty()
          if (!response.isSuccessful) {
            val parsedError = runCatching { errorAdapter.fromJson(bodyText)?.error?.message }.getOrNull()
            throw IllegalStateException(
              "拉取模型失败 (HTTP ${response.code}): ${parsedError ?: bodyText.take(180)}"
            )
          }
          val parsed = modelsAdapter.fromJson(bodyText)
          val ids = parsed?.data?.map { it.id }?.distinct()?.sorted().orEmpty()
          if (ids.isEmpty()) {
            throw IllegalStateException("接口返回成功，但模型列表为空")
          }
          ids
        }
      }
    }

  suspend fun testConnection(
    baseUrl: String,
    apiKey: String,
    modelName: String,
  ): Result<ConnectionTestResult> =
    withContext(Dispatchers.IO) {
      runCatching {
        require(baseUrl.isNotBlank()) { "请先填写 Base URL" }
        require(apiKey.isNotBlank()) { "请先填写 API Key" }
        require(modelName.isNotBlank()) { "请先填写或选择 Model Name" }

        val start = System.currentTimeMillis()
        val reply =
          chatCompletion(
            baseUrl = baseUrl,
            apiKey = apiKey,
            modelName = modelName,
            messages =
              listOf(
                OpenAiChatMessage(
                  role = "user",
                  content = "请用一句话回复「圣光连接成功」，以确认塔罗占卜通道畅通。",
                )
              ),
            temperature = 0.2,
            maxTokens = 64,
          ).getOrThrow()
        val latency = System.currentTimeMillis() - start
        ConnectionTestResult(
          success = true,
          latencyMs = latency,
          replyPreview = reply.trim(),
        )
      }
    }

  suspend fun chatCompletion(
    baseUrl: String,
    apiKey: String,
    modelName: String,
    messages: List<OpenAiChatMessage>,
    temperature: Double = 0.75,
    maxTokens: Int? = 1800,
  ): Result<String> =
    withContext(Dispatchers.IO) {
      runCatching {
        require(baseUrl.isNotBlank()) { "尚未配置 Base URL，请前往「设置」完善接口参数" }
        require(apiKey.isNotBlank()) { "尚未配置 API Key，请前往「设置」填写并保存" }
        require(modelName.isNotBlank()) { "尚未配置 Model Name，请前往「设置」选择或填写模型" }

        val normalized = normalizeBaseUrl(baseUrl)
        val payload =
          OpenAiChatRequest(
            model = modelName.trim(),
            messages = messages,
            temperature = temperature,
            max_tokens = maxTokens,
          )
        val bodyJson = chatReqAdapter.toJson(payload)
        val request =
          Request.Builder()
            .url("$normalized/chat/completions")
            .addHeader("Authorization", "Bearer ${apiKey.trim()}")
            .addHeader("Content-Type", "application/json")
            .post(bodyJson.toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).execute().use { response ->
          val respText = response.body?.string().orEmpty()
          if (!response.isSuccessful) {
            val apiMsg = runCatching { errorAdapter.fromJson(respText)?.error?.message }.getOrNull()
            throw IllegalStateException(
              "API 调用失败 (HTTP ${response.code}): ${apiMsg ?: respText.take(220)}"
            )
          }
          val parsed = chatRespAdapter.fromJson(respText)
          val content = parsed?.choices?.firstOrNull()?.message?.content
          if (content.isNullOrBlank()) {
            throw IllegalStateException("模型返回内容为空，请检查模型名称与接口兼容性")
          }
          content.trim()
        }
      }
    }
}
