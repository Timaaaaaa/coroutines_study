# Coroutine Study Project

Практическая песочница для корутин уровня продакшн. Все примеры написаны без `GlobalScope`, с упором на структурированность и предсказуемость поведения.

## Модули
- `:core` — общие утилиты: диспетчеры, результат, логгер, `FakeApi`/`FakeDb`, репозиторий.
- `:examples:01_basics` — запуск, `async/awaitAll`, fan-out/fan-in.
- `:examples:02_context_and_dispatchers` — работа с контекстом и диспетчерами.
- `:examples:03_cancellation_and_timeouts` — отмена, `withTimeout/withTimeoutOrNull`, `NonCancellable` в `finally`.
- `:examples:04_structured_concurrency` — отличие `coroutineScope` и `supervisorScope`.
- `:examples:05_exceptions_supervision` — `SupervisorJob`, `CoroutineExceptionHandler`.
- `:examples:06_flow_vs_channel` — cold `Flow` против hot `Channel/SharedFlow/StateFlow`.
- `:examples:07_concurrency_primitives` — `Mutex`, `Semaphore`, атомики.
- `:examples:08_performance` — оптимизация переключений и кооперативная отмена.
- `:examples:09_testing` — `kotlinx-coroutines-test`: `runTest`, `StandardTestDispatcher`, примеры тестов.
- `:examples:10_android_patterns` — псевдо-ViewModel: `SupervisorJob`, `StateFlow`, дебаунс поиска.

Каждый модуль имеет `main()` для быстрого запуска из консоли: `./gradlew :examples:01_basics:run` (или аналогично для других модулей).

## Правила корутин в продакшне (15 пунктов)
1. Не используйте `GlobalScope` — только `coroutineScope`, `supervisorScope` или явные `CoroutineScope` с жизненным циклом.
2. Всегда учитывайте структурированную конкуренцию: дети отменяют родителя при ошибке, если не используется supervisor.
3. `SupervisorJob` применяйте только там, где изоляция ошибок оправдана (например, независимые UI-виджеты).
4. Ошибки дочерних корутин должны быть видимы через `CoroutineExceptionHandler` или `await`/`join` — не глотайте исключения.
5. Для CPU-работы используйте `Dispatchers.Default`, для блокирующего I/O — `Dispatchers.IO`.
6. Не переключайте диспетчеры внутри tight loop — переключения стоят дорого.
7. Для fan-out/fan-in используйте `async` + `awaitAll`; для fire-and-forget — `launch`, но только внутри валидного scope.
8. Отмену делайте кооперативной: `ensureActive()`, `yield()`, точки приостановки (`delay`, `flow` операторы).
9. `CancellationException` — это нормальный путь завершения, не логируйте как ошибку.
10. Таймауты: `withTimeout` бросает, `withTimeoutOrNull` возвращает `null` — выбирайте осознанно.
11. `NonCancellable` — только для обязательного `finally`/cleanup.
12. Не злоупотребляйте `withContext` — переключайте диспетчер только когда это действительно нужно.
13. Давайте имена (`CoroutineName`) для сложных деревьев — проще дебажить.
14. Для потоков данных предпочитайте `Flow`/`StateFlow`/`SharedFlow`; `Channel` — для очередей и горячих источников.
15. Покрывайте критичные сценарии тестами через `kotlinx-coroutines-test` с виртуальным временем.

## Типовые анти-паттерны
- `GlobalScope.launch { ... }` без контроля жизненного цикла.
- Перехват `CancellationException` и логирование как ошибок.
- Запуск тяжёлых задач на Main/Default без разграничения CPU vs I/O.
- Массовое использование `withContext` внутри циклов.
- Игнорирование результатов `async` (невызов `await`/`awaitAll`).
- Отсутствие таймаутов и повторов для внешних вызовов.
- `runBlocking` в продакшн-коде (допустимо только в тестах/примеров запуска).
- Глобальные `Channel`/`SharedFlow` без явного владельца и backpressure-стратегии.

## Шпаргалка по диспетчерам
- `Dispatchers.Default` — CPU-bound задачи, алгоритмы, парсинг.
- `Dispatchers.IO` — блокирующее I/O, сетевые и файловые операции.
- `Dispatchers.Unconfined` — только для спец-кейсов и тестов.
- Свой диспетчер/`CoroutineDispatcher` — для ограничений/троттлинга.

## Шпаргалка по Flow/Channel
- `Flow` — cold, каждый коллектор получает свой источник.
- `StateFlow` — хранит последнее состояние, подходит для UI state.
- `SharedFlow` — hot, хорош для единичных событий; настройте `replay` и `BufferOverflow` под задачу.
- `Channel` — очередь задач, требует явного закрытия, учитывайте backpressure (`capacity`, `buffer`, `conflate`).
- Для интенсивных потоков используйте `buffer`, `conflate`, `debounce`, `sample` в зависимости от сценария.

## Как дебажить корутины
- Добавляйте `CoroutineName` к веткам, чтобы видеть их в логах и stacktrace.
- Подключайте `kotlinx-coroutines-debug` (см. зависимость в каталоге версий) и JVM-флаг `-Dkotlinx.coroutines.debug` для отображения идентификаторов корутин в логах.
- Визуально проверяйте деревья: кто родитель, кто дочерний — это влияет на отмену/исключения.

## Запуск и тесты
- Пример запуска модуля: `./gradlew :examples:04_structured_concurrency:run`
- Тесты: `./gradlew :examples:09_testing:test` (используется `kotlinx-coroutines-test` с виртуальным временем).

## Пагинация и отмена
- Пагинация реализована в `Repository` через `refreshArticles`/`paginateSafely`.
- Для UI ввода применяйте `debounce + flatMapLatest` (см. `:examples:10_android_patterns`).
