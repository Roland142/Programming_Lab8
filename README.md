# Лабораторная работа №9 — JavaFX GUI для коллекции HumanBeing

Клиент-серверное Java-приложение для управления коллекцией объектов
`HumanBeing`. Серверная часть (TCP, PostgreSQL, авторизация по SHA-224,
все 16 команд) была реализована в предыдущей лабораторной; в Lab9
консольный клиент заменён на GUI на JavaFX 17 с поддержкой 4 локалей,
автообновлением и анимацией.

## Архитектура

```
Programming_Lab8/
├── common/              ← общий код для клиента и сервера
│   └── src/
│       ├── elements/    HumanBeing, Coordinates, Mood, Car
│       ├── network/     Request, Response, HumanBeingEntry
│       └── exceptions/
├── server/              ← TCP-сервер на порту 1111
│   └── src/
│       ├── core/        Server, ClientHandler, ServerInitializer
│       ├── commands/    Show, Insert, Update, RemoveKey, Clear, …
│       ├── managers/    CollectionManager, AuthManager, DatabaseManager
│       ├── network/     ProcessCommand, ReadRequest, SendResponse
│       └── db/          DatabaseConnection, HumanBeingRepository, schema.sql
└── client/              ← JavaFX GUI-клиент
    └── src/main/
        ├── java/
        │   ├── command/     SessionContext, ServerUnavailableException
        │   ├── core/        Connection (TCP-подключение)
        │   ├── network/     ServerGateway, RequestSender, ResponseReceiver
        │   ├── utils/       HashUtil
        │   └── gui/
        │       ├── App, Launcher, Session
        │       ├── controllers/    Login, Main, ObjectForm, ObjectPopup
        │       ├── view/           CollectionCanvas
        │       ├── i18n/           LocaleManager, Localizer
        │       ├── model/          HumanBeingFx, CollectionStore
        │       ├── net/            GuiGateway, LoginService, Poller
        │       └── util/           UserColorAssigner, MoodColorMap,
        │                           LocalizedFormatter, Dialogs,
        │                           ShowResponseParser
        └── resources/
            ├── fxml/        login, main, edit-dialog, insert-dialog,
            │                object-popup
            ├── i18n/        messages_*.properties (ru / mk / fr / es_NI)
            └── css/         style.css
```

**Технологии:** Java 17, JavaFX 17.0.9, Maven, PostgreSQL 16, TCP/NIO,
Java Object Serialization.

## Функционал GUI

- **Окно авторизации/регистрации** с переключением языка (4 локали).
- **Главное окно** с тёмной шапкой, тулбаром и SplitPane:
  - **Таблица** со всеми объектами коллекции — 8 колонок (ID, КЛЮЧ, ИМЯ,
    X, Y, СКОРОСТЬ, НАСТРОЕНИЕ, ВЛАДЕЛЕЦ).
  - **Фильтрация и сортировка** через Streams API: фильтр по любому
    полю (регистронезависимо), сортировка кликом по заголовку.
  - **Канвас визуализации** — кружки в координатах объектов; радиус
    зависит от `impactSpeed`, цвет — от `ownerLogin` (детерминировано,
    8-цветовая палитра).
  - **Анимация:** при появлении объекта радиус нарастает от 0 до
    финального значения (~350 мс), при удалении — обратный эффект
    (~250 мс), при обновлении — плавная интерполяция (~200 мс).
  - **Popup-карточка** при клике на кружок — заголовок цвета владельца,
    все поля, кнопка «РЕДАКТИРОВАТЬ» (только для своих объектов).
- **Insert/Edit диалоги** с валидацией (`X > -975`, обязательные поля).
  Открываются из тулбара, двойного клика в таблице или кнопки в popup.
- **Все команды** тулбара: `+ Добавить`, `Удалить`, `Очистить`, `Инфо`,
  `Помощь`, `Скрипт`, `История`. Подменю «Ещё» с командами
  `print_ascending`, `print_field_ascending_impact_speed`, `remove_lower`,
  `remove_greater_key`, `remove_all_by_minutes_of_waiting`.
- **Автообновление** у всех клиентов — Poller опрашивает сервер каждые
  3 секунды, изменения применяются с diff'ом и анимацией.
- **Локализация:** русский, македонский, французский, испанский
  (Никарагуа). Локали хранятся в `.properties`-файлах. Числа и даты
  форматируются по активной локали (`NumberFormat`,
  `DateTimeFormatter.ofLocalizedDate`).
- **Скрипт:** FileChooser → построчное выполнение поддерживаемых
  команд (без объектов: info, help, history, show, clear, remove_key,
  remove_greater_key, remove_all_by_minutes_of_waiting, print_ascending,
  print_field_ascending_impact_speed) с локализованным логом.

## Требования

- **JDK 17**. Локально установить можно через
  `brew install openjdk@17` (далее
  `export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home`).
