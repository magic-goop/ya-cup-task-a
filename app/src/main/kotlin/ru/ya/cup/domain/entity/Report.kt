package ru.ya.cup.domain.entity

import java.io.Serializable

data class Report(val id: Long, val timestamp: Long, val fileName: String) : Serializable
