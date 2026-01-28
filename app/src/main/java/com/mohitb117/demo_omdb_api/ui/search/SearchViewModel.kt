package com.mohitb117.demo_omdb_api.ui.search

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.mohitb117.demo_omdb_api.datamodels.SearchResult
import com.mohitb117.demo_omdb_api.datamodels.SearchResultsBody
import com.mohitb117.demo_omdb_api.repositories.MovieRepository
import com.slack.eithernet.ApiResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class Result<out T>(open val searchTerm: String) {
    data class Success<T>(val value: T, override val searchTerm: String) : Result<T>(searchTerm)
    data class Failure<T>(
        val error: Throwable,
        override val searchTerm: String,
        val lastCachedData: T? = null
    ) : Result<T>(searchTerm)

    data class Loading(override val searchTerm: String) : Result<Nothing>(searchTerm)
}

/**
 * ViewModel responsible for fetching / loading results from OMDB.
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: MovieRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val KEY_SEARCH_QUERY = "search_query"
        private const val KEY_LAST_RESULTS = "last_results"
    }

    private var lastCachedData: SearchResultsBody?
        get() = savedStateHandle.get<SearchResultsBody>(KEY_LAST_RESULTS)
        set(value) {
            savedStateHandle[KEY_LAST_RESULTS] = value
        }

    val searchQuery = savedStateHandle.getStateFlow(KEY_SEARCH_QUERY, "")

    private val _viewStateLiveData: MutableLiveData<Result<SearchResultsBody>> =
        MutableLiveData(
            lastCachedData?.let { Result.Success(it, searchQuery.value) } ?: Result.Loading("")
        )

    val searchResultsViewState = _viewStateLiveData.asFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(1000),
        initialValue = _viewStateLiveData.value ?: Result.Loading("")
    )

    private val _favouritedIds = MutableStateFlow<Set<SearchResult>>(emptySet())
    val favouritedIds = _favouritedIds.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    init {
        observeSearchQuery()
        observeFavorites()
    }

    @OptIn(FlowPreview::class)
    private fun observeSearchQuery() {
        searchQuery
            .debounce(500)
            .onEach { loadSearchResult(it) }
            .launchIn(viewModelScope)
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            repository.getFavItems()
                .asFlow()
                .collect {
                    _favouritedIds.value = it.toSet()
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        savedStateHandle[KEY_SEARCH_QUERY] = query
    }

    fun loadSearchResult(
        searchTerm: String,
        isForceRefresh: Boolean = false,
    ) {
        if (!isForceRefresh && (_viewStateLiveData.value is Result.Loading && (_viewStateLiveData.value as Result.Loading).searchTerm == searchTerm)) {
            return
        }

        if (!isForceRefresh) {
            _viewStateLiveData.value = Result.Loading(searchTerm)
        }

        viewModelScope.launch {
            performSearchInternal(searchTerm)
        }
    }

    private suspend fun performSearchInternal(searchTerm: String) {
        val data = try {
            val result = withContext(Dispatchers.IO) { repository.loadResults(searchTerm) }
            when (result) {
                is ApiResult.Success -> {
                    lastCachedData = result.value
                    Result.Success(result.value, searchTerm)
                }

                is ApiResult.Failure -> {
                    val error = when (result) {
                        is ApiResult.Failure.ApiFailure -> result.error.toString()
                        is ApiResult.Failure.HttpFailure -> result.error.toString()
                        is ApiResult.Failure.NetworkFailure -> result.error.toString()
                        is ApiResult.Failure.UnknownFailure -> result.error.toString()
                        else -> "Not sure what is going on!!! :sob: "
                    }
                    Result.Failure(error = Throwable(error), searchTerm = searchTerm, lastCachedData = lastCachedData)
                }
            }
        } catch (exception: Exception) {
            Log.e(SearchViewModel::class.java.simpleName, "Error: ${exception.localizedMessage}")
            Result.Failure(exception, searchTerm, lastCachedData)
        }

        _viewStateLiveData.value = data
    }

    suspend fun toggleFavourite(result: SearchResult): Boolean {
        return repository.toggleFavourite(result)
    }

    /**
     * Refreshes the search results.
     */
    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val term = searchQuery.value
            if (term.isNotBlank()) {
                performSearchInternal(term)
            }
            _isRefreshing.value = false
        }
    }
}
