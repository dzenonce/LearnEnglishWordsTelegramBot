package courcework

fun main(args: Array<String>) {

    val telegramRequest = TelegramBotService(
        botToken = args[0],
    )

    var updateId: Int? = 0

    val regexFindUpdateId: Regex = "\"update_id\":(.+?),".toRegex()
    val regexFindChatId: Regex = "\"id\":(.+?),".toRegex()
    val regexFindMessageText: Regex = "\"text\":\"(.+?)\"".toRegex()

    while (true) {
        Thread.sleep(2000)
        val updates = telegramRequest.getUpdates(updateId)
        println(updates)

        val updateIdString = findRegexQuery(regexFindUpdateId, updates)
        updateId = updateIdString?.toIntOrNull()?.plus(1)

        val chatIdString: String? = findRegexQuery(regexFindChatId, updates)
        val chatId: Int? = chatIdString?.toIntOrNull()

        val text: String? = findRegexQuery(regexFindMessageText, updates)
        if (text?.isNotBlank() != null) {
            val sentResult = telegramRequest.sendMessage(
                chatId,
                text = text
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