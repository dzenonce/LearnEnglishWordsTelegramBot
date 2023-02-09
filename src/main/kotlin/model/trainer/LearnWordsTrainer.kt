package model.trainer

import model.database.DatabaseUserDictionary

data class Word(
    val original: String?,
    val translate: String?,
    var correctAnswersCount: Int = DEFAULT_VALUE_ANSWER_COUNT,
)

data class Statistics(
    val countWords: Int,
    val countLearnedWord: Int,
    val percentLearnedWord: Int,
)

data class Question(
    val fourUnlearnedWords: List<Word>,
    val correctWord: Word,
)

class LearnWordsTrainer(
    private val userId: Long,
    private val countWordsForLearning: Int,
    minimalQuantityCorrectAnswer: Int,
) {

    private val userDatabaseDictionary = DatabaseUserDictionary(
        userId = userId,
        minimalQuantityCorrectAnswer = minimalQuantityCorrectAnswer,
        )

    private var question: Question? = null

    fun resetUserProgress() = userDatabaseDictionary.resetUserProgress()

    fun getStatistics(): Statistics {
        val countWords = userDatabaseDictionary.getSize()
        if (countWords == 0) return Statistics(0, 0, 0)
        val countLearnedWord = userDatabaseDictionary.getNumOfLearnedWords()
        val percentLearnedWord = countLearnedWord * ONE_HUNDRED_PERCENT / countWords

        return Statistics(
            countWords,
            countLearnedWord,
            percentLearnedWord,
        )
    }

    fun getNextQuestion(): Question? {
        val listUnlearnedWords = userDatabaseDictionary.getUnlearnedWords().toMutableList()
        if (listUnlearnedWords.isEmpty()) return null
        if (listUnlearnedWords.size < countWordsForLearning) {
            val learnedWords = userDatabaseDictionary.getLearnedWords()
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
// TODO обратить внимание на декремент изначально был инкремент
    fun checkAnswer(userChoseAnswer: Int?): Boolean {
        return question?.let { question ->
            val correctAnswerId = question.fourUnlearnedWords.indexOf(question.correctWord)
            if (userChoseAnswer == correctAnswerId) {
                userDatabaseDictionary.setCorrectAnswersCount(
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
private const val DEFAULT_VALUE_ANSWER_COUNT = 0