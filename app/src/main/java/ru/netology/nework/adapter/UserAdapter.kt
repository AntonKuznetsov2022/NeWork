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
    private val onInteractionListenerUser: OnInteractionListenerUser
) : ListAdapter<User, UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding =
            CardUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding, onInteractionListenerUser)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val place = getItem(position)
        holder.bind(place)
    }
}

class UserViewHolder(
    private val binding: CardUserBinding,
    private val onInteractionListenerUser: OnInteractionListenerUser,
) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(user: User) {
        binding.apply {
            val urlAvatars = "${user.avatar}"
            avatar.loadCircleCrop(urlAvatars)
            userName.text = user.name

            checkUser.isVisible = true
            checkUser.isChecked = user.isSelected

            checkUser.setOnClickListener {
                onInteractionListenerUser.onClick(user)
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