package model

const val CALLBACK_LEARN_WORDS_CLICKED = "learn_words_clicked"
const val CALLBACK_MENU_STATISTICS_CLICKED = "menu_statistics_clicked"
const val CALLBACK_SHOW_STATISTICS_CLICKED = "show_statistics_clicked"
const val CALLBACK_RESET_STATISTICS_CLICKED = "reset_statistics_clicked"
const val CALLBACK_EXIT_MAIN_MENU_CLICKED = "exit_main_menu_clicked"
const val CALLBACK_GO_BACK_CLICKED = "go_back_clicked"
const val CALLBACK_LOAD_WORDS_FILE_CLICKED = "upload_words_file_clicked"
const val CALLBACK_ANSWER_PREFIX = "answer_"
const val TEXT_ALL_WORDS_LEARNED = "Вы выучили все слова в базе"
const val TEXT_ANSWER_CORRECT = "Правильно"
const val TEXT_ANSWER_WRONG = "Не правильно"
const val TEXT_MAIN_MENU = "Основное меню"
const val TEXT_STATISTICS_MENU = "Управление статистикой"
const val TEXT_DESCRIPTION_STATISTICS_MENU = "Здесь вы можете посмотреть или сбросить статистику"
const val TEXT_LEARN_WORDS = "Изучить слова"
const val TEXT_GET_STATISTICS = "Статистика"
const val TEXT_RESET_STATISTICS = "Сбросить статистику"
const val TEXT_COMPLETE_RESET_STATISTICS = "Статистика сброшена"
const val TEXT_GO_BACK = "Назад"
const val TEXT_UPLOAD_WORDS_FILE = "Загрузить свои слова"
const val TEXT_SEND_FILE_DESCRIPTION =
"""
Вы можете загрузить свои слова, просто отправьте файл в чат.
Формат данных:
английское слово|перевод|
"""
const val TEXT_FILE_ALREADY_EXIST = "Файл с таким названием уже существует!"
const val TEXT_FILE_LOADED_SUCCESSFUL = "Файл успешно загружен!"
const val PAUSE_TELEGRAM_GET_UPDATE = 1500L
const val FILE_TEXT_EXT = ".txt"