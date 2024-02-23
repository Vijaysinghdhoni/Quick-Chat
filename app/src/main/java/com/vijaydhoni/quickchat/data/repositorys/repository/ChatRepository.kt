package com.vijaydhoni.quickchat.data.repositorys.repository

import com.vijaydhoni.quickchat.data.models.*
import com.vijaydhoni.quickchat.util.Resource
import kotlinx.coroutines.flow.Flow

interface ChatRepository {

    fun getSearchedUser(query: String): Flow<Resource<List<User>>>

    fun getCurrentUserId(): String?

    suspend fun getChatRoom(chatRoomId: String): Resource<ChatRoom?>

    suspend fun setChatRoom(chatRoom: ChatRoom)

    suspend fun sendMessage(message: Message, chatRoomId: String): Resource<String>


    suspend fun getAllMessages(chatRoomIds: String): Flow<Resource<List<Message>>>

     fun getRecentChats(userId: String): Flow<Resource<List<ChatRoom>>>

    fun getUserByUserId(userIds: List<String?>): Flow<User?>

    fun getUserUnseenMssg(chatRoomIds: String): Flow<Int>

    suspend fun userActiveOrLastSeen(isActive: Boolean): Resource<String>

    suspend fun getUserMessagingToken()

    suspend fun setUserMessagingToken(token: String)

    suspend fun sendMessageToServer(sendMessage: SendMessage)

    suspend fun saveUserStory(storyImageId: String, userStoryImage: String): Resource<String>

    suspend fun saveUserStoryImageInStorage(imageByteArray: ByteArray): Resource<Pair<String, String>>

    suspend fun getUsersStories(): Flow<Resource<List<UserStory>>>

    suspend fun updateStorySeenField(story: Story)

    fun getCurrentUserStory(): Flow<Resource<UserStory?>>

    fun deleteCurrentUserStory(story: Story): Flow<Resource<String>>

    suspend fun deleteUserStoryFrmStorage(story: Story): Resource<Unit>

    fun getAllUsers(): Flow<Resource<List<User>>>

    fun getAllUnseenChatRooms(): Flow<Resource<List<ChatRoom>>>
    
//    suspend fun saveUserProfileImg(imageByteArray: ByteArray): Resource<String>
//
//    suspend fun saveUserInfo(user: User, shouldRetriveOldImg: Boolean): Resource<User>

}