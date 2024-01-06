package courcework

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TelegramBotService(
    private val botToken: String,
) {

    fun getUpdates(updateId: Int?): String {
        val url = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updateId"
        return sendHttpRequest(url)
    }

    fun sendMessage(chatId: Int?, text: String?): String {
        val url = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=$text"
        return sendHttpRequest(url)
    }

    private fun sendHttpRequest(url: String): String {
        val client: HttpClient = HttpClient.newBuilder().build()
        val request: HttpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .build()
        val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

}

fun main(args: Array<String>) {

    val telegram = TelegramBotService(
        botToken = args[0],
    )

    var updateId: Int? = 0

    while (true) {
        Thread.sleep(2000)
        val updates = telegram.getUpdates(updateId)
        println(updates)

        val updateIdString = findRegexQuery("\"update_id\":(.+?),".toRegex(), updates)
        updateId = updateIdString?.toIntOrNull()?.plus(1)

        val chatIdString: String? = findRegexQuery("\"id\":(.+?),".toRegex(), updates)
        val chatId: Int? = chatIdString?.toIntOrNull()

        val text: String? = findRegexQuery("\"text\":\"(.+?)\"".toRegex(), updates)
        if (text?.isNotBlank() != null) {
            val sentResult = telegram.sendMessage(
                chatId,
                text = decodeUnicode(text)
            )
            println(sentResult)
        }
    }

}

fun findRegexQuery(regex: Regex, context: String): String? {
    val matchResult: MatchResult? = regex.find(context)
    val group = matchResult?.groups
    return group?.get(1)?.value
}

fun decodeUnicode(unicodeMessage: String): String {
    val regex = Regex("\\\\u([0-9A-Fa-f]{4})")

    return regex.replace(unicodeMessage) {
        val codePoint = it.groupValues[1].toInt(16)
        codePoint.toChar().toString()
    }
}
