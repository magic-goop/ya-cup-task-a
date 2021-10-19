package ru.ya.cup.ui.utils

import android.app.Activity
import android.content.Intent
import androidx.core.content.FileProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import ru.ya.cup.R
import ru.ya.cup.domain.entity.Report
import ru.ya.cup.resources.ResourcesManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

interface MailSender {
    fun sendMail(report: Report)
}

internal class MailSenderImpl @Inject constructor(
    private val activity: Activity,
    private val resourcesManager: ResourcesManager
) : MailSender {
    private val sdf: SimpleDateFormat =
        SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault())

    override fun sendMail(report: Report) {
        val subject = resourcesManager
            .getString(R.string.reports_item_report_name_template)
            .format(sdf.format(Date(report.timestamp)))
        val attachment = FileProvider.getUriForFile(
            activity,
            activity.applicationContext.packageName + ".fileprovider",
            File(resourcesManager.getUserFilesDir(), report.fileName)
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_STREAM, attachment)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setDataAndType(attachment, activity.contentResolver.getType(attachment))
        }
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(intent)
        }
    }
}

@Module
@InstallIn(ActivityComponent::class)
internal abstract class MailSenderModule {

    @Binds
    abstract fun bindRouter(impl: MailSenderImpl): MailSender
}
