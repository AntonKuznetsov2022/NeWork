package ru.netology.nework.repository.post

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.dto.AttachmentType
import ru.netology.nework.dto.FeedItem
import ru.netology.nework.dto.Post
import ru.netology.nework.model.MediaModel

interface PostRepository {
    val data: Flow<PagingData<FeedItem>>
    suspend fun dataUserWall(userId: Int): Flow<PagingData<FeedItem>>
    suspend fun get()
    suspend fun getWall(userId: Int)
    suspend fun likeById(authToken: String, id: Int, userId: Int)
    suspend fun unlikeById(authToken: String, id: Int, userId: Int)
    suspend fun removeById(authToken: String, id: Int)
    suspend fun save(authToken: String, post: Post)
    suspend fun saveWithAttachment(authToken: String, post: Post, mediaModel: MediaModel, attachmentType: AttachmentType)
}