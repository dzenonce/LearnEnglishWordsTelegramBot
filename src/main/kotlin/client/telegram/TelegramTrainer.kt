package client.telegram

import client.telegram.ui.*
import constants.*
import database.sqlite.SqliteDatabaseControl
import database.sqlite.SqliteDatabaseUserDictionary
import interfaces.database.IDatabaseControl
import kotlinx.serialization.json.Json
import model.Question
import model.User
import server.data.TelegramBotService
import server.serialization.BotResponse
import server.serialization.Document
import server.serialization.Response
import server.serialization.Update
import server.trainer.LearnWordsTrainer
import java.io.File

var currentQuestion: Question? = null
val databaseControl: IDatabaseControl = SqliteDatabaseControl()

fun main(args: Array<String>) {
    println("[+] bot is running...")

    val botToken = args[0]
    val json = Json { ignoreUnknownKeys = true }
    val telegram = TelegramBotService(
        botToken = botToken,
        json = json,
    )

    databaseControl.initDatabase()
    databaseControl.loadStandardWords(TEXT_STANDARD_WORDS_FILE_NAME)

    val trainers = try {
        HashMap<Long, LearnWordsTrainer>()
    } catch (e: Error) {
        println("[-] trainers initialized failed: ${e.message}")
        return
    }

    var lastUpdateId = 0L

    while (true) {
        Thread.sleep(PAUSE_TELEGRAM_GET_UPDATE)
        val userResponse: Response? = telegram.getUpdates(lastUpdateId)

        if (userResponse?.result?.isEmpty() == true) continue
        val sortedUpdate = userResponse?.result?.sortedBy { it.updateId }
        sortedUpdate?.forEach { update ->
            handleUpdate(
                userUpdate = update,
                trainers = trainers,
                telegram = telegram,
            )
        }
        lastUpdateId = sortedUpdate?.last()?.updateId?.plus(1) ?: 0L
    }
}

