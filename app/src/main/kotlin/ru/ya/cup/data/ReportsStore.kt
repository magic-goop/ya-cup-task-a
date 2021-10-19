package ru.ya.cup.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.ya.cup.data.cache.UserFile
import ru.ya.cup.domain.entity.Report
import ru.ya.cup.resources.ResourcesManager
import java.io.FilenameFilter
import javax.inject.Inject
import javax.inject.Singleton

interface ReportsStore {
    suspend fun getReports(): List<Report>
    suspend fun deleteReport(report: Report)
}

internal class ReportsStoreImpl @Inject constructor(
    private val resourcesManager: ResourcesManager
) : ReportsStore {

    private val fileFilter: FilenameFilter by lazy {
        FilenameFilter { _, name ->
            name.matches(Regex("^(report_)+[0-9]+\\.(csv)$"))
        }
    }

    override suspend fun getReports(): List<Report> = resourcesManager
        .getUserFilesDir()
        .listFiles(fileFilter)
        ?.map { f ->
            val id = f.name.replace(Regex("[^0-9]"), "").toLong()
            Report(id = id, timestamp = id, fileName = f.name)
        }
        ?.sortedBy { -it.id }
        ?: emptyList()

    override suspend fun deleteReport(report: Report) {
        UserFile(resourcesManager.getUserFilesDir(), report.fileName).deleteFile()
    }
}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ReporterModule {
    @Binds
    @Singleton
    abstract fun bindStore(impl: ReportsStoreImpl): ReportsStore
}
