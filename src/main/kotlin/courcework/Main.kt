package courcework

import java.io.File

data class Word(
    val original: String,
    val translate: String,
    val correctAnswersCount: Int = 0,
) {

    var countLearnedWord: Int = 0

}

fun main() {

    val dictionary: MutableList<Word> = mutableListOf()

    val wordsTxt = File("words.txt")
    val countLearnedWordTxt = File("countLearnedWord.txt")

    var counterLearnedWord = 0

    wordsTxt.forEachLine { text ->
        val line = text.split("|")

        val word = Word(
            original = line[0],
            translate = line[1],
            correctAnswersCount = line[2].toIntOrNull() ?: 0,
        )

        dictionary.add(word)

        when (line[2].toIntOrNull() ?: 0) {
            5 -> {
                counterLearnedWord++

                word.countLearnedWord = counterLearnedWord
                countLearnedWordTxt.writeText(word.countLearnedWord.toString())
            }
        }

    }

    dictionary.forEach { println(it) }

    println("Количество выученных слов: ${countLearnedWordTxt.readLines()}")

}