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
                CREATE TABLE IF NOT EXISTS "users" (
                    "id" integer PRIMARY KEY,
                    "username" varchar,
                    "created_at" timestamp,
                    "chat_id" integer UNIQUE
                );
                """.trimIndent()
            )
            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS "words" (
                    "id" integer PRIMARY KEY,
                    "original" varchar UNIQUE,
                    "translate" varchar
                );
                """.trimIndent()
            )
            statement.executeUpdate(
                """
                CREATE TABLE IF NOT EXISTS "user_answers" (
                    "user_id" integer,
                    "word_id" varchar,
                    "correct_answer_count" varchar,
                    "updated_at" timestamp,
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

            File(standardWordsFileName).forEachLine { fileString ->
                val lines = fileString.split("|")
                if (lines.size < 3) return@forEachLine
                statement.executeUpdate(
                    "INSERT OR IGNORE INTO words(original, translate) values('${lines[0]}', '${lines[1]}')"
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
                    INSERT OR IGNORE INTO users(username, created_at, chat_id) $SQL_VALUES(                         
                        '${user.username}',
                        CURRENT_TIMESTAMP,
                        ${user.chatId}
                    )
                """.trimIndent()
            )
        }
    }
}