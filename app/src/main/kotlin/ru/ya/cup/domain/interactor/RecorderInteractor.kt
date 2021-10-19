package ru.ya.cup.domain.interactor

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.tensorflow.lite.support.label.Category
import ru.ya.cup.recorder.RecorderProcessor
import javax.inject.Inject
import javax.inject.Singleton

interface RecorderInteractor {
    suspend fun submitData(sessionId: Long, category: Category, timestamp: Long)
    suspend fun dump(sessionId: Long)
    fun observeClassifierResults(): Flow<Category>
    fun generateSessionId(): Long
}

internal class RecorderInteractorImpl @Inject constructor(
    private val recorderProcessor: RecorderProcessor
) : RecorderInteractor {

    private val _classifierResults = MutableSharedFlow<Category>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override suspend fun submitData(sessionId: Long, category: Category, timestamp: Long) {
        if (category.label != null) {
            _classifierResults.emit(category)
            recorderProcessor.addData(sessionId, timestamp, category)
        }
    }

    override suspend fun dump(sessionId: Long) {
        recorderProcessor.dump(sessionId)
    }

    override fun generateSessionId(): Long = System.currentTimeMillis()

    override fun observeClassifierResults(): Flow<Category> = _classifierResults.asSharedFlow()
}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class InteractorModule {

    @Binds
    @Singleton
    abstract fun bindInteractor(impl: RecorderInteractorImpl): RecorderInteractor
}
