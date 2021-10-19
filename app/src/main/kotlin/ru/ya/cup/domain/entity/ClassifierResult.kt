package ru.ya.cup.domain.entity

import java.io.Serializable

data class ClassifierResult(val score: Float = 0f, val label: String) : Serializable
