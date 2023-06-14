package ru.netology.nework.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.databinding.BottomSheetsBinding


@AndroidEntryPoint
class BottomSheetImage : BottomSheetDialogFragment() {

    companion object {
        private const val CAMERA_OR_GALLERY = "CAMERA_OR_GALLERY"
        private const val CAMERA = "CAMERA"
        private const val GALLERY = "GALLERY"
    }

    lateinit var binding: BottomSheetsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetsBinding.inflate(inflater, container, false)

        val result = ""

        binding.textCamera.setOnClickListener {
            setFragmentResult(CAMERA_OR_GALLERY, bundleOf(CAMERA to result))
            dialog?.cancel()
        }

        binding.textGallery.setOnClickListener {
            setFragmentResult(CAMERA_OR_GALLERY, bundleOf(GALLERY to result))
            dialog?.cancel()
        }

        return binding.root
    }
}