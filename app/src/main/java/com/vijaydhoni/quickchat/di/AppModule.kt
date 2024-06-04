package com.vijaydhoni.quickchat.di

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.vijaydhoni.quickchat.data.api.FcmApiService
import com.vijaydhoni.quickchat.data.firebase.authentication.BaseAuthenticator
import com.vijaydhoni.quickchat.data.firebase.authentication.FireBaseAuthenticatorImpl
import com.vijaydhoni.quickchat.data.firebase.chat.BaseChatRepo
import com.vijaydhoni.quickchat.data.firebase.chat.BaseChatRepoImpl
import com.vijaydhoni.quickchat.data.repo.AuthRepository
import com.vijaydhoni.quickchat.data.repo.ChatRepository
import com.vijaydhoni.quickchat.data.repo.repositoyImpl.AuthRepositoryImpl
import com.vijaydhoni.quickchat.data.repo.repositoyImpl.ChatRepositoryImpl
import com.vijaydhoni.quickchat.util.Constants.Companion.FCM_BASE_URL
import com.vijaydhoni.quickchat.util.Constants.Companion.INTRO_SP
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
    fun providesFirebaseStorage(): StorageReference = FirebaseStorage.getInstance().reference


    //auth
    @Singleton
    @Provides
    fun providesBaseAuthenticator(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
        storageReference: StorageReference
    ): BaseAuthenticator {
        return FireBaseAuthenticatorImpl(firebaseAuth, firestore, storageReference)
    }

    @Singleton
    @Provides
    fun providesAuthRepository(baseAuthenticator: BaseAuthenticator): AuthRepository {
        return AuthRepositoryImpl(baseAuthenticator)
    }

    //chat


    @Singleton
    @Provides
    fun providesBaseChatRepo(
        firebaseFireStore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth,
        firebaseMessaging: FirebaseMessaging,
        storageReference: StorageReference
    ): BaseChatRepo {
        return BaseChatRepoImpl(
            firebaseFireStore,
            firebaseAuth,
            firebaseMessaging,
            storageReference
        )
    }

    @Singleton
    @Provides
    fun providesChatRepository(
        baseChatRepo: BaseChatRepo,
        fcmApiService: FcmApiService
    ): ChatRepository {
        return ChatRepositoryImpl(baseChatRepo, fcmApiService)
    }

    @Singleton
    @Provides
    fun providesFireBaseMessaging(): FirebaseMessaging {
        return FirebaseMessaging.getInstance()
    }

    @Provides
    @Singleton
    fun providesIntroductionSharedPreference(
        app: Application
    ): SharedPreferences = app.getSharedPreferences(INTRO_SP, MODE_PRIVATE)


    @Provides
    @Singleton
    fun providesRetrofit(): Retrofit {
        return Retrofit.Builder().baseUrl(FCM_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()).build()
    }

    @Provides
    @Singleton
    fun providesFcmApiService(retrofit: Retrofit): FcmApiService {
        return retrofit.create(FcmApiService::class.java)
    }

}