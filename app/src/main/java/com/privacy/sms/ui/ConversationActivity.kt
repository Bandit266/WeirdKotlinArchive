package com.privacy.sms.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.privacy.sms.databinding.ActivityConversationBinding
import com.privacy.sms.repository.SmsRepository
import com.privacy.sms.ui.adapter.MessageAdapter
import com.privacy.sms.util.SmsManagerHelper
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ConversationActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityConversationBinding
    private lateinit var repository: SmsRepository
    private lateinit var smsManager: SmsManagerHelper
    private lateinit var messageAdapter: MessageAdapter
    
    private var threadId: Long = 0
    private var address: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConversationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        repository = SmsRepository(this)
        smsManager = SmsManagerHelper(this)
        
        threadId = intent.getLongExtra("threadId", 0)
        address = intent.getStringExtra("address") ?: ""
        
        setupToolbar()
        setupRecyclerView()
        setupMessageInput()
        
        if (threadId != 0L) {
            lifecycleScope.launch {
                repository.markConversationAsRead(threadId)
            }
        }
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = address
        }
    }
    
    private fun setupRecyclerView() {
        messageAdapter = MessageAdapter()
        
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        
        binding.recyclerViewMessages.apply {
            this.layoutManager = layoutManager
            adapter = messageAdapter
        }
        
        if (threadId != 0L) {
            lifecycleScope.launch {
                repository.getMessagesForThread(threadId).collectLatest { messages ->
                    // Decrypt messages before displaying
                    val decryptedMessages = messages.map { message ->
                        repository.getDecryptedMessage(message)
                    }
                    messageAdapter.submitList(decryptedMessages)
                    binding.recyclerViewMessages.scrollToPosition(decryptedMessages.size - 1)
                }
            }
        }
    }
    
    private fun setupMessageInput() {
        binding.buttonSend.setOnClickListener {
            val messageBody = binding.editTextMessage.text.toString().trim()
            
            if (messageBody.isNotEmpty() && address.isNotEmpty()) {
                sendMessage(messageBody)
            }
        }
    }
    
    private fun sendMessage(body: String) {
        lifecycleScope.launch {
            val success = smsManager.sendSms(address, body)
            
            if (success) {
                binding.editTextMessage.text?.clear()
                Snackbar.make(binding.root, "Message sent", Snackbar.LENGTH_SHORT).show()
            } else {
                Snackbar.make(binding.root, "Failed to send message", Snackbar.LENGTH_LONG).show()
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
