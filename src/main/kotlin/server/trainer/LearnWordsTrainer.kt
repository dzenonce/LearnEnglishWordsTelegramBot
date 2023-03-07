package server.trainer

import interfaces.database.IUserDictionary
import model.Question
import model.Statistics

class LearnWordsTrainer(
    private val countWordsForLearning: Int,
    private val userDictionary: IUserDictionary,
) {

    private var question: Question? = null

    fun resetUserProgress() = userDictionary.resetUserProgress()

    fun loadCustomWordsFile(rawWordsSet: Set<String>) = userDictionary.loadCustomWordsFile(rawWordsSet)

    fun getStatistics(): Statistics {
        val countWords = userDictionary.getSize()
        if (countWords == 0) return Statistics(0, 0, 0)
        val countLearnedWord = userDictionary.getNumOfLearnedWords()
        val percentLearnedWord = countLearnedWord * ONE_HUNDRED_PERCENT / countWords

        return Statistics(
            countWords,
            countLearnedWord,
            percentLearnedWord,
        )
    }

    fun getNextQuestion(): Question? {
        val listUnlearnedWords = userDictionary.getUnlearnedWords().toMutableList()
        if (listUnlearnedWords.isEmpty()) return null
        if (listUnlearnedWords.size < countWordsForLearning) {
            val learnedWords = userDictionary.getLearnedWords()
            val missingWords =
                learnedWords.shuffled().take(countWordsForLearning - listUnlearnedWords.size)
            listUnlearnedWords += missingWords
        }

        val fourUnlearnedWords = listUnlearnedWords.shuffled().take(countWordsForLearning)
        val correctWord = fourUnlearnedWords.random()

        question = Question(
            fourUnlearnedWords,
            correctWord,
        )
        return question
    }

    fun checkAnswer(userChoseAnswer: Int?): Boolean {
        return question?.let { question ->
            val correctAnswerId = question.fourUnlearnedWords.indexOf(question.correctWord)
            if (userChoseAnswer == correctAnswerId) {
                userDictionary.setCorrectAnswersCount(
                    question.correctWord.original.toString(),
                    ++question.correctWord.correctAnswersCount,
                )
                true
            } else {
                false
            }
        } ?: false
    }
}

private const val ONE_HUNDRED_PERCENT = 100