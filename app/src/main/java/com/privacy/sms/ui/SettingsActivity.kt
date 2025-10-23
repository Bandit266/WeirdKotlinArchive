package com.privacy.sms.ui

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.privacy.sms.R
import com.privacy.sms.databinding.ActivitySettingsBinding
import com.privacy.sms.util.AnimationHelper
import java.util.concurrent.TimeUnit

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var animationHelper: AnimationHelper
    private var appStartTime: Long = 0L
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        animationHelper = AnimationHelper(this)
        
        // Record app start time if not already recorded
        val prefs = getSharedPreferences("app_stats", MODE_PRIVATE)
        appStartTime = prefs.getLong("app_start_time", 0L)
        if (appStartTime == 0L) {
            appStartTime = System.currentTimeMillis()
            prefs.edit().putLong("app_start_time", appStartTime).apply()
        }
        
        setupUI()
        setupClickListeners()
        loadSettings()
        updateStatistics()
    }
    
    private fun setupUI() {
        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.settings)
        }
        
        // Animate background
        animationHelper.startLiquidAnimation(binding.animatedBackground)
        
        // Pulse statistics cards
        animationHelper.glowPulse(binding.statsCard)
    }
    
    private fun setupClickListeners() {
        // Dark Mode Switch
        binding.darkModeSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            animationHelper.pulseView(buttonView)
            toggleDarkMode(isChecked)
        }
        
        // Push Notifications Switch
        binding.pushNotificationsSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            animationHelper.pulseView(buttonView)
            togglePushNotifications(isChecked)
        }
        
        // Mute Notifications Switch
        binding.muteNotificationsSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            animationHelper.pulseView(buttonView)
            toggleMuteNotifications(isChecked)
        }
        
        // Security Settings Cards
        binding.changePinCard.setOnClickListener {
            animationHelper.bounceView(it)
            showChangePinDialog()
        }
        
        binding.changePatternCard.setOnClickListener {
            animationHelper.bounceView(it)
            showChangePatternDialog()
        }
        
        binding.biometricSettingsCard.setOnClickListener {
            animationHelper.bounceView(it)
            showBiometricSettings()
        }
        
        // Reset Statistics
        binding.resetStatsButton.setOnClickListener {
            animationHelper.bounceView(it)
            resetStatistics()
        }
        
        // About Card
        binding.aboutCard.setOnClickListener {
            animationHelper.bounceView(it)
            showAboutDialog()
        }
        
        // Privacy Policy Card
        binding.privacyCard.setOnClickListener {
            animationHelper.bounceView(it)
            showPrivacyPolicy()
        }
        
        // Clear Data Card
        binding.clearDataCard.setOnClickListener {
            animationHelper.bounceView(it)
            showClearDataDialog()
        }
    }
    
    private fun loadSettings() {
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        
        // Load current settings
        val isDarkMode = prefs.getBoolean("dark_mode", true)
        val pushNotifications = prefs.getBoolean("push_notifications", true)
        val muteNotifications = prefs.getBoolean("mute_notifications", false)
        
        binding.darkModeSwitch.isChecked = isDarkMode
        binding.pushNotificationsSwitch.isChecked = pushNotifications
        binding.muteNotificationsSwitch.isChecked = muteNotifications
    }
    
    private fun updateStatistics() {
        // Calculate uptime
        val currentTime = System.currentTimeMillis()
        val uptime = currentTime - appStartTime
        val hours = TimeUnit.MILLISECONDS.toHours(uptime)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(uptime) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(uptime) % 60
        
        val uptimeText = when {
            hours > 0 -> String.format("%d hours, %d minutes", hours, minutes)
            minutes > 0 -> String.format("%d minutes, %d seconds", minutes, seconds)
            else -> String.format("%d seconds", seconds)
        }
        
        binding.uptimeValue.text = uptimeText
        
        // Get SMS blocked count
        val prefs = getSharedPreferences("app_stats", MODE_PRIVATE)
        val smsBlocked = prefs.getInt("sms_blocked", 0)
        binding.smsBlockedValue.text = smsBlocked.toString()
        
        // Update last sync time
        binding.lastSyncValue.text = "Just now"
        
        // Animate the values
        animationHelper.fadeInOut(binding.uptimeValue)
        animationHelper.fadeInOut(binding.smsBlockedValue)
    }
    
    private fun toggleDarkMode(enabled: Boolean) {
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        prefs.edit().putBoolean("dark_mode", enabled).apply()
        
        // Apply theme change
        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
    
    private fun togglePushNotifications(enabled: Boolean) {
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        prefs.edit().putBoolean("push_notifications", enabled).apply()
        
        // If disabling notifications, also disable mute
        if (!enabled) {
            binding.muteNotificationsSwitch.isChecked = false
            binding.muteNotificationsSwitch.isEnabled = false
        } else {
            binding.muteNotificationsSwitch.isEnabled = true
        }
    }
    
    private fun toggleMuteNotifications(muted: Boolean) {
        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        prefs.edit().putBoolean("mute_notifications", muted).apply()
    }
    
    private fun showChangePinDialog() {
        val intent = Intent(this, ChangePinActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
    
    private fun showChangePatternDialog() {
        val intent = Intent(this, ChangePatternActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
    
    private fun showBiometricSettings() {
        MaterialAlertDialogBuilder(this, R.style.CustomAlertDialog)
            .setTitle("Biometric Settings")
            .setMessage("Configure your biometric authentication preferences")
            .setPositiveButton("Enable") { _, _ ->
                // Enable biometric
                val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
                prefs.edit().putBoolean("biometric_enabled", true).apply()
            }
            .setNegativeButton("Disable") { _, _ ->
                // Disable biometric
                val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
                prefs.edit().putBoolean("biometric_enabled", false).apply()
            }
            .setNeutralButton("Cancel", null)
            .show()
    }
    
    private fun resetStatistics() {
        MaterialAlertDialogBuilder(this, R.style.CustomAlertDialog)
            .setTitle("Reset Statistics")
            .setMessage("Are you sure you want to reset all statistics?")
            .setPositiveButton("Reset") { _, _ ->
                val prefs = getSharedPreferences("app_stats", MODE_PRIVATE)
                prefs.edit().apply {
                    putLong("app_start_time", System.currentTimeMillis())
                    putInt("sms_blocked", 0)
                    apply()
                }
                updateStatistics()
                animationHelper.shakeView(binding.statsCard)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showAboutDialog() {
        MaterialAlertDialogBuilder(this, R.style.CustomAlertDialog)
            .setTitle("About PrivacySMS")
            .setMessage("Version 1.0.0\n\nSecure and encrypted SMS messaging application with advanced privacy features.\n\n© 2024 PrivacySMS")
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showPrivacyPolicy() {
        MaterialAlertDialogBuilder(this, R.style.CustomAlertDialog)
            .setTitle("Privacy Policy")
            .setMessage("PrivacySMS is committed to protecting your privacy.\n\n• All messages are encrypted locally\n• No data is shared with third parties\n• You have full control over your data\n• Messages can be auto-deleted\n• Complete anonymity is maintained")
            .setPositiveButton("I Understand", null)
            .show()
    }
    
    private fun showClearDataDialog() {
        MaterialAlertDialogBuilder(this, R.style.CustomAlertDialog)
            .setTitle("Clear All Data")
            .setMessage("Warning: This will permanently delete all messages, settings, and profile data. This action cannot be undone.")
            .setPositiveButton("Clear All") { _, _ ->
                clearAllData()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun clearAllData() {
        // Clear all SharedPreferences
        listOf("app_settings", "app_stats", "profile_prefs", "auth_prefs").forEach { name ->
            getSharedPreferences(name, MODE_PRIVATE).edit().clear().apply()
        }
        
        // Navigate back to auth screen
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Update statistics when returning to settings
        updateStatistics()
    }
}