package model

import model.serialization.GetFileRequest
import model.serialization.InlineKeyboard
import model.serialization.ReplyMarkup
import model.serialization.SendMessageRequest

fun getBodyMainMenu(chatId: Long) =
    SendMessageRequest(
        chatId = chatId,
        text = TEXT_MAIN_MENU,
        replyMarkup = ReplyMarkup(
            inlineKeyboard =
            listOf(
                listOf(
                    InlineKeyboard(
                        text = TEXT_LEARN_WORDS,
                        callbackData = CALLBACK_LEARN_WORDS_CLICKED,
                    )
                ),
                listOf(
                    InlineKeyboard(
                        text = TEXT_STATISTICS_MENU,
                        callbackData = CALLBACK_MENU_STATISTICS_CLICKED,
                    )
                ),
                listOf(
                    InlineKeyboard(
                        text = TEXT_UPLOAD_WORDS_FILE,
                        callbackData = CALLBACK_LOAD_WORDS_FILE_CLICKED,
                    )
                )
            )
        )
    )

fun getBodyStatisticsMenu(chatId: Long) =
    SendMessageRequest(
        chatId = chatId,
        text = TEXT_DESCRIPTION_STATISTICS_MENU,
        replyMarkup = ReplyMarkup(
            listOf(
                listOf(
                    InlineKeyboard(
                        text = TEXT_GET_STATISTICS,
                        callbackData = CALLBACK_SHOW_STATISTICS_CLICKED,
                    )
                ),
                listOf(
                    InlineKeyboard(
                        text = TEXT_RESET_STATISTICS,
                        callbackData = CALLBACK_RESET_STATISTICS_CLICKED,
                    )
                ),
                listOf(
                    InlineKeyboard(
                        text = TEXT_GO_BACK,
                        callbackData = CALLBACK_GO_BACK_CLICKED,
                    )
                )
            )
        )
    )

fun getBodyLearnWordsMenu(chatId: Long, question: Question): SendMessageRequest {

    val wordsList =
        question.fourUnlearnedWords.mapIndexed { index, word ->
            listOf(
                InlineKeyboard(
                    text = word.original,
                    callbackData = "$CALLBACK_ANSWER_PREFIX$index"
                )
            )
        }.toMutableList()

    val exitMainMenuButton =
        listOf(
            InlineKeyboard(
                text = TEXT_MAIN_MENU,
                callbackData = CALLBACK_EXIT_MAIN_MENU_CLICKED,
            )
        )
    wordsList.add(exitMainMenuButton)

    return SendMessageRequest(
        chatId = chatId,
        text = question.correctWord.translate,
        replyMarkup = ReplyMarkup(
            wordsList,
        )
    )
}

fun getBodyUploadWordsListMenu(chatId: Long) =
    SendMessageRequest(
        chatId = chatId,
        text = TEXT_SEND_FILE_DESCRIPTION,
        replyMarkup = ReplyMarkup(
            listOf(
                listOf(
                    InlineKeyboard(
                        text = TEXT_GO_BACK,
                        callbackData = CALLBACK_GO_BACK_CLICKED,
                    )
                )
            )
        )
    )

fun getBodyRequestFileInfo(fileId: String) =
    GetFileRequest(
        fileId = fileId,
    )