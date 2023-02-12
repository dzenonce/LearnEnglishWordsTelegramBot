package model.database

import model.constants.DATABASE_CONNECT_URL
import model.constants.SQL_TIMEOUT_QUERY
import model.trainer.Word
import java.sql.DriverManager
import java.sql.Statement

class DatabaseUserDictionary(
    private val userId: Long,
    private val minimalQuantityCorrectAnswer: Int,
) : IUserDictionary {

    override fun getNumOfLearnedWords(): Int {
        DriverManager.getConnection(DATABASE_CONNECT_URL).use { connection ->
            val statement: Statement = connection.createStatement()
            statement.queryTimeout = SQL_TIMEOUT_QUERY
            return statement.executeQuery(
                """
                SELECT COUNT(*) AS count_line
                FROM user_answers WHERE correct_answer_count >= $minimalQuantityCorrectAnswer AND user_id = $userId;
                """.trimIndent()
            ).getInt("count_line")
        }
    }

    override fun getSize(): Int {
        DriverManager.getConnection(DATABASE_CONNECT_URL).use { connection ->
            val statement: Statement = connection.createStatement()
            statement.queryTimeout = SQL_TIMEOUT_QUERY
            getLearnedWords()
            return statement
                .executeQuery("SELECT COUNT(*) AS count_lines FROM words;")
                .getInt("count_lines")
        }
    }

    override fun getLearnedWords(): List<Word> {
        DriverManager.getConnection(DATABASE_CONNECT_URL).use { connection ->
            val statement: Statement = connection.createStatement()
            statement.queryTimeout = SQL_TIMEOUT_QUERY
            val learnedWordsIds =
                statement.executeQuery(
                    """
                    SELECT word_id, correct_answer_count FROM user_answers
                    WHERE correct_answer_count >= $minimalQuantityCorrectAnswer AND user_id = $userId;
                    """.trimIndent()
                )
            val listLearnedWords: MutableList<Word> = mutableListOf()
            val listLearnedWordsMap: HashMap<Int, Int> = hashMapOf()

            while (learnedWordsIds.next()) {
                val wordId = learnedWordsIds.getInt("word_id")
                val correctAnswerCount = learnedWordsIds.getInt("correct_answer_count")
                listLearnedWordsMap[wordId] = correctAnswerCount
            }

            for (learnedWord in listLearnedWordsMap) {
                val resultGetWords =
                    statement.executeQuery(
                        """
                        SELECT original, translate FROM words
                        WHERE id = ${learnedWord.key};
                        """.trimIndent()
                    )
                listLearnedWords.add(
                    Word(
                        original = resultGetWords.getString("original"),
                        translate = resultGetWords.getString("translate"),
                        correctAnswersCount = learnedWord.value
                    )
                )
            }
            return listLearnedWords
        }
    }

    override fun getUnlearnedWords(): List<Word> {
        DriverManager.getConnection(DATABASE_CONNECT_URL).use { connection ->
            val statement: Statement = connection.createStatement()
            statement.queryTimeout = SQL_TIMEOUT_QUERY
            val resultGetWords =
                statement.executeQuery("SELECT * FROM words;")
            val listWords: MutableList<Word> = mutableListOf()
            while (resultGetWords.next()) {
                listWords.add(
                    Word(
                        original = resultGetWords.getString("original"),
                        translate = resultGetWords.getString("translate"),
                    )
                )
            }
            return listWords
        }
    }

    override fun setCorrectAnswersCount(original: String, correctAnswersCount: Int) {
        DriverManager.getConnection(DATABASE_CONNECT_URL).use { connection ->
            val statement: Statement = connection.createStatement()
            statement.queryTimeout = SQL_TIMEOUT_QUERY
            val originalWordId = statement.executeQuery(
                """
                    SELECT id FROM words
                    WHERE original = '$original';
                """.trimIndent()
            )
            val currentOriginalWordId = originalWordId.getInt("id")

            val currentWordInUserAnswer = statement.executeQuery(
                """
                    SELECT word_id FROM user_answers
                    WHERE user_id = $userId AND word_id = $currentOriginalWordId;
                """.trimIndent()
            )
            if (currentWordInUserAnswer.getInt("word_id") == 0)
                appendUserResponse(currentOriginalWordId, correctAnswersCount)
            else statement.executeUpdate(
                """
                    UPDATE user_answers
                    SET correct_answer_count = correct_answer_count + 1
                    WHERE user_id = $userId AND word_id = $currentOriginalWordId;
                """.trimIndent()
            )
        }
    }

    override fun resetUserProgress() {
    }

    private fun appendUserResponse(originalWordId: Int, correctAnswersCount: Int) {
        DriverManager.getConnection(DATABASE_CONNECT_URL).use { connection ->
            val statement: Statement = connection.createStatement()
            statement.queryTimeout = SQL_TIMEOUT_QUERY
            statement.executeUpdate(
                """
                    INSERT OR IGNORE INTO user_answers(user_id, word_id, correct_answer_count, updated_at) 
                        values($userId, $originalWordId, $correctAnswersCount, CURRENT_TIMESTAMP)
                """.trimIndent()
            )
        }
    }

}

private const val DEFAULT_VALUE_ANSWER_COUNT = 0
