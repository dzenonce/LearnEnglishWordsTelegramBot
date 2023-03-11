package interfaces.database

import model.User

interface IDatabaseControl {
    fun initDatabase()
    fun loadStandardWords(standardWordsFileName: String)
    fun createCustomWordsTable(userId: Long)
    fun addNewUser(user: User)

    fun saveLastBotMessageId(chatId: Long, lastBotId: Long)
    fun saveResultBotMessageId(chatId: Long, resultBotId: Long)
    fun getLastBotMessageId(chatId: Long): Long
    fun getResultBotMessageId(chatId: Long): Long
    fun resetResultMessageId(chatId: Long)
}