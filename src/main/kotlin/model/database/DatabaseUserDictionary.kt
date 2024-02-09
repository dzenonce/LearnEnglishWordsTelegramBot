package model.database

import model.constants.*
import model.trainer.Word
import java.sql.DriverManager
import java.sql.Statement

class DatabaseUserDictionary(
    private val userId: Long,
    private val minimalQuantityCorrectAnswer: Int
) : IUserDictionary {

    override fun getNumOfLearnedWords(): Int {
        DriverManager.getConnection(DATABASE_CONNECT_URL).use { connection ->
            val statement: Statement = connection.createStatement()
            statement.queryTimeout = SQL_TIMEOUT_QUERY
            val answerCount = statement
                .executeQuery("$SQL_SELECT $COLUMN_CORRECT_ANSWER_COUNT $SQL_FROM $TABLE_USER_ANSWERS;")
                .getInt(COLUMN_CORRECT_ANSWER_COUNT)
            if (answerCount >= 3) return 1
        }
        return 0
    }

    override fun getSize(): Int {
        DriverManager.getConnection(DATABASE_CONNECT_URL).use { connection ->
            val statement: Statement = connection.createStatement()
            statement.queryTimeout = SQL_TIMEOUT_QUERY
            return statement
                .executeQuery("$SQL_SELECT $SQL_COUNT(*) $SQL_AS count_lines $SQL_FROM $TABLE_WORDS;")
                .getInt("count_lines")
        }
    }

    override fun getLearnedWords(): List<Word> {
        return emptyList()
    }

    // TODO если вордайди пользователя пуст, отправить все слова, иначе, выбрать все слова по айди, для которых количество правильных ответов меньше 3х
    override fun getUnlearnedWords(): List<Word> {
        DriverManager.getConnection(DATABASE_CONNECT_URL).use { connection ->
            val statement: Statement = connection.createStatement()
            statement.queryTimeout = SQL_TIMEOUT_QUERY
            val resultGetWords = statement
                .executeQuery("$SQL_SELECT * $SQL_FROM $TABLE_WORDS;")
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
        // TODO здесь мы получаем для пользователя слово по айди и меняем количество правильных ответов на нем
    }

    override fun resetUserProgress() {
    }

}

private const val DEFAULT_VALUE_ANSWER_COUNT = 0
