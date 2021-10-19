package ru.ya.cup.ui.screen.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.tensorflow.lite.support.label.Category
import ru.ya.cup.BuildConfig
import ru.ya.cup.domain.entity.ClassifierResult
import ru.ya.cup.domain.interactor.RecorderInteractor
import ru.ya.cup.domain.interactor.usecases.GetReportsUseCase
import ru.ya.cup.service.HeadsetService
import ru.ya.cup.service.HeadsetState
import ru.ya.cup.service.recorder.AdapterBroadcastReceiver
import ru.ya.cup.service.recorder.RecorderService.Companion.EXTRA_ID
import ru.ya.cup.service.recorder.RecorderServiceDelegate
import ru.ya.cup.ui.common.reduce
import ru.ya.cup.ui.common.viewmodel.BaseViewModel
import javax.inject.Inject

@HiltViewModel
internal class VM @Inject constructor(
    savedStateHandle: SavedStateHandle,
    headsetService: HeadsetService,
    private val recorderInteractor: RecorderInteractor,
    private val serviceDelegate: RecorderServiceDelegate,
    private val getReportsUseCase: GetReportsUseCase
) : BaseViewModel<State, Action, Event>(savedStateHandle, State()) {

    private val receiver = AdapterBroadcastReceiver { intent ->
        intent?.getLongExtra(EXTRA_ID, 0L)?.let {
            render(state.reduce { copy(sessionId = it) })
        }
    }

    init {
        headsetService
            .headsetConnectionFlow()
            .distinctUntilChanged()
            .onEach(::processHeadsetResult)
            .launchIn(viewModelScope)

        if (BuildConfig.DEBUG) {
            recorderInteractor
                .observeClassifierResults()
                .distinctUntilChanged()
                .onEach(::processClassifierResult)
                .launchIn(viewModelScope)
        }

        serviceDelegate.registerClientReceiver(receiver)
    }

    override fun processAction(action: Action) {
        when (action) {
            Action.StartRecording -> {
                val sessionId = recorderInteractor.generateSessionId()
                serviceDelegate.startRecorderService(sessionId)
                render(state.reduce { copy(sessionId = sessionId, lastSessionId = 0L) })
            }
            Action.StopRecording -> {
                serviceDelegate.stopRecorderService()
                val lastSessionId = state.sessionId
                render(state.reduce { copy(sessionId = 0L, lastSessionId = lastSessionId) })
            }
            Action.OnResumed -> {
                serviceDelegate.removeAutoStop()
                serviceDelegate.healthCheck()
            }
            Action.OnPaused -> serviceDelegate.setAutoStop()
            Action.CheckReports -> checkReports()
        }
    }

    private fun processHeadsetResult(headsetState: HeadsetState) {
        val newState = state.reduce {
            val tmp = when {
                headsetState.wiredHeadset != null && headsetState.bluetoothHeadset != null -> headsetState.wiredHeadset || headsetState.bluetoothHeadset
                else -> headsetState.wiredHeadset == true || headsetState.bluetoothHeadset == true
            }
            copy(isHeadsetConnected = tmp)
        }
        render(newState)
    }

    private fun processClassifierResult(category: Category) {
        val newState = state.reduce {
            copy(classifierResult = ClassifierResult(category.score, category.label))
        }
        render(newState)
    }

    private fun checkReports() {
        getReportsUseCase(GetReportsUseCase.Params)
            .filter { it.isSuccess }
            .onEach { result ->
                val reports = result.getOrDefault(emptyList())
                reports.firstOrNull { it.id == state.lastSessionId }?.let {
                    singleEvent(Event.SendReport(it))
                }
                render(state.reduce {
                    copy(hasReports = reports.isNotEmpty(), lastSessionId = 0L)
                })
            }
            .launchIn(scope = viewModelScope)
    }

    override fun onCleared() {
        serviceDelegate.unregisterReceiver(receiver)
        super.onCleared()
    }
}
