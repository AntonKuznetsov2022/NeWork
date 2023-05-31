package ru.netology.nework.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nework.dto.Coords
import ru.netology.nework.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val authorId: Int,
    val author: String,
    val authorAvatar: String?,
    val authorJob: String?,
    val content: String,
    val published: String,
    val latitude: Double?,
    val longitude: Double?,
    val link: String?,
    val likeOwnerIds: List<Int>,
    val mentionIds: List<Int>,
    val mentionedMe: Boolean = false,
    val likedByMe: Boolean = false,
    @Embedded
    val attachment: AttachmentEmbedded?,
    val ownedByMe: Boolean = false,
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
            likeOwnerIds,
            mentionIds,
            mentionedMe,
            likedByMe,
            attachment?.toDto(),
            ownedByMe,
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
                dto.likeOwnerIds,
                dto.mentionIds,
                dto.mentionedMe,
                dto.likedByMe,
                AttachmentEmbedded.fromDto(dto.attachment),
                dto.ownedByMe,
            )
    }
}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(): List<PostEntity> = map(PostEntity::fromDto)