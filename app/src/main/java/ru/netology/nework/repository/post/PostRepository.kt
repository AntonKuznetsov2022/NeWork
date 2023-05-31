package ru.netology.nework.repository.post

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.dto.FeedItem
import ru.netology.nework.dto.Post

interface PostRepository {
    val data: Flow<PagingData<FeedItem>>
    suspend fun get()
    suspend fun save(authToken: String, post: Post)
    suspend fun likeById(authToken: String, id: Int, userId: Int)
    suspend fun unlikeById(authToken: String, id: Int, userId: Int)
}