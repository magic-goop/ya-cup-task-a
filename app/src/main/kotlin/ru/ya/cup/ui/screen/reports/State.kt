package ru.ya.cup.ui.screen.reports

import ru.ya.cup.domain.entity.Report
import ru.ya.cup.ui.common.BaseState

internal data class State(val reports: List<Report>? = null) : BaseState()