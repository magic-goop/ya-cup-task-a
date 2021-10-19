package ru.ya.cup.ui.common

import java.io.Serializable

abstract class BaseState : Serializable

fun <S : BaseState> S.reduce(reduceState: S.() -> S): S {
    return reduceState()
}
