import model.serialization.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import model.*

var currentQuestion: Question? = null

fun main(args: Array<String>) {

    val botToken = args[0]
    val telegram = TelegramBotService(
        botToken = botToken,
    )

    val trainers = try {
        HashMap<Long, LearnWordsTrainer>()
    } catch (e: Error) {
        println("Невозможно создать экземпляры hashMap trainers ${e.message}")
        return
    }

    var lastUpdateId = 0L

    val json = Json { ignoreUnknownKeys = true }

    while (true) {
        Thread.sleep(1500)
        val responseString =
            try {
                telegram.getUpdates(lastUpdateId)
            } catch (e: Error) {
                println("Get Updates with error: ${e.message}")
                continue
            }

        val response: Response = json.decodeFromString(responseString)
        if (response.result.isEmpty()) continue
        println(responseString)
        val sortedUpdate = response.result.sortedBy { it.updateId }
        sortedUpdate.forEach {
            handleUpdate(
                update = it,
                json = json,
                trainers = trainers,
                botToken = botToken,
            )
        }
        lastUpdateId = sortedUpdate.last().updateId + 1
    }
}

fun handleUpdate(
    update: Update,
    json: Json,
    trainers: HashMap<Long, LearnWordsTrainer>,
    botToken: String,
) {

    val telegram = TelegramBotService(
        botToken = botToken,
    )

    val message: String = update.message?.text.toString()
    val chatId: Long = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return
    val callbackData: String = update.callbackQuery?.data.toString()
    val callbackQueryId: String = update.callbackQuery?.id.toString()

    val trainer = trainers.getOrPut(chatId) {
        LearnWordsTrainer(
            fileName = "$chatId.txt",
            numberOfCountOption = 4,
            requiredCountCorrectAnswer = 3,
        )
    }

    val mainMenuBody = json.encodeToString(getMainMenu(chatId))
    if (message == "/start")
        try {
            telegram.sendMenu(mainMenuBody)
        } catch (e: Error) {
            println("Ошибка при отправке главного меню: ${e.message}")
            return
        }

    when (callbackData.lowercase()) {
        CALLBACK_LEARN_WORDS_CLICKED -> {
            currentQuestion =
                checkNextQuestionAndSend(
                    json = json,
                    trainer = trainer,
                    chatId = chatId,
                    botToken = botToken,
                    callbackQueryId = callbackQueryId
                )
            telegram.answerCallbackQuery(callbackQueryId)
        }

        CALLBACK_MENU_STATISTICS_CLICKED -> try {
            telegram.sendMenu(
                menuBody = json.encodeToString(getStatisticsMenu(chatId = chatId))
            )
            telegram.answerCallbackQuery(callbackQueryId)
        } catch (e: Error) {
            println("Ошибка при отправке меню статистики: ${e.message}")
            return
        }

        CALLBACK_EXIT_MAIN_MENU_CLICKED -> try {
            telegram.sendMenu(mainMenuBody)
            telegram.answerCallbackQuery(callbackQueryId)
        } catch (e: Error) {
            println("Ошибка при выходе в главное меню: ${e.message}")
            return
        }

        CALLBACK_GO_BACK_CLICKED -> try {
            telegram.sendMenu(mainMenuBody)
            telegram.answerCallbackQuery(callbackQueryId)
        } catch (e: Error) {
            println("Ошибка при нажатии кнопки назад: ${e.message}")
            return
        }

        CALLBACK_SHOW_STATISTICS_CLICKED -> try {
            telegram.sendMessage(
                chatId = chatId,
                text = getStatisticsString(trainer.getStatistics())
            )
            telegram.answerCallbackQuery(callbackQueryId)
        } catch (e: Error) {
            println("Ошибка при отправке статистики: ${e.message}")
            return
        }

        CALLBACK_RESET_STATISTICS_CLICKED -> try {
            trainer.resetProgress()
            telegram.answerCallbackQuery(callbackQueryId, text = TEXT_COMPLETE_RESET_STATISTICS)
        } catch (e: Error) {
            println("Ошибка при сбросе статистики: ${e.message}")
            return
        }
    }

    if (callbackData.startsWith(CALLBACK_ANSWER_PREFIX)) {
        val answerIndex = callbackData.substringAfter(CALLBACK_ANSWER_PREFIX).toInt()
        val checkAnswerResult = trainer.checkAnswer(answerIndex)
        when (checkAnswerResult) {
            true -> try {
                telegram.sendMessage(
                    chatId = chatId,
                    text = TEXT_ANSWER_CORRECT
                )
            } catch (e: Error) {
                println("Ошибка при отправке корректного результата ответа: ${e.message}")
                return
            }

            false -> try {
                telegram.sendMessage(
                    chatId = chatId,
                    text = "$TEXT_ANSWER_WRONG : ${currentQuestion?.correctWord?.original} - ${currentQuestion?.correctWord?.translate}"
                )
            } catch (e: Error) {
                println("Ошибка при отправке результата неправильного ответа: ${e.message}")
                return
            }
        }
        try {
            currentQuestion =
                checkNextQuestionAndSend(
                    trainer = trainer,
                    chatId = chatId,
                    botToken = botToken,
                    json = json,
                    callbackQueryId = callbackQueryId,
                )
        } catch (e: Error) {
            println("Ошибра при отправке списка слов для изучения: ${e.message}")
            return
        }
    }
}

fun getStatisticsString(statistics: Statistics) =
    "Выучено ${statistics.countLearnedWord} из ${statistics.countWords} слов | ${statistics.percentLearnedWord}%"

fun checkNextQuestionAndSend(
    json: Json,
    trainer: LearnWordsTrainer,
    chatId: Long,
    botToken: String,
    callbackQueryId: String
): Question? {
    val question: Question? = trainer.getNextQuestion()
    val telegram = TelegramBotService(botToken)
    if (question == null)
        try {
            telegram.sendMessage(
                chatId = chatId,
                text = TEXT_ALL_WORDS_LEARNED
            )
        } catch (e: Error) {
            println("Ошибка при отправке сообщения \"$TEXT_ALL_WORDS_LEARNED\" ${e.message}")
        }
    else {
        try {
            telegram.sendMenu(
                menuBody = json.encodeToString(
                    getLearnWordsMenuBody(
                        chatId = chatId,
                        question = question,
                    )
                )
            )
            telegram.answerCallbackQuery(callbackQueryId)
        } catch (e: Error) {
            println("Ошибка при отправке меню изучения слов: ${e.message}")
        }
    }
    return question
}