package model.database

data class User(
    val id: Long,
    val username: String,
    val createdAt: Long,
    val chatId: Long,
)