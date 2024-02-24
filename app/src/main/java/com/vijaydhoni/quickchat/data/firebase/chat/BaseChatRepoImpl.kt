package com.vijaydhoni.quickchat.data.firebase.chat

import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageReference
import com.vijaydhoni.quickchat.data.models.*
import com.vijaydhoni.quickchat.util.Constants.Companion.CHATROOMCOLLECTION
import com.vijaydhoni.quickchat.util.Constants.Companion.CHATSCOLLECTION
import com.vijaydhoni.quickchat.util.Constants.Companion.USERCOLLECTION
import com.vijaydhoni.quickchat.util.Constants.Companion.USERSTORYCOLLECTION
import com.vijaydhoni.quickchat.util.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject

class BaseChatRepoImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseMessaging: FirebaseMessaging,
    private val storage: StorageReference
) : BaseChatRepo {

    //will not collect last emitted value
    override fun getSearchedUser(query: String): Flow<Resource<List<User>>> = callbackFlow {
        try {
            trySend(Resource.Loading())
            firestore.collection(USERCOLLECTION).whereGreaterThanOrEqualTo("userName", query)
                .whereLessThanOrEqualTo("userName", query.trim() + "\uf8ff").get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val users = it.result.toObjects(User::class.java)
                        trySend(Resource.Success(users))
                    }
                }
                .addOnFailureListener {
                    trySend(Resource.Error(it.message ?: "Unknown Error"))
                }
        } catch (ex: Exception) {
            trySend(Resource.Error(ex.message ?: "Unknown Error"))
        } catch (ex: FirebaseFirestoreException) {
            trySend(Resource.Error(ex.message ?: "Unknown Error"))
        }
        awaitClose {
            close()
        }
    }

    override fun getCurrentUserId(): String? {
        return firebaseAuth.uid
    }


    override suspend fun getChatRoom(chatRoomId: String): Resource<ChatRoom?> {
        return try {
            val snapshot =
                firestore.collection(CHATROOMCOLLECTION).document(chatRoomId).get().await()
            val chatRoom = snapshot.toObject(ChatRoom::class.java)
            Resource.Success(chatRoom)
        } catch (ex: Exception) {
            Resource.Error(ex.message ?: "Unknown Error")
        } catch (ex: FirebaseFirestoreException) {
            Resource.Error(ex.message ?: "Unknown Error")
        }
    }

    override suspend fun setChatRoom(chatRoom: ChatRoom) {
        firestore.collection(CHATROOMCOLLECTION).document(chatRoom.chatRoomId!!).set(chatRoom)
    }

    override suspend fun sendMessage(message: Message, chatRoomId: String): Resource<String> {
        return try {
            firestore.collection(CHATROOMCOLLECTION).document(chatRoomId).collection(
                CHATSCOLLECTION
            ).add(message).await()
            Resource.Success("Message Send Sucessfuly")
        } catch (ex: Exception) {
            Resource.Error(ex.message ?: "Unknown Error")
        } catch (ex: FirebaseFirestoreException) {
            Resource.Error(ex.message ?: "Unknown Error")
        }

    }

    override fun getAllMessages(chatRoomIds: String): Flow<Resource<List<Message>>> =
        callbackFlow {
            val userId = firebaseAuth.uid
            try {
                val chatsCollection =
                    firestore.collection(CHATROOMCOLLECTION).document(chatRoomIds).collection(
                        CHATSCOLLECTION
                    )
                val listner = chatsCollection.orderBy("timeStamp", Query.Direction.DESCENDING)
                    .addSnapshotListener { value, error ->
                        if (error != null) {
                            trySend(Resource.Error(error.message ?: "Unknown Error"))
                        }

                        if (value != null && !value.isEmpty) {
                            val messages = mutableListOf<Message>()
                            for (doc in value.documents) {
                                val message = doc.toObject(Message::class.java)
                                message?.let {
                                    if (it.senderId != userId && !it.seen) {
                                        chatsCollection.document(doc.id).update("seen", true)
                                        firestore.collection(CHATROOMCOLLECTION)
                                            .document(chatRoomIds)
                                            .update("lastMssgSeen", true)
                                    }
                                    messages.add(it)
                                }
                            }
                            trySend(Resource.Success(messages))
                        }
                    }

                awaitClose {
                    listner.remove()
                    close()
                }
            } catch (ex: Exception) {
                trySend(Resource.Error(ex.message ?: "Unknown Error"))
            } catch (ex: FirebaseFirestoreException) {
                trySend(Resource.Error(ex.message ?: "Unknown Error"))
            }


        }

    // impliment this with coroutines
    override fun getUserUnseenMssg(chatRoomIds: String): Flow<Int> = callbackFlow {

        firestore.collection(CHATROOMCOLLECTION).document(chatRoomIds).collection(
            CHATSCOLLECTION
        ).whereEqualTo("seen", false)
            .get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val unseenMsggNum = it.result.size()
                    trySend(unseenMsggNum)
                }
            }
        awaitClose {
            close()
        }
    }


    override suspend fun getRecentChats(userId: String): Resource<List<ChatRoom>> {
        return try {
            val snapShot =
                firestore.collection(CHATROOMCOLLECTION).whereArrayContains("usersIds", userId)
                    .orderBy("lastMssgTimeStamp", Query.Direction.DESCENDING).get().await()
            val recentsChats = snapShot.toObjects(ChatRoom::class.java)
            Resource.Success(recentsChats)
        } catch (ex: Exception) {
            Resource.Error(ex.message ?: "Unknown Error")
        } catch (ex: FirebaseFirestoreException) {
            Resource.Error(ex.message ?: "Unknown Error")
        }
    }

    override fun getUserByUserId(userIds: List<String?>): Flow<User?> = callbackFlow {

        val snapShot = if (userIds[0].equals(firebaseAuth.uid)) {
            firestore.collection(USERCOLLECTION).document(userIds[1]!!)
        } else {
            firestore.collection(USERCOLLECTION).document(userIds[0]!!)
        }
        snapShot.get().addOnCompleteListener {
            if (it.isSuccessful) {
                val user = it.result.toObject(User::class.java)
                trySend(user)
            }
        }
        awaitClose {
            close()
        }
    }

    override suspend fun userActiveOrLastSeen(isActive: Boolean): Resource<String> {
        return try {
            val userId = firebaseAuth.uid
            if (userId != null) {
                val userRef = firestore.collection(USERCOLLECTION).document(userId)
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

    override suspend fun getUserMessagingToken() {
        try {
            val token = firebaseMessaging.token.await()
            if (token.isNotEmpty()) {
                firestore.collection(USERCOLLECTION).document(firebaseAuth.uid!!)
                    .update("fcmToken", token)
                    .await()
                Log.d("FCM", " token updated sucesfully")
            } else {
                Log.d("FCM", " token is empty in repo")
            }
        } catch (ex: FirebaseFirestoreException) {
            Log.d("FCM", ex.localizedMessage ?: "Unknown error")
        } catch (ex: FirebaseException) {
            Log.d("FCM", ex.localizedMessage ?: "Unknown error")
        } catch (ex: Exception) {
            Log.d("FCM", ex.localizedMessage ?: "Unknown error")
        }
    }


    override suspend fun setUserMessagingToken(token: String) {
        if (!firebaseAuth.uid.isNullOrEmpty()) {
            firestore.collection(USERCOLLECTION).document(firebaseAuth.uid!!)
                .update("fcmToken", token)
                .await()
        }
    }

    override suspend fun saveUserStory(
        storyImageId: String,
        userStoryImage: String
    ): Resource<String> {
        return try {
            val currentUserUid = firebaseAuth.uid
            val userCollection = firestore.collection(USERCOLLECTION).document(currentUserUid!!)
            val currentUser = userCollection.get().await().toObject(User::class.java)
            val userStoryCollection =
                firestore.collection(USERSTORYCOLLECTION).document(currentUserUid)
            val newStory = Story(
                storyImageUrl = userStoryImage,
                storyID = storyImageId,
                storyCreatedTimestamp = Timestamp.now(),
                userID = currentUserUid
            )

            firestore.runTransaction { transaction ->
                val userStoryDocument = transaction.get(userStoryCollection)
                val userStory = userStoryDocument.toObject(UserStory::class.java)

                if (userStory == null) {
                    val newUserStory = UserStory(
                        user = currentUser,
                        lastUpdatedTime = Timestamp.now(),
                        stories = listOf(newStory)
                    )
                    transaction.set(userStoryCollection, newUserStory)
                } else {
                    val updatedStories = userStory.stories.toMutableList()
                    updatedStories.add(newStory)

                    transaction.update(userStoryCollection, "lastUpdatedTime", Timestamp.now())
                    transaction.update(userStoryCollection, "stories", updatedStories)
                    transaction.update(userStoryCollection, "user", currentUser)
                }
            }.await()

            Resource.Success("Story Added Successfully")
        } catch (ex: FirebaseFirestoreException) {
            Resource.Error(ex.localizedMessage ?: "Firestore error")
        } catch (ex: Exception) {
            Resource.Error(ex.localizedMessage ?: "Unknown error")
        }
    }

    override suspend fun saveUserStoryImageInStorage(imageByteArray: ByteArray): Resource<Pair<String, String>> {
        return try {
            val imageUUID = UUID.randomUUID().toString()
            val imageDirectory =
                storage.child("userStatusImages/${firebaseAuth.uid}/${imageUUID}")
            val result = imageDirectory.putBytes(imageByteArray).await()
            val imageUrl = result.storage.downloadUrl.await().toString()
            Resource.Success(Pair(imageUUID, imageUrl))
        } catch (ex: StorageException) {
            Resource.Error(ex.localizedMessage ?: "Unknown Error")
        } catch (ex: Exception) {
            Resource.Error(ex.localizedMessage ?: "Unknown Error")
        }
    }


    override suspend fun getUsersStories(): Resource<List<UserStory>> {
        return try {
            val storyCollection =
                firestore.collection(USERSTORYCOLLECTION).get().await()
            val userStorys = storyCollection.toObjects(UserStory::class.java)
            Resource.Success(userStorys)
        } catch (ex: FirebaseFirestoreException) {
            Resource.Error(ex.localizedMessage ?: "Firestore Error")
        } catch (ex: Exception) {
            Resource.Error(ex.localizedMessage ?: "Unknown Error")
        }
    }


    override suspend fun getCurrentUserStories(): Resource<UserStory?> {
        return try {
            val response =
                firestore.collection(USERSTORYCOLLECTION).document(firebaseAuth.uid!!).get().await()
            val currentUserStory = response.toObject(UserStory::class.java)
            Resource.Success(currentUserStory)
        } catch (ex: FirebaseFirestoreException) {
            Resource.Error(ex.localizedMessage ?: "Firestore Error")
        } catch (ex: Exception) {
            Resource.Error(ex.localizedMessage ?: "Firestore Error")
        }
    }


    override suspend fun updateStorySeenField(story: Story) {
        try {
            val userStoryRef =
                firestore.collection(USERSTORYCOLLECTION).document(story.userID!!)
            val response =
                userStoryRef.get().await()
            val userStory = response.toObject(UserStory::class.java)
            val storyIndex = userStory?.stories?.indexOfFirst { it.storyID == story.storyID }
            if (storyIndex != -1) {
                val currentUserId = firebaseAuth.uid!!
                if (currentUserId !in userStory?.stories?.get(storyIndex!!)?.storySeenBy.orEmpty()) {
                    //orEmpty is same as ?: emptyList() , if the currentUser id not present in storySeenBy list then it will add it
                    // and if the storySeenBy list is not there then it will create it with emptyList and then add currentUserId
                    // and if current User id  is not present there then it will add it with the exiting list by + currentUserId
                    userStory?.stories?.get(storyIndex!!)?.storySeenBy =
                        userStory?.stories?.get(storyIndex!!)?.storySeenBy.orEmpty() + currentUserId
                    userStoryRef.set(userStory!!)
                }
            }
        } catch (ex: Exception) {
            Log.d("story", ex.localizedMessage ?: "unkonwn error")
        } catch (ex: FirebaseFirestoreException) {
            Log.d("story", ex.localizedMessage ?: "unkonwn error")
        }
    }

    override suspend fun deleteCurrentUserStory(story: Story): Resource<String> {
        return try {
            val userRef =
                firestore.collection(USERSTORYCOLLECTION).document(firebaseAuth.uid!!)
            val ref = userRef.get().await()
            var userStory = ref.toObject(UserStory::class.java)
            val storyIndex = userStory?.stories?.indexOfFirst { it.storyID == story.storyID }

            if (storyIndex != -1) {
                val updatedUserStory = userStory?.stories?.toMutableList()
                updatedUserStory?.removeAt(storyIndex!!)
                userStory = userStory?.copy(
                    stories = updatedUserStory!!.toList()
                )
                userRef.set(userStory!!).await()
                Resource.Success("Delete Success")
            } else {
                Resource.Error("Unexcepted Error Try Again")
            }


        } catch (ex: FirebaseFirestoreException) {
            Resource.Error(ex.localizedMessage ?: "Unknown error Try Again")
        } catch (ex: Exception) {
            Resource.Error(ex.localizedMessage ?: "Unknown error Try Again")
        }
    }

    override suspend fun deleteStoryImgfrmStorage(story: Story): Resource<Unit> {
        return try {
            val imageDirectory =
                storage.child("userStatusImages/${firebaseAuth.uid}/${story.storyID}")
            imageDirectory.delete().await()
            Resource.Success(Unit)
        } catch (ex: FirebaseException) {
            Resource.Error(ex.localizedMessage ?: "Unknown error Try Again")
        } catch (ex: Exception) {
            Resource.Error(ex.localizedMessage ?: "Unknown error Try Again")
        }
    }

    override suspend fun getAllusers(): Resource<List<User>> {
        return try {
            val usersRef = firestore.collection(USERCOLLECTION).get().await()
            val users = usersRef.toObjects(User::class.java)
            Resource.Success(users)
        } catch (ex: FirebaseFirestoreException) {
            Resource.Error(ex.localizedMessage ?: "Unknown error Try Again")
        } catch (ex: Exception) {
            Resource.Error(ex.localizedMessage ?: "Unknown error Try Again")
        }
    }

    override suspend fun getAllUnseenChatRooms(): Resource<List<ChatRoom>> {
        return try {
            val query = firestore.collection(CHATROOMCOLLECTION)
                .whereArrayContains("usersIds", firebaseAuth.uid!!)
                .whereEqualTo("lastMssgSeen", false)
                .whereNotEqualTo("lastMssgSenderId", firebaseAuth.uid!!).get().await()
            val chatRooms = query.toObjects(ChatRoom::class.java)
            Resource.Success(chatRooms)
        } catch (ex: FirebaseFirestoreException) {
            Resource.Error(ex.localizedMessage ?: "Unknown error Try Again")
        } catch (ex: Exception) {
            Resource.Error(ex.localizedMessage ?: "Unknown error Try Again")
        }
    }

//    override suspend fun saveUserProfileImg(imageByteArray: ByteArray): Resource<String> {
//        return try {
//            val imageDirectory =
//                storage.child("profileImages/${firebaseAuth.uid}/${UUID.randomUUID()}")
//            val result = imageDirectory.putBytes(imageByteArray).await()
//            val imageUrl = result.storage.downloadUrl.await().toString()
//            Resource.Success(imageUrl)
//        } catch (ex: Exception) {
//            Resource.Error(ex.message ?: "Unknown Error")
//        } catch (ex: StorageException) {
//            Resource.Error(ex.message ?: "Unknown Error")
//        }
//    }
//
//
//    override suspend fun saveUserInfo(user: User, shouldRetriveOldImg: Boolean): Resource<User> {
//        return try {
//            firestore.runTransaction { transaction ->
//                val docRef = firestore.collection(USERCOLLECTION).document(firebaseAuth.uid!!)
//                if (shouldRetriveOldImg) {
//                    val currentUser = transaction.get(docRef).toObject(User::class.java)
//                    val newUser = user.copy(imagePath = currentUser?.imagePath ?: "")
//                    transaction.set(docRef, newUser)
//                } else {
//                    transaction.set(docRef, user)
//                }
//            }.await()
//            Resource.Success(user)
//        } catch (ex: Exception) {
//            Resource.Error(ex.message ?: "Unknown Error")
//        } catch (ex: FirebaseFirestoreException) {
//            Resource.Error(ex.message ?: "Unknown Error")
//        }
//    }
//

}