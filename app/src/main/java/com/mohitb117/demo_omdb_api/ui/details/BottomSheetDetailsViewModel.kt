package com.mohitb117.demo_omdb_api.ui.details

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mohitb117.demo_omdb_api.datamodels.DetailsResultsBody
import com.mohitb117.demo_omdb_api.datamodels.SearchResult
import com.mohitb117.demo_omdb_api.repositories.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailsViewState(
    val searchTerm: String = "dogs",
    val searchResult: DetailsResultsBody? = null,
    val errorReceived: String? = null
)

/**
 * ViewModel responsible for fetching detailed results from from OMDB api.
 */
@HiltViewModel
class BottomSheetDetailsViewModel
@Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    private val _viewState: MutableLiveData<DetailsViewState?> = MutableLiveData(null)
    val viewState: LiveData<DetailsViewState?> = _viewState

    fun loadDetailsResult(imdbId: String) {
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            Log.e(BottomSheetDetailsViewModel::class.java.simpleName, "Error Encountered: ${throwable.localizedMessage}")
            _viewState.postValue(DetailsViewState(imdbId, null, throwable.localizedMessage))
        }

        viewModelScope.launch(Dispatchers.IO + exceptionHandler) {
            val result = repository.loadDetails(imdbId)

            when {
                result.isSuccessful -> _viewState.postValue(DetailsViewState(imdbId, result.body()!!))

                else -> _viewState.postValue(DetailsViewState(imdbId, null, result.errorBody().toString()))
            }
        }
    }

    suspend fun isFavourite(imdbId: String): Boolean {
        return repository.isFavourite(imdbId)
    }

    fun insert(searchResult: SearchResult) {
        viewModelScope.launch { repository.insert(searchResult) }
    }

    fun delete(imdbId: String) {
        viewModelScope.launch { repository.delete(imdbId) }
    }
}