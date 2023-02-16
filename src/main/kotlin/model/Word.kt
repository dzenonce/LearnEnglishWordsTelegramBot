package model

import constants.DEFAULT_VALUE_ANSWER_COUNT

data class Word(
    val original: String?,
    val translate: String?,
    var correctAnswersCount: Int = DEFAULT_VALUE_ANSWER_COUNT,
)

