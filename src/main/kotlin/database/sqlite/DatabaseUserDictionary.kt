package database.sqlite

import constants.DATABASE_CONNECT_URL
import constants.SQL_TIMEOUT_QUERY
import constants.TABLE_STANDARD_WORDS
import database.interfaces.IUserDictionary
import model.Word
import java.sql.DriverManager

class DatabaseUserDictionary(
    private val userId: Long,
    private val minimalQuantityCorrectAnswer: Int,
) : IUserDictionary {

    private var hasUserCustomWords = checkHasUserCustomWords()
    private val customTableName = "custom_user_words_$userId"

    override fun getNumOfLearnedWords(): Int {
        DriverManager.getConnection(DATABASE_CONNECT_URL)
            .use { connection ->
                val queryGetNumLearnedWords =
                    """
                    SELECT COUNT(*)
                    FROM user_answers WHERE correct_answer_count >= $minimalQuantityCorrectAnswer AND user_id = $userId;
                    """.trimIndent()

                connection.prepareStatement(queryGetNumLearnedWords)
                    .use { statement ->
                        statement.queryTimeout = SQL_TIMEOUT_QUERY
                        return statement.executeQuery().getInt(1)
                    }
            }
    }

    override fun getSize(): Int {
        DriverManager.getConnection(DATABASE_CONNECT_URL)
            .use { connection ->
                val queryGetSize =
                    if (hasUserCustomWords) "SELECT COUNT(*) FROM $customTableName;"
                    else "SELECT COUNT(*) FROM $TABLE_STANDARD_WORDS;"
                connection.prepareStatement(queryGetSize)
                    .use { statement ->
                        statement.queryTimeout = SQL_TIMEOUT_QUERY
                        return statement.executeQuery().getInt(1)
                    }
            }
    }

    override fun getLearnedWords(): List<Word> {
        DriverManager.getConnection(DATABASE_CONNECT_URL).use { connection ->
            connection.createStatement().use { statement ->
                statement.queryTimeout = SQL_TIMEOUT_QUERY

                val currentWordTable = if (hasUserCustomWords) customTableName else TABLE_STANDARD_WORDS
                val rSListLearnedWords =
                    statement.executeQuery(
                        """
                        SELECT
                            word_id,
                            original,
                            translate,
                            correct_answer_count
                        FROM user_answers ua
                            LEFT JOIN $currentWordTable w
                            ON ua.word_id = w.id
                        WHERE correct_answer_count >= $minimalQuantityCorrectAnswer
                            AND user_id = $userId;
                        """.trimIndent()
                    )
                val listLearnedWords: MutableList<Word> = mutableListOf()
                while (rSListLearnedWords.next()) {
                    listLearnedWords.add(
                        Word(
                            original = rSListLearnedWords.getString("original"),
                            translate = rSListLearnedWords.getString("translate"),
                        )
                    )
                }
                return listLearnedWords
            }
        }
    }

    override fun getUnlearnedWords(): List<Word> {
        DriverManager.getConnection(DATABASE_CONNECT_URL).use { connection ->
            connection.createStatement().use { statement ->
                statement.queryTimeout = SQL_TIMEOUT_QUERY

                val resultGetWords =
                    if (hasUserCustomWords) statement.executeQuery("SELECT * FROM $customTableName")
                    else statement.executeQuery("SELECT * FROM words")

                val listAllWords: MutableList<Word> = mutableListOf()
                while (resultGetWords.next()) {
                    listAllWords.add(
                        Word(
                            original = resultGetWords.getString("original"),
                            translate = resultGetWords.getString("translate"),
                        )
                    )
                }
                listAllWords.minusAssign(getLearnedWords().toSet())
                return listAllWords
            }
        }
    }

    override fun setCorrectAnswersCount(original: String, correctAnswersCount: Int) {
        DriverManager.getConnection(DATABASE_CONNECT_URL)
            .use { connection ->

                val currentWordTable = if (hasUserCustomWords) customTableName else TABLE_STANDARD_WORDS
                val queryGetOriginalWordId =
                    """
                    SELECT id FROM $currentWordTable
                    WHERE original = ?;
                    """.trimIndent()

                val correctWordId =
                    connection.prepareStatement(queryGetOriginalWordId)
                        .use { statement ->
                            statement.queryTimeout = SQL_TIMEOUT_QUERY
                            statement.setString(1, original)
                            statement.executeQuery().getInt("id")
                        }

                val queryGetCorrectWordId =
                    """
                    SELECT word_id FROM user_answers
                    WHERE user_id = $userId AND word_id = $correctWordId;                    
                    """.trimIndent()
                val currentWordIdInUserAnswer =
                    connection.prepareStatement(queryGetCorrectWordId)
                        .use { statement ->
                            statement.executeQuery().getInt("word_id")
                        }

                if (currentWordIdInUserAnswer == 0)
                    appendUserResponse(correctWordId, correctAnswersCount)
                else connection.createStatement().use { statement ->
                    statement.executeUpdate(
                        """
                        UPDATE user_answers
                        SET correct_answer_count = correct_answer_count + 1
                        WHERE user_id = $userId AND word_id = $currentWordIdInUserAnswer;
                        """.trimIndent()
                    )
                }
            }
    }

    override fun resetUserProgress() {
        DriverManager.getConnection(DATABASE_CONNECT_URL).use { connection ->
            connection.createStatement().use { statement ->
                statement.queryTimeout = SQL_TIMEOUT_QUERY
                statement.executeUpdate(
                    """
                    UPDATE user_answers
                    SET correct_answer_count = $DEFAULT_VALUE_ANSWER_COUNT
                    WHERE user_id = $userId;
                    """.trimIndent()
                )
            }
        }
    }

    override fun loadCustomWordsFile(rawWordsSet: Set<String>) {
        val wordsList: MutableList<Word> = mutableListOf()
        rawWordsSet.forEach { text ->
            val line = text.split("|")
            if (line.size < 3) return@forEach
            wordsList.add(
                Word(
                    original = line[0],
                    translate = line[1]
                )
            )
        }
        println("[+] user file loaded")

        DatabaseControl().createCustomWordsTable(userId)
        DriverManager.getConnection(DATABASE_CONNECT_URL).use { connection ->
            val queryCopyOriginalWordsIntoCustom =
                "INSERT OR IGNORE INTO $customTableName SELECT * FROM words;"
            connection.prepareStatement(queryCopyOriginalWordsIntoCustom)
                .use { statement -> statement.executeUpdate() }

            val queryAddCustomWords =
                """
                INSERT OR IGNORE INTO 
                    $customTableName(original, translate)
                    values(?, ?);
                """.trimIndent()
            connection.prepareStatement(queryAddCustomWords).use { statement ->
                wordsList.forEach { word ->
                    statement.setString(1, word.original)
                    statement.setString(2, word.translate)
                    statement.executeUpdate()
                }
            }

            val queryChangeHasCustomWordsToTrue =
                """
                UPDATE users
                SET has_custom_words = true
                WHERE chat_id = $userId;
                """.trimIndent()
            connection.prepareStatement(queryChangeHasCustomWordsToTrue).use { statement ->
                statement.executeUpdate()
            }
        }
        hasUserCustomWords = checkHasUserCustomWords()
    }

    private fun appendUserResponse(originalWordId: Int, correctAnswersCount: Int) {
        DriverManager.getConnection(DATABASE_CONNECT_URL).use { connection ->
            connection.createStatement().use { statement ->
                statement.queryTimeout = SQL_TIMEOUT_QUERY
                statement.executeUpdate(
                    """
                    INSERT OR IGNORE INTO 
                        user_answers(user_id, word_id, correct_answer_count, updated_at) 
                        values($userId, $originalWordId, $correctAnswersCount, CURRENT_TIMESTAMP)
                    """.trimIndent()
                )
            }
        }
    }

    private fun checkHasUserCustomWords(): Boolean {
        DriverManager.getConnection(DATABASE_CONNECT_URL).use { connection ->
            connection.createStatement().use { statement ->
                statement.queryTimeout = SQL_TIMEOUT_QUERY
                val rSHasCustomWords =
                    statement.executeQuery(
                        """
                        SELECT has_custom_words FROM users
                        WHERE chat_id = $userId
                        """.trimIndent()
                    )
                return rSHasCustomWords.getBoolean(1)
            }
        }
    }

}

private const val DEFAULT_VALUE_ANSWER_COUNT = 0