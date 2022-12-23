package courcework

import java.io.File

data class Word(
    val original: String,
    val translate: String,
    val correctAnswersCount: Int = 0,
)

fun main() {

    while (true) {

        println("Меню: \n1 - Учить слова \n2 - Статистика \n3 - Выход ")

        when (readln()) {
            "1" -> println("TODO меню учить слова")
            "2" -> printStats()
            "3" -> return

            else -> println("Введен недопустимый параметр")
        }

    }

}

fun printStats() {

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

    val countWord = dictionary.size
    val learnedWord = dictionary.filter { it.correctAnswersCount >= 3 }.size
    val percentLearnedWord = learnedWord.toDouble() / countWord.toDouble() * 100

    println("Выучено $learnedWord из $countWord | ${percentLearnedWord.toInt()}%")

}
