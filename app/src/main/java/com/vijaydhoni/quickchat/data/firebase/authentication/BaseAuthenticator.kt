package com.vijaydhoni.quickchat.data.firebase.authentication

import android.app.Activity
import com.vijaydhoni.quickchat.data.models.User
import com.vijaydhoni.quickchat.util.Resource
import kotlinx.coroutines.flow.Flow

interface BaseAuthenticator {


    fun createUserWithPhone(
        phone: String,
        activity: Activity,
        isResendOtp: Boolean
    ): Flow<Resource<String>>

    fun sigInWithCredential(otp: String): Flow<Resource<String>>

    fun getCurrentUserId(): Resource<String?>

    suspend fun getCurrentUserDetail(): Resource<User?>

    suspend fun setUserDetails(user: User): Resource<String>

    fun logoutUser()

    suspend fun saveUserProfileImg(imageByteArray: ByteArray): Resource<String>

    suspend fun userActiveOrLastSeen(isActive: Boolean): Resource<String>

}