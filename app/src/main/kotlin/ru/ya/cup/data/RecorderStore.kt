package ru.ya.cup.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.ya.cup.R
import ru.ya.cup.data.cache.UserFile
import ru.ya.cup.domain.entity.Cortege
import ru.ya.cup.resources.ResourcesManager
import javax.inject.Inject
import javax.inject.Singleton

interface RecorderStore {
    fun writeCorteges(sessionId: Long, list: List<Cortege>)
}

internal class RecorderStoreImpl @Inject constructor(
    private val resourcesManager: ResourcesManager
) : RecorderStore {

    private val mutex: Mutex = Mutex()
    private var reportUser: UserFile? = null
    private var lastSessionId: Long = 0

    @OptIn(DelicateCoroutinesApi::class)
    override fun writeCorteges(sessionId: Long, list: List<Cortege>) {
        // here we need a separate scope from any component's scope
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                mutex.withLock {
                    val data = mutableListOf<String>()
                    if (reportUser == null || lastSessionId != sessionId) {
                        reportUser =
                            UserFile(resourcesManager.getUserFilesDir(), "report_${sessionId}.csv")
                        data.add(resourcesManager.getString(R.string.csv_header))
                    }
                    lastSessionId = sessionId
                    list.map { it.toCsvRow(resourcesManager) }.forEach(data::add)
                    reportUser?.writeToFile(data)
                }
            }
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class Module {
    @Binds
    @Singleton
    abstract fun bindStore(impl: RecorderStoreImpl): RecorderStore
}
