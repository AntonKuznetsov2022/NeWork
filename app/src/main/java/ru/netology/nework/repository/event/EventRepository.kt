package ru.netology.nework.repository.event

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.dto.EventItem
import ru.netology.nework.dto.FeedItem

interface EventRepository {
    val data: Flow<PagingData<EventItem>>
    suspend fun get()
    suspend fun likeById(authToken: String, id: Int, userId: Int)
    suspend fun unlikeById(authToken: String, id: Int, userId: Int)
}