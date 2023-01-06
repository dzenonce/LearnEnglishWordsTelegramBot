package courcework

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class Telegram(
    private val botToken: String,
) {

    fun getUpdates(updateId: Int): String {
        val url = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updateId"

        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

}

fun main(args: Array<String>) {

    val telegramRequest = Telegram(
        botToken = args[0],
    )

    var updateId = 0

    while (true) {
        Thread.sleep(2000)
        val updates = telegramRequest.getUpdates(updateId)
        println(updates)
        val lastUpdateId = updates.lastIndexOf("update_id")
        val endUpdateId = updates.lastIndexOf(",\n\"message\"")
        if (lastUpdateId == -1 || endUpdateId == -1) continue
        val updateIdString = updates.substring(lastUpdateId + 11, endUpdateId)
        updateId = updateIdString.toInt() + 1
    }

}
