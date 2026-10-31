package com.example.challenge_letssign.utils

sealed class AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>()
    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : AppResult<Nothing>()

    val isSuccess: Boolean
        get() = this is Success

    val isError: Boolean
        get() = this is Error

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }
}

inline fun <T> runCatchingResult(
    errorMessage: String = "Ocorreu um erro inesperado.",
    block: () -> T
): AppResult<T> {
    return try {
        AppResult.Success(block())
    } catch (e: Exception) {
        AppResult.Error(
            message = e.message?.takeIf { it.isNotBlank() } ?: errorMessage,
            cause = e
        )
    }
}
