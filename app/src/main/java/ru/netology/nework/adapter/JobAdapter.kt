package ru.netology.nework.adapter

import android.annotation.SuppressLint
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.R
import ru.netology.nework.databinding.CardJobBinding
import ru.netology.nework.dto.Job
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter


interface OnInteractionListenerJob {
    fun onEdit(job: Job) {}
    fun onRemove(job: Job) {}
}

class JobAdapter(
    private val onInteractionListenerJob: OnInteractionListenerJob
) : ListAdapter<Job, JobViewHolder>(JobDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding =
            CardJobBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(binding, onInteractionListenerJob)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val place = getItem(position)
        holder.bind(place)
    }
}

class JobViewHolder(
    private val binding: CardJobBinding,
    private val onInteractionListenerJob: OnInteractionListenerJob,
) :
    RecyclerView.ViewHolder(binding.root) {
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    fun bind(job: Job) {
        binding.apply {

            val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
            val dateStart = OffsetDateTime.parse(job.start).toLocalDateTime().format(formatter)

            position.text = job.position
            nameCompany.text = job.name
            dateWork.text =
                if (job.finish != null) ("$dateStart - ${
                    OffsetDateTime.parse(job.finish).toLocalDateTime().format(formatter)
                }") else ("$dateStart - " + root.context.getString(
                    R.string.until_now
                ))
            link.isVisible = job.link != null
            link.text = job.link
            menu.isVisible = job.ownedByMe

            menu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_menu)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListenerJob.onRemove(job)
                                true
                            }

                            R.id.edit -> {
                                onInteractionListenerJob.onEdit(job)
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

class JobDiffCallback : DiffUtil.ItemCallback<Job>() {
    override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean {
        return oldItem == newItem
    }
}