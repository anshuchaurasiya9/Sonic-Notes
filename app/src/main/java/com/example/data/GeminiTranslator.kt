package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiTranslator {
    private const val TAG = "GeminiTranslator"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Supported target languages for translation and speech input
    val LANGUAGES = listOf(
        LanguageOption("en", "English", "en-US"),
        LanguageOption("es", "Español (Spanish)", "es-ES"),
        LanguageOption("hi", "हिन्दी (Hindi)", "hi-IN"),
        LanguageOption("fr", "Français (French)", "fr-FR"),
        LanguageOption("de", "Deutsch (German)", "de-DE"),
        LanguageOption("ja", "日本語 (Japanese)", "ja-JP"),
        LanguageOption("it", "Italiano (Italian)", "it-IT"),
        LanguageOption("zh", "中文 (Chinese)", "zh-CN"),
        LanguageOption("pt", "Português (Portuguese)", "pt-PT"),
        LanguageOption("ar", "العربية (Arabic)", "ar-SA")
    )

    suspend fun translate(title: String, content: String, targetLanguage: String): Pair<String, String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "API Key is missing or invalid.")
            return@withContext Pair(title, content + "\n\n(Translation failed: Gemini API key is not configured in the Secrets panel)")
        }

        val prompt = """
            You are a translation assistant. Translate the following note (title and body content) into $targetLanguage.
            Respond ONLY with a valid JSON object matching this schema, do NOT include markdown block wrapping or other text:
            {
              "title": "translated title here",
              "content": "translated content here"
            }
            
            Note to translate:
            Title: ${title.replace("\"", "\\\"")}
            Content: ${content.replace("\"", "\\\"")}
        """.trimIndent()

        val jsonRequest = JSONObject().apply {
            put("contents", org.json.JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", org.json.JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
            })
        }

        val requestBody = jsonRequest.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val url = "$BASE_URL?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "Error from Gemini service: ${response.code} $errBody")
                    return@withContext Pair(title, content + "\n\n(Translation failed: API returned error ${response.code})")
                }

                val responseBody = response.body?.string() ?: return@withContext Pair(title, content)
                val jsonResponse = JSONObject(responseBody)
                val textResponse = jsonResponse
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")

                val parsedTranslation = JSONObject(textResponse)
                val translatedTitle = parsedTranslation.optString("title", title)
                val translatedContent = parsedTranslation.optString("content", content)

                Pair(translatedTitle, translatedContent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during translation: ", e)
            Pair(title, content + "\n\n(Translation failed: ${e.localizedMessage})")
        }
    }
}

data class LanguageOption(val code: String, val name: String, val bcp47: String)
