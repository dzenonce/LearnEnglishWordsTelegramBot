package database.interfaces

import model.User

interface IDatabaseControl {
    fun initDatabase()
    fun loadStandardWords(standardWordsFileName: String)
    fun addNewUser(user: User)
}