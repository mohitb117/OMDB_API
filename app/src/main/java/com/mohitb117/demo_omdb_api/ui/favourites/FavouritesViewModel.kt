package com.mohitb117.demo_omdb_api.ui.favourites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.mohitb117.demo_omdb_api.datamodels.SearchResult
import com.mohitb117.demo_omdb_api.repositories.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for fetching / loading results from cached data from OMDB on RoomDB.
 */
@HiltViewModel
class FavouritesViewModel
@Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {
    fun getFavItems() = repository.getFavItems()

    private val _favouritedIds = MutableStateFlow<Set<SearchResult>>(emptySet())
    val favouritedIdsFlow = _favouritedIds.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getFavItems()
                .asFlow()
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(1000),
                    initialValue = emptyList()
                )
                .collect {
                    _favouritedIds.value = it.toSet()
                }
        }
    }
}