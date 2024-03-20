package client.console.extensions

import constants.MAXIMUM_QUANTITY_SHOWING_UNLEARNED_WORDS
import model.Question
import model.Word

fun Question.asConsoleString(): String {
    val wordsList =
        if (this.unlearnedWords.size in 1..MAXIMUM_QUANTITY_SHOWING_UNLEARNED_WORDS) this.unlearnedWords
        else this.unlearnedWords.take(MAXIMUM_QUANTITY_SHOWING_UNLEARNED_WORDS)

    val questionVariant =
        wordsList
            .mapIndexed { index: Int, word: Word -> "${index.plus(1)} - ${word.translate}" }
            .joinToString("\n")

    return this.correctWord?.original + "\n" + questionVariant + "\n" + "---- \n(0) - Выход в меню \n----"
}