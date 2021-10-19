package ru.ya.cup.domain.interactor.usecases

import ru.ya.cup.data.ReportsStore
import ru.ya.cup.domain.entity.Report
import ru.ya.cup.domain.interactor.UseCase
import javax.inject.Inject

class DeleteReportUseCase @Inject constructor(
    private val reportsStore: ReportsStore
) : UseCase<DeleteReportUseCase.Params, Boolean>() {

    override suspend fun execute(params: Params): Result<Boolean> {
        return try {
            reportsStore.deleteReport(params.report)
            Result.success(true)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    data class Params(val report: Report)
}