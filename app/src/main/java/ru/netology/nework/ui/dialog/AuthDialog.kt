package ru.netology.nework.ui.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ru.netology.nework.R

class AuthDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.log_account)
            .setMessage(R.string.enter_in_app)
            .setNegativeButton(R.string.guest) { dialog, _ ->
                dialog.cancel()
                return@setNegativeButton
            }
            .setPositiveButton(R.string.enter) { _, _ ->
                findNavController().navigate(R.id.action_global_signInFragment)
                return@setPositiveButton
            }.create()
    }
}