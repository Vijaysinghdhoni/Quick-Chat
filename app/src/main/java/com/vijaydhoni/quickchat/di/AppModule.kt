package com.vijaydhoni.quickchat.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.vijaydhoni.quickchat.data.firebase.BaseAuthenticator
import com.vijaydhoni.quickchat.data.firebase.FireBaseAuthenticatorImpl
import com.vijaydhoni.quickchat.data.repositorys.repository.AuthRepository
import com.vijaydhoni.quickchat.data.repositorys.repositoyimpl.AuthRepositoryimpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Singleton
    @Provides
    fun providesFirebaseFireStore() = Firebase.firestore

    @Singleton
    @Provides
    fun providesFirebaseAuth() = FirebaseAuth.getInstance()

    @Singleton
    @Provides
    fun providesBaseAuthenticator(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): BaseAuthenticator {
        return FireBaseAuthenticatorImpl(firebaseAuth, firestore)
    }

    @Singleton
    @Provides
    fun providesAuthRepository(baseAuthenticator: BaseAuthenticator): AuthRepository {
        return AuthRepositoryimpl(baseAuthenticator)
    }

}