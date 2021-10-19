package ru.ya.cup.recorder

import android.content.Context
import android.media.AudioRecord
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.*
import org.tensorflow.lite.support.audio.TensorAudio
import org.tensorflow.lite.task.audio.classifier.AudioClassifier
import ru.ya.cup.domain.interactor.RecorderInteractor
import timber.log.Timber
import javax.inject.Inject

interface Recorder {
    fun startRecording(sessionId: Long)
    fun stopRecording()
    fun release()
    fun isRecording(): Boolean
}

internal class RecorderImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val recorderInteractor: RecorderInteractor
) : Recorder {
    private companion object {
        const val LOOP_INTERVAL: Long = 100L
        const val TF_MODEL = "soundclassifier_with_metadata.tflite"
    }

    private val job: Job = SupervisorJob()
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + job)
    private var loopJob: Job? = null

    private var audioClassifier: AudioClassifier? = null
    private var audioRecord: AudioRecord? = null
    private var audioTensor: TensorAudio? = null

    private var sessionId: Long = 0L

    override fun startRecording(sessionId: Long) {
        if (audioClassifier != null) return
        this.sessionId = sessionId
        Timber.d("Start recording with sessionId: $sessionId")

        val classifier = AudioClassifier.createFromFile(context, TF_MODEL)
        val tensor = classifier.createInputTensorAudio()

        val record = classifier.createAudioRecord()
        record.startRecording()

        audioClassifier = classifier
        audioRecord = record
        audioTensor = tensor

        loop()
    }

    override fun stopRecording() {
        Timber.d("Stop recording")
        scope.launch {
            recorderInteractor.dump(sessionId)
        }
        loopJob?.cancel()
        audioRecord?.stop()
        audioRecord = null
        audioClassifier = null
    }

    override fun release() {
        Timber.d("Release recorder")
        loopJob?.cancel()
        job.cancel()
    }

    override fun isRecording(): Boolean = audioClassifier != null

    private fun loop() {
        loopJob = scope.launch {
            while (true) {
                delay(LOOP_INTERVAL)
                val startTime = System.currentTimeMillis()
                audioTensor?.load(audioRecord)
                audioClassifier?.classify(audioTensor)?.firstOrNull()?.let { output ->
                    output.categories.maxByOrNull { it.score }?.let { category ->
                        recorderInteractor.submitData(sessionId, category, startTime)
                    }
                }
            }
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ModuleRecorder {
    @Binds
    abstract fun bindRecorder(impl: RecorderImpl): Recorder
}
