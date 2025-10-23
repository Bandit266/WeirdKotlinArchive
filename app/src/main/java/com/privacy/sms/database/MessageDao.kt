package com.privacy.sms.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.privacy.sms.model.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE threadId = :threadId AND isArchived = 0 ORDER BY date DESC")
    fun getMessagesForThread(threadId: Long): Flow<List<Message>>
    
    @Query("SELECT * FROM messages WHERE threadId = :threadId AND isArchived = 1 ORDER BY date DESC")
    fun getArchivedMessagesForThread(threadId: Long): Flow<List<Message>>
    
    @Query("SELECT * FROM messages WHERE isArchived = 1 AND archiveUntil <= :currentTime")
    suspend fun getExpiredArchivedMessages(currentTime: Long): List<Message>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<Message>)
    
    @Update
    suspend fun updateMessage(message: Message)
    
    @Delete
    suspend fun deleteMessage(message: Message)
    
    @Query("DELETE FROM messages WHERE threadId = :threadId")
    suspend fun deleteAllMessagesInThread(threadId: Long)
    
    @Query("UPDATE messages SET isArchived = :isArchived, archiveUntil = :archiveUntil WHERE threadId = :threadId")
    suspend fun archiveThreadMessages(threadId: Long, isArchived: Boolean, archiveUntil: Long?)
    
    @Query("UPDATE messages SET isArchived = 0, archiveUntil = NULL WHERE isEmergencyBypass = 1")
    suspend fun unarchiveAllEmergencyBypass()
    
    @Query("UPDATE messages SET read = 1 WHERE threadId = :threadId")
    suspend fun markThreadAsRead(threadId: Long)
}
