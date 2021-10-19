package ru.ya.cup

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import ru.ya.cup.notifications.NotificationCtrl
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class TheApplication : Application() {

    @Inject
    lateinit var notificationCtrl: NotificationCtrl

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        notificationCtrl.init()
    }
}
