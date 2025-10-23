package com.privacy.sms.ui

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.privacy.sms.R
import com.privacy.sms.databinding.ActivityChangePatternBinding
import com.privacy.sms.util.AnimationHelper

class ChangePatternActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityChangePatternBinding
    private lateinit var animationHelper: AnimationHelper
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePatternBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        animationHelper = AnimationHelper(this)
        setupUI()
    }
    
    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.change_pattern)
        }
        
        animationHelper.startLiquidAnimation(binding.animatedBackground)
        binding.placeholderText.text = "Change Pattern feature coming soon!"
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
