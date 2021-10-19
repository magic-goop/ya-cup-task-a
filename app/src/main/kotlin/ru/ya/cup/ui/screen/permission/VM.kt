package ru.ya.cup.ui.screen.permission

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.ya.cup.ui.common.viewmodel.BaseViewModel
import javax.inject.Inject

@HiltViewModel
internal class VM @Inject constructor(
    savedStateHandle: SavedStateHandle
) : BaseViewModel<State, Action, Nothing>(savedStateHandle, State)
