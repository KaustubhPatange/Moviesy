package com.kpstv.yts.data.models

sealed class Result<out T> {
    data class Success<T>(val data: T): Result<T>()
    data class Error<T>(val ex: Exception): Result<T>()

    object Empty: Result<Nothing>()

    companion object {
        /**
         * Return [Success] with [data] to emit.
         */
        fun<T> success(data: T) = Success(data)

        fun<T> error(ex: Exception) = Error<T>(ex)

        /**
         * Return [Empty].
         */
        fun empty() = Empty
    }
}