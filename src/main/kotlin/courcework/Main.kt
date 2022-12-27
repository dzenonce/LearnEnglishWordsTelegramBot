package courcework

import java.io.File

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
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
            "1" -> learnWords()
            "2" -> printStatistics()
            "3" -> return

            else -> println("Введен недопустимый параметр")
        }

    }

}

fun printStatistics() {

    val countWord = dictionary.size

    val countLearnedWord = dictionary.filter { it.correctAnswersCount >= REQUIRED_COUNT_CORRECT_ANSWER }.size

    val percentLearnedWord = countLearnedWord.toDouble() / countWord.toDouble() * 100

    println("Выучено $countLearnedWord из $countWord | ${percentLearnedWord.toInt()}%")

}

fun learnWords() {

    println("Для выхода в главное меню введите 0")

    while (true) {

        val listUnlearnedWords =
            dictionary.filter { it.correctAnswersCount < REQUIRED_COUNT_CORRECT_ANSWER }.toMutableList()

        if (listUnlearnedWords.isEmpty()) {
            println("Вы выучили все слова")
            return
        }

        if (listUnlearnedWords.size < NUMBER_OF_ANSWER_OPTIONS) {

            val learnedWords = dictionary.filter { it.correctAnswersCount >= REQUIRED_COUNT_CORRECT_ANSWER }
            val missingWords = learnedWords.shuffled().take(NUMBER_OF_ANSWER_OPTIONS - listUnlearnedWords.size)

            missingWords.forEach { listUnlearnedWords.add(it) }

        }

        val listFourWordToLearn = listUnlearnedWords.shuffled().take(NUMBER_OF_ANSWER_OPTIONS)

        val correctWord = listFourWordToLearn.random().original

        println(correctWord)

        val responseOptionsForIteration: MutableMap<Int, String> = mutableMapOf()

        listFourWordToLearn.forEachIndexed { index, word ->

            responseOptionsForIteration.set(
                index + 1,
                word.original
            )

            println("${index + 1} - ${word.translate}")
        }

        val userChoseAnswer = readln()?.toIntOrNull() ?: println("Введите цифру!")

        if (userChoseAnswer.equals(0)) break

        val selectedWord = responseOptionsForIteration.get(userChoseAnswer)

        if (correctWord.equals(selectedWord)) {
            dictionary.forEach {
                if (it.original.equals(selectedWord)) it.correctAnswersCount++
            }

        }

    }

}

const val REQUIRED_COUNT_CORRECT_ANSWER = 3
const val NUMBER_OF_ANSWER_OPTIONS = 4