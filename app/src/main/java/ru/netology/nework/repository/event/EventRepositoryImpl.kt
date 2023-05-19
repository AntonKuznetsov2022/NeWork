package ru.netology.nework.repository.event

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okio.IOException
import ru.netology.nework.api.ApiService
import ru.netology.nework.dao.EventDao
import ru.netology.nework.dao.EventRemoteKeyDao
import ru.netology.nework.db.AppDb
import ru.netology.nework.dto.EventItem
import ru.netology.nework.entity.EventEntity
import ru.netology.nework.entity.toEntity
import ru.netology.nework.error.NetworkError
import ru.netology.nework.error.UnknownError
import ru.netology.nework.error.ApiError
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor(
    private val eventDao: EventDao,
    private val apiService: ApiService,
    private val eventRemoteKeyDao: EventRemoteKeyDao,
    private val appDb: AppDb,
) : EventRepository {

    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<EventItem>> = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        pagingSourceFactory = { eventDao.allEventPaging() },
        remoteMediator = EventRemoteMediator(apiService, eventDao, eventRemoteKeyDao, appDb),
    ).flow
        .map { it.map(EventEntity::toDto) }

    override suspend fun get() {
        try {
            val eventsResponse = apiService.getAllEvent()
            if (!eventsResponse.isSuccessful) {
                throw ApiError(eventsResponse.code(), eventsResponse.message())
            }

            val body = eventsResponse.body() ?: throw ApiError(
                eventsResponse.code(),
                eventsResponse.message()
            )
            eventDao.insert(body.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            e.printStackTrace()
            throw UnknownError
        }
    }

}