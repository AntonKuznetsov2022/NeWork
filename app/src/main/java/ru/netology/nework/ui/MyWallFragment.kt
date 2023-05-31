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
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import ru.netology.nework.R
import ru.netology.nework.adapter.OnInteractionListener
import ru.netology.nework.adapter.PostAdapter
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.databinding.FragmentMywallBinding
import ru.netology.nework.dto.Post
import ru.netology.nework.ui.dialog.AuthDialog
import ru.netology.nework.viewmodel.PostViewModel
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class MyWallFragment : Fragment() {

    lateinit var binding: FragmentMywallBinding

    private val viewModel: PostViewModel by activityViewModels()

    @Inject
    lateinit var appAuth: AppAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentMywallBinding.inflate(inflater, container, false)

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
        })

        binding.myPosts.adapter = adapter
        lifecycleScope.launchWhenCreated {
            viewModel.data.collectLatest {
                adapter.submitData(it)
            }
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            if (state.error) {
                Snackbar.make(binding.root, R.string.error_loading, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry_loading) {
                        viewModel.loadPosts()
                    }
                    .show()
            }
        }

        if (appAuth.getToken() != null) {
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