package ru.ya.cup.ui.common.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.ya.cup.extension.flowWithLifecycle
import ru.ya.cup.ui.common.BaseState
import ru.ya.cup.ui.common.viewmodel.BaseViewModel

internal abstract class BaseFragment<State : BaseState, Action, Event, ViewModel : BaseViewModel<State, Action, Event>> :
    Fragment() {

    companion object {
        const val PERM_NAME: String = Manifest.permission.RECORD_AUDIO
    }

    protected abstract val vm: ViewModel

    @get:LayoutRes
    protected abstract val resourceId: Int

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(resourceId, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.stateFlow
            .flowWithLifecycle(
                lifecycle = viewLifecycleOwner.lifecycle,
                minActiveState = Lifecycle.State.STARTED
            )
            .onEach(::render)
            .launchIn(scope = viewLifecycleOwner.lifecycleScope)
        vm.eventFlow
            .flowWithLifecycle(
                lifecycle = viewLifecycleOwner.lifecycle,
                minActiveState = Lifecycle.State.RESUMED
            )
            .onEach(::handleEvent)
            .launchIn(scope = viewLifecycleOwner.lifecycleScope)
    }

    protected open fun render(state: State) {}
    protected open fun handleEvent(event: Event) {}
    protected fun submitAction(action: Action) = vm.submitAction(action)

    protected fun isPermissionGranted(): Boolean = ContextCompat
        .checkSelfPermission(requireContext(), PERM_NAME) == PackageManager.PERMISSION_GRANTED
}
