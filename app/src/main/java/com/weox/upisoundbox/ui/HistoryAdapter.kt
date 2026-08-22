package com.weox.upisoundbox.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.weox.upisoundbox.databinding.ItemHistoryBinding
import com.weox.upisoundbox.model.PaymentHistoryItem

class HistoryAdapter :
    ListAdapter<PaymentHistoryItem, HistoryAdapter.HistoryViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HistoryViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PaymentHistoryItem) {
            binding.tvAmount.text = item.displayAmount
            binding.tvSource.text = item.sourceApp
            binding.tvTime.text = item.timeAgo
            binding.tvRaw.text = item.rawText

            binding.root.alpha = 0f
            binding.root.animate()
                .alpha(1f)
                .setDuration(300)
                .setStartDelay((adapterPosition * 50).toLong())
                .start()
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<PaymentHistoryItem>() {
        override fun areItemsTheSame(oldItem: PaymentHistoryItem, newItem: PaymentHistoryItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PaymentHistoryItem, newItem: PaymentHistoryItem): Boolean {
            return oldItem == newItem
        }
    }
}
