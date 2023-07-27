package ru.netology.nework.repository.job

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.netology.nework.api.ApiService
import ru.netology.nework.dao.JobDao
import ru.netology.nework.dto.Job
import ru.netology.nework.entity.JobEntity
import ru.netology.nework.entity.toDto
import ru.netology.nework.entity.toEntity
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.NetworkError
import ru.netology.nework.error.UnknownError
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JobRepositoryImpl @Inject constructor(
    private val jobDao: JobDao,
    private val apiService: ApiService,
) : JobRepository {

    override val data: Flow<List<Job>> = jobDao.getAll().map {
        it.toDto()
    }.flowOn(Dispatchers.Default)

    override suspend fun getJobsById(userId: Int) {
        try {
            jobDao.removeAll()
            val jobResponse = apiService.getJobsById(userId)
            if (!jobResponse.isSuccessful) {
                throw ApiError(jobResponse.code(), jobResponse.message())
            }
            val body = jobResponse.body() ?: throw ApiError(
                jobResponse.code(),
                jobResponse.message()
            )
            jobDao.insert(body.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            e.printStackTrace()
            throw UnknownError
        }
    }

    override suspend fun saveJob(authToken: String, job: Job) {
        try {
            val jobResponse = apiService.saveJob(authToken, job)
            if (!jobResponse.isSuccessful) {
                throw ApiError(jobResponse.code(), jobResponse.message())
            }

            val body = jobResponse.body() ?: throw ApiError(
                jobResponse.code(),
                jobResponse.message()
            )
            jobDao.insert(JobEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            e.printStackTrace()
            throw UnknownError
        }
    }

    override suspend fun removeJob(authToken: String, id: Int) {
        try {
            val jobResponse = apiService.removeJob(authToken, id)
            if (!jobResponse.isSuccessful) {
                throw ApiError(jobResponse.code(), jobResponse.message())
            }
            jobDao.removeById(id)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            e.printStackTrace()
            throw UnknownError
        }
    }
}