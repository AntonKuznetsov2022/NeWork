package ru.netology.nework.dto

sealed class FeedItem {
    abstract val id: Long
}

data class Post(
    override val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String? = null,
    val authorJob: String? = null,
    val content: String,
    val published: String,
    val coords: Coords? = null,
    val link: String? = null,
    //val likeOwnerIds: List<Int>,
    //val mentionIds: List<Int>,
    val mentionedMe: Boolean = false,
    val likedByMe: Boolean = false,
    val likes: Int,
    val ownedByMe: Boolean = false,
) : FeedItem ()

data class Attachment(
    val url: String,
    val type: AttachmentType,
)

enum class AttachmentType {
    IMAGE
}