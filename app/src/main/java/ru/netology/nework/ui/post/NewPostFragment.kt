package ru.netology.nework.ui.post

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentNewPostBinding
import ru.netology.nework.ui.auth.SignUpFragment
import ru.netology.nework.ui.dialog.BottomSheetImage
import ru.netology.nework.util.AndroidUtils
import ru.netology.nework.util.StringArg
import ru.netology.nework.viewmodel.PostViewModel

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class NewPostFragment : Fragment() {
    lateinit var binding: FragmentNewPostBinding
    private var APP_NAME = "editText"

    companion object {
        var Bundle.textArg: String? by StringArg
    }

    private val viewModel: PostViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewPostBinding.inflate(inflater, container, false)

        super.onCreate(savedInstanceState)
        val editText = context?.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE)
        if (editText != null) {
            binding.editText.setText(editText.getString(APP_NAME, ""))
        }

        if (arguments?.textArg != null) {
            with(binding.editText) {
                requestFocus()
                setSelection(text.toString().length)
            }
        }

        arguments?.textArg
            ?.let(binding.editText::setText)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            saveText(binding.editText.text.toString())
            findNavController().navigateUp()
        }

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.add, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.add -> {
                        if (binding.editText.text.isEmpty()) {
                            Snackbar.make(
                                binding.root,
                                getString(R.string.content_post_is_empty),
                                Snackbar.LENGTH_LONG
                            ).show()
                        } else {
                            saveText("")
                            viewModel.changeContent(binding.editText.text.toString())
                            viewModel.changeLink(binding.editLink.text.toString())
                            viewModel.save()
                            AndroidUtils.hideKeyboard(requireView())
                        }
                        true
                    }

                    else -> false
                }
        }, viewLifecycleOwner)

        viewModel.postCreated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
            viewModel.loadPosts()
        }

        bindAddContent()

        return binding.root
    }

    fun saveText(text: String) {
        val editText = context?.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE)
        editText?.edit()?.apply { putString(APP_NAME, text).apply() }
    }

    private fun bindAddContent() {
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
                        viewModel.changePhoto(uri.toFile(), uri)
                    }
                }
            }

        binding.apply {
            addContentBut.setOnClickListener {
                if (viewModel.media.value?.uri != null) {
                    findNavController().navigate(
                        R.id.action_global_onPictureFragment,
                        Bundle().apply { textArg = viewModel.media.value?.uri.toString() })
                } else {
                    BottomSheetImage()
                        .show(parentFragmentManager, null)
                }
            }

            viewModel.media.observe(viewLifecycleOwner) { media ->
                if (media != null) {
                    addContentBut.text = getText(R.string.content_added)
                    deleteContentBut.isVisible = true
                }
            }

            deleteContentBut.setOnClickListener {
                viewModel.clearPhoto()
                addContentBut.text = getText(R.string.add_content)
                deleteContentBut.isVisible = false
            }
        }

        setFragmentResultListener(SignUpFragment.CAMERA_OR_GALLERY)
        { _, bundle ->
            if (bundle.getString(SignUpFragment.CAMERA) != null) {
                ImagePicker.with(this@NewPostFragment)
                    .cameraOnly()
                    .cropSquare()
                    .compress(2048)
                    .createIntent(photoLauncher::launch)
            }
            if (bundle.getString(SignUpFragment.GALLERY) != null) {
                ImagePicker.with(this@NewPostFragment)
                    .galleryOnly()
                    .cropSquare()
                    .compress(2048)
                    .createIntent(photoLauncher::launch)
            }
        }
    }
}