package ru.netology.nework.ui.events

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import ru.netology.nework.R
import ru.netology.nework.adapter.EventAdapter
import ru.netology.nework.adapter.OnInteractionListenerEvent
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentEventsBinding
import ru.netology.nework.dto.Event
import ru.netology.nework.ui.dialog.AuthDialog
import ru.netology.nework.ui.post.NewPostFragment.Companion.textArg
import ru.netology.nework.viewmodel.EventViewModel
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class EventsFragment : Fragment() {

    lateinit var binding: FragmentEventsBinding

    private val viewModel: EventViewModel by activityViewModels()

    @Inject
    lateinit var appAuth: AppAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEventsBinding.inflate(inflater, container, false)

        val adapter = EventAdapter(object : OnInteractionListenerEvent {

            override fun onLike(event: Event) {
                if (appAuth.getToken() == null) {
                    AuthDialog()
                        .show(parentFragmentManager, null)
                } else {
                    if (!event.likedByMe) {
                        viewModel.likeById(event.id)
                    } else {
                        viewModel.unlikeById(event.id)
                    }
                }
            }

            override fun onShare(event: Event) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, event.content)
                    type = "text/plain"
                }
                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.post_share))
                startActivity(shareIntent)
            }

            override fun onEdit(event: Event) {
                /*                findNavController().navigate(
                                    R.id.action_nav_feed_to_newPostFragment,
                                    Bundle().apply { textArg = event.content })*/
                viewModel.edit(event)
            }

            override fun onRemove(event: Event) {
                viewModel.removeById(event.id)
            }

            override fun onParticipant(event: Event) {
                if (appAuth.getToken() == null) {
                    AuthDialog()
                        .show(parentFragmentManager, null)
                } else {
                    if (!event.participatedByMe) {
                        viewModel.participantById(event.id)
                    } else {
                        viewModel.unParticipantById(event.id)
                    }
                }
            }

            override fun onPicture(event: Event) {
                findNavController().navigate(
                    R.id.action_global_onPictureFragment,
                    Bundle().apply { textArg = event.attachment?.url })
            }
        })

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart == 0) {
                    binding.events.smoothScrollToPosition(0)
                }
            }
        })

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.swipeRefresh.isRefreshing = state.refreshing
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) {
                        viewModel.loadEvents()
                    }
                    .show()
            }
        }

        binding.events.adapter = adapter

        lifecycleScope.launchWhenCreated {
            viewModel.data.collectLatest(adapter::submitData)
        }

        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest { state ->
                binding.swipeRefresh.isRefreshing =
                    state.refresh is LoadState.Loading ||
                            state.prepend is LoadState.Loading ||
                            state.append is LoadState.Loading
            }
        }

        binding.swipeRefresh.setOnRefreshListener(adapter::refresh)

        binding.fab.setOnClickListener {
            if (appAuth.getToken() == null) {
                AuthDialog()
                    .show(parentFragmentManager, null)
            } else {
                findNavController().navigate(R.id.action_nav_events_to_newEventsFragment)
            }
        }

        return binding.root
    }
}