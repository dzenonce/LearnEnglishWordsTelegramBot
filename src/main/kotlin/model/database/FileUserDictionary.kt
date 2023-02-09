package model.database

import model.constants.TEXT_STANDARD_WORDS_FILE_NAME
import model.constants.QUANTITY_MINIMAL_CORRECT_ANSWER
import model.trainer.Word
import java.io.File

class FileUserDictionary(
    private val fileName: String = TEXT_STANDARD_WORDS_FILE_NAME,
    private val minimalCountCorrectAnswer: Int = QUANTITY_MINIMAL_CORRECT_ANSWER,
) : IUserDictionary {

    private val sourceWordsFile = File(TEXT_STANDARD_WORDS_FILE_NAME)
    private var dictionary = loadDictionary()

    override fun getNumOfLearnedWords(): Int {
        return dictionary.filter { it.correctAnswersCount >= minimalCountCorrectAnswer }.size
    }

    override fun getSize(): Int {
        return dictionary.size
    }

    override fun getLearnedWords(): List<Word> {
        return dictionary.filter { it.correctAnswersCount >= minimalCountCorrectAnswer }
    }

    override fun getUnlearnedWords(): List<Word> {
        return dictionary.filter { it.correctAnswersCount < minimalCountCorrectAnswer }
    }

    override fun setCorrectAnswersCount(original: String, correctAnswersCount: Int) {
        dictionary.find { it.original == original }?.correctAnswersCount = correctAnswersCount
        saveDictionary()
    }

    override fun resetUserProgress() {
        dictionary.forEach { it.correctAnswersCount = DEFAULT_VALUE_ANSWER_COUNT }
        saveDictionary()
    }

    fun reloadDictionary() {
        dictionary = loadDictionary()
        resetUserProgress()
        removeDuplicatesFromFile()
    }

    private fun loadDictionary(): MutableList<Word> {
        try {
            val userWordsFile = File(fileName)
            if (!userWordsFile.exists()) {  // TODO заменяется на sql if not exists создаем таблицу для пользователя
                sourceWordsFile.copyTo(userWordsFile) // TODO здесь копируем столбцы из исходной бд со словами в пользовательскую
            }
            val dictionaryList: MutableList<Word> = mutableListOf()
            userWordsFile.forEachLine { text -> // TODO здесь читаем слова из бд пользоваетеля и работаем с ними в памяти (можно пробовать работать с бд на прямую)
                val line = text.split("|")
                if (line.size < 3) return@forEachLine
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
            throw IllegalStateException("[-] loadDictionary failed: ${e.message}")
        }
    }

    private fun saveDictionary() { // TODO тоже меняем на работу с бд пользователя
        val userWordsFile = File(fileName)
        val newFileContent =
            dictionary.map { "${it.original}|${it.translate}|${it.correctAnswersCount}" }.toSet()
        userWordsFile.writeText(newFileContent.joinToString(separator = "\n"))
    }

    private fun removeDuplicatesFromFile() {
        val file = File(fileName)
        val dictionaryWithoutDuplicates = file.readLines().toSet()
        file.writeText(dictionaryWithoutDuplicates.joinToString(separator = "\n"))
    }

}

private const val DEFAULT_VALUE_ANSWER_COUNT = 0
