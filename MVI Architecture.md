# MVI Architecture — Dimas Project

> **Last updated:** April 6, 2026  
> This document is the source of truth for the project's architecture, conventions, and patterns.  
> An AI agent reading this file can fully understand the current codebase structure before making changes.

---

## Table of Contents

1. [Overview](#1-overview)
2. [Tech Stack](#2-tech-stack)
3. [Project Package Structure](#3-project-package-structure)
4. [Architecture Layers](#4-architecture-layers)
   - [Presentation Layer (MVI)](#41-presentation-layer-mvi)
   - [Domain Layer](#42-domain-layer)
   - [Data Layer](#43-data-layer)
5. [MVI Base Contracts](#5-mvi-base-contracts)
   - [BaseUiState](#51-baseuistate)
   - [BaseIntent](#52-baseintent)
   - [BaseViewModel](#53-baseviewmodel)
6. [Dependency Injection — Koin](#6-dependency-injection--koin)
7. [Core Infrastructure](#7-core-infrastructure)
   - [Network](#71-network-ktor)
   - [Local Database (Room)](#72-local-database-room)
   - [Preferences (DataStore)](#73-preferences-datastore)
8. [Feature: RandomNumber (Reference Implementation)](#8-feature-randomnumber-reference-implementation)
9. [How to Add a New Feature](#9-how-to-add-a-new-feature)
10. [Naming Conventions](#10-naming-conventions)

---

## 1. Overview

The project follows a **Clean Architecture** with an **MVI (Model–View–Intent)** pattern in the presentation layer.

```
┌─────────────────────────────────────────────┐
│               Presentation (MVI)            │
│  View (Compose) ←→ ViewModel ←→ UiState     │
│              ↑ Intent dispatched             │
├─────────────────────────────────────────────┤
│                 Domain Layer                │
│         UseCase  ←→  Repository (interface) │
├─────────────────────────────────────────────┤
│                  Data Layer                 │
│  RepositoryImpl ←→ RemoteDataSource         │
│                 ←→ LocalDataSource          │
│                                             │
│  Network (Ktor) │ Room DB │ DataStore       │
└─────────────────────────────────────────────┘
```

**Data flow:**
```
User Action
   │
   ▼
View dispatches Intent via viewModel.onIntent(intent)
   │
   ▼
BaseViewModel.onIntent() → handleIntent()
   │
   ▼
ViewModel calls UseCase
   │
   ▼
UseCase calls Repository interface
   │
   ▼
RepositoryImpl orchestrates RemoteDataSource / LocalDataSource
   │
   ▼
New state emitted via updateState { copy(...) }
   │
   ▼
View observes uiState: StateFlow<S> and recomposes
```

---

## 2. Tech Stack

| Concern | Library | Version |
|---|---|---|
| Language | Kotlin | 2.0.21 |
| UI | Jetpack Compose + Material 3 | BOM 2025.02.00 |
| DI | **Koin** | 3.5.6 |
| Network | Ktor (Android engine) | 2.3.12 |
| Serialization | KotlinX Serialization JSON | 1.7.1 |
| Local DB | Room | 2.6.1 |
| Preferences | DataStore Preferences | 1.1.1 |
| Image Loading | Coil | 2.7.0 |
| Async | Kotlin Coroutines | 1.8.1 |
| Navigation | Navigation 3 | 1.0.1 |
| KSP | KSP (for Room) | 2.0.21-1.0.25 |

> ⚠️ **Hilt was fully removed.** Koin is the sole DI framework.  
> There are **no** `@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel`, `@Inject`, or `@Module`/`@InstallIn` annotations anywhere in the codebase.

---

## 3. Project Package Structure

```
app/src/main/java/com/dimas/dimasproject/
│
├── DimasApp.kt                    # Application class — Koin startKoin{}
├── MainActivity.kt                # Single activity, hosts Compose
│
├── core/
│   ├── network/
│   │   └── NetworkClient.kt       # Ktor HttpClient factory (singleton object)
│   ├── local/
│   │   ├── db/
│   │   │   ├── AppDatabase.kt     # Room database definition
│   │   │   └── entity/
│   │   │       └── UserEntity.kt  # Room entity: table "users"
│   │   └── preferences/
│   │       └── AppSettings.kt     # DataStore wrapper utility class
│   └── presentation/              # ── MVI base contracts ──
│       ├── BaseUiState.kt         # Marker interface — persistent screen state
│       ├── BaseIntent.kt          # Marker interface — user actions
│       ├── BaseUiEffect.kt        # Marker interface — one-time side effects
│       └── BaseViewModel.kt       # Abstract ViewModel<S, I, E>
│
├── di/                            # Global Koin modules (app-level)
│   ├── CoreModule.kt              # val coreModule — provides DataStore
│   ├── NetworkModule.kt           # val networkModule — provides HttpClient
│   └── LocalModule.kt             # val localModule — provides AppDatabase, AppSettings
│
└── feature/
    └── randomnumber/              # Feature module (reference implementation)
        ├── di/
        │   └── RandomNumberModule.kt   # val randomNumberModule — feature Koin bindings
        ├── data/
        │   ├── model/
        │   │   ├── RandomNumber.kt         # Domain model
        │   │   ├── RandomNumberEntity.kt   # Room entity + toDomain()/toEntity() mappers
        │   │   └── RandomNumberResponse.kt # Ktor deserialization DTO + toDomain() mapper
        │   ├── local/
        │   │   ├── RandomNumberDao.kt          # Room DAO interface
        │   │   └── RandomNumberLocalDataSource.kt
        │   ├── remote/
        │   │   └── RandomNumberRemoteDataSource.kt
        │   └── repository/
        │       └── RandomNumberRepositoryImpl.kt
        ├── domain/
        │   ├── repository/
        │   │   └── RandomNumberRepository.kt   # Interface (contract)
        │   └── usecase/
        │       ├── FetchRandomNumberUseCase.kt
        │       └── GetAllRandomNumbersUseCase.kt
        └── presentation/
            ├── RandomNumberUiState.kt   # State (: BaseUiState) + Event (: BaseIntent)
            ├── RandomNumberViewModel.kt # : BaseViewModel<State, Event>
            └── RandomNumberScreen.kt   # Composable, uses koinViewModel()
```

---

## 4. Architecture Layers

### 4.1 Presentation Layer (MVI)

The presentation layer is built on the **MVI pattern** enforced by base contracts.

**Roles:**
- **Model** → `UiState` data class implementing `BaseUiState`
- **View** → Composable function observing `uiState: StateFlow<S>`
- **Intent** → Sealed interface implementing `BaseIntent`, dispatched via `viewModel.onIntent(intent)`

**Rules:**
- The View **never** modifies state directly — it only dispatches intents
- The ViewModel is the **single** owner of state
- State is always **immutable** — use `copy()` via `updateState { }`
- Use `collectAsState()` in Compose to observe state

### 4.2 Domain Layer

**Rules:**
- Contains **only** pure Kotlin (no Android dependencies)
- `Repository` is an **interface** defined here, implemented in the data layer
- `UseCase` classes are single-responsibility, named as `<Verb><Noun>UseCase`
- UseCases receive and return **domain models** (never entities or DTOs)
- `operator fun invoke()` is used so UseCases are called like functions

### 4.3 Data Layer

**Rules:**
- `RemoteDataSource` — talks to network (Ktor), returns DTOs
- `LocalDataSource` — talks to Room DAO, returns entities
- `RepositoryImpl` — orchestrates remote + local, maps to domain models
- Mapping is done via **extension functions** co-located with their model file:
  - `RandomNumberEntity.toDomain()` / `RandomNumber.toEntity()`
  - `RandomNumberResponse.toDomain()`

---

## 5. MVI Base Contracts

All base contracts live in `core/presentation/`.

### 5.1 BaseUiState

```kotlin
// core/presentation/BaseUiState.kt
interface BaseUiState
```

Marker interface. Every screen state **must** be a `data class` implementing `BaseUiState`.  
Represents **persistent** screen state that survives recomposition.

### 5.2 BaseIntent

```kotlin
// core/presentation/BaseIntent.kt
interface BaseIntent
```

Marker interface. Every screen's user actions **must** be a `sealed interface` implementing `BaseIntent`.

### 5.3 BaseUiEffect

```kotlin
// core/presentation/BaseUiEffect.kt
interface BaseUiEffect
```

Marker interface. Every screen's one-time side effects **must** be a `sealed interface` implementing `BaseUiEffect`.

> **State vs Effect — when to use which:**
>
> | Scenario | Use |
> |---|---|
> | Loading spinner, list data, form values | `BaseUiState` — persistent, survives recomposition |
> | Navigation to another screen | `BaseUiEffect` — one-time, delivered exactly once |
> | Snackbar / Toast message | `BaseUiEffect` — one-time, auto-dismissed |
> | Dialog that must survive back-stack | `BaseUiState` — persistent flag |

### 5.4 BaseViewModel

```kotlin
// core/presentation/BaseViewModel.kt
abstract class BaseViewModel<S : BaseUiState, I : BaseIntent, E : BaseUiEffect>(
    initialState: S
) : ViewModel()
```

| Member | Visibility | Description |
|---|---|---|
| `uiState: StateFlow<S>` | `public` | Observed by the View. Never expose `MutableStateFlow`. |
| `effect: Flow<E>` | `public` | One-time side effects backed by `Channel<E>(BUFFERED)`. Each emission is delivered **exactly once**. |
| `onIntent(intent: I)` | `public` | Called by the View to dispatch a user action |
| `handleIntent(intent: I)` | `protected abstract` | Implemented by each ViewModel; routes intents to private functions |
| `updateState { copy(...) }` | `protected` | Partial state update via reducer lambda |
| `setState(newState: S)` | `protected` | Full state replacement |
| `sendEffect(effect: E)` | `protected` | Sends a one-time effect through the Channel |
| `currentState: S` | `protected` | Synchronous snapshot of the current state |

#### How `effect` works internally

```
ViewModel sends via Channel.send(effect)
        │
        ▼
Channel<E>(BUFFERED) — buffers emissions, survives brief collector absence
        │
        ▼
.receiveAsFlow() — exposes as cold Flow<E>
        │
        ▼
View collects in LaunchedEffect(Unit) — runs once on composition
        │
        ▼
snackbarHostState.showSnackbar(message)  ← suspends until dismissed
```

---

## 6. Dependency Injection — Koin

### Initialization

Koin is started in `DimasApp.onCreate()`:

```kotlin
// DimasApp.kt
class DimasApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@DimasApp)
            modules(
                coreModule,
                networkModule,
                localModule,
                randomNumberModule   // ← add new feature modules here
            )
        }
    }
}
```

### Module Map

| Module | File | Provides |
|---|---|---|
| `coreModule` | `di/CoreModule.kt` | `DataStore<Preferences>` |
| `networkModule` | `di/NetworkModule.kt` | `HttpClient` (Ktor) |
| `localModule` | `di/LocalModule.kt` | `AppDatabase`, `AppSettings` |
| `randomNumberModule` | `feature/randomnumber/di/RandomNumberModule.kt` | DAO, DataSources, Repository, UseCases, ViewModel |

### Koin Scoping Conventions

| Koin keyword | When to use |
|---|---|
| `single { }` | Singletons — DB, HttpClient, DataSources, Repositories |
| `factory { }` | New instance each time — UseCases |
| `viewModel { }` | ViewModels — scoped to Compose `koinViewModel()` |

### In Compose

```kotlin
// No @AndroidEntryPoint needed — use koinViewModel() directly
@Composable
fun MyScreen(viewModel: MyViewModel = koinViewModel()) { ... }
```

---

## 7. Core Infrastructure

### 7.1 Network (Ktor)

**File:** `core/network/NetworkClient.kt`

- Engine: `Android`
- Plugin: `ContentNegotiation` with `KotlinX Serialization JSON`
- `ignoreUnknownKeys = true`, `isLenient = true`
- Plugin: `Logging` at `LogLevel.BODY` (tag: `Ktor =>`)
- `DefaultRequest` sets `AcceptEncoding: identity` to **disable server-side compression** (prevents `MalformedInputException`)
- Timeouts: `connectTimeout = 30_000`, `socketTimeout = 30_000`

> ⚠️ **Known issue history:** The API at `aisenseapi.com` was returning gzip-compressed responses.  
> This is fixed by explicitly setting `header(HttpHeaders.AcceptEncoding, "identity")` in `DefaultRequest`.

### 7.2 Local Database (Room)

**File:** `core/local/db/AppDatabase.kt`

```kotlin
@Database(
    entities = [UserEntity::class, RandomNumberEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun randomNumberDao(): RandomNumberDao
}
```

- Uses `fallbackToDestructiveMigration()` on build
- **When adding a new entity:** register it in the `entities = [...]` list and bump `version`
- **When adding a new DAO:** add the abstract function to `AppDatabase` and expose it in `localModule` or the feature's Koin module

#### Existing Entities

| Entity class | Table name | Columns |
|---|---|---|
| `UserEntity` | `users` | `id` (PK, autoGenerate), `name` |
| `RandomNumberEntity` | `random_numbers` | `id` (PK, autoGenerate), `number`, `range_from`, `range_to` |

### 7.3 Preferences (DataStore)

**File:** `core/local/preferences/AppSettings.kt`

Utility class wrapping `DataStore<Preferences>`. Injected via Koin (`localModule`).

| Key constant | Type | Description |
|---|---|---|
| `AUTH_TOKEN` | `String?` | JWT / auth token |
| `IS_LOGGED_IN` | `Boolean` | Login state flag |
| `USER_ID` | `Int?` | Logged-in user ID |

```kotlin
// Reading (Flow-based)
appSettings.authToken.collect { token -> ... }

// Writing (suspend)
appSettings.saveAuthToken("eyJ...")
appSettings.setLoggedIn(true)
appSettings.clearAll()
```

---

## 8. Feature: RandomNumber (Reference Implementation)

The `randomnumber` feature is the canonical example for how all features are structured.

### API
- **Endpoint:** `GET https://aisenseapi.com/services/v1/random_number`
- **Response:**
```json
{
  "random_number": 3,
  "range": { "from": 1, "to": 6 }
}
```

### Models

| Class | Layer | Purpose |
|---|---|---|
| `RandomNumberResponse` | Data (DTO) | Ktor deserializes JSON into this |
| `RandomNumber` | Domain / Data (shared) | Domain model passed between layers |
| `RandomNumberEntity` | Data (DB) | Room persisted entity |

### State, Events & Effects

```kotlin
// RandomNumberUiState.kt

// State — persistent
data class RandomNumberUiState(
    val isLoading: Boolean = false,
    val latest: RandomNumber? = null,
    val history: List<RandomNumber> = emptyList()
) : BaseUiState

// Intent — user actions
sealed interface RandomNumberUiEvent : BaseIntent {
    data object FetchRandom : RandomNumberUiEvent
}

// Effect — one-time side effects
sealed interface RandomNumberUiEffect : BaseUiEffect {
    data class ShowSnackbar(val message: String) : RandomNumberUiEffect
}
```

### ViewModel

```kotlin
class RandomNumberViewModel(
    private val fetchRandomNumber: FetchRandomNumberUseCase,
    private val getAllRandomNumbers: GetAllRandomNumbersUseCase
) : BaseViewModel<RandomNumberUiState, RandomNumberUiEvent>(RandomNumberUiState()) {

    override fun handleIntent(intent: RandomNumberUiEvent) {
        when (intent) {
            is RandomNumberUiEvent.FetchRandom -> fetchRandom()
            is RandomNumberUiEvent.DismissError -> dismissError()
        }
    }
    // ...
}
```

---

## 9. How to Add a New Feature

Follow these steps in order. Use `randomnumber` as the reference.

### Step 1 — Create the package structure

```
feature/<featurename>/
    di/         <FeatureName>Module.kt
    data/
        model/  <Model>.kt, <Model>Entity.kt, <Model>Response.kt
        local/  <Model>Dao.kt, <Model>LocalDataSource.kt
        remote/ <Model>RemoteDataSource.kt
        repository/ <Model>RepositoryImpl.kt
    domain/
        repository/ <Model>Repository.kt (interface)
        usecase/    <Verb><Model>UseCase.kt
    presentation/
        <FeatureName>UiState.kt
        <FeatureName>ViewModel.kt
        <FeatureName>Screen.kt
```

### Step 2 — Define models

```kotlin
// Domain model
data class MyModel(val id: Int, val name: String)

// Room entity
@Entity(tableName = "my_table")
data class MyModelEntity(@PrimaryKey val id: Int, val name: String)
fun MyModelEntity.toDomain() = MyModel(id, name)
fun MyModel.toEntity() = MyModelEntity(id, name)

// Network DTO
@Serializable
data class MyModelResponse(@SerialName("id") val id: Int, ...)
fun MyModelResponse.toDomain() = MyModel(...)
```

### Step 3 — Register the entity in AppDatabase

```kotlin
@Database(entities = [..., MyModelEntity::class], version = <bumped>)
abstract class AppDatabase : RoomDatabase() {
    abstract fun myModelDao(): MyModelDao  // add this
}
```

### Step 4 — Define State, Intent, and Effect

```kotlin
// MyScreenUiState.kt

data class MyScreenUiState(
    val isLoading: Boolean = false,
    // add screen-specific fields...
) : BaseUiState

sealed interface MyScreenIntent : BaseIntent {
    data object Load : MyScreenIntent
    // add more user actions...
}

sealed interface MyScreenEffect : BaseUiEffect {
    data class ShowSnackbar(val message: String) : MyScreenEffect
    data class NavigateTo(val route: String) : MyScreenEffect
    // add more one-time effects...
}
```

### Step 5 — Implement the ViewModel

```kotlin
class MyViewModel(
    private val myUseCase: MyUseCase
) : BaseViewModel<MyScreenUiState, MyScreenIntent, MyScreenEffect>(MyScreenUiState()) {

    override fun handleIntent(intent: MyScreenIntent) {
        when (intent) {
            is MyScreenIntent.Load -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            myUseCase()
                .onSuccess { data ->
                    updateState { copy(isLoading = false, ...) }
                    sendEffect(MyScreenEffect.ShowSnackbar("Loaded!"))
                }
                .onFailure { error ->
                    updateState { copy(isLoading = false) }
                    sendEffect(MyScreenEffect.ShowSnackbar(error.message ?: "Error"))
                }
        }
    }
}
```

### Step 6 — Create the Koin module

```kotlin
val myFeatureModule = module {
    single { get<AppDatabase>().myModelDao() }
    single { MyRemoteDataSource(get()) }
    single { MyLocalDataSource(get()) }
    single<MyRepository> { MyRepositoryImpl(get(), get()) }
    factory { MyUseCase(get()) }
    viewModel { MyViewModel(get()) }
}
```

### Step 7 — Register the module in DimasApp

```kotlin
startKoin {
    modules(
        coreModule,
        networkModule,
        localModule,
        randomNumberModule,
        myFeatureModule   // ← add here
    )
}
```

### Step 8 — Build the Composable Screen

```kotlin
@Composable
fun MyScreen(viewModel: MyViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect one-time effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is MyScreenEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is MyScreenEffect.NavigateTo   -> navController.navigate(effect.route)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // ... screen content ...

        // Dispatch intent on user action
        Button(onClick = { viewModel.onIntent(MyScreenIntent.Load) }) { ... }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
```

---

## 10. Naming Conventions

| Artifact | Convention | Example |
|---|---|---|
| Feature package | `feature/<lowercase>` | `feature/randomnumber` |
| UI State | `<Feature>UiState` | `RandomNumberUiState` |
| UI Event / Intent | `<Feature>UiEvent` | `RandomNumberUiEvent` |
| UI Effect (one-time) | `<Feature>UiEffect` | `RandomNumberUiEffect` |
| ViewModel | `<Feature>ViewModel` | `RandomNumberViewModel` |
| Screen Composable | `<Feature>Screen` | `RandomNumberScreen` |
| UseCase | `<Verb><Noun>UseCase` | `FetchRandomNumberUseCase` |
| Repository interface | `<Feature>Repository` | `RandomNumberRepository` |
| Repository impl | `<Feature>RepositoryImpl` | `RandomNumberRepositoryImpl` |
| Remote data source | `<Feature>RemoteDataSource` | `RandomNumberRemoteDataSource` |
| Local data source | `<Feature>LocalDataSource` | `RandomNumberLocalDataSource` |
| DAO | `<Feature>Dao` | `RandomNumberDao` |
| Room Entity | `<Model>Entity` | `RandomNumberEntity` |
| Network DTO | `<Model>Response` | `RandomNumberResponse` |
| Koin module | `val <feature>Module` | `val randomNumberModule` |
| Domain model | `<Model>` (plain) | `RandomNumber` |

