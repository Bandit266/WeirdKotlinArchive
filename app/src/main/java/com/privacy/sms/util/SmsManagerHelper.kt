package com.privacy.sms.util

import android.content.Context
import android.telephony.SmsManager
import com.privacy.sms.model.Message
import com.privacy.sms.repository.SmsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SmsManagerHelper(private val context: Context) {
    
    private val smsManager = context.getSystemService(SmsManager::class.java)
    private val repository = SmsRepository(context)
    
    suspend fun sendSms(address: String, body: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                smsManager.sendTextMessage(address, null, body, null, null)
                
                // Save sent message to database
                val message = Message(
                    threadId = address.hashCode().toLong(),
                    address = address,
                    body = body,
                    date = System.currentTimeMillis(),
                    type = 2, // Sent
                    read = true
                )
                repository.insertMessage(message)
                
                // Update conversation
                val conversation = repository.getConversationById(address.hashCode().toLong())
                if (conversation != null) {
                    val updatedConversation = conversation.copy(
                        snippet = body,
                        date = System.currentTimeMillis(),
                        messageCount = conversation.messageCount + 1
                    )
                    repository.updateConversation(updatedConversation)
                }
                
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}
