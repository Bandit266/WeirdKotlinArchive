package com.privacy.sms.ui

import android.Manifest
import android.animation.ValueAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.privacy.sms.R
import com.privacy.sms.databinding.ActivityMainBinding
import com.privacy.sms.repository.SmsRepository
import com.privacy.sms.ui.adapter.ConversationAdapter
import com.privacy.sms.util.AnimationHelper
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: SmsRepository
    private lateinit var conversationAdapter: ConversationAdapter
    private lateinit var animationHelper: AnimationHelper
    private var isMenuOpen = false
    
    companion object {
        private const val REQUEST_SMS_PERMISSIONS = 100
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_CONTACTS
        )
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        animationHelper = AnimationHelper(this)
        repository = SmsRepository(this)
        
        setupUI()
        setupDrawer()
        setupRecyclerView()
        setupFloatingActionButton()
        setupAnimations()
        checkPermissions()
        checkDefaultSmsApp()
    }
    
    private fun setupUI() {
        // Setup toolbar with hamburger icon
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu)
            title = getString(R.string.inbox)
        }
        
        // Setup animated gradient background
        setupGradientAnimation()
    }
    
    private fun setupGradientAnimation() {
        val gradientDrawable = GradientDrawable().apply {
            gradientType = GradientDrawable.LINEAR_GRADIENT
            orientation = GradientDrawable.Orientation.TL_BR
        }
        
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = 10000
        animator.repeatCount = ValueAnimator.INFINITE
        animator.repeatMode = ValueAnimator.REVERSE
        animator.interpolator = AccelerateDecelerateInterpolator()
        
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            val startColor = interpolateColor(
                ContextCompat.getColor(this, R.color.dark_bg),
                ContextCompat.getColor(this, R.color.purple_900),
                value
            )
            val endColor = interpolateColor(
                ContextCompat.getColor(this, R.color.dark_bg_secondary),
                ContextCompat.getColor(this, R.color.pink_600),
                value
            )
            
            gradientDrawable.colors = intArrayOf(startColor, endColor)
            binding.root.background = gradientDrawable
        }
        
        animator.start()
    }
    
    private fun interpolateColor(startColor: Int, endColor: Int, fraction: Float): Int {
        val startA = (startColor shr 24) and 0xff
        val startR = (startColor shr 16) and 0xff
        val startG = (startColor shr 8) and 0xff
        val startB = startColor and 0xff
        
        val endA = (endColor shr 24) and 0xff
        val endR = (endColor shr 16) and 0xff
        val endG = (endColor shr 8) and 0xff
        val endB = endColor and 0xff
        
        val a = (startA + (endA - startA) * fraction).toInt()
        val r = (startR + (endR - startR) * fraction).toInt()
        val g = (startG + (endG - startG) * fraction).toInt()
        val b = (startB + (endB - startB) * fraction).toInt()
        
        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }
    
    private fun setupDrawer() {
        // Setup navigation drawer
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                }
                R.id.nav_contacts -> {
                    startActivity(Intent(this, ContactsActivity::class.java))
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                }
                R.id.nav_vault -> {
                    startActivity(Intent(this, VaultActivity::class.java))
                }
                R.id.nav_logout -> {
                    showLogoutDialog()
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
        
        // Setup header view
        val headerView = binding.navigationView.getHeaderView(0)
        val profileImage = headerView.findViewById<CircleImageView>(R.id.profileImage)
        val username = headerView.findViewById<android.widget.TextView>(R.id.username)
        val userEmail = headerView.findViewById<android.widget.TextView>(R.id.userEmail)
        
        // Set default profile image and info
        profileImage.setImageResource(R.drawable.ic_anonymous_user)
        username.text = getString(R.string.anonymous_user)
        userEmail.text = "Secure & Private"
        
        // Add drawer listener for animations
        binding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                val slideX = drawerView.width * slideOffset
                binding.mainContent.translationX = slideX * 0.3f
                binding.mainContent.scaleX = 1 - (slideOffset * 0.1f)
                binding.mainContent.scaleY = 1 - (slideOffset * 0.1f)
                binding.drawerOverlay.alpha = slideOffset * 0.5f
            }
            
            override fun onDrawerOpened(drawerView: View) {
                isMenuOpen = true
                animationHelper.pulseView(headerView.findViewById(R.id.vaultButton))
            }
            
            override fun onDrawerClosed(drawerView: View) {
                isMenuOpen = false
            }
            
            override fun onDrawerStateChanged(newState: Int) {}
        })
        
        // Setup vault button in drawer footer
        val vaultButton = headerView.findViewById<View>(R.id.vaultButton)
        vaultButton?.setOnClickListener {
            animationHelper.bounceView(it)
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this, VaultActivity::class.java))
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }, 300)
        }
    }
    
    private fun setupRecyclerView() {
        conversationAdapter = ConversationAdapter(
            onConversationClick = { conversation ->
                animationHelper.pulseView(binding.recyclerViewConversations.findViewHolderForAdapterPosition(0)?.itemView ?: return@ConversationAdapter)
                val intent = Intent(this, ConversationActivity::class.java)
                intent.putExtra("threadId", conversation.threadId)
                intent.putExtra("address", conversation.address)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            },
            onConversationLongClick = { conversation ->
                showConversationOptions(conversation.threadId)
            }
        )
        
        binding.recyclerViewConversations.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = conversationAdapter
            itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator().apply {
                addDuration = 300
                removeDuration = 300
            }
        }
        
        // Show empty state if no conversations
        binding.emptyState.visibility = View.VISIBLE
        binding.emptyStateText.text = getString(R.string.no_messages)
        
        // Observe conversations
        lifecycleScope.launch {
            repository.getAllConversations().collectLatest { conversations ->
                if (conversations.isEmpty()) {
                    binding.emptyState.visibility = View.VISIBLE
                    binding.recyclerViewConversations.visibility = View.GONE
                } else {
                    binding.emptyState.visibility = View.GONE
                    binding.recyclerViewConversations.visibility = View.VISIBLE
                    conversationAdapter.submitList(conversations)
                }
            }
        }
    }
    
    private fun setupFloatingActionButton() {
        // Add subtle floating animation
        animationHelper.glowPulse(binding.fabNewMessage)
        
        binding.fabNewMessage.setOnClickListener {
            animationHelper.bounceView(it)
            
            // Show "In Development" modal
            showInDevelopmentDialog()
        }
    }
    
    private fun setupAnimations() {
        // Add subtle background animation
        val glowView = binding.glowOverlay
        val glowAnimator = ValueAnimator.ofFloat(0.1f, 0.3f)
        glowAnimator.duration = 4000
        glowAnimator.repeatMode = ValueAnimator.REVERSE
        glowAnimator.repeatCount = ValueAnimator.INFINITE
        glowAnimator.interpolator = AccelerateDecelerateInterpolator()
        
        glowAnimator.addUpdateListener { animation ->
            glowView.alpha = animation.animatedValue as Float
        }
        
        glowAnimator.start()
    }
    
    private fun showInDevelopmentDialog() {
        val dialog = MaterialAlertDialogBuilder(this, R.style.CustomAlertDialog)
            .setTitle(getString(R.string.in_development))
            .setMessage(getString(R.string.feature_coming_soon))
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        
        dialog.show()
        
        // Animate dialog appearance
        dialog.window?.decorView?.let { view ->
            view.scaleX = 0.8f
            view.scaleY = 0.8f
            view.alpha = 0f
            
            view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(300)
                .setInterpolator(androidx.interpolator.view.animation.FastOutSlowInInterpolator())
                .start()
        }
    }
    
    private fun showConversationOptions(threadId: Long) {
        val options = arrayOf(
            "Archive for 1 hour",
            "Archive for 6 hours",
            "Archive for 24 hours",
            "Archive for 7 days",
            "Move to Vault",
            "Delete conversation"
        )
        
        MaterialAlertDialogBuilder(this, R.style.CustomAlertDialog)
            .setTitle("Conversation Options")
            .setItems(options) { _, which ->
                lifecycleScope.launch {
                    when (which) {
                        0 -> repository.archiveConversation(threadId, 60)
                        1 -> repository.archiveConversation(threadId, 360)
                        2 -> repository.archiveConversation(threadId, 1440)
                        3 -> repository.archiveConversation(threadId, 10080)
                        4 -> moveToVault(threadId)
                        5 -> {
                            val conversation = repository.getConversationById(threadId)
                            conversation?.let { 
                                repository.deleteConversation(it)
                                showSnackbar("Conversation deleted")
                            }
                        }
                    }
                }
            }
            .show()
    }
    
    private fun moveToVault(threadId: Long) {
        // TODO: Implement vault functionality
        showSnackbar("Moving to vault...")
    }
    
    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(this, R.style.CustomAlertDialog)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                // Clear any stored credentials
                getSharedPreferences("auth_prefs", MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply()
                
                // Navigate back to auth screen
                val intent = Intent(this, AuthActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(ContextCompat.getColor(this, R.color.dark_bg_card))
            .setTextColor(ContextCompat.getColor(this, R.color.text_primary))
            .show()
    }
    
    private fun checkPermissions() {
        val missingPermissions = REQUIRED_PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                missingPermissions.toTypedArray(),
                REQUEST_SMS_PERMISSIONS
            )
        }
    }
    
    private fun checkDefaultSmsApp() {
        val defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(this)
        if (defaultSmsPackage != packageName) {
            MaterialAlertDialogBuilder(this, R.style.CustomAlertDialog)
                .setTitle("Set as Default SMS App")
                .setMessage("PrivacySMS needs to be your default SMS app to work properly.")
                .setPositiveButton("Set Default") { _, _ ->
                    val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
                    intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, packageName)
                    startActivity(intent)
                }
                .setNegativeButton("Later", null)
                .show()
        }
    }
    
    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    binding.drawerLayout.openDrawer(GravityCompat.START)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}