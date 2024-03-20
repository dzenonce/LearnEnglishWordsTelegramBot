package server.trainer

import constants.QUANTITY_WORDS_FOR_LEARNING
import interfaces.database.IUserDictionary
import model.Question
import model.Statistics
import model.Word

class LearnWordsTrainer(
    var countWordsForLearning: Int = QUANTITY_WORDS_FOR_LEARNING,
    private val userDictionary: IUserDictionary,
) {

    private var question: Question? = null

    fun resetUserProgress() = userDictionary.resetUserProgress()

    fun loadCustomWordsFile(rawWordsSet: Set<String>) = userDictionary.loadCustomWordsFile(rawWordsSet)

    fun getStatistics(): Statistics {
        val countWords = userDictionary.getSize()
        if (countWords == 0) return Statistics()
        val countLearnedWord = userDictionary.getNumOfLearnedWords()
        val percentLearnedWord = countLearnedWord * ONE_HUNDRED_PERCENT / countWords

        return Statistics(
            countWords,
            countLearnedWord,
            percentLearnedWord,
        )
    }

    fun getNextQuestion(): Question {
        val listUnlearnedWords = userDictionary.getUnlearnedWords().toMutableList()
        if (listUnlearnedWords.isEmpty()) return Question()
        if (listUnlearnedWords.size < countWordsForLearning) {
            val learnedWords = userDictionary.getLearnedWords()
            val missingWords =
                learnedWords.shuffled().take(countWordsForLearning - listUnlearnedWords.size)
            listUnlearnedWords += missingWords
        }

        val unlearnedWords = listUnlearnedWords.shuffled().take(countWordsForLearning)
        val correctWord = unlearnedWords.random()

        question = Question(
            unlearnedWords,
            correctWord,
        )
        return question as Question
    }

    fun checkAnswer(userChoseAnswer: Int?): Boolean {
        return question?.let { question ->
            val correctAnswerId = question.unlearnedWords.indexOf(question.correctWord)
            if (userChoseAnswer == correctAnswerId) {
                val correctWord = question.correctWord ?: Word()
                userDictionary.setCorrectAnswersCount(
                    correctWord.original.toString(),
                    ++correctWord.correctAnswersCount,
                )
                true
            } else {
                false
            }
        } ?: false
    }
}

private const val ONE_HUNDRED_PERCENT = 100