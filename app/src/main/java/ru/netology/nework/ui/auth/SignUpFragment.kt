package ru.netology.nework.ui.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.adapter.loadCircleCrop
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentSignUpBinding
import ru.netology.nework.ui.dialog.BottomSheetImage
import ru.netology.nework.ui.dialog.ConfirmSaveProfileDialog
import ru.netology.nework.util.AndroidUtils
import ru.netology.nework.viewmodel.SignUpViewModel
import javax.inject.Inject

@AndroidEntryPoint
class SignUpFragment : Fragment() {

    companion object {
        private const val CAMERA_OR_GALLERY = "CAMERA_OR_GALLERY"
        private const val CAMERA = "CAMERA"
        private const val GALLERY = "GALLERY"
    }

    @Inject
    lateinit var appAuth: AppAuth

    lateinit var binding: FragmentSignUpBinding
    private val viewModel by viewModels<SignUpViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)

        val photoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                when (it.resultCode) {
                    ImagePicker.RESULT_ERROR -> {
                        Snackbar.make(
                            binding.root,
                            getString(R.string.photo_error),
                            Snackbar.LENGTH_LONG
                        ).show()
                    }

                    else -> {
                        val uri = it.data?.data ?: return@registerForActivityResult
                        viewModel.changeAvatar(uri.toFile(), uri)
                    }
                }
            }

        setupListeners()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        viewModel.stateSignUp.observe(viewLifecycleOwner) { state ->
            if (state.signUpWrong) {
                Snackbar.make(binding.root, R.string.user_registered, Snackbar.LENGTH_LONG)
                    .show()
            }
            if (state.signUpError) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .show()
            }
        }

        binding.clearBut.setOnClickListener {
            viewModel.clearAvatar()
        }

        binding.previewAvatar.setOnClickListener {
            BottomSheetImage()
                .show(parentFragmentManager, null)
        }

        setFragmentResultListener(CAMERA_OR_GALLERY) { _, bundle ->
            if (bundle.getString(CAMERA) != null) {
                ImagePicker.with(this)
                    .cameraOnly()
                    .cropSquare()
                    .compress(192)
                    .createIntent(photoLauncher::launch)
            }
            if (bundle.getString(GALLERY) != null) {
                ImagePicker.with(this)
                    .galleryOnly()
                    .cropSquare()
                    .compress(192)
                    .createIntent(photoLauncher::launch)
            }
        }

        viewModel.mediaAvatar.observe(viewLifecycleOwner) { mediaAvatar ->
            if (mediaAvatar == null) {
                binding.clearBut.isGone = true
                binding.previewAvatar.setImageResource(R.drawable.ic_person_add)
                return@observe
            }
            binding.clearBut.isVisible = true
            binding.previewAvatar.loadCircleCrop(mediaAvatar.uri.toString())
        }

        binding.registrationBut.setOnClickListener {
            if (isValidate()) {
                viewModel.signUp(
                    binding.loginUp.text.toString(),
                    binding.passwordUp.text.toString(),
                    binding.name.text.toString(),
                )
                AndroidUtils.hideKeyboard(requireView())
            }
        }

        viewModel.signUpApp.observe(viewLifecycleOwner) {
            appAuth.setAuth(it.id, it.token)
            findNavController().navigateUp()
            findNavController().navigateUp()
            ConfirmSaveProfileDialog()
                .show(parentFragmentManager, null)
        }

        return binding.root
    }

    private fun isValidate(): Boolean =
        validateUserName() && validateLogin() && validatePassword() && validateConfirmPassword()

    private fun setupListeners() {
        binding.name.addTextChangedListener(TextFieldValidation(binding.name))
        binding.loginUp.addTextChangedListener(TextFieldValidation(binding.loginUp))
        binding.passwordUp.addTextChangedListener(TextFieldValidation(binding.passwordUp))
        binding.confirmPasswordUp.addTextChangedListener(TextFieldValidation(binding.confirmPasswordUp))
    }

    private fun validateUserName(): Boolean {
        if (binding.name.text.toString().trim().isEmpty()) {
            binding.nameLayout.error = context?.getString(R.string.required_field)
            binding.name.requestFocus()
            return false
        } else {
            binding.nameLayout.isErrorEnabled = false
        }
        return true
    }

    private fun validateLogin(): Boolean {
        if (binding.loginUp.text.toString().trim().isEmpty()) {
            binding.loginUpLayout.error = context?.getString(R.string.required_field)
            binding.loginUp.requestFocus()
            return false
        } else {
            binding.loginUpLayout.isErrorEnabled = false
        }
        return true
    }

    private fun validatePassword(): Boolean {
        if (binding.passwordUp.text.toString().trim().isEmpty()) {
            binding.passwordUpLayout.error = context?.getString(R.string.required_field)
            binding.passwordUp.requestFocus()
            return false
        } else {
            binding.passwordUpLayout.isErrorEnabled = false
        }
        return true
    }

    private fun validateConfirmPassword(): Boolean {
        when {
            binding.confirmPasswordUp.text.toString().trim().isEmpty() -> {
                binding.confirmPasswordUpLayout.error = context?.getString(R.string.required_field)
                binding.confirmPasswordUp.requestFocus()
                return false
            }

            binding.confirmPasswordUp.text.toString() != binding.passwordUp.text.toString() -> {
                binding.confirmPasswordUpLayout.error =
                    context?.getString(R.string.passwords_dont_match)
                binding.confirmPasswordUp.requestFocus()
                return false
            }

            else -> {
                binding.confirmPasswordUpLayout.isErrorEnabled = false
            }
        }
        return true
    }

    inner class TextFieldValidation(private val view: View) : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            when (view.id) {
                R.id.name -> {
                    validateUserName()
                }

                R.id.loginUp -> {
                    validateLogin()
                }

                R.id.passwordUp -> {
                    validatePassword()
                }

                R.id.confirmPasswordUp -> {
                    validateConfirmPassword()
                }
            }
        }
    }
}
