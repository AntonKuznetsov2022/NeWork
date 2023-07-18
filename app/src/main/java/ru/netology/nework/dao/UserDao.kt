package ru.netology.nework.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.entity.UserEntity

@Dao
interface UserDao {

    @Query("SELECT * FROM UserEntity ORDER BY id DESC")
    fun getAll(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: List<UserEntity>)

    @Query("SELECT * FROM UserEntity WHERE id = :id")
    suspend fun getUser(id: Int): UserEntity

    @Upsert
    suspend fun save(user: UserEntity)

    suspend fun speakerById(id: Int) {
        val user = getUser(id)
        save(user.copy(isSelected = true))
    }

    suspend fun unSpeakerById(id: Int) {
        val user = getUser(id)
        save(user.copy(isSelected = false))
    }
}