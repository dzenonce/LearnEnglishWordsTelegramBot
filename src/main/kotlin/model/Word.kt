package model

import constants.QUANTITY_DEFAULT_VALUE_ANSWER_COUNT

data class Word(
    val original: String? = null,
    val translate: String? = null,
    var correctAnswersCount: Int = QUANTITY_DEFAULT_VALUE_ANSWER_COUNT,
)

