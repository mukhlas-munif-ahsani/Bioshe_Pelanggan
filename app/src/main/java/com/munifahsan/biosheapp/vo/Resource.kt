package com.munifahsan.biosheapp.vo

data class Resource<out T>(
    val status: Status,
    val data: T?,
    val message: String?,
    val progress: Double?
) {
    companion object {
        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data, null, null)
        }

        fun <T> error(msg: String, data: T?): Resource<T> {
            return Resource(Status.ERROR, data, msg, null)
        }

        fun <T> loading(progress: Double?, data: T?): Resource<T> {
            return Resource(Status.LOADING, data, null, progress)
        }
    }
}
