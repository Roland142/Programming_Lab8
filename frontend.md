# Разработка GUI-клиента (Lab9): подробный отчёт

Документ описывает процесс разработки клиентской части лабораторной
работы №9: какие требования стояли, какой был backend, какие
архитектурные решения приняты, как реализован каждый компонент GUI,
какие компромиссы сделаны.

---

## 1. Постановка задачи

### Требования из задания (вариант)

1. Интерфейс реализуется средствами **JavaFX**.
2. Поддержка 4 локалей — **русский, македонский, французский,
   испанский (Никарагуа)**. Числа, даты и время отображаются по локали.
   Переключение языка **без перезапуска**. Локализованные строки
   хранятся **в файле свойств** (`.properties`).
3. Заменить консольный клиент Lab7 на GUI-клиент со следующим
   функционалом:
   1. Окно авторизации/регистрации.
   2. Отображение текущего пользователя.
   3. Таблица со всеми объектами коллекции (каждое поле — отдельная
      колонка); фильтрация и сортировка по любой колонке через
      **Streams API**.
   4. Поддержка всех команд из предыдущих лабораторных.
   5. Область визуализации (Graphics/Canvas), круги в координатах
      объектов, размер от поля объекта, разные цвета у разных
      пользователей. Клик → информация. Авто­обновление у всех
      клиентов. Анимация при отрисовке.
   6. Редактирование своих объектов из таблицы и из канваса.
   7. Удаление выбранного объекта.
4. Прототип интерфейса согласовать с преподавателем (дизайн дан
   в `GUI.pdf`).

### Анализ исходного backend (Lab7)

Серверная часть была готова и без изменений выполняла всё что нужно:

- Maven-проект с тремя модулями: `common`, `server`, `client` (CLI).
- Java 17, TCP/NIO на порту 1111, `ObjectInputStream`/`ObjectOutputStream`
  для сериализации `Request`/`Response`.
- 16 команд: `register`, `login`, `insert`, `update`, `remove_key`,
  `clear`, `remove_lower`, `remove_greater_key`,
  `remove_all_by_minutes_of_waiting`, `info`, `help`, `history`,
  `execute_script`, `show`, `print_ascending`,
  `print_field_ascending_impact_speed`.
- Авторизация через SHA-224 (`utils/HashUtil`), таблица `users` в БД.
- Коллекция: `TreeMap<Long, HumanBeing>` под `ReentrantLock` +
  `Map<Long, String> ownerByKey` для владельцев.
- PostgreSQL с таблицами `users` и `human_beings` (схема в
  `server/src/db/schema.sql`).

В CLI клиенте были также готовые компоненты, которые удалось
**переиспользовать без изменений**:

- `network/ServerGateway` — обёртка над TCP, авто-переподключение.
- `network/RequestSender`, `network/ResponseReceiver` — сериализация.
- `core/Connection` — установка соединения, env-переменная `PORT`.
- `command/SessionContext` — credentials текущей сессии.
- `command/exceptions/ServerUnavailableException`.
- `utils/HashUtil` — SHA-224.

### Критический нюанс протокола

Существующий `Show.execute()` возвращал коллекцию **текстом**:

```java
String result = sorted.stream()
        .map(HumanBeing::toString)
        .collect(Collectors.joining("\n-----\n"));
return new Response(result, true);
```

Этого хватало для CLI, но для GUI неприемлемо:

- Таблица в дизайне содержит колонку **КЛЮЧ** (map_key TreeMap) и
  колонку **ВЛАДЕЛЕЦ** — обе отсутствуют в `toString()`.
- Команда `remove_key` требует ключ, который из текста надо парсить.
- Цвет круга на канвасе зависит от логина владельца.
- Парсинг `toString()` хрупкий: формат может поменяться.

Решение — минимально расширить общий протокол совместимым образом
(см. этап 1).

---

## 2. Архитектурные решения

### 2.1 Минимальное расширение протокола

