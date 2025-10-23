package com.privacy.sms.database

import androidx.room.*
import com.privacy.sms.model.Conversation
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations WHERE isArchived = 0 ORDER BY isPinned DESC, date DESC")
    fun getAllConversations(): Flow<List<Conversation>>
    
    @Query("SELECT * FROM conversations WHERE isArchived = 1 ORDER BY date DESC")
    fun getArchivedConversations(): Flow<List<Conversation>>
    
    @Query("SELECT * FROM conversations WHERE threadId = :threadId")
    suspend fun getConversationById(threadId: Long): Conversation?
    
    @Query("SELECT * FROM conversations WHERE isArchived = 1 AND archiveUntil <= :currentTime")
    suspend fun getExpiredArchivedConversations(currentTime: Long): List<Conversation>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: Conversation): Long
    
    @Update
    suspend fun updateConversation(conversation: Conversation)
    
    @Delete
    suspend fun deleteConversation(conversation: Conversation)
    
    @Query("UPDATE conversations SET unreadCount = unreadCount + 1, snippet = :snippet, date = :date WHERE threadId = :threadId")
    suspend fun incrementUnreadCount(threadId: Long, snippet: String, date: Long)
    
    @Query("UPDATE conversations SET unreadCount = 0 WHERE threadId = :threadId")
    suspend fun markConversationAsRead(threadId: Long)
    
    @Query("UPDATE conversations SET isArchived = :isArchived, archiveUntil = :archiveUntil WHERE threadId = :threadId")
    suspend fun archiveConversation(threadId: Long, isArchived: Boolean, archiveUntil: Long?)
    
    @Query("UPDATE conversations SET isPinned = :isPinned WHERE threadId = :threadId")
    suspend fun pinConversation(threadId: Long, isPinned: Boolean)
}