fun handleUpdate(
    userUpdate: Update,
    trainers: HashMap<Long, LearnWordsTrainer>,
    telegram: TelegramBotService,
) {

    val chatId: Long = userUpdate.message?.chat?.id ?: userUpdate.callbackQuery?.message?.chat?.id ?: return
    val message: String = userUpdate.message?.text.toString()
    val callbackData: String = userUpdate.callbackQuery?.data.toString()
    val callbackQueryId: String = userUpdate.callbackQuery?.id.toString()
    val document: Document? = userUpdate.message?.document
    val messageId: Long = userUpdate.message?.messageId ?: 0L
    val username: String = userUpdate.message?.chat?.username ?: TEXT_UNSPECIFIED
    val date: Long = userUpdate.message?.date ?: 0L

    val trainer = trainers.getOrPut(chatId) {
        LearnWordsTrainer(
            countWordsForLearning = QUANTITY_WORDS_FOR_LEARNING,
            userDictionary = SqliteDatabaseUserDictionary(
                userId = chatId,
                minimalQuantityCorrectAnswer = QUANTITY_MINIMAL_CORRECT_ANSWER
            )
        )
    }

    if (document != null) {
        val rawWordsSet =
            getUserWordsFileAndSave(
                chatId = chatId,
                document = document,
                telegram = telegram,
            )
        trainer.loadCustomWordsFile(rawWordsSet)
        telegram.deleteMessage(
            chatId = chatId,
            messageId = messageId
        )
    }

    val mainMenuBody = getBodyMainMenu(chatId)
    if (message == "/start") {
        databaseControl.addNewUser(
            User(
                chatId = chatId,
                username = username,
                createdAt = date,
            )
        )
        telegram.sendMenu(mainMenuBody)?.saveLastBotMessageIdToDatabase()
    }

    when (callbackData.lowercase()) {
        CALLBACK_LEARN_WORDS_CLICKED -> {
            currentQuestion =
                checkNextQuestionAndSend(
                    trainer = trainer,
                    telegram = telegram,
                    chatId = chatId,
                    callbackQueryId = callbackQueryId,
                )
            telegram.handleCallbackQuery(callbackQueryId)
        }

        CALLBACK_MENU_STATISTICS_CLICKED -> {
            telegram.editMessage(
                chatId = chatId,
                messageIdToEdit = databaseControl.getLastBotMessageId(chatId),
                rawMessageBody = getBodyStatisticsMenu(
                    chatId = chatId,
                ),
            )?.saveLastBotMessageIdToDatabase()
            telegram.handleCallbackQuery(callbackQueryId)
        }

        CALLBACK_LOAD_WORDS_FILE_CLICKED -> {
            telegram.editMessage(
                chatId = chatId,
                messageIdToEdit = databaseControl.getLastBotMessageId(chatId),
                rawMessageBody = getBodyUploadWordsListMenu(chatId)
            )?.saveLastBotMessageIdToDatabase()
            telegram.handleCallbackQuery(callbackQueryId)
        }

        CALLBACK_SHOW_STATISTICS_CLICKED -> {
            telegram.editMessage(
                chatId = chatId,
                messageIdToEdit = databaseControl.getLastBotMessageId(chatId),
                rawMessageBody = getBodyStatistics(
                    chatId = chatId,
                    statistics = trainer.getStatistics()
                )
            )?.saveLastBotMessageIdToDatabase()
            telegram.handleCallbackQuery(callbackQueryId)
        }

        CALLBACK_RESET_STATISTICS_CLICKED -> {
            trainer.resetUserProgress()
            telegram.handleCallbackQuery(callbackQueryId, text = TEXT_COMPLETE_RESET_STATISTICS)
        }

        CALLBACK_EXIT_MAIN_MENU_CLICKED -> {
            telegram.editMessage(
                chatId = chatId,
                messageIdToEdit = databaseControl.getLastBotMessageId(chatId),
                rawMessageBody = mainMenuBody,
            )?.saveLastBotMessageIdToDatabase()
            telegram.handleCallbackQuery(callbackQueryId)
        }

        CALLBACK_EXIT_MAIN_MENU_FROM_WORDS_CLICKED -> {
            telegram.deleteMessage(
                chatId = chatId,
                messageId = databaseControl.getResultBotMessageId(chatId),
            )
            databaseControl.resetResultMessageId(chatId)
            telegram.editMessage(
                chatId = chatId,
                messageIdToEdit = databaseControl.getLastBotMessageId(chatId),
                rawMessageBody = mainMenuBody,
            )?.saveLastBotMessageIdToDatabase()
            telegram.handleCallbackQuery(callbackQueryId)
        }

        CALLBACK_GO_BACK_CLICKED -> {
            telegram.editMessage(
                chatId = chatId,
                messageIdToEdit = databaseControl.getLastBotMessageId(chatId),
                rawMessageBody = mainMenuBody,
            )?.saveLastBotMessageIdToDatabase()
            telegram.handleCallbackQuery(callbackQueryId)
        }

        CALLBACK_GO_BACK_FROM_STATISTIC_CLICKED -> {
            telegram.editMessage(
                chatId = chatId,
                messageIdToEdit = databaseControl.getLastBotMessageId(chatId),
                rawMessageBody = getBodyStatisticsMenu(
                    chatId = chatId,
                ),
            )?.saveLastBotMessageIdToDatabase()
        }

        CALLBACK_GO_BACK_FROM_UPLOAD_FILE_CLICKED -> {
            telegram.editMessage(
                chatId = chatId,
                messageIdToEdit = databaseControl.getLastBotMessageId(chatId),
                rawMessageBody = mainMenuBody,
            )?.saveLastBotMessageIdToDatabase()
            telegram.handleCallbackQuery(callbackQueryId)
        }
    }

    if (callbackData.startsWith(CALLBACK_ANSWER_PREFIX)) {
        val answerIndex = callbackData.substringAfter(CALLBACK_ANSWER_PREFIX).toInt()
        val checkAnswerResult = trainer.checkAnswer(answerIndex)
        val answerResult =
            if (checkAnswerResult) TEXT_ANSWER_CORRECT else
                "$TEXT_ANSWER_WRONG : ${currentQuestion?.correctWord?.original} - ${currentQuestion?.correctWord?.translate}"

        telegram.editMessage(
            chatId = chatId,
            messageIdToEdit = databaseControl.getLastBotMessageId(chatId),
            rawMessageBody = getBodyResultAnswer(
                chatId = chatId,
                resultAnswer = answerResult,
            )
        ).let { botResponse ->
            val resultMessageId = databaseControl.getResultBotMessageId(chatId)
            if (resultMessageId != 0L) telegram.deleteMessage(chatId, resultMessageId)
            botResponse?.saveResultBotMessageId()
        }

        currentQuestion =
            checkNextQuestionAndSend(
                trainer = trainer,
                telegram = telegram,
                chatId = chatId,
                callbackQueryId = callbackQueryId,
            )
    }
}

