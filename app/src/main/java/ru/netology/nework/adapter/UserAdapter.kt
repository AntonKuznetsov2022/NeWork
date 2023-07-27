package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.databinding.CardUserBinding
import ru.netology.nework.dto.User
import ru.netology.nework.util.loadCircleCrop

interface OnInteractionListenerUser {
    fun onClick(user: User) {}
}

class UserAdapter(
    private val onInteractionListenerUser: OnInteractionListenerUser,
    private val item: Int,
) : ListAdapter<User, UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding =
            CardUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding, onInteractionListenerUser, item)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val place = getItem(position)
        holder.bind(place)
    }
}

class UserViewHolder(
    private val binding: CardUserBinding,
    private val onInteractionListenerUser: OnInteractionListenerUser,
    private val item: Int,
) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(user: User) {
        when(item) {
            0 -> {
                binding.apply {
                    val urlAvatars = "${user.avatar}"
                    avatar.loadCircleCrop(urlAvatars)
                    userName.text = user.name

                    checkUser.isChecked = user.isSelected

                    checkUser.setOnClickListener {
                        onInteractionListenerUser.onClick(user)
                    }
                }
            }
            1 -> {
                binding.apply {
                    val urlAvatars = "${user.avatar}"
                    avatar.loadCircleCrop(urlAvatars)
                    userName.text = user.name

                    checkUser.isVisible = false
                }
            }
        }

    }
}

class UserDiffCallback : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
        return oldItem == newItem
    }
}