package ru.ya.cup.recorder

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.tensorflow.lite.support.label.Category
import ru.ya.cup.data.RecorderStore
import ru.ya.cup.domain.entity.Cortege
import ru.ya.cup.domain.entity.RecordLabel
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

interface RecorderProcessor {
    suspend fun addData(sessionId: Long, timestamp: Long, category: Category)
    suspend fun dump(sessionId: Long)
}

internal class RecorderProcessorImpl @Inject constructor(
    private val recorderStore: RecorderStore
) : RecorderProcessor {
    companion object {
        val WINDOW_DURATION: Long = TimeUnit.MINUTES.toMillis(5)
        const val SCORE_THRESHOLD: Float = 0.3f
        const val TIME_FORMAT_FULL: String = "%02d:%02d.%d"
        const val TIME_FORMAT_SHORT: String = "%02d.%d"
    }

    private val mutex: Mutex = Mutex()
    private val window: MutableList<Pair<Long, Category>> = mutableListOf()
    private val tail: MutableList<Pair<Long, Category>> = mutableListOf()
    private var currentSessionId: Long = 0

    override suspend fun addData(sessionId: Long, timestamp: Long, category: Category) {
        withContext(Dispatchers.Default) {
            mutex.withLock {
                if (currentSessionId != sessionId) {
                    if (window.isNotEmpty() || tail.isNotEmpty()) {
                        processDump(currentSessionId)
                    }
                    window.clear()
                    tail.clear()
                    currentSessionId = sessionId
                }

                window.add(Pair(timestamp, category))

                if (window.size > 1) {
                    val gap = window.last().first - window.first().first
                    if (gap > WINDOW_DURATION) {
                        processData(sessionId, window.toList())
                        window.clear()
                    }
                }
            }
        }
    }

    override suspend fun dump(sessionId: Long) {
        withContext(Dispatchers.Default) {
            mutex.withLock {
                processDump(sessionId)
            }
        }
    }

    private fun processDump(sessionId: Long) {
        val normalized = normalize(tail + window)
        if (normalized.isNotEmpty()) {
            saveCorteges(sessionId, buildCortege(normalized))
        }
        window.clear()
        tail.clear()
    }

    private fun processData(sessionId: Long, list: List<Pair<Long, Category>>) {
        val normalized = normalize(tail + list)
        val cortegeList = buildCortege(normalized.take(tail.size + list.size / 2))
        saveCorteges(sessionId, cortegeList)

        tail.clear()
        tail.addAll(list.takeLast(list.size / 2))
    }

    // naive normalization
    private fun normalize(list: List<Pair<Long, Category>>): List<Pair<Long, RecordLabel>> {
        return list.mapIndexed { index, p ->
            if (p.second.score < SCORE_THRESHOLD) {
                Pair(p.first, RecordLabel.SILENCE)
            } else if (index > 1 && index < list.size - 2) {
                val labels = (index - 1..index + 1).map { i ->
                    RecordLabel.fromString(list[i].second.label)
                }
                Pair(p.first, labels.maxOrNull() ?: RecordLabel.SILENCE)
            } else {
                Pair(p.first, RecordLabel.fromString(p.second.label))
            }
        }
    }

    private fun buildCortege(normalized: List<Pair<Long, RecordLabel>>): List<Cortege> {
        val result = mutableListOf<Cortege>()
        var duration = ""
        for (i in normalized.indices) {
            val current = normalized[i]
            if (i > 0 && current.second == normalized[i - 1].second) {
                continue
            }
            val time = convertTimestamp(current.first)

            val type = current.second
            val startTime = String.format(TIME_FORMAT_FULL, time.first, time.second, time.third)
            for (j in (i + 1) until normalized.size) {
                val next = normalized[j]
                if (current.second != next.second) {
                    duration = String.format(
                        TIME_FORMAT_SHORT,
                        (next.first - current.first) / 1000,
                        (next.first - current.first) % 1000
                    )
                    break
                }
            }
            result.add(Cortege(type, startTime, duration))
        }
        result.forEach { Timber.d(it.toString()) }
        return result
    }

    private fun saveCorteges(sessionId: Long, data: List<Cortege>) {
        recorderStore.writeCorteges(sessionId, data)
    }

    private fun convertTimestamp(timestamp: Long): Triple<Long, Long, Long> {
        val millis: Long = timestamp % 1000
        val second: Long = timestamp / 1000 % 60
        val minute: Long = timestamp / (1000 * 60) % 60
        return Triple(minute, second, millis)
    }
}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ModuleRecorderProcessor {
    @Binds
    @Singleton
    abstract fun bindProcessor(impl: RecorderProcessorImpl): RecorderProcessor
}