package ru.netology.nework.ui.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ru.netology.nework.R

class AuthDialog : DialogFragment() {

    @SuppressLint("ResourceType")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setView(R.layout.dialog_enter_profile)
            .setNeutralButton(R.string.guest) { dialog, _ ->
                dialog.cancel()
                return@setNeutralButton
            }
            .setPositiveButton(R.string.enter) { _, _ ->
                findNavController().navigate(R.id.action_global_signInFragment)
                return@setPositiveButton
            }.create()
    }
}