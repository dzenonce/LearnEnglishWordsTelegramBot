package courcework

import java.io.File

data class Statistics(
    val countWords: Int,
    val countLearnedWord: Int,
    val percentLearnedWord: Int,
)

data class Question(
    val fourUnlearnedWords: List<Word>,
    val correctWord: Word,
)

class LearnWordsTrainer {

    fun getStatistics(): Statistics {
        val countWord = dictionary.size
        val countLearnedWord = dictionary.filter { it.correctAnswersCount >= REQUIRED_COUNT_CORRECT_ANSWER }.size
        val percentLearnedWord = countLearnedWord * ONE_HUNDRED_PERCENT / countWord

        return Statistics(
            countWord,
            countLearnedWord,
            percentLearnedWord,
        )
    }

    fun getNextQuestion(): Question? {
        val listUnlearnedWords =
            dictionary.filter { it.correctAnswersCount < REQUIRED_COUNT_CORRECT_ANSWER }.toMutableList()
        if (listUnlearnedWords.isEmpty()) return null

        if (listUnlearnedWords.size < NUMBER_OF_COUNT_OPTION) {
            val learnedWords = dictionary.filter { it.correctAnswersCount >= REQUIRED_COUNT_CORRECT_ANSWER }
            val missingWords = learnedWords.shuffled().take(NUMBER_OF_COUNT_OPTION - listUnlearnedWords.size)
            listUnlearnedWords += missingWords
        }
        val fourUnlearnedWords = listUnlearnedWords.shuffled().take(NUMBER_OF_COUNT_OPTION)
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

    private val wordsTxt = File("words.txt")
    private val dictionary = loadDictionary()
    private var question: Question? = null

    private fun loadDictionary(): MutableList<Word> {
        val dictionaryList: MutableList<Word> = mutableListOf()
        wordsTxt.forEachLine { text ->
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
    }

    private fun saveDictionary() {
        wordsTxt.writeText("")
        dictionary.forEach {
            wordsTxt.appendText("${it.original}|${it.translate}|${it.correctAnswersCount}\n")
        }
    }

}

private const val NUMBER_OF_COUNT_OPTION = 4
private const val REQUIRED_COUNT_CORRECT_ANSWER = 3
private const val ONE_HUNDRED_PERCENT = 100