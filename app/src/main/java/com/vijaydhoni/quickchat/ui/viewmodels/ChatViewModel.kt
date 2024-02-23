package com.vijaydhoni.quickchat.ui.viewmodels

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vijaydhoni.quickchat.BaseApplication
import com.vijaydhoni.quickchat.data.models.*
import com.vijaydhoni.quickchat.data.repositorys.repository.ChatRepository
import com.vijaydhoni.quickchat.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    app: Application
) : AndroidViewModel(app) {



    private val _chatRoom: MutableStateFlow<Resource<ChatRoom?>> =
        MutableStateFlow(Resource.Unspecified())
    val chatRoom = _chatRoom.asStateFlow()

    private val _messageSend: MutableStateFlow<Resource<String>> =
        MutableStateFlow(Resource.Unspecified())
    val messageSend = _messageSend.asStateFlow()

    private val _messages: MutableStateFlow<Resource<List<Message>>> =
        MutableStateFlow(Resource.Unspecified())
    val messages = _messages.asStateFlow()

    private val _recentChats: MutableStateFlow<Resource<List<ChatRoom>>> =
        MutableStateFlow(Resource.Unspecified())
    val recentsChats = _recentChats.asStateFlow()

    private val _saveUserStory: MutableSharedFlow<Resource<String>> = MutableSharedFlow()
    val saveUserStory = _saveUserStory.asSharedFlow()

    private val _userStories: MutableStateFlow<Resource<List<UserStory>>> =
        MutableStateFlow(Resource.Unspecified())

    val userStories = _userStories.asStateFlow()

    private val _currentUserStories: MutableStateFlow<Resource<UserStory?>> =
        MutableStateFlow(Resource.Unspecified())
    val currentUserStory = _currentUserStories.asStateFlow()

    private val _deleteUserStory: MutableSharedFlow<Resource<String>> = MutableSharedFlow()
    val deleteUserStory = _deleteUserStory.asSharedFlow()

    private val _allUsers: MutableStateFlow<Resource<List<User>>> =
        MutableStateFlow(Resource.Unspecified())
    val allUsers = _allUsers.asStateFlow()


    private val _getUnseenChatRoooms: MutableStateFlow<Resource<List<ChatRoom>>> =
        MutableStateFlow(Resource.Unspecified())
    val unseenChatRooms = _getUnseenChatRoooms.asStateFlow()

//    private val _user: MutableStateFlow<Resource<User>> = MutableStateFlow(Resource.Unspecified())
//    val user = _user.asStateFlow()

    fun getSearchedUser(query: String) = chatRepository.getSearchedUser(query)

//    fun getsearchedUserfromdb(query: String) {
//        runBlocking {
//            chatRepository.getSearchedUser(query).collectLatest {
//                _searchedUser.value = it
//            }
//        }
//    }

    fun getCurrentUserId() = chatRepository.getCurrentUserId()

    fun getChatRoom(chatRoomId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _chatRoom.value = Resource.Loading()
            val chatRoom = chatRepository.getChatRoom(chatRoomId)
            _chatRoom.value = chatRoom
        }
    }

    fun setChatRoom(chatRoom: ChatRoom) {
        viewModelScope.launch(Dispatchers.IO) {
            chatRepository.setChatRoom(chatRoom)
        }
    }

    fun sendUserMessage(message: Message, chatRoomId: String, otherUser: User) {
        viewModelScope.launch(Dispatchers.IO) {
            _messageSend.value = Resource.Loading()
            val response = chatRepository.sendMessage(message, chatRoomId)
//            if (response is Resource.Success) {
//                try {
//                    val otherUserfcmToken = otherUser.fcmToken
//                    val name = otherUser.userName
//                    val mssgBody = message.message
//                    if (otherUserfcmToken.isNotBlank() && !name.isNullOrBlank()) {
//                        val sendMessageDto = SendMessage(
//                            otherUserfcmToken,
//                            NotificationBody(
//                                title = name,
//                                body = mssgBody
//                            )
//                        )
//                        chatRepository.sendMessageToServer(sendMessageDto)
//                        Log.d("FCM", "notifications send succesfuly")
//                    } else {
//                        _messageSend.emit(Resource.Error("No User Found"))
//                    }
//                } catch (ex: HttpException) {
//                    _messageSend.emit(Resource.Error(ex.localizedMessage ?: "Unknown message"))
//                } catch (ex: IOException) {
//                    _messageSend.emit(Resource.Error(ex.localizedMessage ?: "Unknown message"))
//                }
//            }
            _messageSend.value = response
        }
    }

    fun getAllUsersMessages(chatRoomId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _messages.value = Resource.Loading()
            chatRepository.getAllMessages(chatRoomId).collect {
                _messages.value = it
            }
        }
    }


    fun getAllRecentChats(currentUserId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _recentChats.value = Resource.Loading()
            chatRepository.getRecentChats(
                userId =
                currentUserId
            ).collect {
                _recentChats.value = it
            }
        }
    }

    fun getUserById(userIds: List<String?>) =
        chatRepository.getUserByUserId(userIds)

    fun getUserUnseenMssgNumbers(chatRoomIds: String) =
        chatRepository.getUserUnseenMssg(chatRoomIds)


    fun setUserActiveOrNot(isActive: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {

            when (val response = chatRepository.userActiveOrLastSeen(isActive)) {
                is Resource.Success -> {
                    Log.d("active", response.data.toString())
                }
                is Resource.Error -> {
                    Log.d("active", response.message.toString())
                }
                else -> {}
            }
        }
    }

    fun getAndSetUserMessagingToken() {
        viewModelScope.launch(Dispatchers.IO) {
            chatRepository.getUserMessagingToken()
        }
    }

    fun saveUserStatus(storyImage: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _saveUserStory.emit(Resource.Loading())
            val imageBitmap = MediaStore.Images.Media.getBitmap(
                getApplication<BaseApplication>().contentResolver,
                storyImage
            )
            val byteArrayOutputStream = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 96, byteArrayOutputStream)
            val imageByteArray = byteArrayOutputStream.toByteArray()
            when (val imageUrl = chatRepository.saveUserStoryImageInStorage(imageByteArray)) {

                is Resource.Success -> {
                    val response = chatRepository.saveUserStory(
                        storyImageId = imageUrl.data?.first!!,
                        userStoryImage = imageUrl.data.second
                    )
                    _saveUserStory.emit(response)
                }

                is Resource.Error -> {
                    _saveUserStory.emit(
                        Resource.Error(
                            imageUrl.message ?: "Cannot share story"
                        )
                    )
                }

                else -> {}


            }

        }
    }

    fun getUserStories() {
        viewModelScope.launch(Dispatchers.IO) {
            chatRepository.getUsersStories().collectLatest {
                _userStories.value = it
            }
        }
    }


    fun setUserStorySeen(story: Story) {
        viewModelScope.launch {
            chatRepository.updateStorySeenField(story)
        }
    }

    fun getCurrentUserStory() {
        chatRepository.getCurrentUserStory().onEach {
            _currentUserStories.value = it
        }.launchIn(viewModelScope)
    }

    fun deleteStory(story: Story) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = chatRepository.deleteUserStoryFrmStorage(story)
            if (response is Resource.Success) {
                chatRepository.deleteCurrentUserStory(story).collectLatest {
                    _deleteUserStory.emit(it)
                }
            } else {
                _deleteUserStory.emit(Resource.Error("Error Deleting Story Try Again"))
            }
        }
    }

    fun getAllUsers() {
        viewModelScope.launch(Dispatchers.IO) {
            chatRepository.getAllUsers().collectLatest {
                _allUsers.value = it
            }
        }
    }

    fun getUnseenChatRooms() {
        viewModelScope.launch(Dispatchers.IO) {
            chatRepository.getAllUnseenChatRooms().collectLatest {
                _getUnseenChatRoooms.value = it
            }
        }
    }

}
