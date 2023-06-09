package ru.netology.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.EventItem
import ru.netology.nework.dto.EventType
import ru.netology.nework.model.FeedModelState
import ru.netology.nework.repository.event.EventRepository
import javax.inject.Inject

private val empty = Event(
    id = 0,
    authorId = 0,
    author = "",
    content = "",
    datetime = "",
    published = "",
    type = EventType.OFFLINE,
    likedByMe = false,
)

@HiltViewModel
@ExperimentalCoroutinesApi
class EventViewModel @Inject constructor(
    private val repository: EventRepository,
    private val appAuth: AppAuth,
) : ViewModel() {

    val edited = MutableLiveData(empty)

    private val _state = MutableLiveData<FeedModelState>()
    val state: LiveData<FeedModelState>
        get() = _state

    val data: Flow<PagingData<EventItem>> = appAuth.data.flatMapLatest { authState ->
        repository.data
            .map { events ->
                events.map { event ->
                    if (event is Event) {
                        event.copy(ownedByMe = authState?.id == event.authorId)
                    } else {
                        event
                    }
                }
            }
    }.flowOn(Dispatchers.Default)

    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            try {
                _state.value = FeedModelState(loading = true)
                repository.get()
                _state.value = FeedModelState()
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun edit(event: Event) {
        edited.value = event
    }

    fun removeById(id: Int) {
        viewModelScope.launch {
            try {
                appAuth.getToken()?.let { token ->
                    repository.removeById(token, id)
                    _state.value = FeedModelState()
                }
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun likeById(id: Int) {
        viewModelScope.launch {
            try {
                appAuth.getToken()?.let { token ->
                    repository.likeById(token, id, appAuth.getId())
                    _state.value = FeedModelState()
                }
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun unlikeById(id: Int) {
        viewModelScope.launch {
            try {
                appAuth.getToken()?.let { token ->
                    repository.unlikeById(token, id, appAuth.getId())
                    _state.value = FeedModelState()
                }
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }
}