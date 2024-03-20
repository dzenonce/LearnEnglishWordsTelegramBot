import client.console.extensions.asConsoleString
import constants.TEST_SPECIAL_LETTERS
import constants.TEST_WORDS_FILE
import constants.TEXT_STANDARD_WORDS_FILE_NAME
import database.file.FileUserDictionary
import model.Question
import model.Word
import org.junit.jupiter.api.Test
import server.trainer.LearnWordsTrainer
import java.io.File

class ConsoleClientOutputTest {

    private val trainer = LearnWordsTrainer(
        userDictionary = FileUserDictionary(),
    )

    @Test
    fun `test asConsoleString() with 4 words`() {
        generateWordsList(
            wordsFile = TEST_WORDS_FILE,
            wordsCount = 4
        ).let { listWords ->
            Question(
                unlearnedWords = listWords,
                correctWord = listWords.random(),
            ).asConsoleString()

        }
    }

    @Test
    fun `test asConsoleString() with 4 words shuffled`() {
        trainer.countWordsForLearning = 4
        trainer.getNextQuestion().let { question ->
            Question(
                unlearnedWords = question.unlearnedWords.shuffled(),
                correctWord = question.correctWord,
            ).asConsoleString()
        }
    }

    @Test
    fun `test asConsoleString() with empty word`() {
        Question().asConsoleString()
    }

    @Test
    fun `test asConsoleString() with 10 words`() {
        trainer.countWordsForLearning = 10
        trainer.getNextQuestion().asConsoleString()
    }

    @Test
    fun `test asConsoleString() with 200 words`() {
        loadWords(
            wordsFile = TEXT_STANDARD_WORDS_FILE_NAME,
            wordsCount = 200,
        ).let { wordsList ->
            println(wordsList)
            println(
                Question(
                    unlearnedWords = wordsList,
                    correctWord = wordsList.random(),
                ).asConsoleString()
            )
        }
    }

    @Test
    fun `test asConsoleString() special symbols`() {
        generateWordListWithSymbols(
            listSize = 4,
            letters = TEST_SPECIAL_LETTERS,
        ).let { wordsList ->
            Question(
                unlearnedWords = wordsList,
                correctWord = wordsList.random(),
            ).asConsoleString()
        }
    }

    private fun loadWords(wordsFile: String, wordsCount: Int = 4): List<Word> {
        val listWords: MutableList<Word> = mutableListOf()

        File(wordsFile)
            .readLines()
            .forEach {
                val line = it.split("|")
                if (line.size <= wordsCount) {
                    line.size
                }
                while (listWords.size < wordsCount) {
                    listWords.add(
                        Word(
                            original = line[0],
                            translate = line[1],
                            correctAnswersCount = line[2].toIntOrNull() ?: 0,
                        )
                    )
                }
            }
        return listWords
    }

    private fun generateWordsList(wordsFile: String, wordsCount: Int): List<Word> {
        val listWords: MutableList<Word> = mutableListOf()

        File(wordsFile).let { wordsFile ->
            for (i in 1..wordsCount) {
                for (l in wordsFile.readLines()) {
                    val line = l.split("|")
                    listWords.add(
                        Word(
                            original = line[0],
                            translate = line[1],
                        )
                    )
                }
            }
        }
        return listWords
    }

    private fun generateWordListWithSymbols(
        listSize: Int,
        letters: String,
    ): List<Word> {
        val listWords: MutableList<Word> = mutableListOf()
        if (listSize == 0) return listOf(Word())
        for (i in 0 until listSize) {
            listWords.add(
                Word(
                    original = letters.random().toString(),
                    translate = letters.random().toString(),
                )
            )
        }
        return listWords
    }

}