package com.vijaydhoni.quickchat.ui.view.fragments.authFragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.vijaydhoni.quickchat.R
import com.vijaydhoni.quickchat.databinding.FragmentOTPBinding
import com.vijaydhoni.quickchat.ui.viewmodels.AuthenticationViewModel
import com.vijaydhoni.quickchat.util.Resource
import com.vijaydhoni.quickchat.util.setStatusBarColour
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OTPFragment : Fragment() {
    private val binding: FragmentOTPBinding by lazy {
        FragmentOTPBinding.inflate(layoutInflater)
    }

    private val viewModel by activityViewModels<AuthenticationViewModel>()
    private var countdownTimer: CountDownTimer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args by navArgs<OTPFragmentArgs>()
        val userPhoneNumber = args.phone
        observeSendOtpInUserNumber()
        binding.confrmOtpBttn.setOnClickListener {
            val otp = binding.otpView.text
            if (otp.isNullOrEmpty()) binding.otpView.error = "complete OTP" else
                signInWithOtp(otp.toString(), userPhoneNumber)
        }
        binding.otpView.setOtpCompletionListener {
            signInWithOtp(it, userPhoneNumber)
        }
        binding.resendOtpTxt.setOnClickListener {
            viewModel.createUserWithPhone(userPhoneNumber, requireActivity(), true)
        }

    }

    private fun signInWithOtp(otp: String, userPhoneNumber: String) {

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.signInWithCredential(otp).collectLatest { resource ->

                    when (resource) {
                        is Resource.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.confrmOtpBttn.startAnimation()
                        }

                        is Resource.Success -> {
                            binding.progressBar.visibility = View.GONE
                            binding.confrmOtpBttn.revertAnimation()
                            binding.otpView.setText("")
                            Toast.makeText(requireContext(), resource.data, Toast.LENGTH_SHORT)
                                .show()
                            val bundle = Bundle()
                            bundle.putString("userPhonenumber", userPhoneNumber)
                            findNavController().navigate(
                                R.id.action_OTPFragment_to_userProfileFragment,
                                bundle
                            )
                        }

                        is Resource.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.confrmOtpBttn.revertAnimation()
                            Toast.makeText(
                                requireContext(),
                                resource.message,
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.otpView.setText("")
                        }

                        else -> {

                        }

                    }

                }
            }
        }
    }

    private fun observeSendOtpInUserNumber() {
        startResendTimer()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.createUser
                    .collectLatest { resource ->

                        when (resource) {
                            is Resource.Loading -> {
                                binding.progressBar.visibility = View.VISIBLE
                            }

                            is Resource.Success -> {
                                binding.progressBar.visibility = View.GONE
                                Toast.makeText(
                                    requireContext(),
                                    resource.data,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            is Resource.Error -> {
                                binding.progressBar.visibility = View.GONE
                                Toast.makeText(
                                    requireContext(),
                                    resource.message,
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }

                            else -> {

                            }
                        }

                    }
            }
        }

    }

    private fun startResendTimer() {
        binding.resendOtpTxt.isEnabled = false
        binding.resendOtpTxt.setTextColor(resources.getColor(R.color.g_light_black))
        countdownTimer = object : CountDownTimer(60000, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                binding.resendOtpTxt.text = "You will be able to Resend OTP in $secondsLeft seconds"
            }

            override fun onFinish() {
                binding.resendOtpTxt.isEnabled = true
                binding.resendOtpTxt.setTextColor(resources.getColor(R.color.g_white))
            }
        }.start()
    }

    override fun onResume() {
        super.onResume()
        setStatusBarColour(activity as AppCompatActivity, R.color.g_blue)
    }

}