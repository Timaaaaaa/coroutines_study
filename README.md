# Coroutines study guide

Практический набор примеров по ключевым темам Kotlin Coroutines. Каждая секция имеет собственный `main()` (см. Gradle-задачи `:app:runLessonXX...`) и покрыта тестами на основе `kotlinx-coroutines-test`.

## Как запускать

- Запустить определённый блок: `./gradlew :app:runLesson01Basics`, `./gradlew :app:runLesson04Structured`, и т.д.
- Прогнать все примеры тестов: `./gradlew :app:test`.
- Все примеры лежат в модуле `app` (JVM, без Android SDK и `GlobalScope`).

## Структура примеров

- `app/src/main/java/com/way/samurai/lessons/Lesson01Basics.kt` — стартовые конструкции (`runBlocking`, `coroutineScope`).
- `Lesson02ContextAndDispatchers` — выбор диспетчеров, избегание лишних переключений.
- `Lesson03CancellationAndTimeouts` — отмена, `withTimeout/withTimeoutOrNull`, `NonCancellable` только в `finally`.
- `Lesson04StructuredConcurrency` — fan-out/fan-in c `async/awaitAll`.
- `Lesson05ExceptionsAndSupervision` — разница `coroutineScope`/`supervisorScope`, `SupervisorJob`, `CoroutineExceptionHandler`.
- `Lesson06FlowVsChannel` — отличие cold/hot, `StateFlow` для UI state, `SharedFlow` для событий, `Channel` для очереди задач, backpressure/buffer/conflate.
- `Lesson07ConcurrencyPrimitives` — `Mutex`, `Semaphore`, atomics, примеры rate/concurrency limit.
- `Lesson08Performance` — CPU vs I/O, кооперативная отмена (`yield()`/`ensureActive()`), избегание лишних `withContext`.
- `Lesson09Testing` — `runTest`, `StandardTestDispatcher`, управление временем.
- `Lesson10AndroidPatterns` — псевдо-ViewModel без Android SDK с `SupervisorJob` и `flatMapLatest` для поиска.
- Общие реалистичные сущности: `FakeApi` (рандомные фейлы+`delay`), `FakeDb` (медленный доступ), `Repository` (кэш + сеть + база), пагинация с отменой предыдущих запросов через `flatMapLatest` в `UseCases`.

## Правила корутин в продакшне (чек-лист)

1. Строго структурная конкуррентность: дети живут в переданном scope, никакого `GlobalScope`.
2. Используйте `coroutineScope`/`supervisorScope` для группировки работы и автоматической отмены.
3. Fan-out/fan-in делайте через `async {}` + `awaitAll()` для независимых подзадач.
4. Отмена — это нормальный контроль потока: не логируйте `CancellationException` как ошибку.
5. Всегда освобождайте ресурсы в `finally` + `withContext(NonCancellable)` только для закрытия ресурсов.
6. Ставьте таймауты на небезопасные операции (`withTimeout`, `withTimeoutOrNull`).
7. Разделяйте диспетчеры: `Dispatchers.Default` для CPU, `Dispatchers.IO` для блокирующего I/O.
8. Не переключайте диспетчер в tight loop; по возможности оставайтесь на одном до конца цепочки.
9. Для UI состояния используйте `StateFlow`, для одноразовых событий — `SharedFlow`, для очереди задач — `Channel`.
10. Добавляйте backpressure: `buffer`, `conflate`, `Semaphore`/`Mutex` для ограничений.
11. Покрывайте конкурентную логику тестами через `runTest` и контролируемые диспетчеры.
12. Для конкурентных наборов запросов используйте `Semaphore` для лимита параллельности и `Mutex` для критических секций.
13. Не оборачивайте отмену/исключения в catch-all без повторного выброса `CancellationException`.
14. Настраивайте `CoroutineName` и `CoroutineExceptionHandler` для дебага и читаемых логов.
15. Держите корутины ближе к владельцу жизненного цикла (ViewModel/UseCase/репозиторий), а не в слоях утилит.

## Типовые анти-паттерны

- `GlobalScope.launch` для бизнес-логики — приводит к утечкам и неотслеживаемым ошибкам.
- Неограниченный `launch` из UI без отмены/жизненного цикла.
- Лишние `withContext(Dispatchers.IO)` вокруг уже неблокирующего кода.
- Игнорирование отмены (бесконечные циклы без `yield()`/`ensureActive()`).
- Логирование `CancellationException` как ошибки.
- Принудительный `try/catch` вокруг `coroutineScope` без различия `CancellationException` и настоящих ошибок.
- Горячий `Channel` без потребителя или без лимитов емкости (может привести к OOM).
- Запуск большого числа `async` без сбора `awaitAll` (утечки и незавершённые работы).

