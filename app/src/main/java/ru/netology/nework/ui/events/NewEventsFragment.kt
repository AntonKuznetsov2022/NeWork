package ru.netology.nework.ui.events

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TimePicker
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
import ru.netology.nework.databinding.FragmentNewEventBinding
import ru.netology.nework.dto.EventType
import ru.netology.nework.ui.auth.SignUpFragment
import ru.netology.nework.ui.dialog.BottomSheetImage
import ru.netology.nework.ui.post.NewPostFragment.Companion.textArg
import ru.netology.nework.util.AndroidUtils
import ru.netology.nework.viewmodel.EventViewModel
import ru.netology.nework.viewmodel.UserViewModel
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class NewEventsFragment : Fragment() {

    lateinit var binding: FragmentNewEventBinding
    private val viewModel: EventViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewEventBinding.inflate(inflater, container, false)
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
                                getString(R.string.content_event_is_empty),
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

        viewModel.eventCreated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
            viewModel.loadEvents()
            userViewModel.loadUsers()
        }

        bindAddData()
        bindAddUser()
        bindAddPlace()
        bindAddContent()
        bindAddType()

        return binding.root
    }

    @SuppressLint("NewApi")
    private fun bindAddData() {
        val calendar = Calendar.getInstance()
        binding.apply {

            val formatterDate =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.uuuuuu", Locale.getDefault())
            val formatterShow = SimpleDateFormat("dd.MM.yyyy в HH:mm", Locale.getDefault())

            addTimeBut.text = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy в HH:mm"))

            addTimeBut.setOnClickListener {
                DatePickerDialog(
                    requireContext(),
                    object : DatePickerDialog.OnDateSetListener {
                        override fun onDateSet(
                            view: DatePicker?,
                            year: Int,
                            month: Int,
                            dayOfMonth: Int
                        ) {
                            calendar.set(year, month, dayOfMonth)
                            TimePickerDialog(
                                requireContext(),
                                object : TimePickerDialog.OnTimeSetListener {
                                    override fun onTimeSet(
                                        view: TimePicker?,
                                        hourOfDay: Int,
                                        minute: Int
                                    ) {
                                        calendar.apply {
                                            set(Calendar.HOUR_OF_DAY, hourOfDay)
                                            set(Calendar.MINUTE, minute)
                                        }
                                        addTimeBut.text = formatterShow.format(calendar.timeInMillis)
                                        viewModel.saveDatetime(
                                            formatterDate.format(calendar.timeInMillis)
                                        )
                                    }
                                },
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                true
                            ).show()
                        }
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }

        }
    }

    @SuppressLint("SetTextI18n")
    private fun bindAddUser() {
        binding.apply {
            addUserBut.setOnClickListener {
                findNavController().navigate(R.id.action_newEventsFragment_to_usersFragment)
            }

            if (viewModel.edited.value?.speakerIds?.size != 0) {
                addUserBut.text =
                    "${getText(R.string.user_added)} ${viewModel.edited.value?.speakerIds!!.count()}"
                deleteUserBut.isVisible = true
            }

            deleteUserBut.setOnClickListener {
                viewModel.clearSpeakers()
                userViewModel.loadUsers()
                addUserBut.text = getText(R.string.add_user)
                deleteUserBut.isVisible = false
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun bindAddPlace() {
        binding.apply {
            addPlaceBut.setOnClickListener {
                if (viewModel.edited.value?.coords != null) {
                    val bundle = Bundle().apply {
                        putDouble("lat", viewModel.edited.value?.coords!!.lat)
                        putDouble("long", viewModel.edited.value?.coords!!.long)
                    }
                    findNavController().navigate(
                        R.id.action_newEventsFragment_to_mapFragment,
                        bundle
                    )
                } else {
                    findNavController().navigate(R.id.action_newEventsFragment_to_mapFragment)
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
                ImagePicker.with(this@NewEventsFragment)
                    .cameraOnly()
                    .cropSquare()
                    .compress(2048)
                    .createIntent(photoLauncher::launch)
            }
            if (bundle.getString(SignUpFragment.GALLERY) != null) {
                ImagePicker.with(this@NewEventsFragment)
                    .galleryOnly()
                    .cropSquare()
                    .compress(2048)
                    .createIntent(photoLauncher::launch)
            }
        }
    }

    private fun bindAddType() {
        binding.apply {
            addTypeBut.setOnClickListener {
                if (addTypeBut.text == getString(R.string.online)) {
                    addTypeBut.text = getString(R.string.offline)
                    viewModel.changeEventType(EventType.OFFLINE)
                } else {
                    addTypeBut.text = getString(R.string.online)
                    viewModel.changeEventType(EventType.ONLINE)
                }
            }
        }
    }
}
