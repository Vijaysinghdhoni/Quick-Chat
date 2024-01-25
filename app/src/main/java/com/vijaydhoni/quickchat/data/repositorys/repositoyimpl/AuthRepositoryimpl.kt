package com.vijaydhoni.quickchat.data.repositorys.repositoyimpl

import android.app.Activity
import com.google.firebase.firestore.DocumentReference
import com.vijaydhoni.quickchat.data.firebase.BaseAuthenticator
import com.vijaydhoni.quickchat.data.models.User
import com.vijaydhoni.quickchat.data.repositorys.repository.AuthRepository
import com.vijaydhoni.quickchat.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AuthRepositoryimpl @Inject constructor(private val baseAuthRepository: BaseAuthenticator) :
    AuthRepository {
    override fun createUserWithPhone(
        phone: String,
        activity: Activity,
        isResendOtp: Boolean
    ): Flow<Resource<String>> {
        return baseAuthRepository.createUserWithPhone(phone, activity, isResendOtp)
    }

    override fun sigInWithCredential(otp: String): Flow<Resource<String>> {
        return baseAuthRepository.sigInWithCredential(otp)
    }

    override suspend fun getCurrentUserDetail(): Resource<User?> {
        return baseAuthRepository.getCurrentUserDetail()
    }

    override fun getCurrentUserId(): Resource<String?> {
        return baseAuthRepository.getCurrentUserId()
    }

    override  suspend fun setUserDetails(user: User): Resource<String> {
        return baseAuthRepository.setUserDetails(user)
    }

}