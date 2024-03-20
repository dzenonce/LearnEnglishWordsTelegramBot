package client.console

import client.console.extensions.asConsoleString
import database.file.FileUserDictionary
import server.trainer.LearnWordsTrainer

fun main() {

    val trainer = try {
        LearnWordsTrainer(
            countWordsForLearning = 4,
            userDictionary = FileUserDictionary(),
        )
    } catch (e: Exception) {
        println("Некорректный файл")
        return
    }

    while (true) {
        println("Меню: \n1 - Учить слова \n2 - Статистика \n3 - Выход ")
        when (readln()) {
            "1" -> {
                while (true) {
                    val question = trainer.getNextQuestion()
                    if (question.isNotEmpty()) {
                        println("Вы выучили все слова в базе!")
                        break
                    }

                    println(question.asConsoleString())
                    val userChoseAnswer = readln().toIntOrNull()
                    if (userChoseAnswer == 0) break

                    if (trainer.checkAnswer(userChoseAnswer?.minus(1))) {
                        println("Правильно!")
                    } else {
                        println("Не правильно: ${question.correctWord?.original} - ${question.correctWord?.translate} ")
                    }
                }
            }

            "2" -> {
                val statistics = trainer.getStatistics()
                println("Выучено ${statistics.countLearnedWord} из ${statistics.countWords} слов / ${statistics.percentLearnedWord}%")
            }

            "3" -> return
            else -> println("Введен недопустимый параметр")
        }
    }
}