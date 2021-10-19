package ru.ya.cup.ui.utils

import android.animation.*
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator

object AnimationHelper {
    private const val ANIM_DURATION: Long = 200L
    private const val ANIM_SCALE: Float = 1.1f

    fun fadeIn(view: View) {
        this.fadeIn(view, ANIM_DURATION)
    }

    fun fadeIn(view: View, duration: Long) {
        view.alpha = 0f
        view.animate().cancel()
        view.animate()
            .alpha(1f)
            .setDuration(duration)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    view.visibility = View.VISIBLE
                }
            })
            .setInterpolator(DecelerateInterpolator())
            .start()

    }

    fun fadeOut(view: View, endVisibility: Int = View.GONE) {
        this.fadeOut(view, ANIM_DURATION, endVisibility)
    }

    fun fadeOut(view: View, duration: Long, endVisibility: Int) {
        view.animate().cancel()
        view.animate()
            .alpha(0f)
            .setDuration(duration)
            .setInterpolator(DecelerateInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    view.visibility = endVisibility
                }
            })
            .start()
    }

    fun createPulseAnimator(
        view: View,
        animDuration: Long = ANIM_DURATION,
        maxScale: Float = ANIM_SCALE
    ): Animator =
        AnimatorSet().apply {
            playTogether(listOf(View.SCALE_X, View.SCALE_Y)
                .map { ObjectAnimator.ofFloat(view, it, 1f, maxScale, 1f) }
                .onEach {
                    it.repeatCount = ValueAnimator.INFINITE
                    it.repeatMode = ValueAnimator.REVERSE
                }
            )
            duration = animDuration
            interpolator = AccelerateDecelerateInterpolator()
        }
}
