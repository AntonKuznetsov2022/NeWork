package ru.netology.nework.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.entity.EventEntity

@Dao
interface EventDao {

    @Query("SELECT * FROM EventEntity ORDER BY id DESC")
    fun getAll(): Flow<List<EventEntity>>

    @Query("SELECT COUNT(*) == 0 FROM EventEntity")
    suspend fun isEmpty(): Boolean

    @Query("SELECT * FROM EventEntity ORDER BY id DESC")
    fun allEventPaging(): PagingSource<Int, EventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(events: List<EventEntity>)

    @Query("SELECT * FROM EventEntity WHERE id = :id")
    suspend fun getEvent(id: Int): EventEntity

    @Query("DELETE FROM EventEntity WHERE id = :id")
    suspend fun removeById(id: Int)

    @Query("DELETE FROM EventEntity")
    suspend fun removeAll()

    @Upsert
    suspend fun save(event: EventEntity)

    suspend fun likedById(id: Int, userId: Int) {
        val event = getEvent(id)
        val likeUser = event.likeOwnerIds.toMutableList()
        likeUser.add(userId)
        save(event.copy(likedByMe = true, likeOwnerIds = likeUser))
    }

    suspend fun unlikedById(id: Int, userId: Int) {
        val event = getEvent(id)
        val likeUser = event.likeOwnerIds.toMutableList()
        likeUser.remove(userId)
        save(event.copy(likedByMe = false, likeOwnerIds = likeUser))
    }

    suspend fun participantById(id: Int, userId: Int) {
        val event = getEvent(id)
        val participantUser = event.participantsIds.toMutableList()
        participantUser.add(userId)
        save(event.copy(participatedByMe = true, participantsIds = participantUser))
    }

    suspend fun unParticipantById(id: Int, userId: Int) {
        val event = getEvent(id)
        val participantUser = event.participantsIds.toMutableList()
        participantUser.remove(userId)
        save(event.copy(participatedByMe = false, participantsIds = participantUser))
    }
}