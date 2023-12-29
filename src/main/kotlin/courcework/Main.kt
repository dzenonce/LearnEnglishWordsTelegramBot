package courcework

import java.io.File

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
)

val dictionary: MutableList<Word> = mutableListOf()

val wordsTxt = File("words.txt")

fun main() {

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

            listUnlearnedWords += missingWords
        }

        val listFourWordToLearn = listUnlearnedWords.shuffled().take(NUMBER_OF_ANSWER_OPTIONS)

        val correctWord = listFourWordToLearn.random()
        println(correctWord.original)

        listFourWordToLearn.forEachIndexed { index, word ->
            println("${index + 1} - ${word.translate}")
        }
        println("----\n(0) - Выход \n----")

        val userChoseAnswer = readln()?.toIntOrNull() ?: println("Введите цифру!")
        if (userChoseAnswer.equals(0)) break

        val correctWordIndex = listFourWordToLearn.indexOf(correctWord) + 1

        if (userChoseAnswer == correctWordIndex) {
            correctWord.correctAnswersCount++
            println("Правильно!")
            saveDictionary()
        } else println("Неправильно - слово ${correctWord.translate}")
    }

}

fun saveDictionary() {

    wordsTxt.writeText("")

    dictionary.forEach {
        wordsTxt.appendText("${it.original}|${it.translate}|${it.correctAnswersCount}\n")
    }

}

const val REQUIRED_COUNT_CORRECT_ANSWER = 3
const val NUMBER_OF_ANSWER_OPTIONS = 4