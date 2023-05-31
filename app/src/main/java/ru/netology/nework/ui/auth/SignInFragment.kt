package ru.netology.nework.ui.auth

import android.os.Bundle
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
import ru.netology.nework.ui.dialog.AuthDialog
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

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        binding.loginBut.setOnClickListener {
            viewModel.signIn(binding.loginIn.text.toString(), binding.passwordIn.text.toString())
        }

        binding.registrationBut.setOnClickListener {
            //findNavController().navigate(R.id.action_signInFragment_to_signUpFragment)
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
}