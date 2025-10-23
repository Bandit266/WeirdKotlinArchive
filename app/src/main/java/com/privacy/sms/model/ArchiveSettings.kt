package com.privacy.sms.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "archive_settings")
data class ArchiveSettings(
    @PrimaryKey
    val id: Int = 1, // Single row for global settings
    val emergencyKeyword: String = "", // Encrypted emergency bypass keyword
    val isEmergencyBypassEnabled: Boolean = false,
    val defaultArchiveDurationMinutes: Int = 60 // Default 1 hour
)
