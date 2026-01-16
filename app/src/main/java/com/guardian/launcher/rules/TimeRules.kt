package com.guardian.launcher.rules

import java.time.DayOfWeek
import java.time.LocalTime

/**
 * Time-based rules.
 * 
 * Controls:
 * - Daily time limits
 * - Allowed hours (e.g., 9 AM - 6 PM)
 * - Different rules for weekdays vs weekends
 * - Bedtime enforcement
 */
data class TimeRule(
    override val id: String,
    override val name: String,
    override val enabled: Boolean,
    val daysOfWeek: Set<DayOfWeek>,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val maxDailyMinutes: Int? = null
) : Rule() {
    
    override fun evaluate(): Boolean {
        // TODO: Check current day of week
        // TODO: Check current time
        // TODO: Check usage today
        return false
    }
}

/**
 * Time rule builder for easier creation
 */
class TimeRuleBuilder {
    private var id: String = ""
    private var name: String = ""
    private var enabled: Boolean = true
    private var daysOfWeek: Set<DayOfWeek> = setOf()
    private var startTime: LocalTime = LocalTime.MIN
    private var endTime: LocalTime = LocalTime.MAX
    private var maxDailyMinutes: Int? = null
    
    fun id(id: String) = apply { this.id = id }
    fun name(name: String) = apply { this.name = name }
    fun enabled(enabled: Boolean) = apply { this.enabled = enabled }
    fun daysOfWeek(days: Set<DayOfWeek>) = apply { this.daysOfWeek = days }
    fun timeRange(start: LocalTime, end: LocalTime) = apply {
        this.startTime = start
        this.endTime = end
    }
    fun maxDailyMinutes(minutes: Int) = apply { this.maxDailyMinutes = minutes }
    
    fun build() = TimeRule(
        id = id,
        name = name,
        enabled = enabled,
        daysOfWeek = daysOfWeek,
        startTime = startTime,
        endTime = endTime,
        maxDailyMinutes = maxDailyMinutes
    )
}
