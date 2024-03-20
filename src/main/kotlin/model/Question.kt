package model

data class Question(
    val unlearnedWords: List<Word> = listOf(),
    val correctWord: Word? = null,
) {
    fun isNotEmpty() = this.unlearnedWords.isNotEmpty() && correctWord != null
}