@file:Suppress("UNCHECKED_CAST")

package ru.ya.cup.ui.common.viewmodel

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.ya.cup.ui.common.BaseState

abstract class BaseViewModel<State : BaseState, Action, Event>(
    private val savedStateHandle: SavedStateHandle,
    initialState: State
) : ViewModel() {
    private val key: String = javaClass.name
    protected val state: State
        get() = _stateFlow.value

    private val _eventFlow = Channel<Event>(Channel.BUFFERED)
    private val _actionFlow = MutableSharedFlow<Action>()
    private val _stateFlow: MutableStateFlow<State> =
        MutableStateFlow(restoreState() ?: initialState).apply {
            savedStateHandle.setSavedStateProvider(key) {
                bundleOf("state" to state)
            }
        }

    val eventFlow: Flow<Event> = _eventFlow.receiveAsFlow()
    val stateFlow: Flow<State> = _stateFlow

    private fun restoreState(): State? {
        return with(savedStateHandle.get<Bundle>(key)) {
            this?.get("state") as? State
        }
    }

    protected fun render(newState: State) {
        if (newState != state) {
            _stateFlow.value = newState
        }
    }

    fun submitAction(action: Action) {
        viewModelScope.launch {
            _actionFlow.emit(action)
        }
    }

    protected fun singleEvent(newEvent: Event) {
        viewModelScope.launch {
            _eventFlow.send(newEvent)
        }
    }

    protected open fun processAction(action: Action) {}

    init {
        viewModelScope.launch {
            _actionFlow.collect {
                processAction(it)
            }
        }
    }
}
