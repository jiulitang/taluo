package com.example.data.remote

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OpenAiModelsResponse(
  val data: List<OpenAiModelItem> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class OpenAiModelItem(
  val id: String,
  val owned_by: String? = null,
)

@JsonClass(generateAdapter = true)
data class OpenAiChatMessage(
  val role: String,
  val content: String,
)

@JsonClass(generateAdapter = true)
data class OpenAiChatRequest(
  val model: String,
  val messages: List<OpenAiChatMessage>,
  val temperature: Double = 0.75,
  val max_tokens: Int? = 1800,
)

@JsonClass(generateAdapter = true)
data class OpenAiChatResponse(
  val id: String? = null,
  val choices: List<OpenAiChoice> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class OpenAiChoice(
  val index: Int = 0,
  val message: OpenAiChatMessage? = null,
  val finish_reason: String? = null,
)

@JsonClass(generateAdapter = true)
data class OpenAiErrorEnvelope(
  val error: OpenAiErrorDetail? = null,
)

@JsonClass(generateAdapter = true)
data class OpenAiErrorDetail(
  val message: String? = null,
  val type: String? = null,
  val code: String? = null,
)