В `common/network/Response` добавлено опциональное поле `Object payload`
(`Serializable`) и второй конструктор. Старый конструктор
`Response(message, success)` сохранён — CLI полностью совместим.

```java
public Response(String message, boolean success, Object payload) {
    this.message = message;
    this.success = success;
    this.payload = payload;
}
```

Новый класс `common/network/HumanBeingEntry` несёт
`{long key, HumanBeing humanBeing, String ownerLogin}`.

`Show.execute()` теперь:

1. Под локом коллекции собирает `ArrayList<HumanBeingEntry>` через
   `Streams API`.
2. Кладёт его в `payload`.
3. Возвращает старый текстовый `message` для CLI.

Это **единственная правка серверного кода**.

### 2.2 Структура GUI-клиента

Стандартная Maven-раскладка (`src/main/java`, `src/main/resources`).
Клиентский Java-пакет `gui` содержит подмодули по слоям:

| Подпакет | Назначение |
|---|---|
| `gui` | `App` (Application), `Launcher` (точка входа fat-jar), `Session` (singleton) |
| `gui.controllers` | `Login`, `Main`, `ObjectForm` (insert/edit), `ObjectPopup` |
| `gui.view` | `CollectionCanvas` — круги + анимации |
| `gui.model` | `HumanBeingFx` (JavaFX-обёртка), `CollectionStore` (наблюдаемое хранилище) |
| `gui.net` | `GuiGateway` (synchronized-обёртка), `LoginService`, `Poller` |
| `gui.i18n` | `LocaleManager`, `Localizer` |
| `gui.util` | `UserColorAssigner`, `MoodColorMap`, `LocalizedFormatter`, `Dialogs`, `ShowResponseParser` |

Ресурсы:

```
client/src/main/resources/
├── fxml/        login.fxml, main.fxml, edit-dialog.fxml,
│                insert-dialog.fxml, object-popup.fxml
├── i18n/        messages*.properties (4 локали + fallback)
└── css/         style.css
```

### 2.3 Точка входа и fat-jar

JavaFX и `maven-shade-plugin` имеют известный конфликт: если main-класс
наследует `Application`, JRE требует наличие модулей JavaFX в
module-path и отказывается стартовать. Решение — класс-посредник
`gui.Launcher`:

```java
public class Launcher {
    public static void main(String[] args) {
        App.main(args);  // делегируем в Application.launch
    }
}
```

В `client/pom.xml` `mainClass = gui.Launcher`. `App` — отдельный класс,
наследующий `Application`.

В `maven-shade-plugin`:
- `ServicesResourceTransformer` — для корректного объединения
  `META-INF/services` JavaFX (без него FXML loader не находит
  built-in элементы).
- Фильтры `module-info.class`, `META-INF/*.SF/.DSA/.RSA` — без них
  fat-jar валится с подписью.

Fat-jar выходит ~8 МБ для macOS arm64.

### 2.4 Глобальное состояние: Session

Singleton `gui.Session` хранит:
- `SessionContext` — login + passwordHash после успешного входа;
- `GuiGateway` — единственное TCP-подключение на всё приложение.

Это упрощает доступ из любого контроллера: `Session.get().context()`,
`Session.get().gateway()`. Альтернатива — DI или передача через
конструкторы — избыточно для размера проекта.

### 2.5 Сценарий синхронизации

```
                  каждые 3 с                на onSucceeded
Poller (background) ────────────► Request("show", ..., login, hash)
        │                                          │
        │                                          ▼
        │                    Response.payload (List<HumanBeingEntry>)
        │                                          │
        │                                          ▼
        └────► Platform.runLater() ──► CollectionStore.sync(entries)
                                                    │
                                                    ├── ObservableList → TableView
                                                    └── ListChangeListener → CollectionCanvas
                                                                ├── add → Timeline scale-in
                                                                ├── remove → Timeline scale-out
                                                                └── update → smooth move
```

