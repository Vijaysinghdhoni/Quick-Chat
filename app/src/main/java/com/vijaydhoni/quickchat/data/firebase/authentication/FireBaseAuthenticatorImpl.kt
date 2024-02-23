package com.vijaydhoni.quickchat.data.firebase.authentication

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.Timestamp
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageReference
import com.vijaydhoni.quickchat.data.models.User
import com.vijaydhoni.quickchat.util.Constants.Companion.USERCOLLECTION
import com.vijaydhoni.quickchat.util.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class FireBaseAuthenticatorImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFirestore: FirebaseFirestore,
    private val storage: StorageReference
) :
    BaseAuthenticator {

    private lateinit var onVerificationCode: String
    private lateinit var resecendingToken: PhoneAuthProvider.ForceResendingToken

    override fun createUserWithPhone(
        phone: String,
        activity: Activity,
        isResendOtp: Boolean
    ): Flow<Resource<String>> =
        callbackFlow {
            try {
                trySend(Resource.Loading())

                val onVerificationCallback =
                    object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        override fun onVerificationCompleted(p0: PhoneAuthCredential) {

                        }

                        override fun onVerificationFailed(p0: FirebaseException) {
                            trySend(Resource.Error(p0.message!!))
                        }

                        override fun onCodeSent(
                            verificationCode: String,
                            p1: PhoneAuthProvider.ForceResendingToken
                        ) {
                            super.onCodeSent(verificationCode, p1)
                            trySend(Resource.Success("OTP Sent Successfully"))
                            onVerificationCode = verificationCode
                            resecendingToken = p1
                        }
                    }

                val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                    .setPhoneNumber(phone)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(activity)
                    .setCallbacks(onVerificationCallback)



                if (isResendOtp) {
                    PhoneAuthProvider.verifyPhoneNumber(
                        options.setForceResendingToken(
                            resecendingToken
                        ).build()
                    )
                } else {
                    PhoneAuthProvider.verifyPhoneNumber(options.build())
                }

            } catch (ex: Exception) {
                trySend(Resource.Error(ex.message!!))
            } catch (ex: FirebaseAuthException) {
                trySend(Resource.Error(ex.message!!))
            }

            awaitClose {
                close()
            }
        }

    override fun sigInWithCredential(otp: String): Flow<Resource<String>> = callbackFlow {
        try {
            trySend(Resource.Loading())
            val credential = PhoneAuthProvider.getCredential(onVerificationCode, otp)
            firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        trySend(Resource.Success("OTP verified"))
                    }
                }.addOnFailureListener {
                    trySend(Resource.Error(it.message!!))
                }

        } catch (ex: Exception) {
            trySend(Resource.Error(ex.message!!))
        }
        awaitClose {
            close()
        }
    }

    override fun getCurrentUserId(): Resource<String?> {
        return try {
            val userId = firebaseAuth.currentUser?.uid
            Resource.Success(userId)
        } catch (ex: Exception) {
            Resource.Error(ex.message!!)
        } catch (ex: FirebaseAuthException) {
            Resource.Error(ex.message!!)
        }
    }

    override suspend fun getCurrentUserDetail(): Resource<User?> {
        return try {
            val snapshot =
                firebaseFirestore.collection("User").document(firebaseAuth.uid!!).get().await()
            val user = snapshot.toObject(User::class.java)
            Resource.Success(user)
        } catch (ex: Exception) {
            Resource.Error(ex.message ?: "Unknown error")
        } catch (ex: FirebaseFirestoreException) {
            Resource.Error(ex.message ?: "Unknown error")
        }
    }


    override suspend fun setUserDetails(user: User): Resource<String> {
        return try {
            firebaseFirestore.collection(USERCOLLECTION).document(firebaseAuth.uid!!).set(user)
                .await()
            Resource.Success("Details updated")

        } catch (ex: Exception) {
            Resource.Error(ex.message ?: "Unknown error")
        } catch (ex: FirebaseFirestoreException) {
            Resource.Error(ex.message ?: "Unknown error")
        }
    }

    override fun logoutUser() {
        firebaseAuth.signOut()
    }

    override suspend fun saveUserProfileImg(imageByteArray: ByteArray): Resource<String> {
        return try {
            val imageDirectory =
                storage.child("profileImages/${firebaseAuth.uid}")
            val previousImages = imageDirectory.listAll().await()

            for (item in previousImages.items) {
                item.delete().await()
            }

            val newImageReference = imageDirectory.child(UUID.randomUUID().toString())
            val result = newImageReference.putBytes(imageByteArray).await()
            val imageUrl = result.storage.downloadUrl.await().toString()
            Resource.Success(imageUrl)
        } catch (ex: Exception) {
            Resource.Error(ex.message ?: "Unknown Error")
        } catch (ex: StorageException) {
            Resource.Error(ex.message ?: "Unknown Error")
        }
    }

    override suspend fun userActiveOrLastSeen(isActive: Boolean): Resource<String> {
        return try {
            val userId = firebaseAuth.currentUser?.uid
            if (userId != null) {
                val userRef = firebaseFirestore.collection(USERCOLLECTION).document(userId)
                val currentTime = Timestamp.now()
                userRef.update(
                    "isUserActive", isActive,
                    "lastSeenTime", currentTime
                ).await()
                Resource.Success("Online")
            } else {
                Resource.Success("No User Found")
            }
        } catch (ex: Exception) {
            Resource.Error(ex.message ?: "Unknown Error")
        } catch (ex: FirebaseFirestoreException) {
            Resource.Error(ex.message ?: "Unknown Error")
        }
    }

}