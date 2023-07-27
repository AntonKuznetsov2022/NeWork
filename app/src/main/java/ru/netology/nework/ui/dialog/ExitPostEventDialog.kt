package ru.netology.nework.ui.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nework.R
import ru.netology.nework.viewmodel.EventViewModel
import ru.netology.nework.viewmodel.JobViewModel
import ru.netology.nework.viewmodel.PostViewModel

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class ExitPostEventDialog : DialogFragment() {

    companion object {
        private const val VALUE = "POST"
        fun newInstance(value: String?) = ExitPostEventDialog().apply {
            arguments = bundleOf(VALUE to value)
        }
    }

    private val eventViewModel: EventViewModel by activityViewModels()
    private val postViewModel: PostViewModel by activityViewModels()
    private val jobViewModel: JobViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setView(R.layout.dialog_exit_post_event)
            .setNeutralButton(R.string.no) { dialog, _ ->
                dialog.cancel()
                return@setNeutralButton
            }
            .setPositiveButton(R.string.yes) { _, _ ->
                if (requireArguments().getString(VALUE) == "POST") {
                    postViewModel.cancelEditPost()
                } else if (requireArguments().getString(VALUE) == "EVENT") {
                    eventViewModel.cancelEditEvent()
                } else {
                    jobViewModel.cancelEditJob()
                }
                findNavController().navigateUp()
                return@setPositiveButton
            }.create()
    }
}