Poller — JavaFX `ScheduledService<List<HumanBeingEntry>>` с интервалом
3 сек и экспоненциальным backoff (до 5 сек) при сбое сети.
`createTask()` вызывает `gateway.sendBlocking(request)` (synchronized
на gateway, чтобы UI-команды и polling не конкурировали за сокет).

---

## 3. Детали реализации

### 3.1 Локализация (этап 3)

#### Файлы свойств

Один fallback (русский, без суффикса) и три локализованных:

```
i18n/messages.properties        ← fallback (русский)
i18n/messages_mk.properties
i18n/messages_fr.properties
i18n/messages_es_NI.properties
```

Java 17 `ResourceBundle.getBundle("i18n.messages", locale)` сам делает
правильный fallback: `messages_es_NI` → `messages_es` →
`messages.properties`. Файлы UTF-8 — Java 9+ корректно их читает.

Около 80 ключей: `app.title`, `login.*`, `toolbar.*`, `table.*`,
`popup.*`, `field.*`, `edit.error.*`, `dialog.*`, `mood.*` и т.д.

#### LocaleManager

Singleton с `ObjectProperty<Locale>`. Метод `tr(key)` возвращает строку,
`tr(key, args)` — через `MessageFormat` (для `{0}`, `{1}` подстановок).

```java
public String tr(String key, Object... args) {
    String pattern = ResourceBundle.getBundle("i18n.messages", getLocale())
            .getString(key);
    return new MessageFormat(pattern, getLocale()).format(args);
}
```

#### Localizer и реактивный текст

Ключевое требование — переключение языка **без перезапуска**. Решение:
строковые свойства UI биндятся к `StringBinding`, зависящему от
`localeProperty()`.

```java
public static StringBinding binding(String key) {
    Callable<String> compute = () -> LocaleManager.get().tr(key);
    return Bindings.createStringBinding(compute,
            LocaleManager.get().localeProperty());
}

public static void bind(StringProperty property, String key) {
    property.bind(binding(key));
}
```

При вызове `LocaleManager.get().setLocale(Locale.FRENCH)` все
`StringBinding`-и автоматически пересчитываются JavaFX-биндинговой
системой, и подписи мгновенно меняются.

#### Числа и даты

`gui/util/LocalizedFormatter` использует `NumberFormat.getNumberInstance(locale)`
для double, `NumberFormat.getIntegerInstance(locale)` для long/int,
`DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(locale)`
для дат.

Например, число 145.5 в русской локали → `"145,5"`, в испанской
(Никарагуа) → `"145.5"`.

#### Refresh таблицы

Кастомные `cellFactory` в TableView не пересоздаются при смене локали —
их `updateItem()` дёргается только при изменении item. Чтобы
локализованный текст ячеек настроения и владельца обновился, на
изменение `localeProperty()` вешается `objectsTable.refresh()`:

```java
LocaleManager.get().localeProperty().addListener(
    (obs, prev, value) -> objectsTable.refresh());
```

### 3.2 Сетевой слой

#### GuiGateway

`synchronized`-обёртка над `ServerGateway`:

```java
public synchronized Response sendBlocking(Request request)
        throws ServerUnavailableException {
    return gateway.send(request);
}

public Task<Response> sendTask(Request request) {
    return new Task<>() {
        @Override protected Response call() throws Exception {
            return sendBlocking(request);
        }
    };
}
```

`Task<Response>` удобен в контроллерах: подписываемся на
`onSucceeded`/`onFailed`, не думая о потоках — JavaFX сам вызовет
их в FX-thread.

#### LoginService

Строит `Request("login"|"register", null, null, login, HashUtil.hash(password))`
и при успехе записывает credentials в `Session`.

```java
public Task<Response> login(String login, String password) {
    String hash = HashUtil.hash(password);
    Request request = new Request("login", null, null, login, hash);
    Task<Response> task = gateway.sendTask(request);
    task.setOnSucceeded(e -> {
        if (task.getValue().isSuccess()) {
            Session.get().context().setCredentials(login, hash);
        }
    });
    return task;
}
```

