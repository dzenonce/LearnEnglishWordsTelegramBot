package model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import model.serialization.GetFileRequest
import model.serialization.GetFileResponse
import model.serialization.Response
import model.serialization.SendMessageRequest
import java.io.InputStream
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TelegramBotService(
    private val botToken: String,
    private val json: Json,
) {

    fun getUpdates(updateId: Long): Response {
        val url = "$API_TELEGRAM_URL$botToken/getUpdates?offset=$updateId"
        val responseHttpRequest = sendHttpRequest(url)
        println(responseHttpRequest)
        return json.decodeFromString(responseHttpRequest)
    }

    fun sendMessage(chatId: Long, text: String): String {
        val encodedText = URLEncoder.encode(text, "UTF-8")
        val url = "$API_TELEGRAM_URL$botToken/sendMessage?chat_id=$chatId&text=$encodedText"
        return sendHttpRequest(url)
    }

    fun deleteMessage(chatId: Long, messageId: Long): String {
        val url = "$API_TELEGRAM_URL$botToken/deleteMessage?chat_id=$chatId&message_id=$messageId"
        return sendHttpRequest(url)
    }

    fun sendMenu(rawMenuBody: SendMessageRequest): String {
        val url = "$API_TELEGRAM_URL$botToken/sendMessage"
        return sendPostHttpRequest(
            url = url,
            body = json.encodeToString(rawMenuBody)
        )
    }

    fun answerCallbackQuery(callbackQueryId: String? = "", text: String = "", showAlert: Boolean = false): String {
        val encodedText = URLEncoder.encode(text, "UTF-8")
        val url =
            "$API_TELEGRAM_URL$botToken/answerCallbackQuery?callback_query_id=$callbackQueryId&text=$encodedText&show_alert=$showAlert"
        return sendHttpRequest(url)
    }

    fun getFileInfo(rawFileRequestBody: GetFileRequest): GetFileResponse {
        val url = "$API_TELEGRAM_URL$botToken/getFile"
        val fileResponseString = try {
            sendPostHttpRequest(
                url = url,
                body = json.encodeToString(rawFileRequestBody)
            )
        } catch (e: Error) {
            println("Ошибка получения информации о файле: ${e.message}")
        }.toString()
        return json.decodeFromString(fileResponseString)
    }

    fun downloadFile(filePath: String?): InputStream? {
        val url = "https://api.telegram.org/file/bot$botToken/$filePath"
        return sendGetHttpRequest(url)
    }

    private fun sendHttpRequest(url: String): String {
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    private fun sendPostHttpRequest(url: String, body: String): String {
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder()
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .uri(URI.create(url))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    private fun sendGetHttpRequest(url: String): InputStream? {
        val request: HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build()
        val response: HttpResponse<InputStream> = HttpClient.newHttpClient()
            .send(request, HttpResponse.BodyHandlers.ofInputStream())
        println("Status Code Response: ${response.statusCode()}")
        return response.body()
    }

}

private const val API_TELEGRAM_URL = "https://api.telegram.org/bot"