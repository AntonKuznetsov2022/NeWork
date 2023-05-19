package ru.netology.nework.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nework.dto.Coords
import ru.netology.nework.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String?,
    val authorJob: String?,
    val content: String,
    val published: String,
    val latitude: Double?,
    val longitude: Double?,
    val link: String?,
    val mentionedMe: Boolean = false,
    val likedByMe: Boolean = false,
    val likes: Int = 0,
) {
    fun toDto() =
        Post(
            id,
            authorId,
            author,
            authorAvatar,
            authorJob,
            content,
            published,
            latitude?.let { longitude?.let { it1 -> Coords(it, it1) } },
            link,
            mentionedMe,
            likedByMe,
            likes,
        )

    companion object {
        fun fromDto(dto: Post) =
            PostEntity(
                dto.id,
                dto.authorId,
                dto.author,
                dto.authorAvatar,
                dto.authorJob,
                dto.content,
                dto.published,
                dto.coords?.lat,
                dto.coords?.long,
                dto.link,
                dto.mentionedMe,
                dto.likedByMe,
                dto.likes,
            )
    }
}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(): List<PostEntity> = map(PostEntity::fromDto)