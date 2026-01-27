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
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class Result<out T>(open val searchTerm: String) {
    data class Success<T>(val value: T, override val searchTerm: String) : Result<T>(searchTerm)
    data class Failure<T>(val error: Throwable, override val searchTerm: String) :
        Result<T>(searchTerm)

    data class Loading(override val searchTerm: String) : Result<Nothing>(searchTerm)
}

data class SearchViewState(
    val searchTerm: String = "dogs",
    val searchResult: SearchResultsBody? = null,
    val errorReceived: String? = null
)

/**
 * ViewModel responsible for fetching / loading results from OMDB.
 */
@HiltViewModel
class SearchViewModel
@Inject constructor(
    private val repository: MovieRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _viewStateLiveData: MutableLiveData<Result<SearchResultsBody>> =
        MutableLiveData(Result.Loading(""))

    val searchResultsViewState = _viewStateLiveData.asFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(1000),
        initialValue = Result.Loading("")
    )

    // Efficiently hold the IDs of favorited movies

    fun getFavItems() = repository.getFavItems()

    private val _favouritedIds = MutableStateFlow<Set<SearchResult>>(emptySet())
    val favouritedIds = _favouritedIds.asStateFlow()

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

    fun loadSearchResult(searchTerm: String) {
        _viewStateLiveData.value = Result.Loading(searchTerm)

        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            Log.e(
                SearchViewModel::class.java.simpleName,
                "Error Encountered: ${throwable.localizedMessage}"
            )
            val error = Throwable(throwable.localizedMessage)
            _viewStateLiveData.value = Result.Failure(error = error, searchTerm = searchTerm)
        }

        viewModelScope.launch(exceptionHandler) {
            val result = when (
                val result = withContext(Dispatchers.IO) { repository.loadResults(searchTerm) }
            ) {
                is ApiResult.Success -> Result.Success<SearchResultsBody>(
                    searchTerm = searchTerm,
                    value = result.value
                )

                is ApiResult.Failure -> {
                    val error = when (result) {
                        is ApiResult.Failure.ApiFailure -> result.error.toString()
                        is ApiResult.Failure.HttpFailure -> result.error.toString()
                        is ApiResult.Failure.NetworkFailure -> result.error.toString()
                        is ApiResult.Failure.UnknownFailure -> result.error.toString()
                        else -> "Not sure what is going on!!! :sob: "
                    }

                    Result.Failure(error = Throwable(error), searchTerm = searchTerm)
                }
            }

            _viewStateLiveData.value = result
        }
    }

    suspend fun toggleFavourite(result: SearchResult): Boolean {
        return repository.toggleFavourite(result)
    }
}
