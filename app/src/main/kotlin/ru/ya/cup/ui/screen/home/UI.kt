package ru.ya.cup.ui.screen.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.ya.cup.BuildConfig
import ru.ya.cup.R
import ru.ya.cup.domain.entity.Report
import ru.ya.cup.extension.lazyUi
import ru.ya.cup.ui.common.dialog.DefaultBottomDialog
import ru.ya.cup.ui.common.fragment.BaseFragment
import ru.ya.cup.ui.navigation.Router
import ru.ya.cup.ui.utils.*
import ru.ya.cup.ui.utils.AnimationHelper.fadeIn
import ru.ya.cup.ui.utils.AnimationHelper.fadeOut
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
internal class UI : BaseFragment<State, Action, Event, VM>(), OnTopResumedListener {

    private companion object {
        const val LISTENING_ANIM_DURATION = 2500L
        const val LISTENING_ANIM_SCALE = 1.25f
    }

    @Inject
    lateinit var router: Router

    @Inject
    lateinit var mailSender: MailSender

    override val vm: VM by viewModels()
    override val resourceId: Int = R.layout.fragment_home

    private lateinit var tvHeadsetWarning: TextView
    private lateinit var imgHeadset: ImageView
    private lateinit var tvAction: TextView
    private lateinit var tvNavigateToReports: TextView
    private lateinit var tvListening: TextView
    private lateinit var tvDebugInfo: TextView
    private lateinit var llTutorial: View

    private var isRecording: Boolean = false
    private var sendReportDialog: DefaultBottomDialog? = null

    private val listeningAnimator by lazyUi {
        AnimationHelper.createPulseAnimator(
            tvListening,
            LISTENING_ANIM_DURATION,
            LISTENING_ANIM_SCALE
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvHeadsetWarning = view.findViewById(R.id.tv_headset_warning)
        imgHeadset = view.findViewById(R.id.img_headsets)
        tvAction = view.findViewById(R.id.tv_action)
        tvListening = view.findViewById(R.id.tv_listening)
        tvDebugInfo = view.findViewById(R.id.tv_debug_info)
        tvNavigateToReports = view.findViewById(R.id.tv_navigate_to_reports)
        llTutorial = view.findViewById(R.id.ll_container_tutorial)
        tvHeadsetWarning.visibility = View.INVISIBLE
        imgHeadset.visibility = View.INVISIBLE
        ViewUtil.setViewVisibility(false, tvAction, tvNavigateToReports, tvListening, tvDebugInfo)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                submitAction(Action.CheckReports)
                actionClickFlow()
                    .onEach {
                        isRecording = if (isRecording) {
                            submitAction(Action.StopRecording)
                            false
                        } else {
                            submitAction(Action.StartRecording)
                            true
                        }
                        delay(TimeUnit.SECONDS.toMillis(2))
                    }
                    .collect {
                        tvAction.setText(if (isRecording) R.string.home_end else R.string.home_start)
                        enableBtn(true, tvAction)
                        if (!isRecording) {
                            submitAction(Action.CheckReports)
                        }
                    }
            }
        }

        tvNavigateToReports.setOnClickListener {
            router.navigateToReports()
        }
    }

    override fun render(state: State) {
        super.render(state)
        isRecording = state.sessionId > 0L
        renderHeadset(state)
        renderDebugInfo(state)
        udpdateRecordingState()
        renderBtnReportsState(state)
    }

    override fun handleEvent(event: Event) {
        super.handleEvent(event)
        if (event is Event.SendReport) {
            showSendReport(event.report)
        }
    }

    private fun renderHeadset(state: State) {
        state.isHeadsetConnected?.let { connected ->
            toggleHeadsetWarningVisibility(connected)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun renderDebugInfo(state: State) {
        ViewUtil.setViewVisibility(BuildConfig.DEBUG && state.sessionId > 0L, tvDebugInfo)
        if (BuildConfig.DEBUG && state.sessionId > 0L) {
            tvDebugInfo.text = """
                Label: ${state.classifierResult?.label}
                Score: ${state.classifierResult?.score}
            """.trimIndent()
        }
    }

    private fun udpdateRecordingState() {
        tvAction.visibility = View.VISIBLE
        tvAction.setText(if (isRecording) R.string.home_end else R.string.home_start)
        if (isRecording) {
            llTutorial.visibility = View.GONE
            tvListening.visibility = View.VISIBLE
            listeningAnimator.start()
        } else {
            llTutorial.visibility = View.VISIBLE
            tvListening.visibility = View.GONE
            listeningAnimator.cancel()
        }
    }

    private fun renderBtnReportsState(state: State) {
        ViewUtil.setViewVisibility(state.hasReports, tvNavigateToReports)
        enableBtn(!isRecording, tvNavigateToReports)
    }

    private fun toggleHeadsetWarningVisibility(connected: Boolean) {
        if (connected && tvHeadsetWarning.visibility == View.INVISIBLE) {
            listOf(tvHeadsetWarning, imgHeadset).forEach(::fadeIn)
        } else if (!connected && tvHeadsetWarning.visibility != View.INVISIBLE) {
            listOf(tvHeadsetWarning, imgHeadset).forEach {
                fadeOut(it, endVisibility = View.INVISIBLE)
            }
        }
    }

    private fun actionClickFlow(): Flow<Unit> = callbackFlow {
        tvAction.setOnClickListener {
            enableBtn(false, tvAction, tvNavigateToReports)
            trySend(Unit)
        }
        awaitClose {
            tvAction.setOnClickListener(null)
        }
    }

    override fun onResumed() {
        submitAction(Action.OnResumed)
        checkPermission()
    }

    override fun onResume() {
        super.onResume()
        onResumed()
    }

    override fun onPause() {
        super.onPause()
        submitAction(Action.OnPaused)
        sendReportDialog?.dismiss()
    }

    private fun checkPermission() {
        if (isPermissionGranted().not()) {
            router.navigateToPermission()
        }
    }

    private fun enableBtn(enable: Boolean, vararg view: TextView) {
        view.forEach {
            it.alpha = if (enable) 1f else 0.5f
            it.isEnabled = enable
        }
    }

    private fun showSendReport(report: Report) {
        sendReportDialog?.cancel()
        sendReportDialog = DefaultBottomDialog.Builder()
            .setTitle(R.string.home_report_ready)
            .setMainActionTitle(R.string.home_send_report)
            .setSecondaryActionTitle(R.string.home_send_later)
            .setMainAction {
                mailSender.sendMail(report)
            }
            .build(requireContext())
        sendReportDialog?.show()
    }
}
