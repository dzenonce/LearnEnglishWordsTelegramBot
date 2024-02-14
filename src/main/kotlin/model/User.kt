package model

data class User(
    val username: String,
    val createdAt: Long,
    val chatId: Long,
)