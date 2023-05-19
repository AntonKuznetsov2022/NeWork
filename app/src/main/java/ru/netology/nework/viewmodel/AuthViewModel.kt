package ru.netology.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.model.AuthModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    appAuth: AppAuth,
) : ViewModel() {
    val data: LiveData<AuthModel?> = appAuth
        .data
        .asLiveData(Dispatchers.Default)

    val authorized: Boolean
        get() = data.value != null
}