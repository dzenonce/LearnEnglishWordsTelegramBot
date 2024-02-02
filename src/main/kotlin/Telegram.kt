import kotlinx.serialization.json.Json
import model.*
import model.serialization.Document
import model.serialization.Response
import model.serialization.Update
import java.io.File

var currentQuestion: Question? = null
val json = Json { ignoreUnknownKeys = true }

fun main(args: Array<String>) {
    println("Bot is running")

    val botToken = args[0]
    val telegram = TelegramBotService(
        botToken = botToken,
        json = json,
    )

    val trainers = try {
        HashMap<Long, LearnWordsTrainer>()
    } catch (e: Error) {
        println("Невозможно создать экземпляры hashMap trainers ${e.message}")
        return
    }

    var lastUpdateId = 0L
    while (true) {
        Thread.sleep(PAUSE_TELEGRAM_GET_UPDATE)
        val response: Response? = telegram.getUpdates(lastUpdateId)

        if (response?.result?.isEmpty() == true) continue
        val sortedUpdate = response?.result?.sortedBy { it.updateId }
        sortedUpdate?.forEach {
            handleUpdate(
                update = it,
                json = json,
                trainers = trainers,
                botToken = botToken,
            )
        }
        lastUpdateId = sortedUpdate?.last()?.updateId?.plus(1) ?: 0L
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
        json = json,
    )

    val chatId: Long = update.message?.chat?.id ?: update.callbackQuery?.message?.chat?.id ?: return
    val message: String = update.message?.text.toString()
    val callbackData: String = update.callbackQuery?.data.toString()
    val callbackQueryId: String = update.callbackQuery?.id.toString()
    val document: Document? = update.message?.document
    val messageId: Long = update.message?.messageId ?: 0L

    val trainer = trainers.getOrPut(chatId) {
        LearnWordsTrainer(
            fileName = "$chatId$FILE_TEXT_EXT",
            numberOfCountOption = 4,
            requiredCountCorrectAnswer = 3,
        )
    }

    if (document != null) {
        getUserWordsFileAndSave(
            chatId = chatId,
            document = document,
            telegram = telegram,
        )
        trainer.reloadDictionary()
        telegram.deleteMessage(
            chatId = chatId,
            messageId = messageId
        )
    }

    val mainMenuBody = getBodyMainMenu(chatId)
    if (message == "/start") telegram.sendMenu(mainMenuBody)

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

        CALLBACK_MENU_STATISTICS_CLICKED -> {
            telegram.sendMenu(
                rawMessageBody = getBodyStatisticsMenu(
                    chatId = chatId,
                ),
            )
            telegram.answerCallbackQuery(callbackQueryId)
        }

        CALLBACK_LOAD_WORDS_FILE_CLICKED -> {
            telegram.sendMenu(
                rawMessageBody = getBodyUploadWordsListMenu(chatId)
            )
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

        CALLBACK_EXIT_MAIN_MENU_CLICKED -> {
            telegram.sendMenu(mainMenuBody)
            telegram.answerCallbackQuery(callbackQueryId)
        }

        CALLBACK_GO_BACK_CLICKED -> {
            telegram.sendMenu(mainMenuBody)
            telegram.answerCallbackQuery(callbackQueryId)
        }

    }

    if (callbackData.startsWith(CALLBACK_ANSWER_PREFIX)) {
        val answerIndex = callbackData.substringAfter(CALLBACK_ANSWER_PREFIX).toInt()
        val checkAnswerResult = trainer.checkAnswer(answerIndex)
        when (checkAnswerResult) {
            true -> telegram.sendMessage(
                chatId = chatId,
                text = TEXT_ANSWER_CORRECT
            )

            false -> telegram.sendMessage(
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

fun getStatisticsString(statistics: Statistics?) =
    "Выучено ${statistics?.countLearnedWord} из ${statistics?.countWords} слов | ${statistics?.percentLearnedWord}%"

fun checkNextQuestionAndSend(
    json: Json,
    trainer: LearnWordsTrainer,
    chatId: Long,
    botToken: String,
    callbackQueryId: String,
): Question? {
    val question: Question? = trainer.getNextQuestion()
    val telegram = TelegramBotService(
        botToken = botToken,
        json = json,
    )
    if (question == null)
        telegram.sendMessage(
            chatId = chatId,
            text = TEXT_ALL_WORDS_LEARNED
        )
    else {
        telegram.sendMenu(
            rawMessageBody = getBodyLearnWordsMenu(
                chatId = chatId,
                question = question,
            )
        )
        telegram.answerCallbackQuery(callbackQueryId)
    }
    return question
}

fun getUserWordsFileAndSave(chatId: Long, document: Document, telegram: TelegramBotService) {
    val userCustomTempFile = File("$chatId${document.fileName}")

    val fileResponse = telegram.getFileInfo(
        getBodyRequestFileInfo(document.fileId)
    )
    fileResponse?.response.let { tgFile ->
        if (userCustomTempFile.exists()) {
            telegram.sendMessage(
                chatId = chatId,
                text = TEXT_FILE_ALREADY_EXIST,
            )
            return
        }
        val file =
            telegram.downloadFile(tgFile?.filePath)
        userCustomTempFile.outputStream().use { outputStream ->
            file?.use { inputStream ->
                inputStream.copyTo(outputStream, 16 * 1024)
            }
        }
        telegram.sendMessage(
            chatId = chatId,
            text = TEXT_FILE_LOADED_SUCCESSFUL,
        )
    }
    userCustomTempFile.readLines()
        .forEach { File("$chatId$FILE_TEXT_EXT").appendText("\n$it") }
    userCustomTempFile.delete()
}