package com.privacy.sms.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.privacy.sms.databinding.ActivityArchiveBinding
import com.privacy.sms.repository.SmsRepository
import com.privacy.sms.ui.adapter.ConversationAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ArchiveActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityArchiveBinding
    private lateinit var repository: SmsRepository
    private lateinit var conversationAdapter: ConversationAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArchiveBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        
        repository = SmsRepository(this)
        
        setupRecyclerView()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Archived Conversations"
        }
    }
    
    private fun setupRecyclerView() {
        conversationAdapter = ConversationAdapter(
            onConversationClick = { conversation ->
                val intent = Intent(this, ConversationActivity::class.java)
                intent.putExtra("threadId", conversation.threadId)
                intent.putExtra("address", conversation.address)
                startActivity(intent)
            },
            onConversationLongClick = { conversation ->
                showUnarchiveDialog(conversation.threadId)
            }
        )
        
        binding.recyclerViewArchive.apply {
            layoutManager = LinearLayoutManager(this@ArchiveActivity)
            adapter = conversationAdapter
        }
        
        lifecycleScope.launch {
            repository.getArchivedConversations().collectLatest { conversations ->
                conversationAdapter.submitList(conversations)
                
                if (conversations.isEmpty()) {
                    binding.textEmptyArchive.visibility = android.view.View.VISIBLE
                    binding.recyclerViewArchive.visibility = android.view.View.GONE
                } else {
                    binding.textEmptyArchive.visibility = android.view.View.GONE
                    binding.recyclerViewArchive.visibility = android.view.View.VISIBLE
                }
            }
        }
    }
    
    private fun showUnarchiveDialog(threadId: Long) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Unarchive Conversation")
            .setMessage("Do you want to restore this conversation?")
            .setPositiveButton("Unarchive") { _, _ ->
                lifecycleScope.launch {
                    repository.unarchiveConversation(threadId)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
