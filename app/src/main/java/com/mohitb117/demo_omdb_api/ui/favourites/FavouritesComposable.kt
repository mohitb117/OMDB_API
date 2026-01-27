package com.mohitb117.demo_omdb_api.ui.search

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mohitb117.demo_omdb_api.datamodels.SearchResult
import com.mohitb117.demo_omdb_api.datamodels.SearchResultsBody
import com.mohitb117.demo_omdb_api.ui.common.MovieItemListComposable

@Composable
fun FavouritesComposableContent(
    modifier: Modifier = Modifier,
    uiState: Set<SearchResult>,
    isMovieFavourited: (SearchResult) -> Boolean = { false },
    onToggleMovieFavorited: suspend (SearchResult) -> Boolean = { false },
) {
    MovieItemListComposable(
        modifier = modifier,
        searchResults = SearchResultsBody(
            Search = uiState.toList(),
            totalResults = "",
            Response = "",
        ),
        isMovieFavourited = isMovieFavourited,
        onToggleMovieFavorited = onToggleMovieFavorited,
    )
}
