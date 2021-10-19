package ru.ya.cup.domain.entity

enum class RecordLabel {
    SILENCE, EXHALE, INHALE;

    companion object {
        fun fromString(name: String): RecordLabel = when (name) {
            "1 Exhale" -> EXHALE
            "2 Inhale" -> INHALE
            else -> SILENCE
        }
    }
}
