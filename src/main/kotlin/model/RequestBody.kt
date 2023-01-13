package model

fun getMainMenuBody(chatId: Int?) =
    """
            {
            	"chat_id": $chatId,
            	"text": "Основное меню",
            	"reply_markup": {
            		"inline_keyboard": [
            			[
            				{
            					"text": "Изучить слова",
            					"callback_data": "$LEARN_WORDS_CLICKED"
            				}
            			],
                        [
                            {
            					"text": "Статистика",
            					"callback_data": "$STATISTICS_CLICKED"
            				}
                        ]
            		]
            	}
            }
        """.trimIndent()

fun getLearnWordsMenuBody(chatId: Int?, question: Question, buttonsList: List<String>) =
    """
        {
           "chat_id": "$chatId",
            "text": "${question.correctWord.original}",
            "reply_markup": {
        	    "inline_keyboard": 
                    $buttonsList
            }
        }
    """.trimMargin()