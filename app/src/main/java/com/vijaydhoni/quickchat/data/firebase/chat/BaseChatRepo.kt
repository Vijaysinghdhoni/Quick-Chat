package com.vijaydhoni.quickchat.data.firebase.chat

import com.vijaydhoni.quickchat.data.models.*
import com.vijaydhoni.quickchat.util.Resource
import kotlinx.coroutines.flow.Flow

interface BaseChatRepo {

    fun getSearchedUser(query: String): Flow<Resource<List<User>>>

    fun getCurrentUserId(): String?

    suspend fun getChatRoom(chatRoomId: String): Resource<ChatRoom?>

    suspend fun setChatRoom(chatRoom: ChatRoom)

    suspend fun sendMessage(message: Message, chatRoomId: String): Resource<String>

    fun getAllMessages(chatRoomIds: String): Flow<Resource<List<Message>>>

    suspend fun getRecentChats(userId: String): Resource<List<ChatRoom>>

    fun getUserByUserId(userIds: List<String?>): Flow<User?>

    fun getUserUnseenMssg(chatRoomIds: String): Flow<Int>

    suspend fun userActiveOrLastSeen(isActive: Boolean): Resource<String>

    suspend fun getUserMessagingToken()

    suspend fun setUserMessagingToken(token: String)

    suspend fun saveUserStory(storyImageId: String, userStoryImage: String): Resource<String>

    suspend fun saveUserStoryImageInStorage(imageByteArray: ByteArray): Resource<Pair<String, String>>

    suspend fun getUsersStories(): Resource<List<UserStory>>

    suspend fun updateStorySeenField(story: Story)

    suspend fun getCurrentUserStories(): Resource<UserStory?>

    suspend fun deleteCurrentUserStory(story: Story): Resource<String>

    suspend fun deleteStoryImgfrmStorage(story: Story): Resource<Unit>

    suspend fun getAllusers(): Resource<List<User>>

    suspend fun getAllUnseenChatRooms(): Resource<List<ChatRoom>>

//    suspend fun saveUserProfileImg(imageByteArray: ByteArray): Resource<String>
//
//    suspend fun saveUserInfo(user: User, shouldRetriveOldImg: Boolean): Resource<User>


}