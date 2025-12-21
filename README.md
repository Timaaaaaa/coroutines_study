# Koin Study for Android (Basics → Best Practices)

Учебный Android-проект на Kotlin, который показывает, как построить DI-архитектуру с Koin для небольшой фичи “Profile/Tasks”. Проект собирается как обычное Android-приложение (Compose UI) и содержит пошаговые задания, примеры анти-паттернов и тесты.

## Структура проекта и Koin-модулей

**Файловая структура**

- `app/src/main/java/com/way/samurai/koinstudy`
  - `di/Modules.kt` — все Koin-модули (core, network, data, feature).
  - `data/` — `FakeApi`, `TaskRemoteDataSource`, `UserRepositoryImpl`, диспетчеры.
  - `domain/` — модели, интерфейсы репозитория, use case.
  - `presentation/` — `MainActivity`, Compose-экран, `UserViewModel`, примеры scope/parameters.
- `app/src/test` — unit-тесты с koin-test (мок/override, checkModules).
- `app/src/androidTest` — smoke-тест и пример Koin в инструментальных тестах.

**Koin-модули**

- `coreModule` — базовые зависимости (Android context/resources, диспетчеры).
- `networkModule` — Retrofit + Moshi + HttpLoggingInterceptor + `FakeApi`.
- `dataModule` — `TaskRemoteDataSource`, `UserRepositoryImpl` (bind к `UserRepository`).
- `featureTasksModule`
  - UseCases: `GetUserUseCase`, `RefreshUserUseCase`.
  - Scope-пример: `scope(named(TaskWizardTracker.SCOPE_NAME)) { scoped { TaskWizardTracker() } }`.
  - ViewModels: `UserViewModel` и параметризованный `UserDetailsViewModel` (через `parametersOf`).

## Что такое DI и зачем

Внедрение зависимостей отделяет создание объектов от их использования. Это:
- Упрощает тестирование (можно подменить зависимости).
- Делает композицию зависимостей прозрачной.
- Снижает связность слоёв и защищает домен от деталей платформы.

## Koin за 10 минут

1. Описать модуль:
   ```kotlin
   val coreModule = module {
       singleOf(::AppDispatchers) { bind<DispatcherProvider>() }
   }
   ```
2. Запустить Koin в `Application`:
   ```kotlin
   startKoin {
       androidContext(this@KoinStudyApp)
       androidLogger(androidLoggerLevel())
       modules(coreModule, networkModule, dataModule, featureTasksModule)
   }
   ```
3. Получить зависимости:
   - В классе/функции: `get<FakeApi>()`.
   - В Activity/Compose: `val vm: UserViewModel = getViewModel()`.
4. ViewModel в Koin:
   ```kotlin
   viewModel { UserViewModel(get(), get(), get()) }
   ```
5. Параметры:
   ```kotlin
   viewModel { (userId: String) -> UserDetailsViewModel(userId, get()) }
   // получение: getViewModel<UserDetailsViewModel> { parametersOf("42") }
   ```
6. Scope:
   ```kotlin
   scope(named(TaskWizardTracker.SCOPE_NAME)) {
       scoped { TaskWizardTracker() }
   }
   // открытие scope: koin.createScope("wizard", named(TaskWizardTracker.SCOPE_NAME))
   ```

## Single vs Factory vs Scoped

- `single` — долгоживущие (API, Repository, Retrofit, UseCase без состояния).
- `factory` — лёгкие объекты без внутреннего состояния.
- `scoped` — живут пока открыт scope (экран/фича/мастерка).
- ViewModel в Koin использует специальную DSL `viewModel { ... }`, которая сама управляет жизненным циклом.

## Как организованы слои и зависимости

- **presentation** → зависит только от **domain** (получает use cases и интерфейсы).
- **domain** → чистый Kotlin, не знает о Koin/Android/Retrofit.
- **data** → реализует интерфейсы domain (через `bind<UserRepository>()`), использует Koin для своих зависимостей.
- **networkModule** предоставляет Retrofit/FakeApi, но UI не тянет эти зависимости напрямую.

Диаграмма зависимостей:
`MainActivity -> UserViewModel -> (GetUserUseCase, RefreshUserUseCase) -> UserRepository (интерфейс) -> UserRepositoryImpl -> TaskRemoteDataSource -> FakeApi`

## Анти-паттерны

- ❌ Service Locator и `GlobalContext.get()` в бизнес-логике.
- ❌ ViewModel получает `Context` напрямую (используйте `androidContext()` только внутри DI-модулей).
- ❌ Все зависимости в одном модуле без разделения по слоям/фичам.
- ❌ `single` для короткоживущих объектов или тяжелые фабрики без необходимости.
- ❌ Циклические зависимости между модулями/слоями.
- ❌ Ленивые инжекции без понимания жизненного цикла (не держать `by inject()` в статических синглтонах без очистки).

