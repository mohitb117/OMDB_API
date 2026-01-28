package com.mohitb117.demo_omdb_api.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.mohitb117.demo_omdb_api.activities.ui.theme.DEMO_OMDB_APITheme
import com.mohitb117.demo_omdb_api.datamodels.SearchResult
import com.mohitb117.demo_omdb_api.datamodels.SearchResultsBody
import com.mohitb117.demo_omdb_api.ui.favourites.FavouritesComposableContent
import kotlinx.coroutines.launch

@ExperimentalMaterial3Api
@Composable
fun MovieItemListComposable(
    modifier: Modifier,
    searchResults: SearchResultsBody,
    isMovieFavourited: (SearchResult) -> Boolean = { false },
    onToggleMovieFavorited: suspend (SearchResult) -> Boolean = { false },
) {
    val searchItems = searchResults.Search.orEmpty()
    val coroutineScope = rememberCoroutineScope()

    if (searchItems.isEmpty()) {
        Text(modifier = Modifier.fillMaxWidth(), text = "No results found")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            items(
                items = searchItems,
                key = { it.imdbID }
            ) { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Absolute.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val borderWidth = 4.dp
                    val roundedCornerShape = RoundedCornerShape(borderWidth)
                    var isFavourited by rememberSaveable { mutableStateOf(false) }

                    isFavourited = isMovieFavourited(item)

                    AsyncImage(
                        model = item.poster,
                        placeholder = painterResource(id = R.mipmap.img),
                        modifier = Modifier
                            .size(100.dp)
                            .padding(5.dp)
                            .border(
                                BorderStroke(borderWidth, Color.Gray),
                                roundedCornerShape
                            )
                            .padding(borderWidth / 2)
                            .clip(roundedCornerShape),
                        contentDescription = "Poster",
                        contentScale = ContentScale.Crop,
                    )

                    Text(
                        modifier = Modifier.fillMaxWidth(fraction = .3f),
                        text = item.title,
                    )

                    Switch(
                        modifier = Modifier.fillMaxWidth(fraction = .1f),
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
    }
}

@Composable
fun ListItem(
    modifier: Modifier = Modifier
) {

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
                    title = "Star Wars: Episode IV - A New Hope",
                    type = "movie",
                    poster = "https://m.media-amazon.com/images/M/MV5BOTA5NjhiOTAtZWM0ZC00MWNhLWE1OTktZDA4MjkwYmY0OTU3XkEyXkFqcGdeQXVyMTQxNzMzNDI@._V1_SX300.jpg"
                ),
                SearchResult(
                    imdbID = "tt0080684",
                    title = "Star Wars: Episode V - The Empire Strikes Back",
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
