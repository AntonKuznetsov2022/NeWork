package ru.netology.nework.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import ru.netology.nework.R
import ru.netology.nework.adapter.JobAdapter
import ru.netology.nework.adapter.OnInteractionListenerJob
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentProfileBinding
import ru.netology.nework.dto.Job
import ru.netology.nework.util.loadCircleCrop
import ru.netology.nework.viewmodel.JobViewModel
import ru.netology.nework.viewmodel.UserViewModel
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class ProfileFragment : Fragment() {

    lateinit var binding: FragmentProfileBinding
    private val userViewModel: UserViewModel by activityViewModels()
    private val jobViewModel: JobViewModel by activityViewModels()

    @Inject
    lateinit var appAuth: AppAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        val jobAdapter = JobAdapter(object : OnInteractionListenerJob {
            override fun onEdit(job: Job) {
                jobViewModel.editJob(job)
                val bundle = Bundle().apply {
                    putString("dateStart", job.start)
                    putString("dateFinish", job.finish)
                }
                findNavController().navigate(R.id.action_profileFragment_to_newJobFragment, bundle)
            }

            override fun onRemove(job: Job) {
                jobViewModel.removeById(job.id)
            }
        })

        val userId = arguments?.getInt("userId")
        if (userId != null) {
            jobViewModel.userId.value = userId
            jobViewModel.getJobById(userId)
            userViewModel.getUserById(userId)
            binding.fab.isVisible = appAuth.getId() == userId
        }

        userViewModel.user.observe(viewLifecycleOwner) { user ->
            binding.apply {
                val urlAvatars = "${user.avatar}"
                avatar.loadCircleCrop(urlAvatars)
                userName.text = user.name
                login.text = user.login
            }
        }

        binding.jobList.adapter = jobAdapter

        lifecycleScope.launchWhenCreated {
            jobViewModel.data.collectLatest {
                jobAdapter.submitList(it)
            }
        }

        jobViewModel.stateJob.observe(viewLifecycleOwner) { state ->
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) {
                        if (userId != null) {
                            jobViewModel.getJobById(userId)
                        }
                    }
                    .show()
            }
            binding.loadJob.isVisible = state.loading
        }

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_newJobFragment)
        }

        return binding.root
    }
}