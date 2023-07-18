package ru.netology.nework.repository.event

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okio.IOException
import ru.netology.nework.api.ApiService
import ru.netology.nework.dao.EventDao
import ru.netology.nework.dao.EventRemoteKeyDao
import ru.netology.nework.db.AppDb
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.AttachmentType
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.EventItem
import ru.netology.nework.dto.Media
import ru.netology.nework.entity.EventEntity
import ru.netology.nework.entity.toEntity
import ru.netology.nework.error.NetworkError
import ru.netology.nework.error.UnknownError
import ru.netology.nework.error.ApiError
import ru.netology.nework.model.MediaModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val eventDao: EventDao,
    private val apiService: ApiService,
    eventRemoteKeyDao: EventRemoteKeyDao,
    appDb: AppDb,
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

    override suspend fun likeById(authToken: String, id: Int, userId: Int) {
        try {
            val eventResponse = apiService.likeEventById(authToken, id)
            if (!eventResponse.isSuccessful) {
                throw ApiError(eventResponse.code(), eventResponse.message())
            }
            eventDao.likedById(id, userId)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            e.printStackTrace()
            throw UnknownError
        }
    }

    override suspend fun unlikeById(authToken: String, id: Int, userId: Int) {
        try {
            val eventResponse = apiService.unlikeEventById(authToken, id)
            if (!eventResponse.isSuccessful) {
                throw ApiError(eventResponse.code(), eventResponse.message())
            }
            eventDao.unlikedById(id, userId)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            e.printStackTrace()
            throw UnknownError
        }
    }

    override suspend fun participantById(authToken: String, id: Int, userId: Int) {
        try {
            val eventResponse = apiService.participantById(authToken, id)
            if (!eventResponse.isSuccessful) {
                throw ApiError(eventResponse.code(), eventResponse.message())
            }
            eventDao.participantById(id, userId)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            e.printStackTrace()
            throw UnknownError
        }
    }

    override suspend fun unParticipantById(authToken: String, id: Int, userId: Int) {
        try {
            val eventResponse = apiService.unParticipantById(authToken, id)
            if (!eventResponse.isSuccessful) {
                throw ApiError(eventResponse.code(), eventResponse.message())
            }
            eventDao.unParticipantById(id, userId)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            e.printStackTrace()
            throw UnknownError
        }
    }

    override suspend fun removeById(authToken: String, id: Int) {
        try {
            val eventResponse = apiService.removeEventById(authToken, id)
            if (!eventResponse.isSuccessful) {
                throw ApiError(eventResponse.code(), eventResponse.message())
            }
            eventDao.removeById(id)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            e.printStackTrace()
            throw UnknownError
        }
    }

    override suspend fun save(authToken: String, event: Event) {
        try {
            val eventsResponse = apiService.saveEvent(authToken, event)
            if (!eventsResponse.isSuccessful) {
                throw ApiError(eventsResponse.code(), eventsResponse.message())
            }

            val body = eventsResponse.body() ?: throw ApiError(
                eventsResponse.code(),
                eventsResponse.message()
            )
            eventDao.insert(EventEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            e.printStackTrace()
            throw UnknownError
        }
    }

    override suspend fun saveWithAttachment(
        authToken: String,
        event: Event,
        mediaModel: MediaModel,
        attachmentType: AttachmentType,
    ) {
        try {
            val media = upload(authToken, mediaModel)
            val eventsResponse = apiService.saveEvent(
                authToken, event.copy(
                    attachment = Attachment(media.url, attachmentType)
                )
            )
            if (!eventsResponse.isSuccessful) {
                throw ApiError(eventsResponse.code(), eventsResponse.message())
            }

            val body = eventsResponse.body() ?: throw ApiError(
                eventsResponse.code(),
                eventsResponse.message()
            )
            eventDao.insert(EventEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            e.printStackTrace()
            throw UnknownError
        }
    }

    private suspend fun upload(authToken: String, media: MediaModel): Media {
        val part = MultipartBody.Part.createFormData(
            "file", media.file.name, media.file.asRequestBody()
        )

        val postsResponse = apiService.uploadMedia(authToken, part)
        if (!postsResponse.isSuccessful) {
            throw ApiError(postsResponse.code(), postsResponse.message())
        }

        return requireNotNull(postsResponse.body())
    }
}