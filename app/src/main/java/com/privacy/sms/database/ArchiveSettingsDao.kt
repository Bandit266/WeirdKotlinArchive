package com.privacy.sms.database

import androidx.room.*
import com.privacy.sms.model.ArchiveSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface ArchiveSettingsDao {
    @Query("SELECT * FROM archive_settings WHERE id = 1")
    fun getSettings(): Flow<ArchiveSettings?>
    
    @Query("SELECT * FROM archive_settings WHERE id = 1")
    suspend fun getSettingsOnce(): ArchiveSettings?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: ArchiveSettings)
    
    @Update
    suspend fun updateSettings(settings: ArchiveSettings)
}
