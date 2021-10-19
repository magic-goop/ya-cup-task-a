package ru.ya.cup.ui.utils

import android.view.View
import androidx.annotation.NonNull

object ViewUtil {
    fun setViewVisibility(visible: Boolean, @NonNull vararg views: View) {
        if (views.isEmpty()) {
            return
        }
        for (v in views) {
            val currentVisibility = v.visibility == View.VISIBLE
            if (currentVisibility != visible) {
                v.visibility = if (visible) View.VISIBLE else View.GONE
            }
        }
    }
}