### 3.3 Модель и хранилище

#### HumanBeingFx

JavaFX-обёртка с `LongProperty`/`DoubleProperty`/`StringProperty`/
`ObjectProperty<Mood>` для каждого поля. Метод `applyFrom(entry)`
обновляет все поля — это работает в паре с `CollectionStore.sync()`:
ссылка на FX-объект остаётся той же, поэтому биндинги в TableView и
кружки на канвасе автоматически перерисуются.

Метод `matchesFilter(text)` — фильтрация по любому полю через Streams:

```java
public boolean matchesFilter(String text) {
    if (text == null || text.isBlank()) return true;
    String needle = text.toLowerCase().trim();
    return Stream.of(
            String.valueOf(id.get()), name.get(), /* и т.д. */)
            .filter(Objects::nonNull)
            .map(String::toLowerCase)
            .anyMatch(s -> s.contains(needle));
}
```

#### CollectionStore

`ObservableList<HumanBeingFx>` (для TableView и канваса) +
`Map<Long, HumanBeingFx>` (для O(1) lookup по ключу).

```java
public void sync(List<HumanBeingEntry> entries) {
    Set<Long> incoming = new HashSet<>();
    for (HumanBeingEntry e : entries) {
        incoming.add(e.getKey());
        HumanBeingFx existing = byKey.get(e.getKey());
        if (existing == null) {
            HumanBeingFx fresh = HumanBeingFx.from(e);
            byKey.put(e.getKey(), fresh);
            items.add(fresh);              // ← триггерит add-event
        } else {
            existing.applyFrom(e);          // ← обновление полей,
                                            //   биндинги сами обновятся
        }
    }
    items.removeIf(fx -> {
        if (!incoming.contains(fx.getKey())) {
            byKey.remove(fx.getKey());
            return true;                    // ← триггерит remove-event
        }
        return false;
    });
}
```

`ListChangeListener` в `CollectionCanvas` ловит add/remove события и
запускает анимации.

### 3.4 Канвас визуализации

`CollectionCanvas extends Pane` (а не `Canvas`) — потому что нужны
клики на отдельные объекты и индивидуальные анимации. Каждому объекту
соответствует JavaFX `Circle`, добавленный в children.

```
          canvasPane (FXML)
                │
                ▼
        CollectionCanvas (Pane)
        ├── Circle (key=1, fill=терракот)
        ├── Circle (key=2, fill=зелёный)
        └── ...
```

#### Координаты → пиксели

```java
private double pixelX(double xCoord) {
    double w = Math.max(getWidth(), 100);
    return w / 2.0 + (xCoord / X_RANGE) * (w / 2.0);
}

private double pixelY(int yCoord) {
    double h = Math.max(getHeight(), 100);
    // Y инвертирован — положительный Y вверху, как в декартовой системе
    return h / 2.0 - ((double) yCoord / Y_RANGE) * (h / 2.0);
}
```

Видимая область координат: `X ∈ [-1000, 1000]`, `Y ∈ [-600, 600]`.
Объекты вне диапазона рисуются за краем.

#### Радиус

```java
double targetRadius(HumanBeingFx fx) {
    return 12 + Math.min(48, Math.abs(fx.getImpactSpeed()) / 10.0);
}
```

Минимум 12 px (чтобы было видно), максимум 60 px.

#### Цвет

`UserColorAssigner.colorFor(login)` — палитра из 8 цветов, выбор
детерминирован: `palette[Math.floorMod(login.hashCode(), 8)]`. Один и
тот же логин на любом клиенте получит один и тот же цвет.

Палитра подобрана по дизайну `GUI.pdf`:

| Цвет | HEX | Назначение |
|---|---|---|
| Терракот | `#c8593a` | акцент, primary user |
| Зелёный | `#5d8b6a` | secondary user |
| Фиолетовый | `#a772a4` | tertiary |
| Синий | `#4a6f8a` | |
| Охра | `#c79a3e` | |
| Розовый-тёмный | `#8a4a6f` | |
| Серо-коричневый | `#6e655a` | |
| Бирюзовый | `#4a8a89` | |

