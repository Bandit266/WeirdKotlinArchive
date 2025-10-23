package com.privacy.sms.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.privacy.sms.R
import com.privacy.sms.databinding.ActivityProfileBinding
import com.privacy.sms.util.AnimationHelper
import java.io.File
import java.io.FileOutputStream

class ProfileActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityProfileBinding
    private lateinit var animationHelper: AnimationHelper
    private var profileImageUri: Uri? = null
    
    companion object {
        private const val REQUEST_IMAGE_PICK = 100
        private const val REQUEST_IMAGE_CAPTURE = 101
        private const val REQUEST_CAMERA_PERMISSION = 102
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        animationHelper = AnimationHelper(this)
        
        setupUI()
        setupClickListeners()
        loadProfile()
    }
    
    private fun setupUI() {
        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.profile)
        }
        
        // Animate profile image
        animationHelper.glowPulse(binding.profileImageCard)
        
        // Setup animated background
        animationHelper.startLiquidAnimation(binding.animatedBackground)
    }
    
    private fun setupClickListeners() {
        // Profile image click
        binding.profileImage.setOnClickListener {
            animationHelper.bounceView(binding.profileImageCard)
            showImagePickerDialog()
        }
        
        // Change photo button
        binding.changePhotoButton.setOnClickListener {
            animationHelper.bounceView(it)
            showImagePickerDialog()
        }
        
        // Remove photo button
        binding.removePhotoButton.setOnClickListener {
            animationHelper.bounceView(it)
            removeProfilePhoto()
        }
        
        // Save button
        binding.saveButton.setOnClickListener {
            animationHelper.bounceView(it)
            saveProfile()
        }
        
        // Username field animation
        binding.usernameInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                animationHelper.highlightField(binding.usernameLayout)
            } else {
                animationHelper.unhighlightField(binding.usernameLayout)
            }
        }
        
        // Bio field animation
        binding.bioInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                animationHelper.highlightField(binding.bioLayout)
            } else {
                animationHelper.unhighlightField(binding.bioLayout)
            }
        }
    }
    
    private fun loadProfile() {
        val prefs = getSharedPreferences("profile_prefs", MODE_PRIVATE)
        val username = prefs.getString("username", getString(R.string.anonymous_user))
        val bio = prefs.getString("bio", "")
        val imagePath = prefs.getString("profile_image", null)
        
        binding.usernameInput.setText(username)
        binding.bioInput.setText(bio)
        
        if (!imagePath.isNullOrEmpty()) {
            Glide.with(this)
                .load(File(imagePath))
                .placeholder(R.drawable.ic_anonymous_user)
                .error(R.drawable.ic_anonymous_user)
                .circleCrop()
                .into(binding.profileImage)
        }
    }
    
    private fun saveProfile() {
        val username = binding.usernameInput.text.toString().trim()
        val bio = binding.bioInput.text.toString().trim()
        
        if (username.isEmpty()) {
            binding.usernameLayout.error = "Username cannot be empty"
            animationHelper.shakeView(binding.usernameLayout)
            return
        }
        
        val prefs = getSharedPreferences("profile_prefs", MODE_PRIVATE)
        prefs.edit().apply {
            putString("username", username)
            putString("bio", bio)
            apply()
        }
        
        // Success animation
        animationHelper.successAnimation(binding.saveButton) {
            Toast.makeText(this, "Profile saved successfully", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        
        MaterialAlertDialogBuilder(this, R.style.CustomAlertDialog)
            .setTitle("Select Photo")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> checkCameraPermissionAndCapture()
                    1 -> pickImageFromGallery()
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }
    
    private fun checkCameraPermissionAndCapture() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        } else {
            captureImage()
        }
    }
    
    private fun captureImage() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        }
    }
    
    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }
    
    private fun removeProfilePhoto() {
        binding.profileImage.setImageResource(R.drawable.ic_anonymous_user)
        
        val prefs = getSharedPreferences("profile_prefs", MODE_PRIVATE)
        val imagePath = prefs.getString("profile_image", null)
        
        if (!imagePath.isNullOrEmpty()) {
            File(imagePath).delete()
        }
        
        prefs.edit().remove("profile_image").apply()
        
        Toast.makeText(this, "Profile photo removed", Toast.LENGTH_SHORT).show()
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_PICK -> {
                    data?.data?.let { uri ->
                        profileImageUri = uri
                        updateProfileImage(uri)
                    }
                }
                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as? Bitmap
                    imageBitmap?.let { bitmap ->
                        saveImageToInternalStorage(bitmap)
                    }
                }
            }
        }
    }
    
    private fun updateProfileImage(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .circleCrop()
            .into(binding.profileImage)
        
        // Save image path
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            saveImageToInternalStorage(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun saveImageToInternalStorage(bitmap: Bitmap) {
        try {
            val file = File(filesDir, "profile_image.jpg")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
            
            // Save path to preferences
            val prefs = getSharedPreferences("profile_prefs", MODE_PRIVATE)
            prefs.edit().putString("profile_image", file.absolutePath).apply()
            
            // Update UI
            Glide.with(this)
                .load(file)
                .circleCrop()
                .into(binding.profileImage)
            
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                captureImage()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
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
