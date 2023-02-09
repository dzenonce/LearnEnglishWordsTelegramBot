package model.database

import model.constants.*
import java.io.File
import java.sql.DriverManager
import java.sql.Statement

class DatabaseControl : IDatabaseControl {

    override fun initDatabase() {
        DriverManager.getConnection(DATABASE_CONNECT_URL).use { connection ->
            val statement: Statement = connection.createStatement()
            statement.queryTimeout = SQL_TIMEOUT_QUERY

            statement.executeUpdate(
                """
                $SQL_CREATE_TABLE_IF_NOT_EXISTS "$TABLE_USERS" (
                    "$COLUMN_ID" $SQL_INTEGER $SQL_PRIMARY_KEY $SQL_UNIQUE,
                    "$COLUMN_USERNAME" $SQL_VARCHAR,
                    "$COLUMN_CREATED_AT" $SQL_TIMESTAMP,
                    "$COLUMN_CHAT_ID" $SQL_INTEGER
                );
                """.trimIndent()
            )
            statement.executeUpdate(
                """
                $SQL_CREATE_TABLE_IF_NOT_EXISTS "$TABLE_WORDS" (
                    "$COLUMN_ID" $SQL_INTEGER $SQL_PRIMARY_KEY,
                    "$COLUMN_ORIGINAL" $SQL_VARCHAR $SQL_UNIQUE,
                    "$COLUMN_TRANSLATE" $SQL_VARCHAR
                );
                """.trimIndent()
            )
            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS "user_answers" (
                    "user_id" integer,
                    "word_id" varchar,
                    "correct_answer_count" varchar,
                    "updated_at" integer,
                    FOREIGN KEY (user_id) REFERENCES users(id),
                    FOREIGN KEY (word_id) REFERENCES words(id)

                );
                """.trimIndent()
            )
        }
        println("[+] database init") // TODO убрать
    }

    override fun loadStandardWords(standardWordsFileName: String) {
        DriverManager.getConnection(DATABASE_CONNECT_URL).use { connection ->
            val statement: Statement = connection.createStatement()
            statement.queryTimeout = SQL_TIMEOUT_QUERY

            var i = 0
            File(standardWordsFileName).forEachLine { fileString ->
                val lines = fileString.split("|")
                if (lines.size < 3) return@forEachLine
                statement.executeUpdate(
                    "$SQL_INSERT_OR_IGNORE_INTO $TABLE_WORDS $SQL_VALUES(${i++}, '${lines[0]}', '${lines[1]}')"
                )
            }
        }
        println("[+] words file loaded") // TODO убрать
    }

    override fun addNewUser(user: User) {
        DriverManager.getConnection(DATABASE_CONNECT_URL).use { connection ->
            val statement: Statement = connection.createStatement()
            statement.queryTimeout = SQL_TIMEOUT_QUERY
            statement.executeUpdate(
                """
                    $SQL_INSERT_OR_IGNORE_INTO $TABLE_USERS $SQL_VALUES(
                        ${user.id},                          
                        '${user.username}',
                        ${user.createdAt},
                        ${user.chatId}
                    )
                """.trimIndent()
            )
        }
        println("[+] user added") // TODO убрать
    }
}