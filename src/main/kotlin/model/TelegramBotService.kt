package model

import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TelegramBotService(
    private val botToken: String,
) {

    fun getUpdates(updateId: Int?): String {
        val url = "$API_TELEGRAM_URL$botToken/getUpdates?offset=$updateId"
        return sendHttpRequest(url)
    }

    fun sendMessage(chatId: Int?, text: String): String {
        val encodedText = URLEncoder.encode(text, "UTF-8")
        val url = "$API_TELEGRAM_URL$botToken/sendMessage?chat_id=$chatId&text=$encodedText"
        return sendHttpRequest(url)
    }

    fun sendMenu(menuBody: String): String {
        val url = "$API_TELEGRAM_URL$botToken/sendMessage"
        return sendPostHttpRequest(
            url = url,
            body = menuBody
        )
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

}

const val API_TELEGRAM_URL = "https://api.telegram.org/bot"