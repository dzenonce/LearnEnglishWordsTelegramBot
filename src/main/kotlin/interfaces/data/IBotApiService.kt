package interfaces.data

import server.serialization.*
import java.io.InputStream

interface IBotApiService {

    fun getUpdates(updateId: Long): Response?

    fun deleteMessage(chatId: Long, messageId: Long): String?
    fun editMessage(chatId: Long, messageIdToEdit: Long = 0L, rawMessageBody: SendMessageRequest): BotResponse?

    fun sendMessage(chatId: Long, text: String): BotResponse?
    fun sendMenu(rawMessageBody: SendMessageRequest): BotResponse?

    fun getFileInfo(rawFileRequestBody: GetFileRequest): GetFileResponse?
    fun downloadFile(filePath: String?): InputStream?

}