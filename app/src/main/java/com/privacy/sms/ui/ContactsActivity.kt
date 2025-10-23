package com.privacy.sms.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.privacy.sms.R
import com.privacy.sms.databinding.ActivityContactsBinding
import com.privacy.sms.util.AnimationHelper

class ContactsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityContactsBinding
    private lateinit var animationHelper: AnimationHelper
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        animationHelper = AnimationHelper(this)
        setupUI()
    }
    
    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.contacts)
        }
        
        // Animate background
        animationHelper.startLiquidAnimation(binding.animatedBackground)
        
        // Show placeholder text
        binding.placeholderText.text = "Contacts feature coming soon!"
        animationHelper.fadeInOut(binding.placeholderText)
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
}
