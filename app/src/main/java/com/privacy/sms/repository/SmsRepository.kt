package com.privacy.sms.repository

import android.content.Context
import com.privacy.sms.database.PrivacySMSDatabase
import com.privacy.sms.model.Message
import com.privacy.sms.model.Conversation
import com.privacy.sms.model.ArchiveSettings
import com.privacy.sms.security.EncryptionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SmsRepository(context: Context) {
    
    private val database = PrivacySMSDatabase.getDatabase(
        context,
        PrivacySMSDatabase.generatePassphrase(context)
    )
    
    private val messageDao = database.messageDao()
    private val conversationDao = database.conversationDao()
    private val archiveSettingsDao = database.archiveSettingsDao()
    private val encryptionManager = EncryptionManager(context)
    
    // Conversation operations
    fun getAllConversations(): Flow<List<Conversation>> = conversationDao.getAllConversations()
    
    fun getArchivedConversations(): Flow<List<Conversation>> = conversationDao.getArchivedConversations()
    
    suspend fun getConversationById(threadId: Long): Conversation? {
        return withContext(Dispatchers.IO) {
            conversationDao.getConversationById(threadId)
        }
    }
    
    suspend fun insertConversation(conversation: Conversation): Long {
        return withContext(Dispatchers.IO) {
            conversationDao.insertConversation(conversation)
        }
    }
    
    suspend fun updateConversation(conversation: Conversation) {
        withContext(Dispatchers.IO) {
            conversationDao.updateConversation(conversation)
        }
    }
    
    suspend fun deleteConversation(conversation: Conversation) {
        withContext(Dispatchers.IO) {
            conversationDao.deleteConversation(conversation)
            messageDao.deleteAllMessagesInThread(conversation.threadId)
        }
    }
    
    suspend fun archiveConversation(threadId: Long, durationMinutes: Int) {
        withContext(Dispatchers.IO) {
            val archiveUntil = System.currentTimeMillis() + (durationMinutes * 60 * 1000L)
            conversationDao.archiveConversation(threadId, true, archiveUntil)
            messageDao.archiveThreadMessages(threadId, true, archiveUntil)
        }
    }
    
    suspend fun unarchiveConversation(threadId: Long) {
        withContext(Dispatchers.IO) {
            conversationDao.archiveConversation(threadId, false, null)
            messageDao.archiveThreadMessages(threadId, false, null)
        }
    }
    
    suspend fun pinConversation(threadId: Long, isPinned: Boolean) {
        withContext(Dispatchers.IO) {
            conversationDao.pinConversation(threadId, isPinned)
        }
    }
    
    suspend fun markConversationAsRead(threadId: Long) {
        withContext(Dispatchers.IO) {
            conversationDao.markConversationAsRead(threadId)
            messageDao.markThreadAsRead(threadId)
        }
    }
    
    // Message operations
    fun getMessagesForThread(threadId: Long): Flow<List<Message>> = messageDao.getMessagesForThread(threadId)
    
    fun getArchivedMessagesForThread(threadId: Long): Flow<List<Message>> = messageDao.getArchivedMessagesForThread(threadId)
    
    suspend fun insertMessage(message: Message): Long {
        return withContext(Dispatchers.IO) {
            // Encrypt message body before storing
            val encryptedMessage = message.copy(
                body = encryptionManager.encrypt(message.body)
            )
            messageDao.insertMessage(encryptedMessage)
        }
    }
    
    suspend fun getDecryptedMessage(message: Message): Message {
        return withContext(Dispatchers.IO) {
            message.copy(
                body = try {
                    encryptionManager.decrypt(message.body)
                } catch (e: Exception) {
                    message.body // Return original if decryption fails
                }
            )
        }
    }
    
    suspend fun deleteMessage(message: Message) {
        withContext(Dispatchers.IO) {
            messageDao.deleteMessage(message)
        }
    }
    
    // Archive settings operations
    fun getArchiveSettings(): Flow<ArchiveSettings?> = archiveSettingsDao.getSettings()
    
    suspend fun getArchiveSettingsOnce(): ArchiveSettings {
        return withContext(Dispatchers.IO) {
            archiveSettingsDao.getSettingsOnce() ?: ArchiveSettings().also {
                archiveSettingsDao.insertSettings(it)
            }
        }
    }
    
    suspend fun updateArchiveSettings(settings: ArchiveSettings) {
        withContext(Dispatchers.IO) {
            // Encrypt emergency keyword before storing
            val encryptedSettings = settings.copy(
                emergencyKeyword = if (settings.emergencyKeyword.isNotEmpty()) {
                    encryptionManager.hashString(settings.emergencyKeyword)
                } else {
                    ""
                }
            )
            archiveSettingsDao.updateSettings(encryptedSettings)
        }
    }
    
    suspend fun checkEmergencyKeyword(keyword: String): Boolean {
        return withContext(Dispatchers.IO) {
            val settings = getArchiveSettingsOnce()
            if (!settings.isEmergencyBypassEnabled || settings.emergencyKeyword.isEmpty()) {
                return@withContext false
            }
            
            val hashedInput = encryptionManager.hashString(keyword)
            hashedInput == settings.emergencyKeyword
        }
    }
    
    suspend fun triggerEmergencyBypass() {
        withContext(Dispatchers.IO) {
            messageDao.unarchiveAllEmergencyBypass()
        }
    }
    
    // Check and un-archive expired conversations
    suspend fun checkExpiredArchives() {
        withContext(Dispatchers.IO) {
            val currentTime = System.currentTimeMillis()
            
            val expiredConversations = conversationDao.getExpiredArchivedConversations(currentTime)
            expiredConversations.forEach { conversation ->
                unarchiveConversation(conversation.threadId)
            }
        }
    }
}
