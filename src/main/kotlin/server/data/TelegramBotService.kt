package server.data

import constants.API_TELEGRAM_URL
import database.sqlite.DatabaseControl
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import server.serialization.*
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
        println("ResponseUpdate $responseHttpRequest")
        return responseHttpRequest?.let { json.decodeFromString(it) }
    }

    fun deleteMessage(chatId: Long, messageId: Long): String? {
        val url = "$API_TELEGRAM_URL$botToken/deleteMessage?chat_id=$chatId&message_id=$messageId"
        return sendHttpRequest(url)
    }

    fun editMessage(chatId: Long, rawMessageBody: SendMessageRequest): BotResponse? {
        val lastBotMessageId = DatabaseControl().getLastBotMessageId(chatId)
        val url = "$API_TELEGRAM_URL$botToken/editMessageText?message_id=$lastBotMessageId"
        val response: BotResponse? = sendPostHttpRequest(
            url = url,
            body = json.encodeToString(rawMessageBody)
        )?.let { json.decodeFromString(it) }

        response.let { botResponse ->
            DatabaseControl().saveLastBotMessageId(
                chatId = botResponse?.result?.chat?.id,
                lastBotId = botResponse?.result?.botMessageId
            )
        }
        return response
    }

    fun sendMessage(chatId: Long, text: String): BotResponse? {
        val encodedText = URLEncoder.encode(text, "UTF-8")
        val url = "$API_TELEGRAM_URL$botToken/sendMessage?chat_id=$chatId&text=$encodedText"
        val response: BotResponse? = sendHttpRequest(url)?.let { json.decodeFromString(it) }

        response.let { botResponse ->
            DatabaseControl().saveLastBotMessageId(
                chatId = botResponse?.result?.chat?.id,
                lastBotId = botResponse?.result?.botMessageId
            )
        }
        return response
    }

    fun sendMenu(rawMessageBody: SendMessageRequest): BotResponse? {
        val url = "$API_TELEGRAM_URL$botToken/sendMessage"
        val response: BotResponse? = sendPostHttpRequest(
            url = url,
            body = json.encodeToString(rawMessageBody)
        )?.let { json.decodeFromString(it) }

        response.let { botResponse ->
            DatabaseControl().saveLastBotMessageId(
                chatId = botResponse?.result?.chat?.id,
                lastBotId = botResponse?.result?.botMessageId
            )
        }
        return response
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

    // TODO Кажется эти запросы могут быть в отдельном файле как интерфейс
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