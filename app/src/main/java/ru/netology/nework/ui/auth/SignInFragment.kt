package ru.netology.nework.ui.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentSignInBinding
import ru.netology.nework.util.AndroidUtils
import ru.netology.nework.viewmodel.SignInViewModel
import javax.inject.Inject

@AndroidEntryPoint
class SignInFragment : Fragment() {

    @Inject
    lateinit var appAuth: AppAuth

    lateinit var binding: FragmentSignInBinding
    private val viewModel by viewModels<SignInViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignInBinding.inflate(inflater, container, false)

        setupListeners()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        binding.loginBut.setOnClickListener {
            if (isValidate()) {
                viewModel.signIn(
                    binding.loginIn.text.toString(),
                    binding.passwordIn.text.toString()
                )
                AndroidUtils.hideKeyboard(requireView())
            }
        }

        binding.registrationBut.setOnClickListener {
            findNavController().navigate(R.id.action_signInFragment_to_signUpFragment)
        }

        viewModel.stateSignIn.observe(viewLifecycleOwner) { state ->
            if (state.signInError) {
                Snackbar.make(binding.root, R.string.sign_in_error, Snackbar.LENGTH_LONG)
                    .show()
            }
            if (state.signInWrong) {
                Snackbar.make(binding.root, R.string.sign_in_wrong, Snackbar.LENGTH_LONG)
                    .show()
            }
        }

        viewModel.signInApp.observe(viewLifecycleOwner) {
            appAuth.setAuth(it.id, it.token)
            findNavController().navigateUp()
            Snackbar.make(binding.root, R.string.login_success, Snackbar.LENGTH_LONG)
                .show()
        }

        return binding.root
    }

    private fun isValidate(): Boolean =
        validateLogin() && validatePassword()

    private fun setupListeners() {
        binding.loginIn.addTextChangedListener(TextFieldValidation(binding.loginIn))
        binding.passwordIn.addTextChangedListener(TextFieldValidation(binding.passwordIn))
    }

    private fun validateLogin(): Boolean {
        if (binding.loginIn.text.toString().trim().isEmpty()) {
            binding.loginInLayout.error = context?.getString(R.string.required_field)
            binding.loginIn.requestFocus()
            return false
        } else {
            binding.loginInLayout.isErrorEnabled = false
        }
        return true
    }

    private fun validatePassword(): Boolean {
        if (binding.passwordIn.text.toString().trim().isEmpty()) {
            binding.passwordInLayout.error = context?.getString(R.string.required_field)
            binding.passwordIn.requestFocus()
            return false
        } else {
            binding.passwordInLayout.isErrorEnabled = false
        }
        return true
    }

    inner class TextFieldValidation(private val view: View) : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            when (view.id) {
                R.id.name -> {
                    validateLogin()
                }

                R.id.loginUp -> {
                    validatePassword()
                }
            }
        }
    }
}