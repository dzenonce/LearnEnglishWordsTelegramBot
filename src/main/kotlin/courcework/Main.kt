package courcework

import java.io.File

data class Word(
    val original: String,
    val translate: String,
    val correctAnswersCount: Int = 0,
)

val dictionary: MutableList<Word> = mutableListOf()

fun main() {

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

    while (true) {

        println("Меню: \n1 - Учить слова \n2 - Статистика \n3 - Выход ")

        when (readln()) {
            "1" -> println("TODO меню учить слова")
            "2" -> printStatistics()
            EXIT -> return

            else -> println("Введен недопустимый параметр")
        }

    }

}

fun printStatistics() {

    val countWord = dictionary.size
    val countLearnedWord = dictionary.filter { it.correctAnswersCount >= 3 }.size
    val percentLearnedWord = countLearnedWord.toDouble() / countWord.toDouble() * 100

    println("Выучено $countLearnedWord из $countWord | ${percentLearnedWord.toInt()}%")

}

const val EXIT = "3"