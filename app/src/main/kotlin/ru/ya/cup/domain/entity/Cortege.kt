package ru.ya.cup.domain.entity

import ru.ya.cup.R
import ru.ya.cup.resources.ResourcesManager

/*
type-(Inhale|Exhale|Pause), start time - mm:ss.ms, duration - ss.ms
 */
data class Cortege(val label: RecordLabel, val starTime: String, val duration: String) {

    fun toCsvRow(resourcesManager: ResourcesManager): String {
        val type = when (label) {
            RecordLabel.INHALE -> resourcesManager.getString(R.string.inhale)
            RecordLabel.EXHALE -> resourcesManager.getString(R.string.exhale)
            RecordLabel.SILENCE -> resourcesManager.getString(R.string.pause)
        }
        return "$type,$starTime,$duration"
    }
}
