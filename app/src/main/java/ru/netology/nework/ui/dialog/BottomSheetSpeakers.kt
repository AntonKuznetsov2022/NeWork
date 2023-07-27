package ru.netology.nework.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import ru.netology.nework.adapter.OnInteractionListenerUser
import ru.netology.nework.adapter.UserAdapter
import ru.netology.nework.databinding.FragmentUsersBinding
import ru.netology.nework.viewmodel.EventViewModel
import ru.netology.nework.viewmodel.UserViewModel


@AndroidEntryPoint
@ExperimentalCoroutinesApi
class BottomSheetSpeakers : BottomSheetDialogFragment() {

    lateinit var binding: FragmentUsersBinding

    private val userViewModel: UserViewModel by activityViewModels()
    private val eventViewModel: EventViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUsersBinding.inflate(inflater, container, false)

        val adapter = UserAdapter(object : OnInteractionListenerUser {}, 1)

        binding.usersList.adapter = adapter

        lifecycleScope.launchWhenCreated {
            userViewModel.data.collectLatest {
                adapter.submitList(it.filter { user ->
                    eventViewModel.edited.value?.speakerIds!!.contains(user.id)
                })
            }
        }

        return binding.root
    }
}