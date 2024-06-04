package com.vijaydhoni.quickchat.data.repo.repositoyImpl

import com.vijaydhoni.quickchat.data.api.FcmApiService
import com.vijaydhoni.quickchat.data.firebase.chat.BaseChatRepo
import com.vijaydhoni.quickchat.data.models.*
import com.vijaydhoni.quickchat.data.repo.ChatRepository
import com.vijaydhoni.quickchat.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val baseChatRepo: BaseChatRepo,
    private val fcmApiService: FcmApiService
) :
    ChatRepository {

    override fun getSearchedUser(query: String): Flow<Resource<List<User>>> {
        return baseChatRepo.getSearchedUser(query)
    }

    override fun getCurrentUserId(): String? {
        return baseChatRepo.getCurrentUserId()
    }

    override suspend fun getChatRoom(chatRoomId: String): Resource<ChatRoom?> {
        return baseChatRepo.getChatRoom(chatRoomId)
    }

    override suspend fun setChatRoom(chatRoom: ChatRoom) {
        baseChatRepo.setChatRoom(chatRoom)
    }

    override suspend fun sendMessage(message: Message, chatRoomId: String): Resource<String> {
        return baseChatRepo.sendMessage(message, chatRoomId)
    }

    override suspend fun getAllMessages(chatRoomIds: String): Flow<Resource<List<Message>>> {
        return baseChatRepo.getAllMessages(chatRoomIds)
    }

    override fun getRecentChats(userId: String): Flow<Resource<List<ChatRoom>>> = flow {
        emit(Resource.Loading())
        emit(baseChatRepo.getRecentChats(userId))
    }

    override fun getUserByUserId(userIds: List<String?>): Flow<User?> {
        return baseChatRepo.getUserByUserId(userIds)
    }

    override fun getUserUnseenMssg(chatRoomIds: String): Flow<Int> {
        return baseChatRepo.getUserUnseenMsg(chatRoomIds)
    }

    override suspend fun userActiveOrLastSeen(isActive: Boolean): Resource<String> {
        return baseChatRepo.userActiveOrLastSeen(isActive)
    }

    override suspend fun getUserMessagingToken() {
        baseChatRepo.getUserMessagingToken()
    }

    override suspend fun setUserMessagingToken(token: String) {
        baseChatRepo.setUserMessagingToken(token)
    }

    override suspend fun sendMessageToServer(sendMessage: SendMessage) {
        fcmApiService.sendMessage(sendMessage)
    }

    override suspend fun saveUserStory(
        storyImageId: String,
        userStoryImage: String
    ): Resource<String> {
        return baseChatRepo.saveUserStory(storyImageId, userStoryImage)
    }

    override suspend fun saveUserStoryImageInStorage(imageByteArray: ByteArray): Resource<Pair<String, String>> {
        return baseChatRepo.saveUserStoryImageInStorage(imageByteArray)
    }

    override suspend fun getUsersStories(): Flow<Resource<List<UserStory>>> = flow {
        emit(Resource.Loading())
        emit(baseChatRepo.getUsersStories())
    }

    override suspend fun updateStorySeenField(story: Story) {
        return baseChatRepo.updateStorySeenField(story)
    }

    override fun getCurrentUserStory(): Flow<Resource<UserStory?>> = flow {
        emit(Resource.Loading())
        val response = baseChatRepo.getCurrentUserStories()
        emit(response)
    }

    override fun deleteCurrentUserStory(story: Story): Flow<Resource<String>> = flow {
        emit(Resource.Loading())
        val response = baseChatRepo.deleteCurrentUserStory(story)
        emit(response)
    }

    override suspend fun deleteUserStoryFrmStorage(story: Story): Resource<Unit> {
        return baseChatRepo.deleteStoryImgFrmStorage(story)
    }

    override fun getAllUsers(): Flow<Resource<List<User>>> = flow {
        emit(Resource.Loading())
        val response = baseChatRepo.getAllUsers()
        emit(response)
    }

    override fun getAllUnseenChatRooms(): Flow<Resource<List<ChatRoom>>> = flow {
        emit(Resource.Loading())
        val response = baseChatRepo.getAllUnseenChatRooms()
        emit(response)
    }


    override suspend fun getCurrentUser(): Resource<User?> {
        return baseChatRepo.getCurrentUser()
    }


}