## Best Practices (и как они реализованы)

- Разделение модулей: `core`, `network`, `data`, `featureTasks`.
- Интерфейсы в domain + `bind()` в DI для реализаций.
- `single` для долгоживущих (`UserRepositoryImpl`, `FakeApi`, `Retrofit`), `factory` для use case.
- UI-модуль не тянет сетевые классы напрямую — только use case.
- Без `GlobalContext.get()` в бизнес-логике; старт Koin в `Application`.
- ViewModel не знает о `Context`, зависимости приходят через Koin.
- Scope-пример: `TaskWizardTracker` показывает, как ограничить жизненный цикл компонента.
- Параметры: `UserDetailsViewModel` с `parametersOf(userId)`.
- Избегаем циклов: domain не зависит от data, модули разнесены.
- Lazy injection: используйте осознанно (здесь — только `getViewModel()` в UI, без хранения в долгоживущих синглтонах).

## Реалистичный кейс “Profile/Tasks”

- `FakeApi.getUser()` — имитирует сеть (delay + 20% ошибок).
- `UserRepositoryImpl` — хранит `StateFlow<UserState>`, обновляет из `TaskRemoteDataSource`.
- `GetUserUseCase` — поток состояний.
- `RefreshUserUseCase` — триггер обновления.
- `UserViewModel` — конвертирует `UserState` в `UserUiState` (Loading/Content/Error).
- UI (`MainActivity` + Compose) — кнопка Load/Refresh и отображение состояния.
- Cancellation/flow: `StateFlow` + `collectLatest` в ViewModel, отмена управляется `viewModelScope`.

## Параметры и scope в коде

- Параметры: `UserDetailsViewModel(userId)` объявлен в `featureTasksModule`, берётся через `parametersOf("42")`.
- Scope: `TaskWizardTracker` создаётся внутри `scope(named(TaskWizardTracker.SCOPE_NAME))`, подходит для экранов-мастерок или фич-флоу.

## Ошибки и диагностика

- Типовая ошибка: `NoBeanDefFoundException` — нет биндинга или забыли модуль в `startKoin`.
- Диагностика:
  - `androidLogger()` или `androidLoggerLevel()` для логов.
  - `checkModules` тест (`KoinGraphCheckTest`) проверяет, что граф консистентен.
  - `koinApplication { printLogger() }` можно включить точечно.
- Неправильный scope/single:
  - Утечка состояния между экранами ⇒ используйте `scoped`.
  - Частое создание тяжёлых зависимостей ⇒ переключиться на `single`.

## Тестирование Koin

- Unit-тест с подменой зависимостей: `UserViewModelTest` — использует `KoinTestRule` и `module(override = true)` для моков.
- Моки/override: `modules(override = true)` или передача списка модулей в `KoinTestRule`.
- Проверка графа: `KoinGraphCheckTest` использует `checkModules`.
- Инструментальный тест: `KoinInstrumentationSmokeTest` показывает запуск/остановку Koin на устройстве.
- Тестовые диспетчеры: в тестах ViewModel используется `StandardTestDispatcher` + `Dispatchers.setMain`.

## Практические задания

1. Добавить новый use case (`ObserveTasksUseCase`) и подключить в `featureTasksModule`.
2. Сделать новый scope для экрана “EditProfile” с собственной зависимостью (например, валидатор).
3. Передать параметры в другой ViewModel (новый `UserPostsViewModel(postId)`), используя `parametersOf`.
4. Добавить локальный источник данных (кэш) и переключить `UserRepositoryImpl` на комбинированный data source.
5. Написать UI-тест, который проверяет отображение `UserUiState.Error`.
6. Включить `printLogger()` в debug-сборке и отловить `NoBeanDefFoundException` при удалении модуля.
7. Расширить `TaskWizardTracker` и показать, как закрытие scope очищает состояние.

## Как запустить

1. Установить Android SDK (compile/target 34, minSdk 24) и задать `sdk.dir` в `local.properties`.
2. Собрать проект:
   - `./gradlew :app:assembleDebug`
3. Unit-тесты (koin-test, coroutines-test):
   - `./gradlew :app:test`
4. Инструментальные тесты:
   - `./gradlew :app:connectedAndroidTest`
5. Запустить приложение на эмуляторе/устройстве:
   - `./gradlew :app:installDebug`

> Если запускаете в среде без Android SDK, сборка и `connectedAndroidTest` будут недоступны.
