package ru.netology.nework.repository.job

import kotlinx.coroutines.flow.Flow
import ru.netology.nework.dto.Job

interface JobRepository {
    val data: Flow<List<Job>>
    suspend fun getJobsById(userId: Int)
    suspend fun saveJob(authToken: String, job: Job)
    suspend fun removeJob (authToken: String, id: Int)
}