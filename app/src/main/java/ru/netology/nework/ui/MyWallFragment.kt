package ru.netology.nework.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
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
import ru.netology.nework.adapter.OnInteractionListener
import ru.netology.nework.adapter.PostAdapter
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentMyWallBinding
import ru.netology.nework.dto.Post
import ru.netology.nework.ui.dialog.AuthDialog
import ru.netology.nework.ui.post.NewPostFragment.Companion.textArg
import ru.netology.nework.viewmodel.MyWallViewModel
import ru.netology.nework.viewmodel.PostViewModel
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class MyWallFragment : Fragment() {

    lateinit var binding: FragmentMyWallBinding

    private val viewModel: PostViewModel by activityViewModels()
    private val myWallViewModel: MyWallViewModel by activityViewModels()

    @Inject
    lateinit var appAuth: AppAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentMyWallBinding.inflate(inflater, container, false)

        val adapter = PostAdapter(object : OnInteractionListener {

            override fun onLike(post: Post) {
                if (appAuth.getToken() == null) {
                    AuthDialog()
                        .show(childFragmentManager, null)
                } else {
                    if (!post.likedByMe) {
                        viewModel.likeById(post.id)
                    } else {
                        viewModel.unlikeById(post.id)
                    }
                }
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }
                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.post_share))
                startActivity(shareIntent)
            }

            override fun onEdit(post: Post) {
                findNavController().navigate(
                    R.id.action_nav_my_wall_to_newPostFragment,
                    Bundle().apply { textArg = post.content })
                viewModel.edit(post)
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onPicture(post: Post) {
                findNavController().navigate(
                    R.id.action_global_onPictureFragment,
                    Bundle().apply { textArg = post.attachment?.url })
            }
        })

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (positionStart == 0) {
                    binding.myPosts.smoothScrollToPosition(0)
                }
            }
        })

        binding.myPosts.adapter = adapter

        lifecycleScope.launchWhenCreated {
            myWallViewModel.data.collectLatest(adapter::submitData)
        }

        //binding.emptyPostTitle.isVisible = myWallViewModel.data.count() != 0


        lifecycleScope.launchWhenCreated {
            adapter.loadStateFlow.collectLatest { state ->
                binding.swipeRefresh.isRefreshing =
                    state.refresh is LoadState.Loading ||
                            state.prepend is LoadState.Loading ||
                            state.append is LoadState.Loading
            }
        }

        binding.swipeRefresh.setOnRefreshListener(adapter::refresh)

        myWallViewModel.state.observe(viewLifecycleOwner) { state ->
            binding.swipeRefresh.isRefreshing = state.refreshing
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) {
                        myWallViewModel.loadMyPosts()
                    }
                    .show()
            }
        }

        if (appAuth.getToken() == null) {
            binding.loginTitle.isVisible = true
            binding.loginButton.isVisible = true
            binding.fab.isVisible = false
            binding.myPosts.isVisible = false
        } else {
            binding.loginTitle.isVisible = false
            binding.loginButton.isVisible = false
            binding.fab.isVisible = true
            binding.myPosts.isVisible = true
        }

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_nav_my_wall_to_newPostFragment)
        }

        binding.loginButton.setOnClickListener {
            findNavController().navigate(R.id.action_global_signInFragment)
        }

        return binding.root
    }
}