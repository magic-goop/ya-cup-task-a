package ru.ya.cup.ui.screen.home

import ru.ya.cup.domain.entity.Report

internal sealed class Event {
    data class SendReport(val report: Report) : Event()
}