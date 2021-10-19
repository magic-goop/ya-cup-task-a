package ru.ya.cup.ui.screen.reports

import ru.ya.cup.domain.entity.Report

internal sealed class Action {
    object Init: Action()
    data class DeleteReport(val report: Report) : Action()
}