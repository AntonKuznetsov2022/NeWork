package ru.netology.nework.viewmodel

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
import ru.netology.nework.dto.FeedItem
import ru.netology.nework.dto.Post
import ru.netology.nework.model.FeedModelState
import ru.netology.nework.repository.post.PostRepository
import ru.netology.nework.util.SingleLiveEvent
import javax.inject.Inject


@HiltViewModel
@ExperimentalCoroutinesApi
class MyWallViewModel @Inject constructor(
    private val repository: PostRepository,
    private val appAuth: AppAuth,
) : ViewModel() {

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _state = MutableLiveData<FeedModelState>()
    val state: LiveData<FeedModelState>
        get() = _state

    fun data (userId: Int): Flow<PagingData<FeedItem>> = appAuth.data
        .flatMapLatest { authState ->
            repository.dataUserWall(userId).map { posts ->
                posts.map { post ->
                    if (post is Post) {
                        post.copy(ownedByMe = authState?.id == post.authorId)
                    } else {
                        post
                    }
                }
            }
        }

    fun loadPostsById(userId: Int) {
        viewModelScope.launch {
            _state.value = FeedModelState(loading = true)
            try {
                repository.getWall(userId)
                _state.value = FeedModelState()
            } catch (e: Exception) {
                _state.value = FeedModelState(error = true)
            }
        }
    }
}