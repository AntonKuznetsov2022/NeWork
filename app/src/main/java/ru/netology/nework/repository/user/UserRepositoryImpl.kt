package ru.netology.nework.repository.user

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.netology.nework.api.ApiService
import ru.netology.nework.dao.UserDao
import ru.netology.nework.dto.User
import ru.netology.nework.entity.toDto
import ru.netology.nework.entity.toEntity
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.NetworkError
import ru.netology.nework.error.UnknownError
import java.io.IOException
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val apiService: ApiService,
) : UserRepository {

    override val data: Flow<List<User>> = userDao.getAll().map {
        it.toDto()
    }.flowOn(Dispatchers.Default)

    override suspend fun getAllUsers() {
        try {
            val usersResponse = apiService.getAllUsers()
            if (!usersResponse.isSuccessful) {
                throw ApiError(usersResponse.code(), usersResponse.message())
            }

            val body = usersResponse.body() ?: throw ApiError(
                usersResponse.code(),
                usersResponse.message()
            )
            userDao.insert(body.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            e.printStackTrace()
            throw UnknownError
        }
    }

    override suspend fun getUserById(id: Int): User {
        try {
            val userResponse = apiService.getUserById(id)
            if (!userResponse.isSuccessful) {
                throw ApiError(userResponse.code(), userResponse.message())
            }
            return userResponse.body() ?: throw Exception()
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            e.printStackTrace()
            throw UnknownError
        }
    }
}
