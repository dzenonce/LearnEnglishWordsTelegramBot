package model.database

interface IDatabaseControl {
    fun initDatabase()
    fun loadStandardWords(standardWordsFileName: String)
    fun addNewUser(user: User)
}