package com.vijaydhoni.quickchat.ui.view.fragments.chatFragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.github.drjacky.imagepicker.ImagePicker
import com.vijaydhoni.quickchat.R
import com.vijaydhoni.quickchat.data.models.User
import com.vijaydhoni.quickchat.databinding.FragmentProfileBinding
import com.vijaydhoni.quickchat.ui.view.activity.AuthenticationActivity
import com.vijaydhoni.quickchat.ui.viewmodel.AuthenticationViewModel
import com.vijaydhoni.quickchat.util.Resource
import com.vijaydhoni.quickchat.util.setStatusBarColour
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserProfileFragment : Fragment() {
    private val binding: FragmentProfileBinding by lazy {
        FragmentProfileBinding.inflate(layoutInflater)
    }
    private val authViewModel by activityViewModels<AuthenticationViewModel>()
    private var user: User? = null
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
        authViewModel.getCurrentUserDetail()
        observeUserDetials()
        binding.updateDetailsBttn.setOnClickListener {
            updateUserDetials()
        }
        observeUserDetialsUpdate()
        logoutUser()
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


    private fun logoutUser() {
        binding.logoutUsr.setOnClickListener {
            val alertDialog = AlertDialog.Builder(requireContext()).apply {
                setTitle("Log out!")
                setMessage("Do you want to Logout?")
                setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                setPositiveButton("Yes") { dialog, _ ->
                    authViewModel.setUserActiveOrNot(false)
                    authViewModel.logoutUser()
                    val intent =
                        Intent(requireActivity(), AuthenticationActivity::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    dialog.dismiss()
                }
            }
            alertDialog.create()
            alertDialog.show()

        }
    }

    private fun observeUserDetialsUpdate() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.setUserDetails.collect { response ->
                    when (response) {


                        is Resource.Loading -> {
                            binding.updateDetailsBttn.startAnimation()
                        }

                        is Resource.Success -> {
                            binding.updateDetailsBttn.revertAnimation {
                                binding.updateDetailsBttn.text = response.data
                            }
                        }

                        is Resource.Error -> {
                            binding.userNameEditText.error = response.message
                            Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT)
                                .show()
                        }

                        else -> {}

                    }
                }
            }
        }
    }

    private fun updateUserDetials() {
        val newName = binding.userNameEditText.text?.trim().toString()
        val newUserStatus = binding.userStatusUpdate.text.toString()
        if (user != null) {
            user = user?.copy(
                userName = newName,
                userStatus = newUserStatus
            )
            authViewModel.setUserDetails(user!!, imageUri)
        } else {
            Toast.makeText(requireContext(), "cannot update user details", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun observeUserDetials() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.currentUserDetail.collectLatest { response ->
                    when (response) {

                        is Resource.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }

                        is Resource.Success -> {
                            binding.progressBar.visibility = View.INVISIBLE
                            val userDetails = response.data
                            if (userDetails != null) {
                                //  user = userDetails
                                Log.d("user", "user found $userDetails")
                                setUpUserDetials(userDetails)
                                //setup user detials here not above
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "No User Found",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        is Resource.Error -> {
                            Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT)
                                .show()
                        }

                        else -> {}

                    }
                }
            }
        }
    }


    private fun setUpUserDetials(userDetails: User) {
        user = userDetails
        binding.apply {
            userNameEditText.setText(userDetails.userName)
            phoneNumTxt.setText(userDetails.phone)
            userStatusUpdate.setText(userDetails.userStatus)
        }

        if (userDetails.imagePath.isNotEmpty()) {
            Glide.with(binding.usrPofilePic)
                .load(userDetails.imagePath)
                .placeholder(R.drawable.person_profile_place_holder)
                .error(R.color.g_black)
                .into(binding.usrPofilePic)
        }


    }

    override fun onResume() {
        super.onResume()
        setStatusBarColour(activity as AppCompatActivity, R.color.g_blue)
    }

}