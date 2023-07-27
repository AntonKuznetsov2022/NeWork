package ru.netology.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dto.User
import ru.netology.nework.model.UsersModelState
import ru.netology.nework.repository.user.UserRepositoryImpl
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val repository: UserRepositoryImpl,
    private val appAuth: AppAuth
) : ViewModel() {

    private val _stateUser = MutableLiveData<UsersModelState>()
    val stateUser: LiveData<UsersModelState>
        get() = _stateUser

    val userSignIn = MutableLiveData<User>()

    val user = MutableLiveData<User>()

    val data: Flow<List<User>> = repository.data
        .flowOn(Dispatchers.Default)

    init {
        loadUsers()
    }

    fun loadUsers() = viewModelScope.launch {
        _stateUser.value = UsersModelState(loading = true)
        try {
            repository.getAllUsers()
            _stateUser.value = UsersModelState()
        } catch (e: Exception) {
            _stateUser.value = UsersModelState(error = true)
        }
    }

    fun getUserSignIn() = viewModelScope.launch {
        _stateUser.value = UsersModelState(loading = true)
        try {
            userSignIn.value = repository.getUserById(appAuth.getId())
            _stateUser.value = UsersModelState()
        } catch (e: Exception) {
            _stateUser.value = UsersModelState(error = true)
        }
    }

    fun getUserById(id: Int) = viewModelScope.launch {
        _stateUser.value = UsersModelState(loading = true)
        try {
            user.value = repository.getUserById(id)
            _stateUser.value = UsersModelState()
        } catch (e: Exception) {
            _stateUser.value = UsersModelState(error = true)
        }
    }

    fun addSpeaker(id: Int) = viewModelScope.launch {
        repository.speakerById(id)
    }

    fun removeSpeaker(id: Int) = viewModelScope.launch {
        repository.unSpeakerById(id)
    }
}
