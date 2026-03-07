Architecture Overview
====================

- Purpose
  - Android app that lets users search for movies via the Open Movie Database API, view details, and manage a favorites list persisted locally.

- Tech Stack
  - Platform: Android (Kotlin)
  - UI: Jetpack Compose
  - Architecture: MVVM
  - Remote: Retrofit + Gson
  - Local: Room
  - DI: Dagger Hilt
  - Build: Gradle

- Layered Architecture
  - UI Layer
    - LaunchingActivity
    - Compose-based screens: search, details, favorites
    - ViewModels: SearchViewModel, FavouritesViewModel
  - Domain / Use-Cases
    - MovieRepository: central data facade; coordinates remote API and local DB
  - Data Layer
    - Remote API: OMDBApi (Retrofit interface)
    - Network wiring: NetworkingModule
    - Local DB: MovieResultsDb (Room)
    - DAO: FavouritesDao
    - Data models:
      - SearchResult (Room entity)
      - SearchResultsBody, DetailsResultsBody
      - DetailsResultsBody.toMovieSearchResult() for mapping to a search item
  - Dependency Injection (DI)
    - App entry: OmdbApiApp annotated with @HiltAndroidApp
    - Modules
      - NetworkingModule: Retrofit setup, base URL, converters, logging
      - DbModule: Room DB and DAO provisioning
      - RepositoryModule: MovieRepository provisioning
      - CommonModule: SharedPreferences provision
      - FragmentModule: provides adapters for fragments (if used)
    - Scopes: SingletonComponent (global), ActivityComponent, ViewModelComponent
- Data Flow Summary
  - UI interacts with SearchViewModel and FavouritesViewModel
  - SearchViewModel calls MovieRepository.loadResults(term)
  - MovieRepository delegates to OMDBApi for remote fetch or FavouritesDao for local state
  - Details use path: DetailsResultsBody → toMovieSearchResult() → SearchResult
  - Favorites are persisted in Room and observed via LiveData/Flow

- Key Components (quick reference)
  - App entry and DI bootstrap: app/src/main/java/com/mohitb117/demo_omdb_api/OmdbApiApp.kt
  - API surface: app/src/main/java/com/mohitb117/demo_omdb_api/endpoints/OMDBApi.kt
  - Networking: app/src/main/java/com/mohitb117/demo_omdb_api/injection/NetworkingModule.kt
  - Local DB: app/src/main/java/com/mohitb117/demo_omdb_api/databases/MovieResultsDb.kt
  - Data models: app/src/main/java/com/mohitb117/demo_omdb_api/datamodels/SearchResult.kt
  - Repository: app/src/main/java/com/mohitb117/demo_omdb_api/repositories/MovieRepository.kt
  - UI ViewModels: app/src/main/java/com/mohitb117/demo_omdb_api/ui/search/SearchViewModel.kt, app/src/main/java/com/mohitb117/demo_omdb_api/ui/favourites/FavouritesViewModel.kt
  - DI modules: app/src/main/java/com/mohitb117/demo_omdb_api/injection/RepoModule.kt, DbModule.kt, NetworkingModule.kt, CommonModule.kt, FragmentModule.kt

- How to Extend
  - Add new screens by introducing new ViewModels and UI Composables
  - Extend data models with new API fields or local cache tables
  - Swap in alternative DI scopes or add new modules as needed

- Build and Run Notes
  - API key is embedded in repository logic (ensure you’re compliant with OMDB key usage in your environment)
  - Local development requires Android SDK setup
  - Tests and linters can be wired via Gradle tasks

- Quick Start Commands (optional)
  - Build: ./gradlew assemble
  - Install: ./gradlew installDebug
  - Run unit tests: ./gradlew test
  - Run instrumented tests: ./gradlew connectedAndroidTest
