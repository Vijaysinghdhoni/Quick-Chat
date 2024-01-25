package com.vijaydhoni.quickchat.ui.viewmodels

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vijaydhoni.quickchat.data.models.User
import com.vijaydhoni.quickchat.data.repositorys.repository.AuthRepository
import com.vijaydhoni.quickchat.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthenticationViewModel @Inject constructor(private val authRepository: AuthRepository) :
    ViewModel() {

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

    fun setUserDetails(user: User) {

        if (!validateUser(user)) {
            viewModelScope.launch {
                _setUserDetails.emit(Resource.Loading())
                val response = authRepository.setUserDetails(user)
                _setUserDetails.emit(response)
            }
        } else {
            viewModelScope.launch {
                _setUserDetails.emit(Resource.Error("User name is empty"))
            }
        }


    }

    private fun getCurrentUser() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId()
            if (userId.data != null) {
                _isUserLoading.value = false
            }
        }

    }



    private fun validateUser(user: User) =
        user.userName.isNullOrEmpty() || user.userName!!.length < 5 || user.phone.isEmpty()


}