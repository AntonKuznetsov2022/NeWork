package ru.netology.nework.ui.post

import android.annotation.SuppressLint
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
import ru.netology.nework.ui.dialog.ExitPostEventDialog
import ru.netology.nework.util.AndroidUtils
import ru.netology.nework.util.StringArg
import ru.netology.nework.viewmodel.PostViewModel

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class NewPostFragment : Fragment() {
    lateinit var binding: FragmentNewPostBinding

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

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            val value = arguments?.getString("open")
            AndroidUtils.hideKeyboard(requireView())
            ExitPostEventDialog.newInstance(value)
                .show(parentFragmentManager, null)
        }

        bindEditPost()

        bindAddPlace()
        bindAddContent()

        return binding.root
    }

    private fun bindEditPost() {
        binding.apply {
            editText.setText(viewModel.edited.value?.content)
            editLink.setText(viewModel.edited.value?.link)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun bindAddPlace() {
        binding.apply {
            addPlaceBut.setOnClickListener {
                if (viewModel.edited.value?.coords != null) {
                    val bundle = Bundle().apply {
                        putString("open", "POST")
                        putDouble("lat", viewModel.edited.value?.coords!!.lat)
                        putDouble("long", viewModel.edited.value?.coords!!.long)
                    }
                    findNavController().navigate(
                        R.id.action_newPostFragment_to_mapFragment,
                        bundle
                    )
                } else {
                    val bundle = Bundle().apply {
                        putString("open", "POST")
                    }
                    findNavController().navigate(
                        R.id.action_newPostFragment_to_mapFragment,
                        bundle
                    )
                }
            }

            if (viewModel.edited.value?.coords != null) {
                addPlaceBut.text =
                    "${viewModel.edited.value!!.coords?.lat.toString().take(9)}  ||  ${
                        viewModel.edited.value!!.coords?.long.toString().take(9)
                    }"
                deletePlaceBut.isVisible = true
            }

            deletePlaceBut.setOnClickListener {
                viewModel.saveCoords(null, null)
                addPlaceBut.text = getText(R.string.add_place)
                deletePlaceBut.isVisible = false
            }
        }
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
                } else if (viewModel.edited.value?.attachment != null) {
                    findNavController().navigate(
                        R.id.action_global_onPictureFragment,
                        Bundle().apply { textArg = viewModel.edited.value?.attachment?.url })
                } else {
                    BottomSheetImage()
                        .show(parentFragmentManager, null)
                }
            }

            viewModel.media.observe(viewLifecycleOwner) { media ->
                if (media != null || viewModel.edited.value?.attachment != null) {
                    addContentBut.text = getText(R.string.content_added)
                    deleteContentBut.isVisible = true
                }
            }

            deleteContentBut.setOnClickListener {
                viewModel.clearPhoto()
                viewModel.edited.value = viewModel.edited.value?.copy(attachment = null)
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