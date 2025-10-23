package com.privacy.sms.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputLayout
import com.privacy.sms.R
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class AnimationHelper(private val context: Context) {
    
    fun startLiquidAnimation(view: View) {
        val gradientAnimator = ValueAnimator.ofFloat(0f, 1f)
        gradientAnimator.duration = 8000
        gradientAnimator.repeatCount = ValueAnimator.INFINITE
        gradientAnimator.repeatMode = ValueAnimator.REVERSE
        gradientAnimator.interpolator = AccelerateDecelerateInterpolator()
        
        gradientAnimator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            view.alpha = 0.3f + (value * 0.2f)
            view.scaleX = 1.0f + (value * 0.05f)
            view.scaleY = 1.0f + (value * 0.05f)
        }
        
        gradientAnimator.start()
    }
    
    fun startParticleAnimation(view: View) {
        // Create floating particle effect
        val particleAnimator = ObjectAnimator.ofFloat(view, "translationY", 0f, -20f, 0f)
        particleAnimator.duration = 4000
        particleAnimator.repeatCount = ValueAnimator.INFINITE
        particleAnimator.interpolator = AccelerateDecelerateInterpolator()
        particleAnimator.start()
    }
    
    fun shakeView(view: View) {
        val shakeAnimator = ObjectAnimator.ofFloat(view, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f)
        shakeAnimator.duration = 500
        shakeAnimator.start()
    }
    
    fun pulseView(view: View) {
        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.1f, 1f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.1f, 1f)
        val alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 1f, 0.8f, 1f)
        
        val animator = ObjectAnimator.ofPropertyValuesHolder(view, scaleX, scaleY, alpha)
        animator.duration = 300
        animator.start()
    }
    
    fun bounceView(view: View) {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.9f, 1.1f, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.9f, 1.1f, 1f)
        
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleX, scaleY)
        animatorSet.duration = 300
        animatorSet.interpolator = BounceInterpolator()
        animatorSet.start()
    }
    
    fun slideTransition(fromView: View, toView: View) {
        // Slide out animation
        val slideOut = ObjectAnimator.ofFloat(fromView, "translationX", 0f, -fromView.width.toFloat())
        val fadeOut = ObjectAnimator.ofFloat(fromView, "alpha", 1f, 0f)
        
        val outSet = AnimatorSet()
        outSet.playTogether(slideOut, fadeOut)
        outSet.duration = 300
        outSet.interpolator = AccelerateDecelerateInterpolator()
        
        // Slide in animation
        toView.translationX = toView.width.toFloat()
        toView.alpha = 0f
        toView.visibility = View.VISIBLE
        
        val slideIn = ObjectAnimator.ofFloat(toView, "translationX", toView.width.toFloat(), 0f)
        val fadeIn = ObjectAnimator.ofFloat(toView, "alpha", 0f, 1f)
        
        val inSet = AnimatorSet()
        inSet.playTogether(slideIn, fadeIn)
        inSet.duration = 300
        inSet.interpolator = DecelerateInterpolator()
        
        outSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                fromView.visibility = View.GONE
                inSet.start()
            }
        })
        
        outSet.start()
    }
    
    fun highlightField(layout: TextInputLayout) {
        val colorFrom = ContextCompat.getColor(context, R.color.field_trim)
        val colorTo = ContextCompat.getColor(context, R.color.pink_500)
        
        val animator = ValueAnimator.ofArgb(colorFrom, colorTo)
        animator.duration = 300
        animator.addUpdateListener { animation ->
            layout.boxStrokeColor = animation.animatedValue as Int
        }
        animator.start()
        
        // Add glow effect
        val scaleAnimator = ObjectAnimator.ofFloat(layout, "scaleX", 1f, 1.02f)
        val scaleYAnimator = ObjectAnimator.ofFloat(layout, "scaleY", 1f, 1.02f)
        
        val set = AnimatorSet()
        set.playTogether(scaleAnimator, scaleYAnimator)
        set.duration = 300
        set.start()
    }
    
    fun unhighlightField(layout: TextInputLayout) {
        val colorFrom = ContextCompat.getColor(context, R.color.pink_500)
        val colorTo = ContextCompat.getColor(context, R.color.field_trim)
        
        val animator = ValueAnimator.ofArgb(colorFrom, colorTo)
        animator.duration = 300
        animator.addUpdateListener { animation ->
            layout.boxStrokeColor = animation.animatedValue as Int
        }
        animator.start()
        
        // Remove glow effect
        val scaleAnimator = ObjectAnimator.ofFloat(layout, "scaleX", 1.02f, 1f)
        val scaleYAnimator = ObjectAnimator.ofFloat(layout, "scaleY", 1.02f, 1f)
        
        val set = AnimatorSet()
        set.playTogether(scaleAnimator, scaleYAnimator)
        set.duration = 300
        set.start()
    }
    
    fun fadeInOut(view: View) {
        val fadeIn = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f)
        fadeIn.duration = 200
        
        val fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f)
        fadeOut.duration = 200
        fadeOut.startDelay = 2000
        
        val set = AnimatorSet()
        set.playSequentially(fadeIn, fadeOut)
        set.start()
    }
    
    fun successAnimation(view: View, onComplete: () -> Unit) {
        // Create a sophisticated success animation
        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.1f, 0.95f, 1.05f, 1f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.1f, 0.95f, 1.05f, 1f)
        val rotation = PropertyValuesHolder.ofFloat(View.ROTATION, 0f, 5f, -5f, 3f, -3f, 0f)
        
        val successAnimator = ObjectAnimator.ofPropertyValuesHolder(view, scaleX, scaleY, rotation)
        successAnimator.duration = 800
        successAnimator.interpolator = OvershootInterpolator()
        
        // Fade out animation
        val fadeOut = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f)
        fadeOut.duration = 300
        fadeOut.startDelay = 800
        
        fadeOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                onComplete()
            }
        })
        
        val set = AnimatorSet()
        set.playSequentially(successAnimator, fadeOut)
        set.start()
    }
    
    fun rippleEffect(view: View, x: Float, y: Float) {
        val ripple = View(context)
        ripple.x = x - 50
        ripple.y = y - 50
        
        val scaleX = ObjectAnimator.ofFloat(ripple, "scaleX", 0f, 3f)
        val scaleY = ObjectAnimator.ofFloat(ripple, "scaleY", 0f, 3f)
        val alpha = ObjectAnimator.ofFloat(ripple, "alpha", 0.8f, 0f)
        
        val set = AnimatorSet()
        set.playTogether(scaleX, scaleY, alpha)
        set.duration = 500
        set.start()
    }
    
    fun glowPulse(view: View) {
        val glowAnimator = ValueAnimator.ofFloat(0f, 1f)
        glowAnimator.duration = 2000
        glowAnimator.repeatCount = ValueAnimator.INFINITE
        glowAnimator.repeatMode = ValueAnimator.REVERSE
        
        glowAnimator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            view.alpha = 0.3f + (value * 0.7f)
            
            // Add elevation shadow for glow effect
            view.elevation = 8f + (value * 16f)
        }
        
        glowAnimator.start()
    }
}
