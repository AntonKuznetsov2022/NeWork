package ru.netology.nework.ui

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.activity.addCallback
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentNewJobBinding
import ru.netology.nework.ui.dialog.ExitPostEventDialog
import ru.netology.nework.util.AndroidUtils
import ru.netology.nework.viewmodel.EventViewModel
import ru.netology.nework.viewmodel.JobViewModel
import ru.netology.nework.viewmodel.PostViewModel
import java.text.SimpleDateFormat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class NewJobFragment : Fragment() {

    lateinit var binding: FragmentNewJobBinding
    private val jobViewModel: JobViewModel by activityViewModels()
    private val postViewModel: PostViewModel by activityViewModels()
    private val eventViewModel: EventViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewJobBinding.inflate(inflater, container, false)
        super.onCreate(savedInstanceState)

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.add, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.add -> {
                        if (isValidate()) {
                            jobViewModel.changeTextCompanyName(binding.companyName.text.toString())
                            jobViewModel.changeTextPosition(binding.position.text.toString())
                            jobViewModel.changeTextLink(binding.editLink.text.toString())
                            jobViewModel.save()
                            AndroidUtils.hideKeyboard(requireView())
                        }
                        true
                    }

                    else -> false
                }
        }, viewLifecycleOwner)

        jobViewModel.jobCreated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
            postViewModel.loadPosts()
            eventViewModel.loadEvents()
        }

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            val value = arguments?.getString("open")
            AndroidUtils.hideKeyboard(requireView())
            ExitPostEventDialog.newInstance(value)
                .show(parentFragmentManager, null)
        }

        bindEditEvent()
        bindStartTime()
        bindFinishTime()

        return binding.root
    }

    private fun isValidate(): Boolean {
        binding.apply {
            if (companyName.text.toString().trim().isEmpty()) {
                Snackbar.make(root, R.string.company_name_error, Snackbar.LENGTH_LONG)
                    .show()
                return false
            }
            if (position.text.toString().trim().isEmpty()) {
                Snackbar.make(root, R.string.position_error, Snackbar.LENGTH_LONG)
                    .show()
                return false
            }
            if (addStartTimeBut.text == getText(R.string.start)) {
                Snackbar.make(root, R.string.date_start_error, Snackbar.LENGTH_LONG)
                    .show()
                return false
            }
        }
        return true
    }

    private fun bindEditEvent() {
        binding.apply {
            companyName.setText(jobViewModel.edited.value?.name)
            position.setText(jobViewModel.edited.value?.position)
            editLink.setText(jobViewModel.edited.value?.link)
        }
    }

    @SuppressLint("NewApi")
    private fun bindStartTime() {
        val calendar = Calendar.getInstance()
        binding.apply {

            val formatterDate =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.uuuuuu", Locale.getDefault())
            val formatterShow = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

            val dateStart = arguments?.getString("dateStart")

            if (dateStart != null) {
                addStartTimeBut.text =
                    OffsetDateTime.parse(dateStart).toLocalDateTime()
                        .format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
            }

            addStartTimeBut.setOnClickListener {
                AndroidUtils.hideKeyboard(requireView())
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
                            addStartTimeBut.text =
                                formatterShow.format(calendar.timeInMillis)
                            jobViewModel.startDate(
                                formatterDate.format(calendar.timeInMillis)
                            )
                        }
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }
    }

    @SuppressLint("NewApi")
    private fun bindFinishTime() {
        val calendar = Calendar.getInstance()
        binding.apply {

            val formatterDate =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.uuuuuu", Locale.getDefault())
            val formatterShow = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

            val dateFinish = arguments?.getString("dateFinish")

            if (dateFinish != null) {
                addFinishTimeBut.text =
                    OffsetDateTime.parse(dateFinish).toLocalDateTime()
                        .format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
                deleteDataFinish.isVisible = true
            }

            addFinishTimeBut.setOnClickListener {
                AndroidUtils.hideKeyboard(requireView())
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
                            addFinishTimeBut.text =
                                formatterShow.format(calendar.timeInMillis)
                            jobViewModel.finishDate(
                                formatterDate.format(calendar.timeInMillis)
                            )
                            deleteDataFinish.isVisible = true
                        }
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }

            deleteDataFinish.setOnClickListener {
                jobViewModel.finishDate(null)
                addFinishTimeBut.text = getText(R.string.finish)
                deleteDataFinish.isVisible = false
            }
        }
    }
}