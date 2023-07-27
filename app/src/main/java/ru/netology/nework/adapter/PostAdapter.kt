package ru.netology.nework.adapter

import android.annotation.SuppressLint
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.R
import ru.netology.nework.databinding.CardPostBinding
import ru.netology.nework.dto.AttachmentType
import ru.netology.nework.dto.FeedItem
import ru.netology.nework.dto.Post
import ru.netology.nework.util.load
import ru.netology.nework.util.loadCircleCrop
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

interface OnInteractionListener {
    fun onLike(post: Post) {}
    fun onShare(post: Post) {}
    fun onEdit(post: Post) {}
    fun onRemove(post: Post) {}
    fun onPicture(post: Post) {}
    fun onCoords(post: Post) {}
    fun onProfile(post: Post) {}
}


class PostAdapter(
    private val onInteractionListener: OnInteractionListener,
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(PostDiffCallback()) {

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is Post -> R.layout.card_post
            null -> error(R.string.unknown_item_type)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            R.layout.card_post -> {
                val binding =
                    CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                PostViewHolder(binding, onInteractionListener)
            }

            else -> error("${R.string.unknown_view_type}: $viewType")
        }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is Post -> (holder as? PostViewHolder)?.bind(item)
            null -> error(R.string.unknown_item_type)
        }
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {
    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    fun bind(post: Post) {
        binding.apply {

            val urlAvatars = "${post.authorAvatar}"
            avatar.loadCircleCrop(urlAvatars)

            author.text = post.author
            job.text = post.authorJob

            val publishedTime = OffsetDateTime.parse(post.published).toLocalDateTime()
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy в HH:mm")
            published.text = publishedTime.format(formatter)
            content.text =
                if (post.link != null) (post.content + "\n" + post.link) else post.content
            like.isChecked = post.likedByMe
            like.text = "${countText(post.likeOwnerIds.size)}"

            menu.isVisible = post.ownedByMe

            like.setOnClickListener {
                onInteractionListener.onLike(post)
            }

            share.setOnClickListener {
                onInteractionListener.onShare(post)
            }

            coords.isVisible = post.coords != null

            coords.setOnClickListener {
                onInteractionListener.onCoords(post)
            }

            if (post.attachment?.type == AttachmentType.IMAGE) {
                postImage.visibility = View.VISIBLE
                postImage.load(post.attachment.url)
            } else {
                postImage.visibility = View.GONE
            }

            postImage.setOnClickListener {
                onInteractionListener.onPicture(post)
            }

            avatar.setOnClickListener {
                onInteractionListener.onProfile(post)
            }

            author.setOnClickListener {
                onInteractionListener.onProfile(post)
            }

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_menu)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }

                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
                                true
                            }

                            else -> false
                        }
                    }
                }.show()
            }
        }
    }
}

fun countText(count: Int) = when (count) {
    in 999 downTo 0 -> count
    in 9999 downTo 1000 -> "${count / 1000}.${count % 1000 / 100}K"
    in 999_999 downTo 10_000 -> "${count / 1000}K"
    else -> "${count / 1_000_000}.${count % 1_000_000 / 100_000}M"
}

class PostDiffCallback : DiffUtil.ItemCallback<FeedItem>() {
    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        if (oldItem::class != newItem::class) {
            return false
        }
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        return oldItem == newItem
    }
}