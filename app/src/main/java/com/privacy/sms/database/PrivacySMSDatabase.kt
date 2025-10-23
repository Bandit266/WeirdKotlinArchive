package com.privacy.sms.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.privacy.sms.model.Message
import com.privacy.sms.model.Conversation
import com.privacy.sms.model.ArchiveSettings
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [Message::class, Conversation::class, ArchiveSettings::class],
    version = 1,
    exportSchema = true
)
abstract class PrivacySMSDatabase : RoomDatabase() {
    
    abstract fun messageDao(): MessageDao
    abstract fun conversationDao(): ConversationDao
    abstract fun archiveSettingsDao(): ArchiveSettingsDao
    
    companion object {
        @Volatile
        private var INSTANCE: PrivacySMSDatabase? = null
        
        private const val DATABASE_NAME = "privacy_sms.db"
        
        fun getDatabase(context: Context, passphrase: ByteArray): PrivacySMSDatabase {
            return INSTANCE ?: synchronized(this) {
                val factory = SupportFactory(passphrase)
                
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PrivacySMSDatabase::class.java,
                    DATABASE_NAME
                )
                    .openHelperFactory(factory)
                    .fallbackToDestructiveMigration()
                    .build()
                
                INSTANCE = instance
                instance
            }
        }
        
        // Generate a secure passphrase using Android KeyStore
        fun generatePassphrase(context: Context): ByteArray {
            val prefs = context.getSharedPreferences("privacy_sms_prefs", Context.MODE_PRIVATE)
            val storedPassphrase = prefs.getString("db_passphrase", null)
            
            return if (storedPassphrase != null) {
                storedPassphrase.toByteArray(Charsets.UTF_8)
            } else {
                // Generate a new random passphrase
                val newPassphrase = java.util.UUID.randomUUID().toString()
                prefs.edit().putString("db_passphrase", newPassphrase).apply()
                newPassphrase.toByteArray(Charsets.UTF_8)
            }
        }
    }
}
