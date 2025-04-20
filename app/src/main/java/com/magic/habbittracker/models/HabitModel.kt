package com.magic.habbittracker.models

import java.time.LocalDate
import java.time.LocalDateTime

data class HabitCategory(val id: String, val displayName: String, val colorHex: String) {
    companion object {
        val PERSONAL = HabitCategory("personal", "Personal", "#FF9800")
        val HEALTH = HabitCategory("health", "Health", "#4CAF50")
        val ACADEMIC = HabitCategory("academic", "Academic", "#2196F3")
        val WORK = HabitCategory("work", "Work", "#9C27B0")
        val OTHER = HabitCategory("other", "Other", "#607D8B")
        
        val DEFAULT_CATEGORIES = listOf(PERSONAL, HEALTH, ACADEMIC, WORK, OTHER)
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HabitCategory) return false
        return id == other.id
    }
    
    override fun hashCode(): Int {
        return id.hashCode()
    }
}

enum class HabitFrequency {
    DAILY, WEEKLY, MONTHLY
}

enum class HabitPriority(val level: Int) {
    LOW(1),
    MEDIUM(2),
    HIGH(3)
}

data class Habit(
    val id: Int,
    val title: String,
    val description: String,
    val category: HabitCategory = HabitCategory.OTHER,
    val priority: HabitPriority = HabitPriority.MEDIUM,
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val iconName: String? = null,
    val createdDate: LocalDateTime = LocalDateTime.now(),
    val startDate: LocalDate = LocalDate.now(),
    val completionHistory: MutableMap<LocalDate, Boolean> = mutableMapOf(),
    val reminderTime: LocalDateTime? = null
) {
    fun isCompletedToday(): Boolean {
        return completionHistory[LocalDate.now()] == true
    }
    
    fun toggleCompletionForDate(date: LocalDate): Habit {
        if (date.isBefore(startDate)) {
            return this
        }
        
        val updatedHistory = completionHistory.toMutableMap()
        updatedHistory[date] = !(completionHistory[date] ?: false)
        return this.copy(completionHistory = updatedHistory)
    }
    
    fun getCompletionRate(): Float {
        if (completionHistory.isEmpty()) return 0f
        val completedDays = completionHistory.count { it.value }
        return completedDays.toFloat() / completionHistory.size
    }
    
    fun isAvailableForDate(date: LocalDate): Boolean {
        return !date.isBefore(startDate)
    }
}
