package com.resurface.resurface.data.generation

import com.resurface.resurface.di.GeminiApiKey
import com.resurface.resurface.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

// DTOs mínimos da API generateContent do Gemini.
@Serializable private data class GenRequest(val contents: List<Content>, val generationConfig: GenConfig)
@Serializable private data class Content(val parts: List<Part>)
@Serializable private data class Part(val text: String)
@Serializable private data class GenConfig(
    @SerialName("maxOutputTokens") val maxOutputTokens: Int,
    val temperature: Double,
)
@Serializable private data class GenResponse(val candidates: List<Candidate> = emptyList())
@Serializable private data class Candidate(val content: Content? = null)

/**
 * Cliente REST do Gemini (Flash-Lite, proxy do Nano — D-2/D-3). Faz o POST off-main; qualquer
 * falha (chave vazia, rede, HTTP, parse, vazio) devolve null → o chamador cai no fallback.
 */
@Singleton
class GeminiClient @Inject constructor(
    @GeminiApiKey private val apiKey: String,
    @IoDispatcher private val io: CoroutineDispatcher,
) {
    private val http = OkHttpClient.Builder()
        .callTimeout(8, TimeUnit.SECONDS)
        .build()
    private val json = Json { ignoreUnknownKeys = true }

    /** Gera texto a partir do [prompt]; null se não deu. */
    suspend fun generate(prompt: String, maxTokens: Int): String? = withContext(io) {
        if (apiKey.isBlank()) return@withContext null
        runCatching {
            val body = json.encodeToString(
                GenRequest.serializer(),
                GenRequest(
                    contents = listOf(Content(listOf(Part(prompt)))),
                    generationConfig = GenConfig(maxOutputTokens = maxTokens, temperature = 0.9),
                ),
            ).toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent?key=$apiKey")
                .post(body)
                .build()

            http.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) return@runCatching null
                val text = resp.body?.string() ?: return@runCatching null
                json.decodeFromString(GenResponse.serializer(), text)
                    .candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
            }
        }.getOrNull()
    }

    private companion object {
        // Alias "flash-lite-latest": sempre o Flash-Lite atual (hoje gemini-3.5-flash-lite),
        // proxy fiel do Nano e imune à depreciação de versões fixas (D-2/D-3).
        const val MODEL = "gemini-flash-lite-latest"
    }
}