- **Maven** — используется обычный установленный `mvn`.
- **PostgreSQL 16+** с базой `studs`, ролью `studs` и таблицами
  `users` + `human_beings` (схема в `server/src/db/schema.sql`).
- **`~/.pgpass`** в формате `host:port:db:user:password`, права 600.

## Сборка

Из корня репозитория:

```bash
mvn clean install -DskipTests
```

В результате собираются два fat-jar'а:
- `server/target/server.jar` (~1.3 МБ);
- `client/target/client.jar` (~8 МБ, включает JavaFX 17 для macOS arm64).

## Подготовка БД (для локального запуска через brew)

```bash
brew install postgresql@16
brew services start postgresql@16

export PATH="/opt/homebrew/opt/postgresql@16/bin:$PATH"

psql -d postgres -c "CREATE ROLE studs WITH LOGIN PASSWORD 'studs_pass';"
createdb -O studs studs
psql -d studs -c "CREATE SCHEMA IF NOT EXISTS studs AUTHORIZATION studs;"
PGPASSWORD=studs_pass psql -d studs -U studs -h localhost \
    -f server/src/db/schema.sql

# .pgpass для сервера
echo 'localhost:5432:studs:studs:studs_pass' > ~/.pgpass
chmod 600 ~/.pgpass
```

## Запуск

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"

# Сервер (порт 1111). DB_URL переопределяет хардкоженный jdbc://pg/studs.
DB_URL="jdbc:postgresql://localhost/studs" \
    java -jar server/target/server.jar

# Клиент (в отдельном терминале)
java -jar client/target/client.jar
```

Можно запустить **несколько клиентов параллельно** — каждый откроет
своё окно. Зарегистрируйте разных пользователей (`alice`, `bob`, …)
и убедитесь, что объекты разных владельцев видны на канвасе разными
цветами и автоматически синхронизируются между клиентами.

### Переменные окружения

| Имя       | Назначение                                                | По умолчанию                  |
|-----------|-----------------------------------------------------------|-------------------------------|
| `DB_URL`  | JDBC-URL PostgreSQL для сервера                           | `jdbc:postgresql://pg/studs`  |
| `PORT`    | Порт TCP-подключения клиента к серверу (Connection.java)  | `1111`                        |

## Сценарий проверки

1. Регистрация `alice` → вход → главное окно открывается с пустой
   таблицей.
2. Кнопка «+ Добавить» → заполнить форму → объект появляется в таблице
   и кружком на канвасе с анимацией нарастания.
3. Сменить язык в правом верхнем углу — все подписи мгновенно
   меняются.
4. Открыть второй клиент, зарегистрировать `bob`, добавить объекты —
   у `alice` они появляются через ≤3 секунды с цветом `bob`'а.
5. Клик на чужом кружке → popup без кнопки «Редактировать»; на своём →
   кнопка есть, открывает диалог редактирования.
6. Двойной клик в таблице по своей строке → тот же диалог.
7. Удалить через тулбар «Удалить» (с подтверждением) → объект исчезает
   с анимацией сжатия у всех клиентов.
8. Закрытие окна — Poller останавливается, gateway закрывается.

## Локализация

Все подписи UI находятся в `client/src/main/resources/i18n/`:
- `messages.properties` — fallback (русский);
- `messages_mk.properties` — македонский;
- `messages_fr.properties` — французский;
- `messages_es_NI.properties` — испанский (Никарагуа).

Переключение языка реализовано через `LocaleManager` (синглтон с
`ObjectProperty<Locale>`) и `Localizer.bind()` — все привязанные через
него подписи пересчитываются автоматически без перезапуска.

## Изменения в backend (Lab9)

Чтобы GUI мог отображать таблицу с колонками **КЛЮЧ** и **ВЛАДЕЛЕЦ** и
рисовать круги нужного цвета, в общий протокол добавлено опциональное
поле `Object payload` в `Response` (старый конструктор сохранён — CLI
полностью работоспособен), а команда `Show` дополнительно прикладывает
`List<HumanBeingEntry>` со всеми ключами и владельцами. Это
единственная правка серверного кода.

Также `DatabaseConnection.URL` теперь можно переопределить через
переменную окружения `DB_URL` (по умолчанию остаётся
`jdbc:postgresql://pg/studs`).

## Структура коммитов

Десять коммитов соответствуют десяти этапам реализации:

```
feat: расширить Response payload и Show для структурированной выдачи коллекции
feat: переезд клиента на стандартную maven-раскладку с JavaFX 17
feat: LocaleManager и 4 локали (ru, mk, fr, es_NI)
feat: экран авторизации/регистрации с переключением языка
feat: модель HumanBeingFx, CollectionStore и Poller с автообновлением
feat: главное окно — таблица с фильтром и сортировкой через Streams API
feat: канвас визуализации с цветами по владельцу и popup карточкой объекта
feat: диалоги добавления и редактирования объектов
feat: команды тулбара (delete, clear, info, help, script, history) и подменю
feat: иконки тулбара, обновление локали в таблице, размер окна
```
