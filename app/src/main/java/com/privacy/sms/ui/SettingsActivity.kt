package com.privacy.sms.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.privacy.sms.databinding.ActivitySettingsBinding
import com.privacy.sms.model.ArchiveSettings
import com.privacy.sms.repository.SmsRepository
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var repository: SmsRepository
    private var currentSettings: ArchiveSettings? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        
        repository = SmsRepository(this)
        
        loadSettings()
        setupListeners()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Settings"
        }
    }
    
    private fun loadSettings() {
        lifecycleScope.launch {
            currentSettings = repository.getArchiveSettingsOnce()
            currentSettings?.let { settings ->
                binding.switchEmergencyBypass.isChecked = settings.isEmergencyBypassEnabled
                binding.editTextDefaultDuration.setText(settings.defaultArchiveDurationMinutes.toString())
            }
        }
    }
    
    private fun setupListeners() {
        binding.buttonSaveKeyword.setOnClickListener {
            val keyword = binding.editTextEmergencyKeyword.text.toString().trim()
            
            if (keyword.isEmpty()) {
                Snackbar.make(binding.root, "Please enter a keyword", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            lifecycleScope.launch {
                val settings = currentSettings ?: ArchiveSettings()
                val updatedSettings = settings.copy(
                    emergencyKeyword = keyword,
                    isEmergencyBypassEnabled = binding.switchEmergencyBypass.isChecked
                )
                repository.updateArchiveSettings(updatedSettings)
                currentSettings = updatedSettings
                
                binding.editTextEmergencyKeyword.text?.clear()
                Snackbar.make(binding.root, "Emergency keyword saved", Snackbar.LENGTH_SHORT).show()
            }
        }
        
        binding.buttonSaveDefaultDuration.setOnClickListener {
            val durationText = binding.editTextDefaultDuration.text.toString()
            val duration = durationText.toIntOrNull()
            
            if (duration == null || duration <= 0) {
                Snackbar.make(binding.root, "Please enter a valid duration", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            lifecycleScope.launch {
                val settings = currentSettings ?: ArchiveSettings()
                val updatedSettings = settings.copy(defaultArchiveDurationMinutes = duration)
                repository.updateArchiveSettings(updatedSettings)
                currentSettings = updatedSettings
                
                Snackbar.make(binding.root, "Default duration saved", Snackbar.LENGTH_SHORT).show()
            }
        }
        
        binding.switchEmergencyBypass.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                val settings = currentSettings ?: ArchiveSettings()
                val updatedSettings = settings.copy(isEmergencyBypassEnabled = isChecked)
                repository.updateArchiveSettings(updatedSettings)
                currentSettings = updatedSettings
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
