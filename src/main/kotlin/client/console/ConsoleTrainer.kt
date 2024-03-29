package client.console

import database.file.FileUserDictionary
import model.Question
import model.Word
import server.trainer.LearnWordsTrainer

fun Question.asConsoleString(): String {
    val questionVariant =
        this.fourUnlearnedWords
            .mapIndexed { index: Int, word: Word -> "${index.plus(1)} - ${word.translate}" }
            .joinToString("\n")

    return this.correctWord.original + "\n" + questionVariant + "\n" + "---- \n(0) - Выход в меню \n----"
}

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
                    if (question == null) {
                        println("Вы выучили все слова в базе!")
                        break
                    }

                    println(question.asConsoleString())
                    val userChoseAnswer = readln().toIntOrNull()
                    if (userChoseAnswer == 0) break

                    if (trainer.checkAnswer(userChoseAnswer?.minus(1))) {
                        println("Правильно!")
                    } else {
                        println("Не правильно: ${question.correctWord.original} - ${question.correctWord.translate} ")
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


