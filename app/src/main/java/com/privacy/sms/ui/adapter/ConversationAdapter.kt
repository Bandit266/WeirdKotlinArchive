package com.privacy.sms.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.privacy.sms.databinding.ItemConversationBinding
import com.privacy.sms.model.Conversation
import java.text.SimpleDateFormat
import java.util.*

class ConversationAdapter(
    private val onConversationClick: (Conversation) -> Unit,
    private val onConversationLongClick: (Conversation) -> Unit
) : ListAdapter<Conversation, ConversationAdapter.ConversationViewHolder>(ConversationDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val binding = ItemConversationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ConversationViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class ConversationViewHolder(
        private val binding: ItemConversationBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(conversation: Conversation) {
            binding.apply {
                textContactName.text = conversation.contactName ?: conversation.address
                textSnippet.text = conversation.snippet
                textDate.text = formatDate(conversation.date)
                
                if (conversation.unreadCount > 0) {
                    textUnreadCount.visibility = android.view.View.VISIBLE
                    textUnreadCount.text = conversation.unreadCount.toString()
                } else {
                    textUnreadCount.visibility = android.view.View.GONE
                }
                
                root.setOnClickListener { onConversationClick(conversation) }
                root.setOnLongClickListener {
                    onConversationLongClick(conversation)
                    true
                }
            }
        }
        
        private fun formatDate(timestamp: Long): String {
            val now = Calendar.getInstance()
            val date = Calendar.getInstance().apply { timeInMillis = timestamp }
            
            return when {
                now.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR) &&
                        now.get(Calendar.YEAR) == date.get(Calendar.YEAR) -> {
                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
                }
                now.get(Calendar.WEEK_OF_YEAR) == date.get(Calendar.WEEK_OF_YEAR) &&
                        now.get(Calendar.YEAR) == date.get(Calendar.YEAR) -> {
                    SimpleDateFormat("EEE", Locale.getDefault()).format(Date(timestamp))
                }
                else -> {
                    SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(timestamp))
                }
            }
        }
    }
    
    class ConversationDiffCallback : DiffUtil.ItemCallback<Conversation>() {
        override fun areItemsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
            return oldItem.threadId == newItem.threadId
        }
        
        override fun areContentsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
            return oldItem == newItem
        }
    }
}
