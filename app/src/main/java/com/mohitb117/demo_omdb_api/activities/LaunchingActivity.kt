@file:OptIn(ExperimentalMaterial3Api::class)

package com.mohitb117.demo_omdb_api.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mohitb117.demo_omdb_api.activities.ui.theme.DEMO_OMDB_APITheme
import com.mohitb117.demo_omdb_api.activities.ui.theme.Purple80
import com.mohitb117.demo_omdb_api.datamodels.SearchResult
import com.mohitb117.demo_omdb_api.datamodels.SearchResultsBody
import com.mohitb117.demo_omdb_api.ui.favourites.FavouritesViewModel
import com.mohitb117.demo_omdb_api.ui.favourites.FavouritesComposableContent
import com.mohitb117.demo_omdb_api.ui.search.Result
import com.mohitb117.demo_omdb_api.ui.search.SearchComposableContent
import com.mohitb117.demo_omdb_api.ui.search.SearchViewModel
import dagger.hilt.android.AndroidEntryPoint

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
        val searchQuery by searchViewModel.searchQuery.collectAsStateWithLifecycle()
        val isRefreshing by searchViewModel.isRefreshing.collectAsStateWithLifecycle()

        LaunchComposableContent(
            modifier = modifier,
            searchUiState = searchUiState,
            favouritesUiState = favouritesUiState,
            searchQuery = searchQuery,
            onRefresh = { searchViewModel.refresh() },
            isRefreshing = isRefreshing,
            onSearchQueryChanged = { searchViewModel.onSearchQueryChanged(it) },
            isMovieFavourited = { searchViewModel.favouritedIds.value.any { fav -> fav.imdbID == it.imdbID } },
            onToggleMovieFavorited = { searchViewModel.toggleFavourite(it) },
        )
    }

    @Composable
    fun LaunchComposableContent(
        modifier: Modifier = Modifier,
        searchUiState: Result<SearchResultsBody>,
        favouritesUiState: Set<SearchResult>,
        searchQuery: String,
        isRefreshing: Boolean = false,
        onRefresh: () -> Unit = {},
        onSearchQueryChanged: (String) -> Unit,
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
            }) {
            val searchResultError = (searchUiState as? Result.Failure)?.error

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        text = searchResultError?.localizedMessage ?: stringResource(currentDestination.label),
                        color = if (searchResultError != null) Color.Red else Color.Unspecified,
                    )
                },
                bottomBar = {
                    if (currentDestination == AppDestinations.HOME) {
                        OutlinedTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(color = Purple80),
                            value = searchQuery,
                            onValueChange = { onSearchQueryChanged(it) },
                            label = { Text(text = "Search Movie!") })
                    }
                }
            ) { innerPadding ->
                when (currentDestination) {
                    AppDestinations.HOME -> {
                        SearchMovieInfoLayout(
                            modifier = Modifier.padding(innerPadding),
                            searchQuery = searchQuery,
                            searchUiState = searchUiState,
                            isRefreshing = isRefreshing,
                            onRefresh = onRefresh,
                            isMovieFavourited = isMovieFavourited,
                            onToggleMovieFavorited = onToggleMovieFavorited,
                        )
                    }

                    else -> {
                        FavouritesComposableLayout(
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
        modifier: Modifier = Modifier,
        searchQuery: String = "",
        searchUiState: Result<SearchResultsBody>,
        isRefreshing: Boolean = false,
        onRefresh: () -> Unit = {},
        isMovieFavourited: (SearchResult) -> Boolean = { false },
        onToggleMovieFavorited: suspend (SearchResult) -> Boolean = { false },
    ) {
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            if (searchQuery.isNotEmpty()) {
                val effectiveUiState = when {
                    searchUiState is Result.Failure && searchUiState.lastCachedData != null -> {
                        Result.Success(searchUiState.lastCachedData, searchQuery)
                    }

                    else -> searchUiState
                }

                SearchComposableContent(
                    modifier = Modifier,
                    searchUiState = effectiveUiState,
                    onRefresh = onRefresh,
                    isRefreshing = isRefreshing,
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
                searchUiState = Result.Loading("")
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
                searchQuery = "",
                onSearchQueryChanged = {}
            )
        }
    }
}
