package ru.netology.nework.dto

data class User(
    val id: Int,
    val login: String?,
    val name: String?,
    val avatar: String? = null,
    val isSelected: Boolean = false,
)
