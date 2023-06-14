package ru.netology.nework.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nework.api.ApiService
import ru.netology.nework.error.ApiError
import ru.netology.nework.model.AuthModel
import ru.netology.nework.model.MediaModel
import ru.netology.nework.model.SignUpModelState
import ru.netology.nework.util.SingleLiveEvent
import java.io.File
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val apiService: ApiService,
) : ViewModel() {

    private val _stateSignUp = MutableLiveData<SignUpModelState>()
    val stateSignUp: LiveData<SignUpModelState>
        get() = _stateSignUp

    private val _signUpApp = SingleLiveEvent<AuthModel>()
    val signUpApp: LiveData<AuthModel>
        get() = _signUpApp

    private val _mediaAvatar = MutableLiveData<MediaModel?>(null)
    val mediaAvatar: LiveData<MediaModel?>
        get() = _mediaAvatar

    fun signUp(
        login: String,
        password: String,
        name: String,
    ) = viewModelScope.launch {
        try {
            when (val media = mediaAvatar.value) {
                null -> registration(login, password, name)
                else -> registrationWithAvatar(login, password, name, media)
            }
        } catch (e: Exception) {
            _stateSignUp.value = SignUpModelState(signUpError = true)
        }
    }

    fun changeAvatar(file: File, uri: Uri) {
        _mediaAvatar.value = MediaModel(uri, file)
    }

    fun clearAvatar() {
        _mediaAvatar.value = null
    }

    private suspend fun registration(login: String, password: String, name: String) {
        try {
            val response = apiService.registerUser(login, password, name)

            if (!response.isSuccessful) {
                if (response.code() == 400) {
                    _stateSignUp.value = SignUpModelState(signUpWrong = true)
                }
                _stateSignUp.value = SignUpModelState(signUpError = true)
            }

            val body = response.body() ?: throw ApiError(
                response.code(),
                response.message()
            )
            _signUpApp.postValue(body)
            _stateSignUp.value = SignUpModelState()
        } catch (e: IOException) {
            _stateSignUp.value = SignUpModelState(signUpError = true)
        } catch (e: Exception) {
            _stateSignUp.value = SignUpModelState(signUpWrong = true)
        }
    }

    private suspend fun registrationWithAvatar(
        login: String,
        password: String,
        name: String,
        media: MediaModel
    ) {
        try {
            val part = MultipartBody.Part.createFormData(
                "file", media.file.name, media.file.asRequestBody()
            )

            val response = apiService.registerWithPhoto(
                login.toRequestBody("text/plain".toMediaType()),
                password.toRequestBody("text/plain".toMediaType()),
                name.toRequestBody("text/plain".toMediaType()),
                part
            )

            if (!response.isSuccessful) {
                if (response.code() == 400) {
                    _stateSignUp.value = SignUpModelState(signUpWrong = true)
                }
                _stateSignUp.value = SignUpModelState(signUpError = true)
            }

            val body = response.body() ?: throw ApiError(
                response.code(),
                response.message()
            )
            _signUpApp.postValue(body)
            _stateSignUp.value = SignUpModelState()
            clearAvatar()

        } catch (e: IOException) {
            _stateSignUp.value = SignUpModelState(signUpError = true)
        } catch (e: Exception) {
            _stateSignUp.value = SignUpModelState(signUpWrong = true)
        }
    }
}