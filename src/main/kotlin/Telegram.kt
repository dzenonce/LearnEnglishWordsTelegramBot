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
        val responseString = telegram.getUpdates(lastUpdateId)
        println(responseString)

        val response: Response = json.decodeFromString(responseString)
        if (response.result.isEmpty()) continue
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

fun handleUpdate(update: Update, json: Json, trainers: HashMap<Long, LearnWordsTrainer>, botToken: String) {

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
    if (message == "/start") telegram.sendMenu(mainMenuBody)

    when (callbackData.lowercase()) {
        CALLBACK_LEARN_WORDS_CLICKED -> {
            currentQuestion =
                checkNextQuestionAndSend(
                    trainer = trainer,
                    chatId = chatId,
                    botToken = botToken,
                    json = json,
                    callbackQueryId = callbackQueryId
                )
            telegram.answerCallbackQuery(callbackQueryId)
        }

        CALLBACK_MENU_STATISTICS_CLICKED -> {
            telegram.sendMenu(
                menuBody = json.encodeToString(getStatisticsMenu(chatId = chatId))
            )
            telegram.answerCallbackQuery(callbackQueryId)
        }

        CALLBACK_EXIT_MAIN_MENU_CLICKED -> {
            telegram.sendMenu(mainMenuBody)
            telegram.answerCallbackQuery(callbackQueryId)
        }

        CALLBACK_GO_BACK_CLICKED -> {
            telegram.sendMenu(mainMenuBody)
            telegram.answerCallbackQuery(callbackQueryId)
        }

        CALLBACK_SHOW_STATISTICS_CLICKED -> {
            telegram.sendMessage(
                chatId = chatId,
                text = getStatisticsString(trainer.getStatistics())
            )
            telegram.answerCallbackQuery(callbackQueryId)
        }

        CALLBACK_RESET_STATISTICS_CLICKED -> {
            trainer.resetProgress()
            telegram.answerCallbackQuery(callbackQueryId, text = TEXT_COMPLETE_RESET_STATISTICS)
        }
    }

    if (callbackData.startsWith(CALLBACK_ANSWER_PREFIX)) {
        val answerIndex = callbackData.substringAfter(CALLBACK_ANSWER_PREFIX).toInt()
        val checkAnswerResult = trainer.checkAnswer(answerIndex)
        when (checkAnswerResult) {
            true ->
                telegram.sendMessage(
                    chatId = chatId,
                    text = TEXT_ANSWER_CORRECT
                )

            false ->
                telegram.sendMessage(
                    chatId = chatId,
                    text = "$TEXT_ANSWER_WRONG : ${currentQuestion?.correctWord?.original} - ${currentQuestion?.correctWord?.translate}"
                )
        }
        currentQuestion =
            checkNextQuestionAndSend(
                trainer = trainer,
                chatId = chatId,
                botToken = botToken,
                json = json,
                callbackQueryId = callbackQueryId,
            )
    }
}

fun getStatisticsString(statistics: Statistics) =
    "Выучено ${statistics.countLearnedWord} из ${statistics.countWords} слов | ${statistics.percentLearnedWord}%"

fun checkNextQuestionAndSend(trainer: LearnWordsTrainer, chatId: Long, botToken: String, json: Json, callbackQueryId: String): Question? {
    val question: Question? = trainer.getNextQuestion()
    val telegram = TelegramBotService(botToken)
    if (question == null)
        telegram.sendMessage(
            chatId = chatId,
            text = TEXT_ALL_WORDS_LEARNED
        )
    else {
        telegram.sendMenu(
            menuBody = json.encodeToString(
                getLearnWordsMenuBody(
                    chatId = chatId,
                    question = question,
                )
            )
        )
        telegram.answerCallbackQuery(callbackQueryId)
    }
    return question
}