## Шпаргалка по диспетчерам

- `Dispatchers.Default` — CPU-bound, небольшие массивы данных, парсинг.
- `Dispatchers.IO` — блокирующие вызовы (JDBC, файловая система, сеть через legacy API).
- `Dispatchers.Main` — UI/главный поток (Android/Compose). В примерах используем Default как замену.
- Избегайте частых прыжков между диспетчерами; переключайтесь только при смене природы работы.

## Шпаргалка по Flow/Channel

- Flow — cold, создаёт работу на каждом подписчике. Хорош для запросов/выборок.
- `StateFlow` — горячий, хранит последнее значение, подходит для UI state.
- `SharedFlow` — горячий без состояния, подходит для событий (навигация, сообщения).
- `Channel` — горячая очередь точка-точка; используйте буферы/`conflate`/`Semaphore` для backpressure.
- Инструменты: `debounce`/`flatMapLatest` для отмены старых запросов (пример пагинации), `buffer` для разгрузки продюсера, `conflate` чтобы брать только последнее.

## Отмена и ресурсы

- Отмена — нормальный сценарий: не логируйте `CancellationException` как crash.
- `NonCancellable` допустим только внутри `finally` для корректного закрытия ресурсов.
- `withTimeout` / `withTimeoutOrNull` защищают от зависаний.
- Для кооперативной отмены используйте `yield()` или `ensureActive()` в длинных циклах.

## Ошибки и supervision

- `coroutineScope` отменяет всех детей при ошибке одного.
- `supervisorScope` изолирует детей; используйте `SupervisorJob` для долгоживущих scope (ViewModel, UseCase), плюс `CoroutineExceptionHandler` для логов верхнего уровня.
- Показывайте в логах, что child failure отменяет родителя, а supervisor позволяет пережить ошибку.

## Оптимизация

- Избегайте лишних `withContext` для чисто вычислительных функций — держите один диспетчер.
- Не переключайте dispatcher внутри tight loop; лучше разово задать нужный.
- `Dispatchers.Default` — CPU, `Dispatchers.IO` — блокирующий I/O.
- Кооперативная отмена в циклах через `yield()`/`ensureActive()`.
- `async` — когда нужен результат; `launch` — когда результат не нужен и важен fire-and-forget внутри scope.

## Debugging

- Добавляйте `CoroutineName` в контекст, чтобы видеть человека читаемые имена в логах.
- Подключён `kotlinx-coroutines-debug` — можно включить агент JVM (`-javaagent:kotlinx-coroutines-debug.jar`) для отслеживания стеков.
- Логируйте контекст верхнего уровня через `coroutineContext` и `CoroutineExceptionHandler` (в примерах секции 05).

## Что под капотом (коротко)

- Каждая корутина — это state machine, которую компилятор раскладывает в класс с `suspend` меткой и продолжениями.
- `suspend` функции возвращаются, не блокируя поток: продолжение откладывается в очередь выбранного `Dispatcher`.
- Отмена хранится в `Job`, и диспетчеры/библиотечные `suspend`-функции проверяют `isActive` чтобы бросить `CancellationException`.
- Структурная конкуррентность строит дерево `Job`, где родитель автоматически отменяет детей при сбое (кроме supervisor).

## Пагинация и отмена старых запросов

- В `UseCases` используется `flatMapLatest` + `debounce`, чтобы новая строка поиска отменяла предыдущий запрос к `FakeApi`.
- `Repository` кэширует страницы (`InMemoryCache`) и использует `FakeDb`/`FakeApi` с задержками и рандомными ошибками.

## Чек-лист перед код-ревью

- [ ] Нет `GlobalScope`, все корутины висят на переданном scope.
- [ ] Ошибки изолированы через `supervisorScope`/`SupervisorJob`, где это оправдано.
- [ ] Таймауты стоят на потенциально долгих операциях.
- [ ] Отмена не логируется как ошибка, ресурсы чистятся в `finally` + `NonCancellable`.
- [ ] Нет лишних `withContext`; диспетчеры выбраны по типу работы.
- [ ] Потоки данных используют подходящий тип (`StateFlow`/`SharedFlow`/`Channel`) и backpressure.
- [ ] Fan-out/fan-in реализован через `async/awaitAll` без утечек.
- [ ] Тесты покрывают отмену, таймаут, supervisor и поведение Flow.
