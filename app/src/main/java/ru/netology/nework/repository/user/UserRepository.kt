package ru.netology.nework.repository.user

import kotlinx.coroutines.flow.Flow
import ru.netology.nework.dto.User

interface UserRepository {
    val data: Flow<List<User>>
    suspend fun getAllUsers()
    suspend fun getUserById(id: Int) : User
}