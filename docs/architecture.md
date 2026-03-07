# Architecture Diagram

Mermaid diagram outlining the high-level architecture of the OMDB_API Android app.

```mermaid
graph TD
  subgraph App
    AppApp[OmdbApiApp (DI)]
    LA[LaunchingActivity (UI)]
  end

  subgraph UI
    SV(SearchViewModel)
    FV(FavouritesViewModel)
    LA --> SV
    LA --> FV
  end

  subgraph Domain
    Repo[MovieRepository]
  end
  subgraph Remote
    API[OMDBApi (Retrofit)]
    NetworkModule[NetworkingModule]
  end
  subgraph Local
    DB[MovieResultsDb (Room)]
    DAO[FavouritesDao]
  end
  subgraph DataModels
    SR[SearchResult (Room Entity)]
    SearchBody[SearchResultsBody]
    DetailsBody[DetailsResultsBody]
  end

  AppApp -->|DI| NetworkModule
  AppApp -->|DI| DB
  AppApp -->|DI| Repo
  LA --> SV
  LA --> FV
  SV -->|load results| Repo
  FV -->|observe| Repo
  Repo --> API
  API -->|returns| SearchBody
  Repo --> DB
  DB --> DAO
  Repo --> SR
  DetailsBody --> SR
  SR -->|toMovieSearchResult| SR
  DetailsBody -->|toMovieSearchResult| SR

  %% Simple map of dependencies
  Repo -->|uses| API
  Repo -->|persists| DAO
  DAO -->|exposes| SR
```
