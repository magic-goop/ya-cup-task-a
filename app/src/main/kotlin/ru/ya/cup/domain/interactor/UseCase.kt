package ru.ya.cup.domain.interactor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

abstract class UseCase<in Param, Res> {
    operator fun invoke(params: Param): Flow<Result<Res>> = flow {
        emit(execute(params))
    }.flowOn(Dispatchers.IO).catch { exception ->
        emit(Result.failure(exception))
    }

    protected abstract suspend fun execute(params: Param): Result<Res>

    suspend fun executeSync(params: Param): Result<Res> = execute(params)
}
