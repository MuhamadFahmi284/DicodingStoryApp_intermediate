package com.dicoding.dicodingstoryapp.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dicoding.dicodingstoryapp.data.ResultState
import com.dicoding.dicodingstoryapp.data.StoryRepository
import com.dicoding.dicodingstoryapp.data.api.response.FileUploadResponse
import com.dicoding.dicodingstoryapp.data.api.response.LoginResponse
import com.dicoding.dicodingstoryapp.data.api.response.RegisterResponse
import com.dicoding.dicodingstoryapp.data.api.response.Story
import com.dicoding.dicodingstoryapp.data.api.response.StoryResponse
import com.dicoding.dicodingstoryapp.data.pref.UserModel
import com.dicoding.dicodingstoryapp.data.room.StoryEntity
import com.dicoding.dicodingstoryapp.utils.EspressoIdlingResource
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException

class MainViewModel(private val repository: StoryRepository) : ViewModel() {

    val registerResult = MutableLiveData<ResultState<RegisterResponse>>()
    val loginResult = MutableLiveData<ResultState<LoginResponse>>()
    val uploadResult = MutableLiveData<ResultState<FileUploadResponse>>()

    val stories: LiveData<PagingData<StoryEntity>> =
        repository.getStories().cachedIn(viewModelScope)

    private val _storyDetail = MutableLiveData<ResultState<Story>>()
    val storyDetail: LiveData<ResultState<Story>> = _storyDetail

    private val _storiesWithLocation = MutableLiveData<ResultState<StoryResponse>>()
    val storiesWithLocation: LiveData<ResultState<StoryResponse>> = _storiesWithLocation

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            registerResult.value = ResultState.Loading

            try {
                val response = repository.register(name, email, password)
                registerResult.value = ResultState.Success(response)
            } catch (e: Exception) {
                val errorMessage =
                    (e as? HttpException)?.response()?.errorBody()?.string() ?: e.localizedMessage
                registerResult.value = ResultState.Error(errorMessage)
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            loginResult.value = ResultState.Loading
            EspressoIdlingResource.increment()

            try {
                val response = repository.login(email, password)
                val name = response.loginResult?.name.toString()
                val token = response.loginResult?.token.toString()
                val userModel =
                    UserModel(name = name, email = email, token = token, isLogin = true)
                saveSession(userModel)
                loginResult.value = ResultState.Success(response)
            } catch (e: Exception) {
                val errorMessage =
                    (e as? HttpException)?.response()?.errorBody()?.string() ?: e.localizedMessage
                loginResult.value = ResultState.Error(errorMessage)
            } finally {
                EspressoIdlingResource.decrement()
            }
        }
    }

    fun saveSession(user: UserModel) {
        viewModelScope.launch {
            repository.saveSession(user)
        }
    }

    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun uploadStory(
        image: MultipartBody.Part,
        description: RequestBody,
        lat: Float? = null,
        lon: Float? = null
    ) {
        viewModelScope.launch {
            uploadResult.value = ResultState.Loading
            try {
                val response = repository.uploadStory(image, description, lat, lon)
                uploadResult.value = ResultState.Success(response)
            } catch (e: Exception) {
                val errorMessage =
                    (e as? HttpException)?.response()?.errorBody()?.string() ?: e.localizedMessage
                registerResult.value = ResultState.Error(errorMessage)
            }
        }
    }

    fun getStoryDetail(id: String) {
        viewModelScope.launch {
            _storyDetail.value = ResultState.Loading
            try {
                val response = repository.getStoryDetail(id)
                val story = response.story

                if (story != null) {
                    _storyDetail.value = ResultState.Success(story)
                }

            } catch (e: Exception) {
                _storyDetail.value = ResultState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun getStoriesWithLocation() {
        viewModelScope.launch {
            _storiesWithLocation.value = ResultState.Loading
            try {
                val response = repository.getStoriesWithLocation()
                _storiesWithLocation.value = ResultState.Success(response)
            } catch (e: Exception) {
                _storiesWithLocation.value =
                    ResultState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }
}