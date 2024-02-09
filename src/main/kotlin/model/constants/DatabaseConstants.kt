package model.constants

const val DATABASE_NAME = "learn_words_bot"
const val DATABASE_FILE_EXT = ".db"
const val DATABASE_CONNECT_URL = "jdbc:sqlite:$DATABASE_NAME$DATABASE_FILE_EXT"

const val TABLE_USERS = "users"
const val TABLE_WORDS = "words"
const val TABLE_USER_ANSWERS = "user_answers"

const val COLUMN_ID = "id"
const val COLUMN_CHAT_ID = "chat_id"
const val COLUMN_USER_ID = "user_id"
const val COLUMN_WORD_ID = "word_id"
const val COLUMN_USERNAME = "username"
const val COLUMN_ORIGINAL = "original"
const val COLUMN_TRANSLATE = "translate"
const val COLUMN_CORRECT_ANSWER_COUNT = "correct_answer_count"
const val COLUMN_UPDATED_AT = "updated_at"
const val COLUMN_CREATED_AT = "created_at"
