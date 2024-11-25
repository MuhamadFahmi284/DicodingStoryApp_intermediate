package com.dicoding.dicodingstoryapp.data

import androidx.lifecycle.LiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.dicoding.dicodingstoryapp.data.api.response.FileUploadResponse
import com.dicoding.dicodingstoryapp.data.api.response.LoginResponse
import com.dicoding.dicodingstoryapp.data.api.response.RegisterResponse
import com.dicoding.dicodingstoryapp.data.api.response.StoryDetailResponse
import com.dicoding.dicodingstoryapp.data.api.response.StoryResponse
import com.dicoding.dicodingstoryapp.data.api.retrofit.ApiService
import com.dicoding.dicodingstoryapp.data.pref.UserModel
import com.dicoding.dicodingstoryapp.data.pref.UserPreference
import com.dicoding.dicodingstoryapp.data.room.StoryDatabase
import com.dicoding.dicodingstoryapp.data.room.StoryEntity
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody

open class StoryRepository private constructor(
    private val database: StoryDatabase,
    private val userPreference: UserPreference,
    val apiService: ApiService
) {
    suspend fun register(name: String, email: String, password: String): RegisterResponse {
        return apiService.register(name, email, password)
    }

    suspend fun login(email: String, password: String): LoginResponse {
        return apiService.login(email, password)
    }

    suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }

    fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    suspend fun logout() {
        userPreference.logout()
    }

    suspend fun uploadStory(image: MultipartBody.Part, description: RequestBody, lat: Float?, lon: Float?): FileUploadResponse {
        return apiService.uploadStory(image, description, lat, lon)
    }

    fun getStories(): LiveData<PagingData<StoryEntity>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            remoteMediator = StoryRemoteMediator(database, apiService),
            pagingSourceFactory = {
                database.getStoryDao().getStory()
            }
        ).liveData
    }

    suspend fun getStoryDetail(id: String): StoryDetailResponse {
        return apiService.getStoryDetail(id)
    }

    suspend fun getStoriesWithLocation(): StoryResponse {
        return apiService.getStoriesWithLocation()
    }

    suspend fun getAllStories(): StoryResponse {
        return apiService.getStories()
    }

    companion object {
        @Volatile
        private var instance: StoryRepository? = null
        fun getInstance(
            database: StoryDatabase,
            userPreference: UserPreference,
            apiService: ApiService
        ): StoryRepository =
            instance ?: synchronized(this) {
                instance ?: StoryRepository(database, userPreference, apiService)
            }.also { instance = it }

        fun clearInstance() {
            instance = null
        }
    }
}