#### Анимации

```java
private static final double ADD_MS = 350;
private static final double REMOVE_MS = 250;
private static final double UPDATE_MS = 200;
```

- **Добавление** (по требованию пользователя — «круг появляется из
  точки»):
  ```java
  Timeline appear = new Timeline(new KeyFrame(Duration.millis(ADD_MS),
          new KeyValue(circle.radiusProperty(), targetRadius(fx))));
  ```
  Радиус растёт от 0 до финального значения.

- **Удаление** (обратный эффект):
  ```java
  Timeline shrink = new Timeline(new KeyFrame(Duration.millis(REMOVE_MS),
          new KeyValue(circle.radiusProperty(), 0)));
  shrink.setOnFinished(e -> getChildren().remove(circle));
  ```

- **Обновление координат/скорости**: `Timeline` интерполирует
  `centerXProperty`, `centerYProperty`, `radiusProperty` за 200 мс.
  Слушатели вешаются на `xProperty`, `yProperty`, `impactSpeedProperty`
  объекта `HumanBeingFx`, поэтому когда `applyFrom()` обновляет поле,
  анимация запускается автоматически.

### 3.5 Таблица: фильтрация и сортировка через Streams API

```java
FilteredList<HumanBeingFx> filtered =
        new FilteredList<>(store.items(), fx -> true);
filterField.textProperty().addListener((obs, prev, value) ->
        filtered.setPredicate(fx -> fx.matchesFilter(value)));

SortedList<HumanBeingFx> sorted = new SortedList<>(filtered);
sorted.comparatorProperty().bind(objectsTable.comparatorProperty());
objectsTable.setItems(sorted);
```

`HumanBeingFx.matchesFilter()` использует `Stream.of(...)` →
`.filter()` → `.map(String::toLowerCase)` → `.anyMatch()`. Сортировка —
`SortedList` слушает `tableView.comparatorProperty()`, который меняется
при клике на заголовок.

Цветной `cellFactory`:

```java
ownerCol.setCellFactory(col -> new TableCell<>() {
    @Override protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) { setText(null); setStyle(""); return; }
        setText(item);
        setStyle("-fx-text-fill: " + UserColorAssigner.hexFor(item)
                + "; -fx-font-weight: bold;");
    }
});
```

Аналогично для `moodCol` через `MoodColorMap`.

### 3.6 Диалоги

#### Insert/Edit (один контроллер на два FXML)

`gui.controllers.ObjectFormController` обслуживает оба FXML:

- `insert-dialog.fxml` содержит дополнительное поле «Ключ» (`keyField`).
- `edit-dialog.fxml` его не содержит — соответствующий `@FXML`-поле
  будет `null`. Контроллер проверяет `if (keyField != null)` и
  ведёт себя по `Mode.INSERT`/`Mode.EDIT`.

Открытие — статические методы:

```java
public static void openInsert(Window owner, Runnable onSaved) {
    open(owner, "/fxml/insert-dialog.fxml", Mode.INSERT, null, onSaved);
}

public static void openEdit(Window owner, HumanBeingFx fx, Runnable onSaved) {
    open(owner, "/fxml/edit-dialog.fxml", Mode.EDIT, fx, onSaved);
}
```

Обе создают `Stage` с `Modality.WINDOW_MODAL`, загружают FXML,
конфигурируют контроллер. После успешного сохранения — `onSaved.run()`
(в `MainController` это `poller.restart()` — мгновенная синхронизация
без ожидания 3 сек).

Валидация полей собрана в `buildHumanBeingFromFields()`:

```java
if (name.isEmpty() || soundtrack.isEmpty())
    throw new FieldValidationException("edit.error.empty");
try {
    x = Double.parseDouble(xField.getText().trim().replace(',', '.'));
    /* ... */
} catch (NumberFormatException nfe) {
    throw new FieldValidationException("edit.error.number");
}
if (x <= -975)
    throw new FieldValidationException("edit.error.x");
```

