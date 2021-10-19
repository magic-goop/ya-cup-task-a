package ru.ya.cup.ui.screen.home

import ru.ya.cup.domain.entity.ClassifierResult
import ru.ya.cup.domain.entity.Report
import ru.ya.cup.ui.common.BaseState

internal data class State(
    val isHeadsetConnected: Boolean? = null,
    val sessionId: Long = 0,
    val lastSessionId: Long = 0,
    val classifierResult: ClassifierResult? = null,
    val hasReports: Boolean = false
) : BaseState()