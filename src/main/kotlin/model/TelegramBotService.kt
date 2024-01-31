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

    fun getUpdates(updateId: Long): Response? {
        val url = "$API_TELEGRAM_URL$botToken/getUpdates?offset=$updateId"
        val responseHttpRequest = sendHttpRequest(url)
        println(responseHttpRequest)
        return responseHttpRequest?.let { json.decodeFromString(it) }
    }

    fun sendMessage(chatId: Long, text: String): String? {
        val encodedText = URLEncoder.encode(text, "UTF-8")
        val url = "$API_TELEGRAM_URL$botToken/sendMessage?chat_id=$chatId&text=$encodedText"
        return sendHttpRequest(url)
    }

    fun deleteMessage(chatId: Long, messageId: Long): String? {
        val url = "$API_TELEGRAM_URL$botToken/deleteMessage?chat_id=$chatId&message_id=$messageId"
        return sendHttpRequest(url)
    }

    fun sendMenu(rawMessageBody: SendMessageRequest): String? {
        val url = "$API_TELEGRAM_URL$botToken/sendMessage"
        return sendPostHttpRequest(
            url = url,
            body = json.encodeToString(rawMessageBody)
        )
    }

    fun answerCallbackQuery(callbackQueryId: String? = "", text: String = "", showAlert: Boolean = false): String? {
        val encodedText = URLEncoder.encode(text, "UTF-8")
        val url =
            "$API_TELEGRAM_URL$botToken/answerCallbackQuery?callback_query_id=$callbackQueryId&text=$encodedText&show_alert=$showAlert"
        return sendHttpRequest(url)
    }

    fun getFileInfo(rawFileRequestBody: GetFileRequest): GetFileResponse? {
        val url = "$API_TELEGRAM_URL$botToken/getFile"
        val fileResponseString = sendPostHttpRequest(
            url = url,
            body = json.encodeToString(rawFileRequestBody)
        )
        return fileResponseString?.let { json.decodeFromString(it) }
    }

    fun downloadFile(filePath: String?): InputStream? {
        val url = "https://api.telegram.org/file/bot$botToken/$filePath"
        return sendGetHttpRequest(url)
    }

    private fun sendHttpRequest(url: String): String? {
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .build()
        val response: HttpResponse<String> = try {
            client.send(request, HttpResponse.BodyHandlers.ofString())
        } catch (e: Error) {
            println("Ошибка отправки HttpRequest: ${e.message}")
            return null
        }
        return response.body()
    }

    private fun sendPostHttpRequest(url: String, body: String): String? {
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder()
            .header("Content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .uri(URI.create(url))
            .build()
        val response: HttpResponse<String> = try {
            client.send(request, HttpResponse.BodyHandlers.ofString())
        } catch (e: Error) {
            println("Ошибка отправки Post запроса: ${e.message}")
            return null
        }
        return response.body()
    }

    private fun sendGetHttpRequest(url: String): InputStream? {
        val request: HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build()
        val response: HttpResponse<InputStream> = try {
            HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofInputStream())
        } catch (e: Error) {
            println("Ошибка отправки Get запроса: ${e.message}")
            return null
        }
        println("Status Code Response: ${response.statusCode()}")
        return response.body()
    }

}

private const val API_TELEGRAM_URL = "https://api.telegram.org/bot"