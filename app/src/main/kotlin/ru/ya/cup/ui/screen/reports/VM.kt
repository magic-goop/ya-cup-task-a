package ru.ya.cup.ui.screen.reports

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.ya.cup.domain.entity.Report
import ru.ya.cup.domain.interactor.usecases.DeleteReportUseCase
import ru.ya.cup.domain.interactor.usecases.GetReportsUseCase
import ru.ya.cup.ui.common.reduce
import ru.ya.cup.ui.common.viewmodel.BaseViewModel
import javax.inject.Inject

@HiltViewModel
internal class VM @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getReportsUseCase: GetReportsUseCase,
    private val deleteReportUseCase: DeleteReportUseCase
) : BaseViewModel<State, Action, Nothing>(savedStateHandle, State()) {

    override fun processAction(action: Action) {
        super.processAction(action)
        when (action) {
            Action.Init -> loadReports()
            is Action.DeleteReport -> deleteReport(action.report)
        }
    }

    private fun loadReports() {
        getReportsUseCase(GetReportsUseCase.Params)
            .flowOn(Dispatchers.Main)
            .onEach(::processReports)
            .launchIn(scope = viewModelScope)
    }

    private fun deleteReport(report: Report) {
        viewModelScope.launch {
            val res = deleteReportUseCase.executeSync(DeleteReportUseCase.Params(report))
            if (res.isSuccess && res.getOrDefault(false)) {
                loadReports()
            }
        }
    }

    private fun processReports(result: Result<List<Report>>) {
        render(state.reduce { copy(reports = result.getOrDefault(emptyList())) })
    }
}
