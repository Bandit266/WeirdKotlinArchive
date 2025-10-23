package com.privacy.sms.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class Conversation(
    @PrimaryKey(autoGenerate = true)
    val threadId: Long = 0,
    val address: String,
    val contactName: String?,
    val snippet: String,
    val date: Long,
    val messageCount: Int,
    val unreadCount: Int = 0,
    val isArchived: Boolean = false,
    val archiveUntil: Long? = null, // Timestamp when conversation should be un-archived
    val isPinned: Boolean = false
)
