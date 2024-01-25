package com.vijaydhoni.quickchat.util

sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {


    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(errorMessage: String, data: T? = null) : Resource<T>(data, errorMessage)
    class Loading<T>(data: T? = null) : Resource<T>(data)
    class Unspecified<T> : Resource<T>()
}