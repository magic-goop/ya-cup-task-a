package ru.ya.cup.recorder

import io.mockk.coEvery
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.tensorflow.lite.support.label.Category
import ru.ya.cup.data.RecorderStore

// naive tests, didn't have time for the proper ones
@ExtendWith(MockKExtension::class)
class RecorderProcessorTest {

    private companion object {
        const val LABEL_NOISE = "0 Background Noise"
        const val LABEL_INHALE = "2 Inhale"
        const val LABEL_EXHALE = "1 Exhale"
    }

    @MockK
    private lateinit var recorderStore: RecorderStore

    @InjectMockKs
    private lateinit var recorderProcessor: RecorderProcessorImpl

    private val sessionId = System.currentTimeMillis()
    private val categoryNoise: Category = Category(LABEL_NOISE, 0.8f)
    private val categoryInhale: Category = Category(LABEL_INHALE, 0.8f)
    private val categoryExhale: Category = Category(LABEL_EXHALE, 0.8f)

    @BeforeEach
    fun initial() = runBlocking {
        coEvery { recorderStore.writeCorteges(any(), any()) } just runs
        recorderProcessor.dump(0L)
    }

    @Test
    fun `do nothing when dump and any data weren't added`() = runBlocking {
        recorderProcessor.dump(sessionId)
        verify(exactly = 0) { recorderStore.writeCorteges(any(), any()) }
    }

    @Test
    fun `should just add data without processing it`() = runBlocking {
        recorderProcessor.addData(sessionId, 1L, categoryNoise)
        verify(exactly = 0) { recorderStore.writeCorteges(any(), any()) }

        recorderProcessor.addData(
            sessionId,
            RecorderProcessorImpl.WINDOW_DURATION - 100,
            categoryInhale
        )
        verify(exactly = 0) { recorderStore.writeCorteges(any(), any()) }
    }

    @Test
    fun `should process data`() = runBlocking {
        recorderProcessor.addData(sessionId, 1L, categoryNoise)
        verify(exactly = 0) { recorderStore.writeCorteges(any(), any()) }

        recorderProcessor.addData(
            sessionId,
            RecorderProcessorImpl.WINDOW_DURATION + 100,
            categoryInhale
        )
        verify(exactly = 1) { recorderStore.writeCorteges(any(), any()) }
    }

    @Test
    fun `should process data when dumping`() = runBlocking {
        recorderProcessor.addData(sessionId, 1L, categoryNoise)
        verify(exactly = 0) { recorderStore.writeCorteges(any(), any()) }

        recorderProcessor.dump(sessionId)
        verify(exactly = 1) { recorderStore.writeCorteges(any(), any()) }
    }

    @Test
    fun `should process data when sessionId changed`() = runBlocking {
        recorderProcessor.addData(sessionId, 1L, categoryNoise)
        verify(exactly = 0) { recorderStore.writeCorteges(any(), any()) }

        recorderProcessor.addData(sessionId + 1, 2L, categoryNoise)
        verify(exactly = 1) { recorderStore.writeCorteges(any(), any()) }
    }
}
