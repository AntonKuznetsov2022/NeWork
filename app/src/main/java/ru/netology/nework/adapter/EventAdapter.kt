package ru.netology.nework.adapter

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
import ru.netology.nework.databinding.EventPostBinding
import ru.netology.nework.dto.AttachmentType
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.EventItem
import ru.netology.nework.util.load
import ru.netology.nework.util.loadCircleCrop
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter


interface OnInteractionListenerEvent {
    fun onLike(event: Event) {}
    fun onShare(event: Event) {}
    fun onEdit(event: Event) {}
    fun onRemove(event: Event) {}
    fun onParticipant(event: Event) {}
    fun onSpeaker() {}
    fun onPicture(event: Event) {}
}

class EventAdapter(
    private val onInteractionListenerEvent: OnInteractionListenerEvent,
) : PagingDataAdapter<EventItem, RecyclerView.ViewHolder>(EventDiffCallback()) {

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is Event -> R.layout.event_post
            null -> error(R.string.unknown_item_type)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            R.layout.event_post -> {
                val binding =
                    EventPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                EventViewHolder(binding, onInteractionListenerEvent)
            }

            else -> error("${R.string.unknown_view_type}: $viewType")
        }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is Event -> (holder as? EventViewHolder)?.bind(item)
            null -> error(R.string.unknown_item_type)
        }
    }
}

class EventViewHolder(
    private val binding: EventPostBinding,
    private val onInteractionListenerEvent: OnInteractionListenerEvent,
) : RecyclerView.ViewHolder(binding.root) {
    @RequiresApi(Build.VERSION_CODES.O)
    fun bind(event: Event) {
        binding.apply {

            val urlAvatars = "${event.authorAvatar}"
            avatar.loadCircleCrop(urlAvatars)

            author.text = event.author
            job.text = event.authorJob

            val dateTime = OffsetDateTime.parse(event.datetime).toLocalDateTime()
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy в HH:mm")
            dateEvent.text = dateTime.format(formatter)

            type.text = event.type.toString()
            content.text =
                if (event.link != null) (event.content + "\n" + event.link) else (event.content)

            members.text = "${event.participantsIds.size}"

            members.isChecked = event.participatedByMe
            members.setOnClickListener{
                onInteractionListenerEvent.onParticipant(event)
            }

            menu.isVisible = event.ownedByMe

            like.isChecked = event.likedByMe
            like.text = "${countText(event.likeOwnerIds.size)}"
            like.setOnClickListener {
                onInteractionListenerEvent.onLike(event)
            }

            speakers.text = "${countText(event.speakerIds.size)}"

            share.setOnClickListener {
                onInteractionListenerEvent.onShare(event)
            }

            if (event.attachment?.type == AttachmentType.IMAGE) {
                eventImage.visibility = View.VISIBLE
                val urlImages = event.attachment.url
                eventImage.load(urlImages)
            } else {
                eventImage.visibility = View.GONE
            }

            eventImage.setOnClickListener {
                onInteractionListenerEvent.onPicture(event)
            }

            speakers.setOnClickListener{
                onInteractionListenerEvent.onSpeaker()
            }

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_menu)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListenerEvent.onRemove(event)
                                true
                            }

                            R.id.edit -> {
                                onInteractionListenerEvent.onEdit(event)
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

class EventDiffCallback : DiffUtil.ItemCallback<EventItem>() {
    override fun areItemsTheSame(oldItem: EventItem, newItem: EventItem): Boolean {
        if (oldItem::class != newItem::class) {
            return false
        }
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: EventItem, newItem: EventItem): Boolean {
        return oldItem == newItem
    }
}