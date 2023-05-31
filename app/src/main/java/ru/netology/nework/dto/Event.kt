package ru.netology.nework.dto

sealed class EventItem {
    abstract val id: Int
}

data class Event(
    override val id: Int,
    val authorId: Int,
    val author: String,
    val authorAvatar: String? = null,
    val authorJob: String? = null,
    val content: String,
    val datetime: String,
    val published: String,
    val coords: Coords? = null,
    val type: EventType,
    val likeOwnerIds: List<Int> = emptyList(),
    val likedByMe: Boolean = false,
    val speakerIds: List<Int> = emptyList(),
    val participantsIds: List<Int> = emptyList(),
    val participatedByMe: Boolean = false,
    val link: String? = null,
    val ownedByMe: Boolean = false,
    val attachment: Attachment? = null,
) : EventItem ()

enum class EventType {
    OFFLINE,
    ONLINE
}