package database.sqlite

import constants.DATABASE_CONNECT_URL
import constants.SQL_TIMEOUT_QUERY
import interfaces.database.IDatabaseControl
import model.User
import java.io.File
import java.sql.DriverManager

class SqliteDatabaseControl : IDatabaseControl {

    override fun initDatabase() {
        DriverManager.getConnection(DATABASE_CONNECT_URL)
            .use { connection ->
                connection.createStatement()
                    .use { statement ->
                        statement.queryTimeout = SQL_TIMEOUT_QUERY
                        statement.executeUpdate(
                            """
                            CREATE TABLE IF NOT EXISTS "users" (
                                "id" integer PRIMARY KEY,
                                "username" varchar,
                                "created_at" timestamp,
                                "chat_id" integer UNIQUE,
                                "result_bot_message_id" integer,
                                "last_bot_message_id" integer UNIQUE,
                                "has_custom_words" boolean DEFAULT false
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
            }
        println("[+] database initialized")
    }

    override fun loadStandardWords(standardWordsFileName: String) {
        DriverManager.getConnection(DATABASE_CONNECT_URL)
            .use { connection ->
                val queryInsertStandardWords =
                    """
                    INSERT OR IGNORE INTO 
                        words(original, translate) 
                        values(?, ?);
                    """.trimIndent()
                connection.prepareStatement(queryInsertStandardWords)
                    .use { statement ->
                        statement.queryTimeout = SQL_TIMEOUT_QUERY
                        File(standardWordsFileName).forEachLine { fileString ->
                            val lineInFile = fileString.split("|")
                            if (lineInFile.size < 3) return@forEachLine
                            statement.setString(1, lineInFile[0])
                            statement.setString(2, lineInFile[1])
                            statement.executeUpdate()
                        }
                    }
            }
        println("[+] standard words file loaded")
    }

    override fun addNewUser(user: User) {
        DriverManager.getConnection(DATABASE_CONNECT_URL)
            .use { connection ->
                val queryAddNewUser =
                    """
                    INSERT OR IGNORE INTO 
                        users(username, created_at, chat_id) 
                        values(?, CURRENT_TIMESTAMP, ?);                                     
                    """.trimIndent()
                connection.prepareStatement(queryAddNewUser)
                    .use { statement ->
                        statement.queryTimeout = SQL_TIMEOUT_QUERY
                        statement.setString(1, user.username)
                        statement.setLong(2, user.chatId)
                        statement.executeUpdate()
                    }
            }
    }

    override fun createCustomWordsTable(userId: Long) {
        DriverManager.getConnection(DATABASE_CONNECT_URL)
            .use { connection ->
                connection.createStatement()
                    .use { statement ->
                        statement.queryTimeout = SQL_TIMEOUT_QUERY
                        statement.executeUpdate(
                            """
                            CREATE TABLE IF NOT EXISTS custom_user_words_$userId(
                                "id" integer PRIMARY KEY,
                                "original" varchar UNIQUE NOT NULL,
                                "translate" varchar
                            );
                            """.trimIndent()
                        )
                    }
            }
    }

    override fun saveLastBotMessageId(chatId: Long, lastBotId: Long) {
        DriverManager.getConnection(DATABASE_CONNECT_URL)
            .use { connection ->
                connection.createStatement()
                    .use { statement ->
                        statement.queryTimeout = SQL_TIMEOUT_QUERY
                        statement.executeUpdate(
                            """
                            UPDATE users
                                SET last_bot_message_id = $lastBotId
                                WHERE chat_id = $chatId;
                            """.trimIndent()
                        )
                    }
            }
    }

    override fun saveResultBotMessageId(chatId: Long, resultBotId: Long) {
        DriverManager.getConnection(DATABASE_CONNECT_URL)
            .use { connection ->
                connection.createStatement()
                    .use { statement ->
                        statement.queryTimeout = SQL_TIMEOUT_QUERY
                        statement.executeUpdate(
                            """
                            UPDATE users
                                SET result_bot_message_id = $resultBotId
                                WHERE chat_id = $chatId;
                            """.trimIndent()
                        )
                    }
            }
    }

    override fun getLastBotMessageId(chatId: Long): Long {
        DriverManager.getConnection(DATABASE_CONNECT_URL)
            .use { connection ->
                connection.createStatement()
                    .use { statement ->
                        statement.queryTimeout = SQL_TIMEOUT_QUERY
                        return statement.executeQuery(
                            """
                            SELECT last_bot_message_id FROM users
                                WHERE chat_id = $chatId;                                
                            """.trimIndent()
                        ).getLong(1)
                    }
            }
    }

    override fun getResultBotMessageId(chatId: Long): Long {
        DriverManager.getConnection(DATABASE_CONNECT_URL)
            .use { connection ->
                connection.createStatement()
                    .use { statement ->
                        statement.queryTimeout = SQL_TIMEOUT_QUERY
                        return statement.executeQuery(
                            """
                            SELECT result_bot_message_id FROM users
                                WHERE chat_id = $chatId;                                
                            """.trimIndent()
                        ).getLong(1)
                    }
            }
    }

    override fun resetResultMessageId(chatId: Long) {
        DriverManager.getConnection(DATABASE_CONNECT_URL)
            .use { connection ->
                connection.createStatement()
                    .use { statement ->
                        statement.queryTimeout = SQL_TIMEOUT_QUERY
                        statement.executeUpdate(
                            """
                            UPDATE users
                                SET result_bot_message_id = 0
                                WHERE chat_id = $chatId;
                            """.trimIndent()
                        )
                    }
            }
    }

}