`FieldValidationException` несёт ключ ResourceBundle, его сообщение
локализуется при показе.

#### Popup карточка

`ObjectPopupController` открывается как `Stage(StageStyle.UNDECORATED)`.
Заливка заголовка устанавливается inline-стилем по логину владельца:

```java
String ownerHex = UserColorAssigner.hexFor(fx.getOwnerLogin());
headerBox.setStyle("-fx-background-color: " + ownerHex + ";");
```

Кнопка «Редактировать» видна только если
`fx.getOwnerLogin().equals(Session.get().context().getLogin())`.

Закрытие при потере фокуса:

```java
stage.focusedProperty().addListener((obs, prev, focused) -> {
    if (!focused) stage.close();
});
```

#### Универсальные диалоги (info/confirm/prompt)

`gui/util/Dialogs` — статические методы. Каждый создаёт `Stage` с тёмной
шапкой, телом и стандартными кнопками. Заголовки берутся через
`LocaleManager.get().tr(titleKey)`. Используются для:
- результатов команд `info`, `help`, `history`, `print_*` (`Dialogs.info`);
- подтверждений `delete`, `clear` (`Dialogs.confirm`);
- ввода значений для `remove_lower` (имя), `remove_greater_key` (ключ),
  `remove_all_by_minutes_of_waiting` (минуты) (`Dialogs.prompt`).

### 3.7 Скрипты

Реализация осознанно ограничена «stateless»-командами без объектов
(набор: `info`, `help`, `history`, `show`, `clear`, `remove_key`,
`remove_greater_key`, `remove_all_by_minutes_of_waiting`,
`print_ascending`, `print_field_ascending_impact_speed`).

Причина: в CLI скрипты с `insert`/`update`/`remove_lower` парсятся
многострочно (имя, координата X, координата Y, …). В GUI ввод объекта
делается формой — повторять консольный парсер не имеет смысла.
Многострочный ввод объекта в скрипте лишает скрипт-режим практической
ценности (проще нажать «+ Добавить»).

Реализация — обычный `Files.readAllLines()` + цикл:

```java
String[] parts = line.split("\\s+");
String cmd = parts[0];
String[] args = java.util.Arrays.copyOfRange(parts, 1, parts.length);
if (!isStatelessCommand(cmd)) {
    log.append("skip — " + cmd + " (требует объект)").append('\n');
    continue;
}
Response resp = Session.get().gateway().sendBlocking(
        new Request(cmd, args, null, ctx.getLogin(), ctx.getPasswordHash()));
log.append(resp.getMessage()).append('\n');
```

Лог показывается через `Dialogs.info`.

---

## 4. Дизайн (по `GUI.pdf`)

### Палитра

| Назначение | HEX |
|---|---|
| Тёмная шапка / primary-кнопка | `#1f1f1f` |
| Текст шапки | `#f4ede0` |
| Фон страницы | `#f4ede0` |
| Карточка / status-bar / tooltip-фон | `#ebe2d1` |
| Граница | `#d8cdb6` |
| Подпись поля (caption) | `#6e655a` |
| Терракот (accent) | `#c8593a` |
| Терракот hover | `#b04d31` |
| Канвас фон | `#2b2b2b` |

### Типографика

- `-fx-font-family: System` (родной шрифт ОС).
- Заголовки шапки: 22 px, bold, `letter-spacing: 0.15em`.
- Captions полей и колонок: 11 px, bold, `letter-spacing: 0.18em`,
  всегда `UPPERCASE` (по дизайну).
- Тело: 13 px regular.

### Компоненты

