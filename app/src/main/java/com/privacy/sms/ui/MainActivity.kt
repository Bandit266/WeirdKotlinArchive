package com.privacy.sms.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Telephony
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.privacy.sms.R
import com.privacy.sms.databinding.ActivityMainBinding
import com.privacy.sms.repository.SmsRepository
import com.privacy.sms.ui.adapter.ConversationAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: SmsRepository
    private lateinit var conversationAdapter: ConversationAdapter
    
    companion object {
        private const val REQUEST_SMS_PERMISSIONS = 100
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_CONTACTS
        )
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setSupportActionBar(binding.toolbar)
        
        repository = SmsRepository(this)
        
        setupRecyclerView()
        checkPermissions()
        checkDefaultSmsApp()
        
        binding.fabNewMessage.setOnClickListener {
            // Open new message dialog or activity
            startActivity(Intent(this, ConversationActivity::class.java))
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
                showConversationOptions(conversation.threadId)
            }
        )
        
        binding.recyclerViewConversations.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = conversationAdapter
        }
        
        // Observe conversations
        lifecycleScope.launch {
            repository.getAllConversations().collectLatest { conversations ->
                conversationAdapter.submitList(conversations)
            }
        }
    }
    
    private fun showConversationOptions(threadId: Long) {
        val options = arrayOf(
            "Archive for 1 hour",
            "Archive for 6 hours",
            "Archive for 24 hours",
            "Archive for 7 days",
            "Delete conversation"
        )
        
        MaterialAlertDialogBuilder(this)
            .setTitle("Conversation Options")
            .setItems(options) { _, which ->
                lifecycleScope.launch {
                    when (which) {
                        0 -> repository.archiveConversation(threadId, 60)
                        1 -> repository.archiveConversation(threadId, 360)
                        2 -> repository.archiveConversation(threadId, 1440)
                        3 -> repository.archiveConversation(threadId, 10080)
                        4 -> {
                            val conversation = repository.getConversationById(threadId)
                            conversation?.let { repository.deleteConversation(it) }
                        }
                    }
                }
            }
            .show()
    }
    
    private fun checkPermissions() {
        val missingPermissions = REQUIRED_PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                missingPermissions.toTypedArray(),
                REQUEST_SMS_PERMISSIONS
            )
        }
    }
    
    private fun checkDefaultSmsApp() {
        val defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(this)
        if (defaultSmsPackage != packageName) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Set as Default SMS App")
                .setMessage("PrivacySMS needs to be your default SMS app to work properly.")
                .setPositiveButton("Set Default") { _, _ ->
                    val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                    intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
                    startActivity(intent)
                }
                .setNegativeButton("Later", null)
                .show()
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_archive -> {
                startActivity(Intent(this, ArchiveActivity::class.java))
                true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
