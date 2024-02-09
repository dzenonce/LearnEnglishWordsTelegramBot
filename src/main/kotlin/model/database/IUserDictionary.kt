package model.database

import model.trainer.Word

interface IUserDictionary {
    fun getNumOfLearnedWords(): Int
    fun getSize(): Int
    fun getLearnedWords(): List<Word>
    fun getUnlearnedWords(): List<Word>
    fun setCorrectAnswersCount(original: String, correctAnswersCount: Int)
    fun resetUserProgress()
}