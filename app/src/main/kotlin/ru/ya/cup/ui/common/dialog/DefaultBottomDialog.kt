package ru.ya.cup.ui.common.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import com.google.android.material.bottomsheet.BottomSheetDialog
import ru.ya.cup.R

@SuppressLint("InflateParams")
class DefaultBottomDialog private constructor(
    context: Context,
    builder: Builder
) : BottomSheetDialog(context, R.style.AppBottomSheetDialog) {

    init {
        val view: View = LayoutInflater.from(context).inflate(R.layout.dialog_default_bottom, null)
        setContentView(view)
        view.findViewById<TextView>(R.id.tv_title).apply {
            setText(builder.title)
        }
        view.findViewById<TextView>(R.id.tv_action_main).apply {
            setText(builder.mainTitle)
            setOnClickListener {
                builder.mainAction()
                dismiss()
            }
        }
        view.findViewById<TextView>(R.id.tv_action_secondary).apply {
            setText(builder.secondaryTitle)
            setOnClickListener {
                builder.secondaryAction()
                dismiss()
            }
        }
    }

    class Builder {
        internal var title: Int = 0
        internal var mainTitle: Int = 0
        internal var secondaryTitle: Int = 0
        internal var mainAction: () -> Unit = {}
        internal var secondaryAction: () -> Unit = {}

        fun setTitle(@StringRes id: Int): Builder = this.apply {
            title = id
        }

        fun setSecondaryActionTitle(@StringRes id: Int): Builder = this.apply {
            secondaryTitle = id
        }

        fun setMainActionTitle(@StringRes id: Int): Builder = this.apply {
            mainTitle = id
        }

        fun setMainAction(action: () -> Unit) = this.apply {
            mainAction = action
        }

        fun setSecondaryAction(action: () -> Unit) = this.apply {
            secondaryAction = action
        }

        fun build(context: Context): DefaultBottomDialog = DefaultBottomDialog(context, this)
    }
}
