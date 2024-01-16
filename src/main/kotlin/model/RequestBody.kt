package model

import model.serialization.InlineKeyboard
import model.serialization.ReplyMarkup
import model.serialization.SendMessageRequest

fun getMainMenu(chatId: Long) =
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
                )
            )
        )

fun getStatisticsMenu(chatId: Long) =
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

fun getLearnWordsMenuBody(chatId: Long, question: Question): SendMessageRequest {
    val wordsList =
        question.fourUnlearnedWords.mapIndexed { index, word ->
            InlineKeyboard(
                text = word.original,
                callbackData = "$CALLBACK_ANSWER_PREFIX$index"
            )
        }

    return SendMessageRequest(
        chatId = chatId,
        text = question.correctWord.translate,
        replyMarkup = ReplyMarkup(
            listOf(
                wordsList,
                listOf(
                    InlineKeyboard(
                        text = TEXT_MAIN_MENU,
                        callbackData = CALLBACK_EXIT_MAIN_MENU_CLICKED,
                    )
                )
            )
        )
    )
}