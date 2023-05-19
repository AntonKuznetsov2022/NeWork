package ru.netology.nework.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nework.dto.Coords
import ru.netology.nework.dto.Event

@Entity
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String?,
    val authorJob: String?,
    val content: String,
    val datetime: String,
    val published: String,
    val latitude: Double?,
    val longitude: Double?,
    val type: String,
    //val likeOwnerIds: List<Int>,
    val likedByMe: Boolean,
    //val speakerIds: List<Int>,
    //val participantsIds: List<Int>,
    val participatedByMe: Boolean,
    val link: String?,
    val ownedByMe: Boolean = false,
    val likes: Int,
) {
    fun toDto() =
        Event(
            id,
            authorId,
            author,
            authorAvatar,
            authorJob,
            content,
            datetime,
            published,
            latitude?.let { longitude?.let { it1 -> Coords(it, it1) } },
            type,
            //likeOwnerIds,
            likedByMe,
            //speakerIds,
            //participantsIds,
            participatedByMe,
            link,
            ownedByMe,
            likes,
        )

    companion object {
        fun fromDto(dto: Event) =
            EventEntity(
                dto.id,
                dto.authorId,
                dto.author,
                dto.authorAvatar,
                dto.authorJob,
                dto.content,
                dto.datetime,
                dto.published,
                dto.coords?.lat,
                dto.coords?.long,
                dto.type,
                //dto.likeOwnerIds,
                dto.likedByMe,
                //dto.speakerIds,
                //dto.participantsIds,
                dto.participatedByMe,
                dto.link,
                dto.ownedByMe,
                dto.likes,
            )
    }
}

fun List<EventEntity>.toDto(): List<Event> = map(EventEntity::toDto)
fun List<Event>.toEntity(): List<EventEntity> = map(EventEntity::fromDto)