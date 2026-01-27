package com.mohitb117.demo_omdb_api.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mohitb117.demo_omdb_api.activities.ui.theme.DEMO_OMDB_APITheme
import com.mohitb117.demo_omdb_api.datamodels.SearchResult
import com.mohitb117.demo_omdb_api.datamodels.SearchResultsBody
import com.mohitb117.demo_omdb_api.ui.favourites.FavouritesViewModel
import com.mohitb117.demo_omdb_api.ui.search.FavouritesComposableContent
import com.mohitb117.demo_omdb_api.ui.search.Result
import com.mohitb117.demo_omdb_api.ui.search.SearchComposableContent
import com.mohitb117.demo_omdb_api.ui.search.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class LaunchingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            DEMO_OMDB_APITheme {
                LaunchComposable(modifier = Modifier)
            }
        }
    }

    @Composable
    fun LaunchComposable(
        modifier: Modifier = Modifier,
        searchViewModel: SearchViewModel = viewModel(),
        favouritesViewModel: FavouritesViewModel = viewModel(),
    ) {
        val searchUiState by searchViewModel.searchResultsViewState.collectAsStateWithLifecycle()
        val favouritesUiState by favouritesViewModel.favouritedIdsFlow.collectAsStateWithLifecycle()

        LaunchComposableContent(
            modifier = modifier,
            searchUiState = searchUiState,
            favouritesUiState = favouritesUiState,
            onSearch = { searchViewModel.loadSearchResult(it) },
            isMovieFavourited = { searchViewModel.favouritedIds.value.contains(it) },
            onToggleMovieFavorited = { searchViewModel.toggleFavourite(it) },
        )
    }

    @Composable
    fun LaunchComposableContent(
        modifier: Modifier = Modifier,
        searchUiState: Result<SearchResultsBody>,
        favouritesUiState: Set<SearchResult>,
        onSearch: (String) -> Unit,
        isMovieFavourited: (SearchResult) -> Boolean = { false },
        onToggleMovieFavorited: suspend (SearchResult) -> Boolean = { false },
    ) {
        var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

        NavigationSuiteScaffold(
            navigationSuiteItems = {
                AppDestinations.entries.forEach {
                    item(
                        icon = {
                            Icon(it.icon, stringResource(it.contentDescription))
                        },
                        modifier = Modifier,
                        label = { Text(stringResource(it.label)) },
                        selected = it == currentDestination,
                        onClick = { currentDestination = it },
                    )
                }
            }
        ) {
            var searchQuery by rememberSaveable { mutableStateOf("") }

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    if (currentDestination == AppDestinations.HOME) {
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(color = Color.Gray),
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text(text = "Search Movie!") }
                        )
                    }
                }
            ) { innerPadding ->
                when (currentDestination) {
                    AppDestinations.HOME -> {
                        SearchMovieInfoLayout(
                            name = stringResource(currentDestination.label),
                            modifier = Modifier.padding(innerPadding),
                            searchQuery = searchQuery,
                            searchUiState = searchUiState,
                            onSearch = onSearch,
                            isMovieFavourited = isMovieFavourited,
                            onToggleMovieFavorited = onToggleMovieFavorited,
                        )
                    }

                    else -> {
                        FavouritesComposableLayout(
                            name = stringResource(currentDestination.label),
                            modifier = Modifier.padding(innerPadding),
                            uiState = favouritesUiState,
                            isMovieFavourited = isMovieFavourited,
                            onToggleMovieFavorited = onToggleMovieFavorited,
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun SearchMovieInfoLayout(
        name: String,
        modifier: Modifier = Modifier,
        searchQuery: String = "",
        searchUiState: Result<SearchResultsBody>,
        onSearch: (String) -> Unit,
        isMovieFavourited: (SearchResult) -> Boolean = { false },
        onToggleMovieFavorited: suspend (SearchResult) -> Boolean = { false },
    ) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            if (searchQuery.isNotEmpty()) {
                LaunchedEffect(searchQuery) {
                    delay(500) // Debounce for 500ms
                    onSearch(searchQuery)
                }

                SearchComposableContent(
                    modifier = Modifier,
                    searchUiState = searchUiState,
                    onToggleMovieFavorited = onToggleMovieFavorited,
                    isMovieFavourited = isMovieFavourited,
                )
            } else {
                Text("Nothing to search right now!")
            }
        }
    }

    @Composable
    fun FavouritesComposableLayout(
        name: String,
        uiState: Set<SearchResult>,
        modifier: Modifier = Modifier,
        isMovieFavourited: (SearchResult) -> Boolean = { false },
        onToggleMovieFavorited: suspend (SearchResult) -> Boolean = { false },
    ) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            FavouritesComposableContent(
                modifier = Modifier,
                uiState = uiState,
                onToggleMovieFavorited = onToggleMovieFavorited,
                isMovieFavourited = isMovieFavourited,
            )
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun SearchMovieInfoLayoutPreview() {
        DEMO_OMDB_APITheme {
            SearchMovieInfoLayout(
                name = "Android",
                searchUiState = Result.Loading(""),
                onSearch = {}
            )
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun LaunchComposablePreview() {
        DEMO_OMDB_APITheme {
            LaunchComposableContent(
                searchUiState = Result.Loading(""),
                favouritesUiState = emptySet(),
                onSearch = {}
            )
        }
    }
}
