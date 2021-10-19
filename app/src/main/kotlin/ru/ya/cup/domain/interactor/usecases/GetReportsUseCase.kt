package ru.ya.cup.domain.interactor.usecases

import ru.ya.cup.data.ReportsStore
import ru.ya.cup.domain.entity.Report
import ru.ya.cup.domain.interactor.UseCase
import javax.inject.Inject

class GetReportsUseCase @Inject constructor(
    private val reportsStore: ReportsStore
) : UseCase<GetReportsUseCase.Params, List<Report>>() {
    override suspend fun execute(params: Params): Result<List<Report>> =
        Result.success(reportsStore.getReports())

    object Params
}
