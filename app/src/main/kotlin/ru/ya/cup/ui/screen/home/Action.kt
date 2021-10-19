package ru.ya.cup.ui.screen.home

internal sealed class Action {
    object StartRecording : Action()
    object StopRecording : Action()
    object OnResumed: Action()
    object OnPaused: Action()
    object CheckReports: Action()
}