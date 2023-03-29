package tech.relaycorp.ping.ui.ping

import androidx.annotation.StringRes
import tech.relaycorp.ping.R
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

data class ExpireDuration(val value: Int, val unit: Unit) {
    enum class Unit(val valueOptions: List<Int>, @StringRes val stringRes: Int) {
        Minutes(listOf(1, 2, 3, 5, 10, 15, 20, 30, 40, 50), R.string.minutes),
        Hours(listOf(1, 2, 3, 6, 12), R.string.hours),
        Days(listOf(1, 2, 3, 4, 5, 10, 15, 20, 25), R.string.days),
        Months(listOf(1, 2, 3, 4, 5, 6), R.string.months)
    }

    fun toKotlinDuration() = when (unit) {
        Unit.Minutes -> value.minutes
        Unit.Hours -> value.hours
        Unit.Days -> value.days
        Unit.Months -> value.days * 30
    }

    companion object {
        val DEFAULT = ExpireDuration(3, Unit.Days)
    }
}
