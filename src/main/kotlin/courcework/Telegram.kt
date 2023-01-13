package courcework

import model.*

fun main(args: Array<String>) {

    val telegram = TelegramBotService(
        botToken = args[0],
    )

    val trainer = try {
        LearnWordsTrainer(
            numberOfCountOption = 4,
            requiredCountCorrectAnswer = 3,
        )
    } catch (e: Exception) {
        println("Файл не найден")
        return
    }

    var updateId: Int? = 0
    var currentQuestion: Question? = null

    while (true) {
        Thread.sleep(2000)
        val updates = telegram.getUpdates(updateId)
        println("Updates: \n$updates")

        val updateIdString = findRegexQuery(getRegexQuery(UPDATE_ID), updates)
        updateId = updateIdString?.toIntOrNull()?.plus(1)
        val chatIdString: String? = findRegexQuery(getRegexQuery(CHAT_ID), updates)
        val chatId: Int? = chatIdString?.toIntOrNull()
        val message: String? = findRegexQuery(getRegexQuery(MESSAGE_TEXT), updates)

        val menuBody = getMainMenuBody(chatId)
        if (message == "/start") telegram.sendMenu(menuBody)

        val callbackData: String = findRegexQuery(getRegexQuery(CALLBACK_DATA), updates).toString()

        when (callbackData.lowercase()) {
            LEARN_WORDS_CLICKED ->
                currentQuestion =
                    checkNextQuestionAndSend(
                        trainer = trainer,
                        chatId = chatId,
                        botToken = args[0]
                    )

            STATISTICS_CLICKED ->
                telegram.sendMessage(
                    chatId = chatId,
                    text = getStatisticsString(trainer.getStatistics())
                )
        }

        if (callbackData.startsWith(CALLBACK_DATA_ANSWER_PREFIX)) {
            val answerIndex = callbackData.substringAfter(CALLBACK_DATA_ANSWER_PREFIX).toInt()
            val checkAnswerResult = trainer.checkAnswer(answerIndex)
            when (checkAnswerResult) {
                true ->
                    telegram.sendMessage(
                        chatId = chatId,
                        text = CORRECT_ANSWER
                    )

                false ->
                    telegram.sendMessage(
                        chatId = chatId,
                        text = WRONG_ANSWER + " : ${currentQuestion?.correctWord?.original} - ${currentQuestion?.correctWord?.translate}"
                    )
            }
            currentQuestion =
                checkNextQuestionAndSend(
                    trainer = trainer,
                    chatId = chatId,
                    botToken = args[0]
                )
        }
    }
}

fun findRegexQuery(regex: Regex, context: String): String? {
    val matchResult: MatchResult? = regex.find(context)
    val group = matchResult?.groups
    return group?.get(1)?.value
}

fun getRegexQuery(targetQuery: String): Regex {
    return when (targetQuery) {
        "updateId" -> "\"update_id\":(\\d+),".toRegex()
        "chatId" -> "\"chat\":\\{\"id\":(\\d+)".toRegex()
        "messageText" -> "\"text\":\"(.+?)\"".toRegex()
        "callbackData" -> "\"data\":\"(.+?)\"".toRegex()
        else -> "".toRegex()
    }
}

fun getStatisticsString(statistics: Statistics) =
    "Выучено ${statistics.countLearnedWord} из ${statistics.countWords} слов | ${statistics.percentLearnedWord}%"

fun getUserQuestionBody(chatId: Int?, question: Question): String {
    val buttonsList: MutableList<String> = mutableListOf()
    question.fourUnlearnedWords.mapIndexed { index, word ->
        buttonsList.add(
            """
            |[
			|	{
			|	"text": "${word.translate}",
			|	"callback_data": "${CALLBACK_DATA_ANSWER_PREFIX + index}"
			|	}
			|]
            """.trimMargin()
        )
    }
    return getLearnWordsMenuBody(chatId, question, buttonsList)
}

fun checkNextQuestionAndSend(trainer: LearnWordsTrainer, chatId: Int?, botToken: String): Question? {
    val question: Question? = trainer.getNextQuestion()
    if (question == null)
        TelegramBotService(botToken).sendMessage(
            chatId = chatId,
            text = ALL_WORDS_LEARNED
        )
    else
        TelegramBotService(botToken).sendMenu(
            getUserQuestionBody(
                chatId = chatId,
                question = question
            )
        )
    return question
}