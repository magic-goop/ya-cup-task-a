package ru.ya.cup.extension

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

fun <T> lazyUi(block: () -> T): Lazy<T> =
    lazy(mode = LazyThreadSafetyMode.NONE, initializer = block)

val FragmentManager.currentNavigationFragment: Fragment?
    get() = primaryNavigationFragment?.childFragmentManager?.fragments?.first()
