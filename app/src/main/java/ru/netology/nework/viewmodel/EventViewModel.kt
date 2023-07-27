package ru.netology.nework.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dto.AttachmentType
import ru.netology.nework.dto.Coords
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.EventItem
import ru.netology.nework.dto.EventType
import ru.netology.nework.model.FeedModelState
import ru.netology.nework.model.MediaModel
import ru.netology.nework.repository.event.EventRepository
import ru.netology.nework.util.SingleLiveEvent
import java.io.File
import javax.inject.Inject

private val empty = Event(
    id = 0,
    authorId = 0,
    author = "name_name",
    content = "",
    datetime = "",
    published = "1900-01-01T01:01:01.111111Z",
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

    private val _eventCreated = SingleLiveEvent<Unit>()
    val eventCreated: LiveData<Unit>
        get() = _eventCreated

    private val _state = MutableLiveData<FeedModelState>()
    val state: LiveData<FeedModelState>
        get() = _state

    private val _media = MutableLiveData<MediaModel?>(null)
    val media: LiveData<MediaModel?>
        get() = _media

    val data: Flow<PagingData<EventItem>> = appAuth.data
        .flatMapLatest { authState ->
            repository.data.map { events ->
                events.map { event ->
                    if (event is Event) {
                        event.copy(ownedByMe = authState?.id == event.authorId)
                    } else {
                        event
                    }
                }
            }
        }

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

    fun editEvent(event: Event) = viewModelScope.launch {
        edited.value = event
    }

    fun cancelEditEvent() = viewModelScope.launch {
        edited.value = empty
        clearPhoto()
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun changeLink(link: String) {
        val textLink = link.trim()
        if (edited.value?.link == textLink) {
            return
        }
        edited.value = edited.value?.copy(link = textLink.ifBlank { null })
    }

    fun saveSpeakers(speakerIds: Int) {
        val speakerIdsList= edited.value?.speakerIds?.toMutableList()
        speakerIdsList?.add(speakerIds)
        edited.value = speakerIdsList?.let { edited.value?.copy(speakerIds = it) }
    }

    fun removeSpeakers(speakerIds: Int) {
        val speakerIdsList = edited.value?.speakerIds?.toMutableList()
        speakerIdsList?.remove(speakerIds)
        edited.value = speakerIdsList?.let { edited.value?.copy(speakerIds = it) }
    }

    fun clearSpeakers() {
        val speakerIdsList: MutableList<Int> = mutableListOf()
        speakerIdsList.clear()
        edited.value = edited.value?.copy(speakerIds = speakerIdsList)
    }

    fun saveDatetime(dateTime: String) {
        edited.value = edited.value?.copy(datetime = dateTime)
    }

    fun saveCoords(latitude: Double?, longitude: Double?) {
        edited.value =
            edited.value?.copy(coords = latitude?.let { longitude?.let { it1 -> Coords(it, it1) } })
    }

    fun changePhoto(file: File, uri: Uri) {
        _media.value = MediaModel(uri, file, AttachmentType.IMAGE)
    }

    fun clearPhoto() {
        _media.value = null
    }

    fun changeEventType(eventType: EventType) {
        edited.value = edited.value?.copy(type = eventType)
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

    fun participantById(id: Int) {
        viewModelScope.launch {
            try {
                appAuth.getToken()?.let { token ->
                    repository.participantById(token, id, appAuth.getId())
                    _state.value = FeedModelState()
                }
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun unParticipantById(id: Int) {
        viewModelScope.launch {
            try {
                appAuth.getToken()?.let { token ->
                    repository.unParticipantById(token, id, appAuth.getId())
                    _state.value = FeedModelState()
                }
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }

    fun save() {
        edited.value?.let {
            appAuth.getToken()?.let { token ->
                viewModelScope.launch {
                    try {
                        when (val media = media.value) {
                            null -> repository.save(token, it)
                            else -> {
                                media.attachmentType?.let { it1 ->
                                    repository.saveWithAttachment(
                                        token, it, media,
                                        it1
                                    )
                                }
                            }
                        }
                        _eventCreated.value = Unit
                        edited.value = empty
                        clearPhoto()
                        _state.value = FeedModelState()
                    } catch (e: Exception) {
                        _state.value = FeedModelState(error = true)
                    }
                }
            }
        }
    }
}