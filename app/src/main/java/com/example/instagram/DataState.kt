package com.example.instagram

data class DataState<out T>(
    var status: Status,
    val data: T?,
    val message: String?
) {
    companion object {
        fun <T> success(data: T): DataState<T> {
            return DataState(Status.SUCCESS, data, null)
        }

        fun <T> error(data: T?, message: String?): DataState<T> {
            return DataState(Status.ERROR, data, message)
        }

        fun <T> loading(): DataState<T> {
            return DataState(Status.LOADING, null, null)
        }

        fun <T> idle(data: T?): DataState<T>{
            return DataState(Status.IDLE, null, null)
        }

    }
}

enum class Status {
    SUCCESS,
    ERROR,
    LOADING,
    IDLE
}