package com.privacy.sms.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import com.privacy.sms.model.Message
import com.privacy.sms.model.Conversation
import com.privacy.sms.repository.SmsRepository
import com.privacy.sms.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            
            if (messages.isNotEmpty()) {
                handleIncomingSms(context, messages)
            }
        }
    }
    
    private fun handleIncomingSms(context: Context, messages: Array<SmsMessage>) {
        val repository = SmsRepository(context)
        val notificationHelper = NotificationHelper(context)
        
        CoroutineScope(Dispatchers.IO).launch {
            for (smsMessage in messages) {
                val address = smsMessage.originatingAddress ?: "Unknown"
                val body = smsMessage.messageBody ?: ""
                val timestamp = smsMessage.timestampMillis
                
                // Check for emergency keyword
                val settings = repository.getArchiveSettingsOnce()
                if (settings.isEmergencyBypassEnabled && 
                    repository.checkEmergencyKeyword(body.trim())) {
                    // Trigger emergency bypass
                    repository.triggerEmergencyBypass()
                    notificationHelper.showEmergencyBypassNotification()
                    continue
                }
                
                // Get or create conversation
                var conversation = repository.getConversationById(address.hashCode().toLong())
                
                if (conversation == null) {
                    conversation = Conversation(
                        threadId = address.hashCode().toLong(),
                        address = address,
                        contactName = null, // Can be enhanced with contact lookup
                        snippet = body,
                        date = timestamp,
                        messageCount = 1,
                        unreadCount = 1
                    )
                    repository.insertConversation(conversation)
                } else {
                    val updatedConversation = conversation.copy(
                        snippet = body,
                        date = timestamp,
                        messageCount = conversation.messageCount + 1,
                        unreadCount = conversation.unreadCount + 1
                    )
                    repository.updateConversation(updatedConversation)
                }
                
                // Save message
                val message = Message(
                    threadId = address.hashCode().toLong(),
                    address = address,
                    body = body,
                    date = timestamp,
                    type = 1, // Received
                    read = false
                )
                repository.insertMessage(message)
                
                // Show notification if conversation is not archived
                if (!conversation.isArchived) {
                    notificationHelper.showMessageNotification(address, body, conversation.threadId)
                }
            }
        }
    }
}