| FXML-элемент | CSS-класс | Особенности |
|---|---|---|
| Шапка login | `.header` + `.header-title`/`.header-subtitle` | Чёрный фон, центрирование |
| Шапка main | `.header-main` | Левый title + правый user + locale combo |
| Тулбар | `.toolbar` + `.toolbar-btn` | Плоские кнопки без отступа между ними |
| Кнопка primary | `.btn-primary` | Чёрный фон, белый текст |
| Кнопка accent | `.btn-accent` | Терракот фон, белый текст |
| Кнопка secondary | `.btn-secondary` | Бежевая, граница |
| Карточка | `.card` | Бежевый фон, `padding 28 32` |
| Текстовый ввод | `.text-input` | Белый фон, бордер-цвет фокуса терракот |
| Чекбокс «Реальный герой» | `.form-check-real-hero` | Заливка терракот при selected |
| Чекбокс «Зубочистка» | `.form-check-toothpick` | Бежевая заливка с тёмной галкой при selected |
| Канвас header | `.canvas-header` | Чёрная полоса с заголовком ВИЗУАЛИЗАЦИЯ |
| Status bar | `.status-bar` | Бежевый, верхний бордер |
| Popup-root | `.popup-root` | Drop-shadow, скруглённые углы |
| Popup-header | `.popup-header` | Заливка цвета владельца (inline-стиль) |
| Form-root | `.form-root` | Drop-shadow для модального диалога |

### Иконки

Использованы Unicode-символы (без сторонних шрифтов):
- `+` — добавить;
- `🗑` — удалить;
- `ⓘ` — инфо;
- `▤` — скрипт;
- `🔍` — фильтр;
- `▦` — канвас visualisation;
- `👤` — пользователь.

На macOS отрисовываются Apple Color Emoji + системным шрифтом.

---

## 5. Этапы разработки

Реализация разбита на 10 этапов, каждый — один git-коммит,
тестируемый независимо. Перед коммитом — `mvn clean package` и
запуск проверкой что нет ошибок инициализации.

| Этап | Что сделано | Коммит |
|---|---|---|
| 1 | `Response.payload` + `HumanBeingEntry` + `Show`-payload | `7ba88c1` |
| 2 | Реструктуризация client → `src/main/java`, JavaFX, Launcher, заглушка login.fxml | `1e326e5` |
| 3 | `LocaleManager`, `Localizer`, `LocalizedFormatter`, 4 .properties | `50114e2` |
| 4 | `LoginController`, `GuiGateway`, `LoginService`, login.fxml по дизайну, базовый style.css | `2f1a8f0` |
| 5 | `HumanBeingFx`, `CollectionStore`, `Poller`, `ShowResponseParser` | `3493bb9` |
| 6 | `MainController`, main.fxml, таблица + фильтр + сортировка через Streams, статус-бар | `96d383d` |
| 7 | `CollectionCanvas`, `UserColorAssigner`, `MoodColorMap`, `ObjectPopupController` | `db69c45` |
| 8 | `ObjectFormController` (insert/edit), валидация, открытие из тулбара/таблицы/popup | `75d578c` |
| 9 | Команды тулбара (delete, clear, info, help, script, history) + подменю «ЕЩЁ», `Dialogs` | `fbf55da` |
| 10 | Иконки в тулбаре, refresh таблицы при смене локали, размеры окна | `15c6246` |

---

## 6. Trade-offs и ограничения

### 6.1 Backend изменения

Минимальное расширение протокола (`Response.payload`) — было
необходимо, иначе таблица не имеет `КЛЮЧ` и `ВЛАДЕЛЕЦ`. CLI остался
работоспособен (старый конструктор сохранён).

### 6.2 Скрипты

Ограничены stateless-командами (см. 3.7). Объекты добавляются формой.

### 6.3 Координатная система канваса

Видимая область фиксирована — `X ∈ [-1000, 1000]`, `Y ∈ [-600, 600]`.
Объекты с экстремальными координатами окажутся за краем. Можно
заменить на автомасштабирование по `min`/`max` текущих координат, но
тогда каждый новый объект будет «двигать» все остальные — менее
интуитивно.

