package com.mohitb117.demo_omdb_api.ui.search

import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mohitb117.demo_omdb_api.datamodels.SearchResult
import com.mohitb117.demo_omdb_api.datamodels.SearchResultsBody
import com.mohitb117.demo_omdb_api.ui.common.MovieItemListComposable

@Composable
fun SearchComposableContent(
    modifier: Modifier = Modifier,
    searchUiState: Result<SearchResultsBody>,
    isMovieFavourited: (SearchResult) -> Boolean = { false },
    onToggleMovieFavorited: suspend (SearchResult) -> Boolean = { false },
) {
    when (searchUiState) {
        is Result.Success -> {
            MovieItemListComposable(
                modifier = modifier,
                searchResults = searchUiState.value,
                isMovieFavourited = isMovieFavourited,
                onToggleMovieFavorited = onToggleMovieFavorited,
            )
        }

        is Result.Failure -> {
            Text(text = "Error seen = ${searchUiState.error.localizedMessage.orEmpty()}")
        }

        is Result.Loading -> {
            CircularProgressIndicator(
                modifier = Modifier.wrapContentSize(),
            )
        }

        else -> Text("Something went wrong! $searchUiState")
    }
}
