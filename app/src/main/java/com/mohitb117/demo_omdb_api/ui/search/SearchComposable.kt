package com.mohitb117.demo_omdb_api.ui.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mohitb117.demo_omdb_api.datamodels.SearchResult
import com.mohitb117.demo_omdb_api.datamodels.SearchResultsBody
import com.mohitb117.demo_omdb_api.ui.common.MovieItemListComposable

@ExperimentalMaterial3Api
@Composable
fun SearchComposableContent(
    modifier: Modifier = Modifier,
    searchUiState: Result<SearchResultsBody>,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    isMovieFavourited: (SearchResult) -> Boolean = { false },
    onToggleMovieFavorited: suspend (SearchResult) -> Boolean = { false },
) {
    val scrollState = rememberScrollState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
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

            is Result.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.wrapContentSize(),
                )
            }

            else -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                contentAlignment = Alignment.Center
            ) {
                Text("😭No data for you as Something went wrong!😭")
            }
        }
    }
}
