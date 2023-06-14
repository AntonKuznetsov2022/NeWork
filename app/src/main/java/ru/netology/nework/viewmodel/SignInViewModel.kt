package ru.netology.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nework.api.ApiService
import ru.netology.nework.error.ApiError
import ru.netology.nework.model.AuthModel
import ru.netology.nework.model.SignInModelState
import ru.netology.nework.util.SingleLiveEvent
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val apiService: ApiService,
) : ViewModel() {

    private val _stateSignIn = MutableLiveData<SignInModelState>()
    val stateSignIn: LiveData<SignInModelState>
        get() = _stateSignIn

    private val _signInApp = SingleLiveEvent<AuthModel>()
    val signInApp: LiveData<AuthModel>
        get() = _signInApp

    fun signIn(login: String, password: String) = viewModelScope.launch {
        try {
            val response = apiService.updateUser(login, password)
            if (!response.isSuccessful) {
                if (response.code() == 400 || response.code() == 404) {
                    _stateSignIn.value = SignInModelState(signInWrong = true)
                }
                _stateSignIn.value = SignInModelState(signInError = true)
            }

            val body = response.body() ?: throw ApiError(
                response.code(),
                response.message()
            )
            _signInApp.postValue(body)
            _stateSignIn.value = SignInModelState()
        } catch (e: IOException) {
            _stateSignIn.value = SignInModelState(signInError = true)
        } catch (e: Exception) {
            _stateSignIn.value = SignInModelState(signInWrong = true)
        }
    }
}
