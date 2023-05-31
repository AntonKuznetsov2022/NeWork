package ru.netology.nework.dao

import androidx.paging.PagingSource
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.netology.nework.entity.PostEntity

@Dao
interface PostDao {

    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun getAll(): Flow<List<PostEntity>>

    @Query("SELECT COUNT(*) == 0 FROM PostEntity")
    suspend fun isEmpty(): Boolean

    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun allPostPaging(): PagingSource<Int, PostEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Query("SELECT * FROM PostEntity WHERE id = :id")
    suspend fun getPost(id: Int): PostEntity

    @Upsert
    suspend fun save(post: PostEntity)

    suspend fun likedById(id: Int, userId: Int) {
        val post = getPost(id)
        val likeUser = post.likeOwnerIds.toMutableList()
        likeUser.add(userId)
        save(post.copy(likedByMe = true, likeOwnerIds = likeUser))
    }

    suspend fun unlikedById(id: Int, userId: Int) {
        val post = getPost(id)
        val likeUser = post.likeOwnerIds.toMutableList()
        likeUser.remove(userId)
        save(post.copy(likedByMe = false, likeOwnerIds = likeUser))
    }
}
