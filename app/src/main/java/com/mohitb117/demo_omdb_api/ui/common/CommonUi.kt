package com.mohitb117.demo_omdb_api.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
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
import com.mohitb117.demo_omdb_api.ui.favourites.FavouritesComposableContent
import kotlinx.coroutines.launch

enum class ScrollingDirection {
    UP, DOWN
}

@ExperimentalMaterial3Api
@Composable
fun MovieItemListComposable(
    modifier: Modifier,
    searchResults: SearchResultsBody,
    isMovieFavourited: (SearchResult) -> Boolean = { false },
    onToggleMovieFavorited: suspend (SearchResult) -> Boolean = { false },
    onItemTapped: (ItemDetailDestination.Details) -> Unit = {}
) {
    val listState = rememberLazyListState()
    val searchItems = searchResults.Search.orEmpty()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var fabNavigationState by rememberSaveable {
        mutableStateOf(ScrollingDirection.DOWN)
    }

    val isEnd by remember {
        derivedStateOf { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index == listState.layoutInfo.totalItemsCount - 1 }
    }

    val isBegin by remember {
        derivedStateOf { listState.layoutInfo.visibleItemsInfo.firstOrNull()?.index == 0 }
    }

    LaunchedEffect(isBegin) {
        if (isBegin) {
            snackbarHostState.showSnackbar("Top!")
        }
    }

    LaunchedEffect(isEnd) {
        if (isEnd) {
            snackbarHostState.showSnackbar("end of the list!")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                modifier = Modifier.wrapContentSize(),
                onClick = {
                    // show snackbar as a suspend function
                    val previousValue = fabNavigationState

                    val index = when (previousValue) {
                        ScrollingDirection.UP -> 0
                        ScrollingDirection.DOWN -> searchItems.lastIndex
                    }

                    coroutineScope.launch {
                        listState.scrollToItem(index)
                    }

                    val newValue = when (previousValue) {
                        ScrollingDirection.UP -> ScrollingDirection.DOWN
                        ScrollingDirection.DOWN -> ScrollingDirection.UP
                    }

                    fabNavigationState = newValue
                }
            ) {
                val text = when {
                    isBegin -> ScrollingDirection.DOWN.name
                    isEnd -> ScrollingDirection.UP.name
                    else -> fabNavigationState.name
                }

                Text(text)
            }
        }
    ) { paddingValues ->
        if (searchItems.isEmpty()) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingValues),
                text = "No results found"
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                items(
                    items = searchItems,
                ) { item ->
                    ListItem(
                        item = item,
                        isMovieFavourited = isMovieFavourited,
                        onToggleMovieFavorited = onToggleMovieFavorited,
                        onItemTapped = onItemTapped,
                    )
                }
            }
        }
    }
}

@Composable
fun ListItem(
    modifier: Modifier = Modifier,
    item: SearchResult,
    isMovieFavourited: (SearchResult) -> Boolean = { false },
    onToggleMovieFavorited: suspend (SearchResult) -> Boolean = { false },
    onItemTapped: (ItemDetailDestination.Details) -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()

    val borderWidth = 20.dp
    val roundedCornerShape = RoundedCornerShape(borderWidth)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .background(
                color = Purple40,
                shape = roundedCornerShape
            )
            .clickable {
                onItemTapped(ItemDetailDestination.Details(item.title))
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        var isFavourited by rememberSaveable { mutableStateOf(false) }

        isFavourited = isMovieFavourited(item)

        Column(
            modifier = Modifier.weight(2f),
            horizontalAlignment = Alignment.Start,
        ) {
            AsyncImage(
                model = item.poster,
                placeholder = painterResource(id = R.mipmap.img),
                modifier = Modifier
                    .size(150.dp)
                    .padding(5.dp)
                    .border(
                        border = BorderStroke(borderWidth / 8, Color.Gray),
                        shape = RoundedCornerShape(borderWidth)
                    )
                    .padding(borderWidth / 8)
                    .clip(roundedCornerShape),
                contentDescription = "Poster",
                contentScale = ContentScale.Crop,
            )
        }

        Column(
            modifier = Modifier.weight(3f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
        ) {
            Text(
                text = item.title,
            )
        }

        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.End,
        ) {
            Switch(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(end = 5.dp),
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
}

@ExperimentalMaterial3Api
@Preview(showBackground = true)
@Composable
fun MovieItemComposablePreview() {
    DEMO_OMDB_APITheme {
        FavouritesComposableContent(
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
fun MovieItemListComposablePreview() {
    DEMO_OMDB_APITheme {
        FavouritesComposableContent(
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
