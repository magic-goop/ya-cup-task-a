package ru.ya.cup.ui.navigation

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import ru.ya.cup.R
import ru.ya.cup.extension.lazyUi
import javax.inject.Inject

interface Router {
    fun back()
    fun navigateToHome()
    fun navigateToSettings()
    fun navigateToPermission()
    fun navigateToReports()
}

internal class RouterImpl @Inject constructor(
    private val activity: FragmentActivity
) : Router {
    private val navController: NavController by lazyUi {
        activity.findNavController(R.id.nav_host_fragment)
    }

    override fun back() {
        navController.popBackStack()
    }

    override fun navigateToHome() {
        navController.popBackStack()
        navController.navigate(R.id.fragment_home)
    }

    override fun navigateToPermission() {
        navController.popBackStack()
        navController.navigate(R.id.fragment_permission)
    }

    override fun navigateToReports() {
        navController.navigate(R.id.action_home_to_history)
    }

    override fun navigateToSettings() {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            data = Uri.parse("package:${activity.applicationContext.packageName}")
        }.also {
            activity.startActivity(it)
        }
    }
}

@Module
@InstallIn(ActivityComponent::class)
internal abstract class Module {

    @Binds
    abstract fun bindRouter(impl: RouterImpl): Router
}