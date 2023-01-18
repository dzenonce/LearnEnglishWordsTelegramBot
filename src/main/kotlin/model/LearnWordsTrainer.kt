package model

import java.io.File

data class Word(
    val original: String,
    val translate: String,
    var correctAnswersCount: Int = 0,
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
    private val fileName: String = "words.txt",
    private val numberOfCountOption: Int,
    private val requiredCountCorrectAnswer: Int,
) {

    private val sourceWordsFile = File("words.txt")
    private val dictionary = loadDictionary()
    private var question: Question? = null

    fun getStatistics(): Statistics {
        val countWord = dictionary.size
        val countLearnedWord = dictionary.filter { it.correctAnswersCount >= requiredCountCorrectAnswer }.size
        val percentLearnedWord = countLearnedWord * ONE_HUNDRED_PERCENT / countWord

        return Statistics(
            countWord,
            countLearnedWord,
            percentLearnedWord,
        )
    }

    fun getNextQuestion(): Question? {
        val listUnlearnedWords =
            dictionary.filter { it.correctAnswersCount < requiredCountCorrectAnswer }.toMutableList()
        if (listUnlearnedWords.isEmpty()) return null

        if (listUnlearnedWords.size < numberOfCountOption) {
            val learnedWords = dictionary.filter { it.correctAnswersCount >= requiredCountCorrectAnswer }
            val missingWords = learnedWords.shuffled().take(numberOfCountOption - listUnlearnedWords.size)
            listUnlearnedWords += missingWords
        }
        val fourUnlearnedWords = listUnlearnedWords.shuffled().take(numberOfCountOption)
        val correctWord = fourUnlearnedWords.random()

        question = Question(
            fourUnlearnedWords,
            correctWord,
        )

        return question
    }

    fun checkAnswer(userChoseAnswer: Int?): Boolean {
        return question?.let {
            val correctAnswerId = it.fourUnlearnedWords.indexOf(it.correctWord)
            if (userChoseAnswer == correctAnswerId) {
                it.correctWord.correctAnswersCount++
                saveDictionary()
                true
            } else {
                false
            }
        } ?: false
    }

    fun resetProgress() {
        dictionary.forEach {
            it.correctAnswersCount = 0
        }
        saveDictionary()
    }

    private fun loadDictionary(): MutableList<Word> {
        try {
            val userWordsFile = File(fileName)
            if (!userWordsFile.exists()) {
                sourceWordsFile.copyTo(userWordsFile)
            }
            val dictionaryList: MutableList<Word> = mutableListOf()
            userWordsFile.forEachLine { text ->
                val line = text.split("|")
                dictionaryList.add(
                    Word(
                        original = line[0],
                        translate = line[1],
                        correctAnswersCount = line[2].toIntOrNull() ?: 0,
                    )
                )
            }
            return dictionaryList
        } catch (e: IndexOutOfBoundsException) {
            throw IllegalStateException("Ошибка в функции loadDictionary! ${e.message}")
        }
    }

    private fun saveDictionary() {
        val userWordsFile = File(fileName)
        userWordsFile.writeText("")
        dictionary.forEach {
            userWordsFile.appendText("${it.original}|${it.translate}|${it.correctAnswersCount}\n")
        }
    }

}

private const val ONE_HUNDRED_PERCENT = 100