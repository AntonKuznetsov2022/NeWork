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
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentNewPostBinding
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
            binding.edit.setText(editText.getString(APP_NAME, ""))
        }

        if (arguments?.textArg != null) {
            with(binding.edit) {
                requestFocus()
                setSelection(text.toString().length)
            }
        }

        arguments?.textArg
            ?.let(binding.edit::setText)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            saveText(binding.edit.text.toString())
            findNavController().navigateUp()
        }

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.add_post, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.add_post -> {
                        saveText("")
                        viewModel.changeContent(binding.edit.text.toString())
                        viewModel.save()
                        AndroidUtils.hideKeyboard(requireView())
                        true
                    }
                    else -> false
                }
        }, viewLifecycleOwner)

        viewModel.postCreated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
            viewModel.loadPosts()
        }

        return binding.root
    }

    fun saveText(text: String) {
        val editText = context?.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE)
        editText?.edit()?.apply { putString(APP_NAME, text).apply() }
    }
}