package com.privacy.sms.ui

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.andrognito.patternlockview.PatternLockView
import com.andrognito.patternlockview.listener.PatternLockViewListener
import com.andrognito.patternlockview.utils.PatternLockUtils
import com.privacy.sms.R
import com.privacy.sms.databinding.ActivityAuthBinding
import com.privacy.sms.util.AnimationHelper
import java.security.MessageDigest

class AuthActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAuthBinding
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var encryptedPrefs: SharedPreferences
    private var authMethod: AuthMethod = AuthMethod.BIOMETRIC
    private lateinit var animationHelper: AnimationHelper
    
    private enum class AuthMethod {
        BIOMETRIC, PIN, PATTERN
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupEncryptedPreferences()
        animationHelper = AnimationHelper(this)
        setupUI()
        setupAnimations()
        setupBiometricPrompt()
        setupClickListeners()
        setupPatternLock()
        
        // Auto-show biometric prompt if available
        checkBiometricAvailability()
    }
    
    private fun setupEncryptedPreferences() {
        val masterKey = MasterKey.Builder(this)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
            
        encryptedPrefs = EncryptedSharedPreferences.create(
            this,
            "auth_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    private fun setupUI() {
        // Initially show biometric option
        binding.biometricContainer.visibility = View.VISIBLE
        binding.pinContainer.visibility = View.GONE
        binding.patternContainer.visibility = View.GONE
    }
    
    private fun setupAnimations() {
        // Animated gradient background
        animationHelper.startLiquidAnimation(binding.animatedBackground)
        
        // Subtle glow effect
        startGlowAnimation()
        
        // Floating particles effect
        animationHelper.startParticleAnimation(binding.particlesView)
    }
    
    private fun startGlowAnimation() {
        val glowAnimator = ValueAnimator.ofFloat(0.2f, 0.6f)
        glowAnimator.duration = 3000
        glowAnimator.repeatMode = ValueAnimator.REVERSE
        glowAnimator.repeatCount = ValueAnimator.INFINITE
        glowAnimator.interpolator = AccelerateDecelerateInterpolator()
        
        glowAnimator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            binding.glowOverlay.alpha = value
        }
        
        glowAnimator.start()
    }
    
    private fun setupBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)
        
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    showError(getString(R.string.auth_error))
                    animationHelper.shakeView(binding.biometricIcon)
                }
                
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onAuthenticationSuccess()
                }
                
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    showError(getString(R.string.auth_failed))
                    animationHelper.shakeView(binding.biometricIcon)
                }
            })
        
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.app_name))
            .setSubtitle(getString(R.string.unlock_with_biometric))
            .setNegativeButtonText(getString(R.string.use_pin))
            .build()
    }
    
    private fun setupPatternLock() {
        binding.patternLockView.addPatternLockListener(object : PatternLockViewListener {
            override fun onStarted() {
                animationHelper.pulseView(binding.patternLockView)
            }
            
            override fun onProgress(progressPattern: List<PatternLockView.Dot>) {
                // Animate dots as they're connected
            }
            
            override fun onComplete(pattern: List<PatternLockView.Dot>) {
                val patternString = PatternLockUtils.patternToString(binding.patternLockView, pattern)
                verifyPattern(patternString)
            }
            
            override fun onCleared() {
                // Pattern cleared
            }
        })
    }
    
    private fun setupClickListeners() {
        // Biometric button
        binding.biometricButton.setOnClickListener {
            animationHelper.bounceView(it)
            biometricPrompt.authenticate(promptInfo)
        }
        
        // Switch to PIN
        binding.switchToPin.setOnClickListener {
            animationHelper.slideTransition(binding.biometricContainer, binding.pinContainer)
            authMethod = AuthMethod.PIN
        }
        
        // Switch to Pattern
        binding.switchToPattern.setOnClickListener {
            when (authMethod) {
                AuthMethod.BIOMETRIC -> {
                    animationHelper.slideTransition(binding.biometricContainer, binding.patternContainer)
                }
                AuthMethod.PIN -> {
                    animationHelper.slideTransition(binding.pinContainer, binding.patternContainer)
                }
                else -> {}
            }
            authMethod = AuthMethod.PATTERN
        }
        
        // Switch back to Biometric
        binding.switchToBiometric.setOnClickListener {
            when (authMethod) {
                AuthMethod.PIN -> {
                    animationHelper.slideTransition(binding.pinContainer, binding.biometricContainer)
                }
                AuthMethod.PATTERN -> {
                    animationHelper.slideTransition(binding.patternContainer, binding.biometricContainer)
                }
                else -> {}
            }
            authMethod = AuthMethod.BIOMETRIC
        }
        
        // PIN submission
        binding.submitPinButton.setOnClickListener {
            animationHelper.bounceView(it)
            verifyPin()
        }
        
        // PIN input field animation
        binding.pinInput.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                animationHelper.highlightField(binding.pinInputLayout)
            } else {
                animationHelper.unhighlightField(binding.pinInputLayout)
            }
        }
    }
    
    private fun checkBiometricAvailability() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                // Auto-trigger biometric
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.biometricButton.performClick()
                }, 500)
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // Switch to PIN
                binding.switchToPin.performClick()
            }
        }
    }
    
    private fun verifyPin() {
        val pin = binding.pinInput.text.toString()
        
        if (pin.length < 4) {
            showError(getString(R.string.pin_too_short))
            animationHelper.shakeView(binding.pinInputLayout)
            return
        }
        
        // In production, verify against stored hash
        val storedPinHash = encryptedPrefs.getString("pin_hash", "")
        val inputHash = hashPin(pin)
        
        if (storedPinHash.isNullOrEmpty() || storedPinHash == inputHash) {
            // First time or correct PIN
            if (storedPinHash.isNullOrEmpty()) {
                encryptedPrefs.edit().putString("pin_hash", inputHash).apply()
            }
            onAuthenticationSuccess()
        } else {
            showError(getString(R.string.incorrect_pin))
            animationHelper.shakeView(binding.pinInputLayout)
        }
    }
    
    private fun verifyPattern(pattern: String) {
        val storedPatternHash = encryptedPrefs.getString("pattern_hash", "")
        val inputHash = hashPin(pattern)
        
        if (storedPatternHash.isNullOrEmpty() || storedPatternHash == inputHash) {
            // First time or correct pattern
            if (storedPatternHash.isNullOrEmpty()) {
                encryptedPrefs.edit().putString("pattern_hash", inputHash).apply()
            }
            onAuthenticationSuccess()
        } else {
            showError(getString(R.string.incorrect_pattern))
            animationHelper.shakeView(binding.patternLockView)
            binding.patternLockView.clearPattern()
        }
    }
    
    private fun hashPin(pin: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(pin.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
    
    private fun showError(message: String) {
        binding.statusText.text = message
        binding.statusText.setTextColor(ContextCompat.getColor(this, R.color.error))
        animationHelper.fadeInOut(binding.statusText)
    }
    
    private fun onAuthenticationSuccess() {
        binding.statusText.text = getString(R.string.auth_success)
        binding.statusText.setTextColor(ContextCompat.getColor(this, R.color.success))
        
        // Sophisticated unlock animation
        animationHelper.successAnimation(binding.root) {
            navigateToMain()
        }
    }
    
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }
    
    override fun onBackPressed() {
        // Prevent back navigation from auth screen
        super.onBackPressed()
        finishAffinity()
    }
}