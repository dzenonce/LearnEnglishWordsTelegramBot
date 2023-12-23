package courcework

import java.io.File

data class Word(
    val original: String,
    val translate: String,
    val correctAnswersCount: Int = 0,
)

fun main() {

    val dictionary: MutableList<Word> = mutableListOf()

    val wordsTxt = File("words.txt")

    wordsTxt.forEachLine { text ->
        val line = text.split("|")

        dictionary.add(
            Word(
                original = line[0],
                translate = line[1],
                correctAnswersCount = line[2].toIntOrNull() ?: 0,
            )
        )
    }

    dictionary.forEach { println(it) }

}