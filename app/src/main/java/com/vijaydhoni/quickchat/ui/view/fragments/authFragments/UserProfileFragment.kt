package com.vijaydhoni.quickchat.ui.view.fragments.authFragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.google.firebase.Timestamp
import com.vijaydhoni.quickchat.R
import com.vijaydhoni.quickchat.data.models.User
import com.vijaydhoni.quickchat.databinding.FragmentUserProfileBinding
import com.vijaydhoni.quickchat.ui.view.activity.ChatActivity
import com.vijaydhoni.quickchat.ui.viewmodels.AuthenticationViewModel
import com.vijaydhoni.quickchat.util.Resource
import com.vijaydhoni.quickchat.util.setStatusBarColour
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class UserProfileFragment : Fragment() {
    private val binding: FragmentUserProfileBinding by lazy {
        FragmentUserProfileBinding.inflate(layoutInflater)
    }
    private val viewModel by activityViewModels<AuthenticationViewModel>()
    private var user: User? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args by navArgs<UserProfileFragmentArgs>()
        val phone = args.userPhonenumber
        viewModel.getCurrentUserDetail()
        observeCurrentUser()
        binding.letMeInBttn.setOnClickListener {
            setUserDetails(phone)
        }
        observeSetUserDetail()
    }


    private fun setUserDetails(phoneNumber: String) {
        val userName = binding.userName.text.toString().trim()
        if (user != null) {
            user?.userName = userName
        } else {
            user = User(phoneNumber, userName, Timestamp.now())
        }
        viewModel.setUserDetails(user!!)
    }

    private fun observeCurrentUser() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentUserDetail.collectLatest {
                    when (it) {

                        is Resource.Success -> {
                            user = it.data
                            if (user != null) {
                                binding.userName.setText(user!!.userName)
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "No User Found!!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
//                            user?.let { usr ->
//                                binding.userName.setText(usr.userName)
//                            }
                        }

                        is Resource.Error -> {
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        }

                        else -> {

                        }

                    }
                }
            }
        }
    }

    private fun observeSetUserDetail() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.setUserDetails.collectLatest {

                    when (it) {

                        is Resource.Loading -> {
                            binding.letMeInBttn.startAnimation()
                        }

                        is Resource.Success -> {
                            binding.letMeInBttn.revertAnimation()
                            Toast.makeText(
                                requireContext(),
                                "user profile updated suceesfuly",
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(requireContext(), ChatActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }

                        is Resource.Error -> {
                            binding.letMeInBttn.revertAnimation()
                            Toast.makeText(
                                requireContext(),
                                it.message,
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.userName.error = it.message
                        }

                        else -> {

                        }


                    }


                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setStatusBarColour(activity as AppCompatActivity, R.color.g_blue)
    }


}