package com.vijaydhoni.quickchat.ui.view.fragments.authFragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.vijaydhoni.quickchat.R
import com.vijaydhoni.quickchat.databinding.FragmentLoginBinding
import com.vijaydhoni.quickchat.ui.viewmodels.AuthenticationViewModel
import com.vijaydhoni.quickchat.util.setStatusBarColour

class LoginFragment : Fragment() {
    private val binding by lazy {
        FragmentLoginBinding.inflate(layoutInflater)
    }

    private val viewModel by activityViewModels<AuthenticationViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.countryPick.registerCarrierNumberEditText(binding.phnNum)
        binding.loginBttn.setOnClickListener {
            if (!binding.countryPick.isValidFullNumber) {
                binding.phnNum.error = "Invalid Phone Number"
                return@setOnClickListener
            } else {
                val phone = binding.countryPick.fullNumberWithPlus
                viewModel.createUserWithPhone(phone, requireActivity(), false)
                val bundle = Bundle()
                bundle.putString("phone", phone)
                findNavController().navigate(R.id.action_loginFragment_to_OTPFragment, bundle)
            }

        }
    }


    override fun onResume() {
        super.onResume()
        setStatusBarColour(activity as AppCompatActivity, R.color.g_blue)
    }


}