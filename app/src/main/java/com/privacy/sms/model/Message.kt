package com.privacy.sms.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "messages",
    indices = [Index(value = ["threadId", "date"])]
)
data class Message(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val threadId: Long,
    val address: String,
    val body: String,
    val date: Long,
    val type: Int, // 1 = received, 2 = sent
    val read: Boolean = false,
    val isArchived: Boolean = false,
    val archiveUntil: Long? = null, // Timestamp when message should be un-archived
    val isEmergencyBypass: Boolean = false
)
