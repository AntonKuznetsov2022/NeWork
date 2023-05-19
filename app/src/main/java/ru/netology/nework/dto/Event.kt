package ru.netology.nework.dto

sealed class EventItem {
    abstract val id: Long
}

data class Event(
    override val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String? = null,
    val authorJob: String? = null,
    val content: String,
    val datetime: String,
    val published: String,
    val coords: Coords? = null,
    val type: String,
    //val likeOwnerIds: List<Int> = emptyList(),
    val likedByMe: Boolean = false,
    //val speakerIds: List<Int?>? = emptyList(),
    //val participantsIds: List<Int?>? = emptyList(),
    val participatedByMe: Boolean = false,
    val link: String? = null,
    val ownedByMe: Boolean = false,
    val likes: Int = 0,
) : EventItem ()