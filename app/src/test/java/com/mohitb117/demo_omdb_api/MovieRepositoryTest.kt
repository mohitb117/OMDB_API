package com.mohitb117.demo_omdb_api

import com.mohitb117.demo_omdb_api.dao.FavouritesDao
import com.mohitb117.demo_omdb_api.datamodels.SearchResult
import com.mohitb117.demo_omdb_api.datamodels.SearchResultsBody
import com.mohitb117.demo_omdb_api.endpoints.OMDBApi
import com.mohitb117.demo_omdb_api.repositories.MovieRepository
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test

import org.junit.Assert.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.anyString
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.stubbing.OngoingStubbing
import retrofit2.Response

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
private const val FAKE_STRING = "HELLO WORLD"

@Suppress("NOTHING_TO_INLINE")
inline fun <T> whenever(methodCall: T): OngoingStubbing<T> {
    return `when`(methodCall)!!
}

@RunWith(MockitoJUnitRunner::class)
class MovieRepositoryTest {

    @Mock
    private lateinit var mockApi: OMDBApi

    @Mock
    private lateinit var mockFavDao: FavouritesDao

    @Test
    fun dummyApiWithSuccess() = runBlocking {
        // Arrange
        val successfulResult = Response.success(200, SearchResultsBody(emptyList(), "12", "False"))
        whenever(mockApi.loadResults(anyString(), anyString())).thenReturn(successfulResult)

        val repository = MovieRepository(mockApi, mockFavDao)

        // Act.
        val result = repository.loadResults("12345")

        // Assert.
        assertTrue(result.isSuccessful)
        assertEquals(successfulResult.body(), result.body())
    }

    @Test
    fun dummyApiWithError() = runBlocking {
        // Arrange
        val errorResponse = Response.error<SearchResultsBody>(404, "null".toResponseBody())
        whenever(mockApi.loadResults(anyString(), anyString())).thenReturn(errorResponse)

        val repository = MovieRepository(mockApi, mockFavDao)

        // Act.
        val result = repository.loadResults("12345")

        // Assert.
        assertFalse(result.isSuccessful)
        assertNull(result.body())
    }

    @Test
    fun repositoryReturnsTrueWhenDbHasValidValue() = runBlocking {
        // Arrange
        whenever(mockFavDao.getForId("12345"))
            .thenReturn(listOf(SearchResult("", "", "", "")))

        val repository = MovieRepository(mockApi, mockFavDao)

        // Act.
        val result = repository.isFavourite("12345")

        // Assert.
        assertTrue(result)
    }

    @Test
    fun repositoryReturnsTrueWhenDbHasNoValue() = runBlocking {
        // Arrange
        whenever(mockFavDao.getForId("12345"))
            .thenReturn(emptyList())

        val repository = MovieRepository(mockApi, mockFavDao)

        // Act.
        val result = repository.isFavourite("12345")

        // Assert.
        assertFalse(result)
    }
}