package server.data

import constants.API_TELEGRAM_URL
import interfaces.data.IBotApiService
import interfaces.data.INetworkService
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import server.serialization.*
import java.io.InputStream
import java.net.URLEncoder

class TelegramBotService(
    private val botToken: String,
    private val json: Json,
) : IBotApiService {

    private val networkService: INetworkService = NetworkService()

    override fun getUpdates(updateId: Long): Response? {
        val url = "$API_TELEGRAM_URL$botToken/getUpdates?offset=$updateId"
        val responseHttpRequest = networkService.sendHttpRequest(url)
        println("[*] response update $responseHttpRequest")
        return responseHttpRequest?.let { json.decodeFromString(it) }
    }

    override fun deleteMessage(chatId: Long, messageId: Long): String? {
        val url = "$API_TELEGRAM_URL$botToken/deleteMessage?chat_id=$chatId&message_id=$messageId"
        return networkService.sendHttpRequest(url)
    }

    override fun editMessage(chatId: Long, messageIdToEdit: Long, rawMessageBody: SendMessageRequest): BotResponse? {
        val url = "$API_TELEGRAM_URL$botToken/editMessageText?message_id=$messageIdToEdit"
        return networkService.sendPostHttpRequest(
            url = url,
            body = json.encodeToString(rawMessageBody)
        )?.let { json.decodeFromString(it) }
    }

    override fun sendMessage(chatId: Long, text: String): BotResponse? {
        val encodedText = URLEncoder.encode(text, "UTF-8")
        val url = "$API_TELEGRAM_URL$botToken/sendMessage?chat_id=$chatId&text=$encodedText"
        return networkService.sendHttpRequest(url)?.let { json.decodeFromString(it) }
    }

    override fun sendMenu(rawMessageBody: SendMessageRequest): BotResponse? {
        val url = "$API_TELEGRAM_URL$botToken/sendMessage"
        return networkService.sendPostHttpRequest(
            url = url,
            body = json.encodeToString(rawMessageBody)
        )?.let { json.decodeFromString(it) }
    }

    override fun getFileInfo(rawFileRequestBody: GetFileRequest): GetFileResponse? {
        val url = "$API_TELEGRAM_URL$botToken/getFile"
        val fileResponseString = networkService.sendPostHttpRequest(
            url = url,
            body = json.encodeToString(rawFileRequestBody)
        )
        return fileResponseString?.let { json.decodeFromString(it) }
    }

    override fun downloadFile(filePath: String?): InputStream? {
        val url = "https://api.telegram.org/file/bot$botToken/$filePath"
        return networkService.sendGetHttpRequest(url)
    }

    fun handleCallbackQuery(
        callbackQueryId: String? = "",
        text: String = "",
        showAlert: Boolean = false,
    ): String? {
        val encodedText = URLEncoder.encode(text, "UTF-8")
        val url =
            "$API_TELEGRAM_URL$botToken/answerCallbackQuery?callback_query_id=$callbackQueryId&text=$encodedText&show_alert=$showAlert"
        return networkService.sendHttpRequest(url)
    }

}