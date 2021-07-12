package com.mohitb117.demo_omdb_api.injection

import com.mohitb117.demo_omdb_api.endpoints.OMDBApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(
    ActivityComponent::class,
    ViewModelComponent::class,
    SingletonComponent::class,
)
class NetworkingModule {
    @Provides
    fun provideRetrofitApi(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(OMDB_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    fun provideOmdbApiEndpoints(retrofit: Retrofit): OMDBApi {
        return retrofit.create(OMDBApi::class.java)
    }

    companion object {
        const val OMDB_BASE_URL = "https://www.omdbapi.com/"
    }
}