fun checkNextQuestionAndSend(
    trainer: LearnWordsTrainer,
    telegram: TelegramBotService,
    chatId: Long,
    callbackQueryId: String,
): Question? {
    val question: Question? = trainer.getNextQuestion()

    if (question == null)
        telegram.editMessage(
            chatId = chatId,
            messageIdToEdit = databaseControl.getLastBotMessageId(chatId),
            rawMessageBody = getBodyAllWordsLearned(
                chatId = chatId,
                allWorldsLearned = TEXT_ALL_WORDS_LEARNED
            )
        )?.saveLastBotMessageIdToDatabase()
    else {
        if (databaseControl.getResultBotMessageId(chatId) == 0L)
            telegram.editMessage(
                chatId = chatId,
                messageIdToEdit = databaseControl.getLastBotMessageId(chatId),
                rawMessageBody = getBodyLearnWordsMenu(
                    chatId = chatId,
                    question = question,
                )
            )?.saveLastBotMessageIdToDatabase()
        else
            telegram.sendMenu(
                rawMessageBody = getBodyLearnWordsMenu(
                    chatId = chatId,
                    question = question,
                )
            )?.saveLastBotMessageIdToDatabase()

        telegram.handleCallbackQuery(callbackQueryId)
    }
    return question
}

fun getUserWordsFileAndSave(
    chatId: Long,
    document: Document,
    telegram: TelegramBotService,
): Set<String> {
    val userCustomTempFile = File("$chatId${document.fileName}")

    val fileResponse = telegram.getFileInfo(
        getBodyRequestFileInfo(document.fileId)
    )
    fileResponse?.response.let { tgFile ->
        if (userCustomTempFile.exists()) {
            telegram.editMessage(
                chatId = chatId,
                messageIdToEdit = databaseControl.getLastBotMessageId(chatId),
                rawMessageBody = getFileUploadAnswer(
                    chatId = chatId,
                    uploadAnswer = TEXT_FILE_ALREADY_EXIST,
                ),
            )?.saveLastBotMessageIdToDatabase()
            return setOf()
        }
        val userFile =
            telegram.downloadFile(tgFile?.filePath)
        userCustomTempFile.outputStream()
            .use { outputStream ->
                userFile
                    ?.use { inputStream ->
                        inputStream.copyTo(outputStream, 16 * 1024)
                    }
            }
        telegram.editMessage(
            chatId = chatId,
            messageIdToEdit = databaseControl.getLastBotMessageId(chatId),
            rawMessageBody = getFileUploadAnswer(
                chatId = chatId,
                uploadAnswer = TEXT_FILE_LOADED_SUCCESSFUL,
            )
        )?.saveLastBotMessageIdToDatabase()
    }
    val rawWordsSet: Set<String> = userCustomTempFile.readLines().toSet()
    userCustomTempFile.delete()
    return rawWordsSet
}

fun BotResponse.saveLastBotMessageIdToDatabase() {
    databaseControl.saveLastBotMessageId(
        chatId = this.result?.chat?.id ?: 0,
        lastBotId = this.result?.botMessageId ?: 0
    )
}

fun BotResponse.saveResultBotMessageId() {
    databaseControl.saveResultBotMessageId(
        chatId = this.result?.chat?.id ?: 0,
        resultBotId = this.result?.botMessageId ?: 0
    )
}