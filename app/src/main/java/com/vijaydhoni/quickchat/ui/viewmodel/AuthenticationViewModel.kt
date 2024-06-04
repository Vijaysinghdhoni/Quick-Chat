package com.vijaydhoni.quickchat.ui.viewmodel

import android.app.Activity
import android.app.Application
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vijaydhoni.quickchat.BaseApplication
import com.vijaydhoni.quickchat.data.models.User
import com.vijaydhoni.quickchat.data.repo.AuthRepository
import com.vijaydhoni.quickchat.util.Constants.Companion.INTRO_BUTTON_CLICK
import com.vijaydhoni.quickchat.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    val app: Application,
    private val sharedPreferences: SharedPreferences
) :
    AndroidViewModel(app) {

    private val _createUser: MutableStateFlow<Resource<String>> =
        MutableStateFlow(Resource.Unspecified())
    val createUser = _createUser.asStateFlow()

    private val _setUserDetails: MutableStateFlow<Resource<String>> =
        MutableStateFlow(Resource.Unspecified())
    val setUserDetails = _setUserDetails.asStateFlow()

    private val _currentUserDetail: MutableStateFlow<Resource<User?>> =
        MutableStateFlow(Resource.Unspecified())
    val currentUserDetail = _currentUserDetail.asStateFlow()


    private val _isUserLoading = MutableStateFlow(true)
    val isUserLoading = _isUserLoading.asStateFlow()


    private val _currentUserId: MutableStateFlow<Resource<String?>> =
        MutableStateFlow(Resource.Unspecified())
    val currentUserId = _currentUserId.asStateFlow()

    var isNextButtonClicked = sharedPreferences.getBoolean(INTRO_BUTTON_CLICK, false)

    init {
        getCurrentUser()
    }


    fun createUserWithPhone(
        phone: String,
        activity: Activity,
        isResendOtp: Boolean
    ) {
        val response = authRepository.createUserWithPhone(phone, activity, isResendOtp)
        viewModelScope.launch {
            response.collectLatest {
                _createUser.emit(it)
            }
        }
    }

    fun signInWithCredential(
        otp: String
    ) = authRepository.sigInWithCredential(otp)


    fun getCurrentUserDetail() {
        viewModelScope.launch {
            _currentUserDetail.emit(Resource.Loading())
            val user = authRepository.getCurrentUserDetail()
            _currentUserDetail.emit(user)
        }
    }

    fun setUserDetails(user: User, imageUri: Uri?) {

        if (!validateUser(user)) {
            viewModelScope.launch {
                _setUserDetails.emit(Resource.Loading())
                if (imageUri != null) {
                    setUserWithNewImage(user, imageUri)
                } else {
                    setUserWithoutNewImage(user)
                }
            }
        } else {
            viewModelScope.launch {
                _setUserDetails.emit(Resource.Error("Complete User Name required"))
            }
        }


    }

    private suspend fun setUserWithoutNewImage(user: User) {
        val response = authRepository.setUserDetails(user)
        _setUserDetails.emit(response)
    }

    private suspend fun setUserWithNewImage(user: User, imageUri: Uri) {
        val imageBitmap = MediaStore.Images.Media.getBitmap(
            getApplication<BaseApplication>().contentResolver,
            imageUri
        )
        val byteArrayOutputStream = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 96, byteArrayOutputStream)
        val imageByteArray = byteArrayOutputStream.toByteArray()

        when (val imageUrl = authRepository.saveUserProfileImg(imageByteArray)) {

            is Resource.Success -> {
                val response = authRepository.setUserDetails(user.copy(imagePath = imageUrl.data!!))
                _setUserDetails.emit(response)
            }

            is Resource.Error -> {
                _setUserDetails.emit(Resource.Error(imageUrl.message ?: "Cannot save image"))
            }

            else -> {}

        }

    }

    fun getCurrentUser() {
        viewModelScope.launch {
            _currentUserId.emit(Resource.Loading())
            val userId = authRepository.getCurrentUserId()
            if (userId.data != null) {
                _isUserLoading.value = false
                _currentUserId.emit(userId)
            } else {
                _currentUserId.emit(Resource.Unspecified())
            }
        }

    }


    fun setUserActiveOrNot(isActive: Boolean) {
        viewModelScope.launch {

            when (val response = authRepository.userActiveOrLastSeen(isActive)) {
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


    fun logoutUser() {
        authRepository.logout()
    }

    fun nextIntroButtonClicked() {
        sharedPreferences.edit().putBoolean(INTRO_BUTTON_CLICK, true).apply()
    }


    private fun validateUser(user: User) =
        user.userName.isNullOrEmpty() || user.userName!!.length < 5 || user.phone.isEmpty()


}