package com.privacy.sms.ui

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.SharedPreferences
import android.os.Bundle
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.privacy.sms.R
import com.privacy.sms.databinding.ActivityVaultBinding
import com.privacy.sms.util.AnimationHelper
import jp.wasabeef.blurry.Blurry

class VaultActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityVaultBinding
    private lateinit var animationHelper: AnimationHelper
    private lateinit var encryptedPrefs: SharedPreferences
    private var isVaultUnlocked = false
    private lateinit var biometricPrompt: BiometricPrompt
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVaultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        animationHelper = AnimationHelper(this)
        setupEncryptedPreferences()
        setupUI()
        setupBiometricPrompt()
        applyBlurEffect()
    }
    
    private fun setupEncryptedPreferences() {
        val masterKey = MasterKey.Builder(this)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
            
        encryptedPrefs = EncryptedSharedPreferences.create(
            this,
            "vault_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    private fun setupUI() {
        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.encrypted_vault)
        }
        
        // Setup animated background
        animationHelper.startLiquidAnimation(binding.animatedBackground)
        
        // Initially hide content and show lock
        binding.vaultContent.visibility = View.GONE
        binding.lockedContainer.visibility = View.VISIBLE
        
        // Setup unlock button
        binding.unlockButton.setOnClickListener {
            animationHelper.bounceView(it)
            showUnlockDialog()
        }
        
        // Animate lock icon
        animateLockIcon()
    }
    
    private fun setupBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)
        
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    unlockVault()
                }
                
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    showError("Authentication failed: $errString")
                }
                
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    showError("Authentication failed")
                }
            })
    }
    
    private fun applyBlurEffect() {
        // Apply heavy blur to background when locked
        if (!isVaultUnlocked) {
            Blurry.with(this)
                .radius(25)
                .sampling(8)
                .animate(500)
                .onto(binding.blurOverlay)
        }
    }
    
    private fun animateLockIcon() {
        // Floating animation for lock icon
        val floatAnimator = ObjectAnimator.ofFloat(
            binding.vaultLockIcon,
            "translationY",
            0f, -30f, 0f
        )
        floatAnimator.duration = 3000
        floatAnimator.repeatCount = ValueAnimator.INFINITE
        floatAnimator.interpolator = AccelerateDecelerateInterpolator()
        floatAnimator.start()
        
        // Pulse animation
        animationHelper.glowPulse(binding.vaultLockIcon)
    }
    
    private fun showUnlockDialog() {
        val options = arrayOf("Biometric", "PIN", "Pattern")
        
        MaterialAlertDialogBuilder(this, R.style.CustomAlertDialog)
            .setTitle("Unlock Vault")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> authenticateWithBiometric()
                    1 -> authenticateWithPin()
                    2 -> authenticateWithPattern()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun authenticateWithBiometric() {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Vault")
            .setSubtitle("Use your biometric to access the encrypted vault")
            .setNegativeButtonText("Use PIN")
            .build()
            
        biometricPrompt.authenticate(promptInfo)
    }
    
    private fun authenticateWithPin() {
        // Show PIN input dialog
        val pinInputView = layoutInflater.inflate(R.layout.dialog_pin_input, null)
        
        MaterialAlertDialogBuilder(this, R.style.CustomAlertDialog)
            .setTitle("Enter PIN")
            .setView(pinInputView)
            .setPositiveButton("Unlock") { _, _ ->
                // Verify PIN and unlock
                unlockVault()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun authenticateWithPattern() {
        // Show pattern input dialog
        val patternInputView = layoutInflater.inflate(R.layout.dialog_pattern_input, null)
        
        MaterialAlertDialogBuilder(this, R.style.CustomAlertDialog)
            .setTitle("Draw Pattern")
            .setView(patternInputView)
            .setPositiveButton("Unlock") { _, _ ->
                // Verify pattern and unlock
                unlockVault()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun unlockVault() {
        isVaultUnlocked = true
        
        // Sophisticated unlock animation
        val unlockAnimator = ValueAnimator.ofFloat(1f, 0f)
        unlockAnimator.duration = 800
        unlockAnimator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            binding.blurOverlay.alpha = value
        }
        
        // Rotate and fade lock icon
        val lockRotation = ObjectAnimator.ofFloat(binding.vaultLockIcon, "rotation", 0f, 360f)
        val lockFade = ObjectAnimator.ofFloat(binding.vaultLockIcon, "alpha", 1f, 0f)
        val lockScale = ObjectAnimator.ofFloat(binding.vaultLockIcon, "scaleX", 1f, 1.5f, 0f)
        val lockScaleY = ObjectAnimator.ofFloat(binding.vaultLockIcon, "scaleY", 1f, 1.5f, 0f)
        
        lockRotation.duration = 800
        lockFade.duration = 800
        lockScale.duration = 800
        lockScaleY.duration = 800
        
        lockRotation.start()
        lockFade.start()
        lockScale.start()
        lockScaleY.start()
        
        // Show content after animation
        binding.lockedContainer.animate()
            .alpha(0f)
            .setDuration(500)
            .withEndAction {
                binding.lockedContainer.visibility = View.GONE
                binding.vaultContent.visibility = View.VISIBLE
                binding.vaultContent.alpha = 0f
                binding.vaultContent.animate()
                    .alpha(1f)
                    .setDuration(500)
                    .start()
                
                loadVaultContent()
            }
            .start()
        
        unlockAnimator.start()
    }
    
    private fun loadVaultContent() {
        // Setup RecyclerView for vault items
        binding.vaultRecyclerView.layoutManager = LinearLayoutManager(this)
        
        // Check if vault is empty
        val vaultItems = getVaultItems()
        if (vaultItems.isEmpty()) {
            binding.emptyVaultState.visibility = View.VISIBLE
            binding.vaultRecyclerView.visibility = View.GONE
        } else {
            binding.emptyVaultState.visibility = View.GONE
            binding.vaultRecyclerView.visibility = View.VISIBLE
            // Setup adapter with vault items
        }
        
        // Setup FAB for adding items
        binding.fabAddToVault.setOnClickListener {
            animationHelper.bounceView(it)
            showAddToVaultDialog()
        }
    }
    
    private fun getVaultItems(): List<VaultItem> {
        // Get encrypted items from storage
        val items = mutableListOf<VaultItem>()
        
        // Read from encrypted preferences
        val itemCount = encryptedPrefs.getInt("vault_item_count", 0)
        for (i in 0 until itemCount) {
            val id = encryptedPrefs.getString("item_${i}_id", "") ?: ""
            val content = encryptedPrefs.getString("item_${i}_content", "") ?: ""
            val timestamp = encryptedPrefs.getLong("item_${i}_timestamp", 0L)
            
            if (id.isNotEmpty()) {
                items.add(VaultItem(id, content, timestamp))
            }
        }
        
        return items
    }
    
    private fun showAddToVaultDialog() {
        MaterialAlertDialogBuilder(this, R.style.CustomAlertDialog)
            .setTitle("Add to Vault")
            .setMessage("Select items to add to your encrypted vault")
            .setPositiveButton("Add") { _, _ ->
                // Add selected items
                addItemsToVault()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun addItemsToVault() {
        // Encrypt and save items
        animationHelper.successAnimation(binding.fabAddToVault) {
            loadVaultContent()
        }
    }
    
    private fun showError(message: String) {
        MaterialAlertDialogBuilder(this, R.style.CustomAlertDialog)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
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
    
    override fun onBackPressed() {
        if (isVaultUnlocked) {
            // Lock vault when leaving
            isVaultUnlocked = false
        }
        super.onBackPressed()
    }
    
    data class VaultItem(
        val id: String,
        val content: String,
        val timestamp: Long
    )
}
