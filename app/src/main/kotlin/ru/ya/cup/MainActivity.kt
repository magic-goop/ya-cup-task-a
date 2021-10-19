package ru.ya.cup

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.insetter.applyInsetter
import ru.ya.cup.extension.currentNavigationFragment
import ru.ya.cup.extension.lazyUi
import ru.ya.cup.ui.utils.OnTopResumedListener


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private companion object {
        const val PERM_NAME: String = Manifest.permission.RECORD_AUDIO
    }

    private val navHostFragment: NavHostFragment by lazyUi {
        (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        window.setFormat(PixelFormat.RGBA_8888)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.root).applyInsetter {
            type(navigationBars = true) {
                padding()
            }
            type(statusBars = true) {
                margin()
            }
        }
        setupInitialScreen()
    }

    override fun onTopResumedActivityChanged(isTopResumedActivity: Boolean) {
        super.onTopResumedActivityChanged(isTopResumedActivity)
        if (isTopResumedActivity) {
            (supportFragmentManager.currentNavigationFragment as? OnTopResumedListener)?.onResumed()
        }
    }

    private fun setupInitialScreen() {
        val navGraph = navHostFragment.navController.navInflater.inflate(R.navigation.nav_graph)
        if (ContextCompat
                .checkSelfPermission(this, PERM_NAME) == PackageManager.PERMISSION_GRANTED
        ) navGraph.startDestination = R.id.fragment_home
        else navGraph.startDestination = R.id.fragment_permission
        navHostFragment.navController.graph = navGraph
        supportFragmentManager
            .beginTransaction()
            .setPrimaryNavigationFragment(navHostFragment)
            .commit()
    }
}
