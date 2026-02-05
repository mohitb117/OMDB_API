@file:OptIn(ExperimentalMaterial3Api::class)

package com.mohitb117.demo_omdb_api.ui.feed

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mohitb117.demo_omdb_api.R
import com.mohitb117.demo_omdb_api.activities.ItemDetailDestination
import com.mohitb117.demo_omdb_api.activities.ui.theme.DEMO_OMDB_APITheme
import com.mohitb117.demo_omdb_api.activities.ui.theme.Purple40
import com.mohitb117.demo_omdb_api.datamodels.SearchResult
import com.mohitb117.demo_omdb_api.datamodels.SearchResultsBody
import kotlinx.coroutines.launch

@Composable
fun FeedComposableContent(
    modifier: Modifier = Modifier,
    uiState: Set<SearchResult>,
    isMovieFavourited: (SearchResult) -> Boolean = { false },
    onToggleMovieFavorited: suspend (SearchResult) -> Boolean = { false },
    onItemTapped: (ItemDetailDestination.Details) -> Unit = {}
) {
    FeedListComposable(
        modifier = modifier,
        searchResults = SearchResultsBody(
            Search = uiState.toList(),
            totalResults = "",
            Response = "",
        ),
        isMovieFavourited = isMovieFavourited,
        onToggleMovieFavorited = onToggleMovieFavorited,
        onItemTapped = onItemTapped,
    )
}

@ExperimentalMaterial3Api
@Composable
fun FeedListComposable(
    modifier: Modifier,
    searchResults: SearchResultsBody,
    isMovieFavourited: (SearchResult) -> Boolean = { false },
    onToggleMovieFavorited: suspend (SearchResult) -> Boolean = { false },
    onItemTapped: (ItemDetailDestination.Details) -> Unit = {}
) {
    val searchItems = searchResults.Search.orEmpty()

    val pagerState = rememberPagerState(pageCount = { searchItems.size })
    val snackbarHostState = remember { SnackbarHostState() }

    val isTop by remember {
        derivedStateOf {
            pagerState.currentPage == 0 && searchItems.size > 1
        }
    }

    val isEnd by remember {
        derivedStateOf {
            pagerState.currentPage == searchItems.size - 1 && searchItems.size > 1
        }
    }

    LaunchedEffect(isTop) {
        if (isTop) {
            snackbarHostState.showSnackbar("You've reached the top of the list!")
        }
    }
    LaunchedEffect(isEnd) {
        if (isEnd) {
            snackbarHostState.showSnackbar("You've reached the end of the list!")
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (searchItems.isEmpty()) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues),
                text = "No results found"
            )
        } else {
            VerticalPager(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                state = pagerState,
                beyondViewportPageCount = 1,
                contentPadding = PaddingValues(bottom = 150.dp)
            ) { page ->
                val item = searchItems[page]
                FeedListItem(
                    item = item,
                    isMovieFavourited = isMovieFavourited,
                    onToggleMovieFavorited = onToggleMovieFavorited,
                    onItemTapped= onItemTapped,
                )
            }
        }
    }
}

@Composable
fun FeedListItem(
    modifier: Modifier = Modifier,
    item: SearchResult,
    isMovieFavourited: (SearchResult) -> Boolean = { false },
    onToggleMovieFavorited: suspend (SearchResult) -> Boolean = { false },
    onItemTapped: (ItemDetailDestination.Details) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()

    val borderWidth = 20.dp
    val roundedCornerShape = RoundedCornerShape(borderWidth)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp)
            .background(
                color = Purple40,
                shape = roundedCornerShape
            ).clickable { onItemTapped(ItemDetailDestination.Details(item.title)) },
    ) {
        var isFavourited by rememberSaveable { mutableStateOf(false) }

        isFavourited = isMovieFavourited(item)

        AsyncImage(
            model = item.poster,
            placeholder = painterResource(id = R.mipmap.img),
            modifier = Modifier
                .fillMaxSize()
                .border(
                    border = BorderStroke(borderWidth / 8, Color.Gray),
                    shape = RoundedCornerShape(borderWidth)
                )
                .padding(borderWidth / 8)
                .clip(roundedCornerShape)
                .padding(top = 64.dp),
            contentDescription = "Poster",
            contentScale = ContentScale.Crop,
        )

        Text(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.TopStart)
                .padding(10.dp),
            text = item.title,
        )

        Switch(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.TopEnd)
                .padding(5.dp),
            checked = isFavourited,
            onCheckedChange = {
                coroutineScope.launch {
                    val result = onToggleMovieFavorited(item)
                    isFavourited = result
                }
            }
        )
    }
}

@ExperimentalMaterial3Api
@Preview(showBackground = true)
@Composable
fun MovieItemComposablePreview() {
    DEMO_OMDB_APITheme {
        FeedComposableContent(
            uiState = setOf(
                SearchResult(
                    imdbID = "tt0076759",
                    title = "Star Wars: Episode IV - A New Hope Blah BlahBlahnBlahn",
                    type = "movie",
                    poster = "https://m.media-amazon.com/images/M/MV5BOTA5NjhiOTAtZWM0ZC00MWNhLWE1OTktZDA4MjkwYmY0OTU3XkEyXkFqcGdeQXVyMTQxNzMzNDI@._V1_SX300.jpg"
                ),
                SearchResult(
                    imdbID = "tt0080684",
                    title = "Star Wars: Episode V - The Empire Strikes BackBlah Blahn BlahnBlahnBlahnBlahnBlahn",
                    type = "movie",
                    poster = "https://m.media-amazon.com/images/M/MV5BYmU1ZTMwMWUtMWVlUC00MGRiLTgwOTYtOGVjOWFmZGUxMGRmXkEyXkFqcGdeQXVyNjU0OTQ0OTY@._V1_SX300.jpg"
                )
            ),
        )
    }
}

@ExperimentalMaterial3Api
@Preview(showBackground = true)
@Composable
fun FeedListComposablePreview() {
    DEMO_OMDB_APITheme {
        FeedComposableContent(
            uiState = setOf(
                SearchResult(
                    imdbID = "tt0076759",
                    title = "Star Wars: Episode IV - A New Hope",
                    type = "movie",
                    poster = "https://m.media-amazon.com/images/M/MV5BOTA5NjhiOTAtZWM0ZC00MWNhLWE1OTktZDA4MjkwYmY0OTU3XkEyXkFqcGdeQXVyMTQxNzMzNDI@._V1_SX300.jpg"
                ),
            ),
        )
    }
}
