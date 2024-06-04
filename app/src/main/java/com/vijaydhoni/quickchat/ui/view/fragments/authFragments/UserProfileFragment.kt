package com.vijaydhoni.quickchat.ui.view.fragments.authFragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.github.drjacky.imagepicker.ImagePicker
import com.google.firebase.Timestamp
import com.vijaydhoni.quickchat.R
import com.vijaydhoni.quickchat.data.models.User
import com.vijaydhoni.quickchat.databinding.FragmentUserProfileBinding
import com.vijaydhoni.quickchat.ui.view.activity.ChatActivity
import com.vijaydhoni.quickchat.ui.viewmodel.AuthenticationViewModel
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
    private var currentUserID: String? = null

    private var imageUri: Uri? = null
    private lateinit var imageLauncher: ActivityResultLauncher<Intent>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imageLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == Activity.RESULT_OK) {
                    val uri = it.data?.data
                    if (uri != null) {
                        imageUri = uri
                        Glide.with(binding.usrPofilePic)
                            .load(imageUri)
                            .into(binding.usrPofilePic)
                    }
                }
            }
    }


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
        viewModel.getCurrentUser()
        viewModel.getCurrentUserDetail()
        observeCurrentUser()
        binding.letMeInBttn.setOnClickListener {
            setUserDetails(phone)
        }
        observeSetUserDetail()
        observeCurrentUserId()
        changeImgaeBttnclick()
    }


    private fun changeImgaeBttnclick() {
        binding.usrImgChange.setOnClickListener {

            ImagePicker.with(requireActivity())
                .cropSquare()
                .maxResultSize(512, 512)
                .createIntentFromDialog {
                    imageLauncher.launch(it)
                }

        }
    }

    private fun observeCurrentUserId() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentUserId.collectLatest { response ->
                    when (response) {

                        is Resource.Success -> {
                            currentUserID = response.data
                        }

                        is Resource.Error -> {
                            Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT)
                                .show()
                        }
                        else -> {

                        }

                    }
                }

            }
        }
    }


    private fun setUserDetails(phoneNumber: String) {
        val userName = binding.userName.text.toString().trim()
        if (user != null) {
            user?.userName = userName
        } else {
            user = User(phoneNumber, userName, Timestamp.now(), userId = currentUserID)
        }
        viewModel.setUserDetails(user!!, imageUri)
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
                                if (user?.imagePath?.isNotEmpty() == true) {
                                    Glide.with(binding.usrPofilePic)
                                        .load(user?.imagePath)
                                        .placeholder(R.drawable.person_profile_place_holder)
                                        .error(R.color.g_black)
                                        .into(binding.usrPofilePic)
                                }
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