package ru.netology.nework.model

data class FeedModelState(
    val loading: Boolean = false,
    val error: Boolean = false,
    val refreshing: Boolean = false,
)

data class SignInModelState(
    val signInError: Boolean = false,
    val signInWrong: Boolean = false,
)

data class SignUpModelState(
    val signUpError: Boolean = false,
    val passCheck: Boolean = false
)
