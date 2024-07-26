# English Learning Bot

_Demo_

![gifTgBot-ezgif com-video-to-gif-converter](https://github.com/user-attachments/assets/4d6a062c-b238-4f43-ac97-67dbc3c01fae)


Бот для изучения английских слов.
Начальные слова размещаются в файле words.txt, в формате: `английское слово|перевод|0`.
Каждая строка соответствует изучаемому слову.

*Первый запуск*
+ На сервере создается база данных `Sqlite` `learn_english_bot.db`

При запуске бота новым пользователем, добавляется новая запись о пользователе в таблицу `users` базы данных `learn_words_bot.db`.

## Публикация

Для публикации бота на VPS воспользуемся утилитой scp, для запуска – ssh.

### Настройка VPS

1. Создать виртуальный сервер (Ubuntu), получить: ip-адрес, пароль для root пользователя
2. Подключиться к серверу по SSH используя команду `ssh root@100.100.100.100` и введя пароль
3. Обновить установленные пакеты командами `apt update` и `apt upgrade`
4. Устанавливаем JDK коммандой `apt install default-jdk`
5. Убедиться что JDK установлена командой `java --version`

### Публикация и запуск

1. Соберем shadowJar командой `./gradlew shadowJar`
2. Копируем jar на наш VPS переименуя его одновременно в
   bot.jar: `scp build/libs/WordsTelegramBot-1.0-SNAPSHOT.jar root@100.100.100.100:/root/bot.jar`
3. Копируем words.txt на VPS: `scp words.txt root@100.100.100.100:/root/words.txt`
4. Подключиться к серверу по SSH используя команду `ssh root@100.100.100.100` и введя пароль
5. Запустить бота в фоне командой `nohup java -jar bot.jar <ТОКЕН ТЕЛЕГРАМ> &`
6. Проверить работу бота