### 6.4 Polling vs push

Автообновление реализовано через polling каждые 3 с. Альтернатива —
push с сервера (broadcast). Это потребовало бы изменить серверный
протокол (новый тип сообщения, registry клиентов). Polling проще и
соответствует требованию «изменения видны через ≤ N секунд».

### 6.5 Анимация

Длительности (`ADD_MS = 350`, `REMOVE_MS = 250`, `UPDATE_MS = 200`)
вынесены в константы `CollectionCanvas` — легко изменить, если
преподаватель попросит другие значения.

### 6.6 Цветовая палитра пользователей

8 цветов хватает для небольшого числа одновременных пользователей.
При коллизии (`hashCode % 8` совпал у двух логинов) цвета будут
одинаковыми. Для демо-сценария лабораторной этого достаточно.

### 6.7 Закрытие popup при потере фокуса

Сейчас popup закрывается, когда фокус уходит на любое другое окно.
Это иногда раздражает (например, кликнули на Dock и popup закрылся).
В реальном приложении лучше использовать кастомный `auto-hide`
с проверкой целевого окна.

### 6.8 Размер fat-jar

8 МБ для macOS arm64. JavaFX содержит native-библиотеки (только для
текущей платформы, благодаря classifier `mac-aarch64`). Кросс-платформенный
JAR требовал бы classifier'ов для всех ОС — увеличил бы размер до
~30 МБ.

---

## 7. Чеклист требований

| Требование | Реализация |
|---|---|
| JavaFX | ✓ JavaFX 17.0.9 |
| 4 локали в .properties | ✓ ru/mk/fr/es_NI |
| Числа/даты по локали | ✓ `LocalizedFormatter` |
| Переключение языка без перезапуска | ✓ через `StringBinding` на `localeProperty` |
| Окно авторизации/регистрации | ✓ `LoginController` + login.fxml |
| Текущий пользователь | ✓ в шапке main: `Пользователь: {0}` |
| Таблица, каждое поле — колонка | ✓ 8 колонок |
| Фильтр и сортировка через Streams API | ✓ `matchesFilter` + `FilteredList` + `SortedList` |
| Все команды лаб 7 | ✓ insert, update, remove_key, clear, remove_lower, remove_greater_key, remove_all_by_minutes_of_waiting, info, help, history, execute_script, show, print_ascending, print_field_ascending_impact_speed |
| Канвас с примитивами | ✓ JavaFX `Circle` |
| Координаты + размер | ✓ `pixelX/pixelY` + `targetRadius` |
| Разные цвета для разных пользователей | ✓ `UserColorAssigner` |
| Клик → информация | ✓ `ObjectPopupController` |
| Автообновление у всех клиентов | ✓ `Poller` 3 с |
| Анимация (согласована с пользователем) | ✓ scale 0→r при add, r→0 при remove, smooth интерполяция |
| Редактирование из таблицы и из канваса | ✓ двойной клик в таблице + кнопка в popup |
| Удаление | ✓ кнопка УДАЛИТЬ с подтверждением |

---

## 8. Что можно улучшить дальше

- **Push-обновления** вместо polling (subscribe-канал на сервере).
- **Кросс-платформенный fat-jar** через classifier'ы JavaFX.
- **Тулинг** — добавить интеграционные тесты с testcontainers
  PostgreSQL и проверкой полного сценария (login → insert →
  второй клиент видит).
- **Settings** — диалог для смены пароля, выбора темы, настройки
  периода Poller.
- **Логирование** на клиенте через SLF4J с уровнем INFO/DEBUG.
- **Preset locales** — детект системной локали при первом запуске,
  сохранение выбора пользователя в `~/.lab9.properties`.
- **Доступность** — mnemonic'и (Alt+1, Alt+2 …), tab order, скринридер.
- **i18n покрытие** — некоторые server-side сообщения остаются на
  русском (например, ошибки SQL); клиент их показывает as-is. Можно
  ввести коды ошибок в `Response`.
