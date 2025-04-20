package com.magic.habbittracker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.magic.habbittracker.models.Habit
import com.magic.habbittracker.models.HabitCategory
import com.magic.habbittracker.models.HabitFrequency
import com.magic.habbittracker.models.HabitPriority
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val jsonSerializer = Json { 
    ignoreUnknownKeys = true 
    coerceInputValues = true
    prettyPrint = true
    isLenient = true
}

class HabitDataRepository(private val context: Context) {

    companion object {
        private val Context.habitDataStore: DataStore<Preferences> by preferencesDataStore(
            name = "habit_data"
        )

        val HABITS_KEY = stringPreferencesKey("habits")
        val CATEGORIES_KEY = stringPreferencesKey("categories")
        
        private val DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE
        private val DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    }

    val habits: Flow<List<HabitData>> = context.habitDataStore.data.map { preferences ->
        val habitsJson = preferences[HABITS_KEY] ?: "[]"
        try {
            jsonSerializer.decodeFromString<List<HabitData>>(habitsJson)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    val categories: Flow<List<CategoryData>> = context.habitDataStore.data.map { preferences ->
        val categoriesJson = preferences[CATEGORIES_KEY] ?: "[]"
        try {
            jsonSerializer.decodeFromString<List<CategoryData>>(categoriesJson)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun saveHabits(habits: List<HabitData>) {
        try {
            val habitsJson = jsonSerializer.encodeToString(habits)
            context.habitDataStore.edit { preferences ->
                preferences[HABITS_KEY] = habitsJson
            }
            android.util.Log.d("HabitDataRepository", "Saved ${habits.size} habits")
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("HabitDataRepository", "Error saving habits", e)
        }
    }

    suspend fun saveCategories(categories: List<CategoryData>) {
        try {
            val categoriesJson = jsonSerializer.encodeToString(categories)
            context.habitDataStore.edit { preferences ->
                preferences[CATEGORIES_KEY] = categoriesJson
            }
            android.util.Log.d("HabitDataRepository", "Saved ${categories.size} categories")
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("HabitDataRepository", "Error saving categories", e)
        }
    }
    
    suspend fun clearAllData() {
        try {
            context.habitDataStore.edit { preferences ->
                preferences.clear()
            }
            android.util.Log.d("HabitDataRepository", "Cleared all stored data")
        } catch (e: Exception) {
            e.printStackTrace()
            android.util.Log.e("HabitDataRepository", "Error clearing data", e)
        }
    }

    fun Habit.toHabitData(): HabitData {
        val completionMap = completionHistory.map { 
            it.key.toString() to it.value 
        }.toMap()
        
        return HabitData(
            id = id,
            title = title,
            description = description,
            categoryId = category.id,
            priorityLevel = priority.level,
            frequencyName = frequency.name,
            iconName = iconName ?: "",
            createdDate = createdDate.format(DATE_TIME_FORMATTER),
            startDate = startDate.format(DATE_FORMATTER),
            completionHistory = completionMap,
            reminderTime = reminderTime?.format(DATE_TIME_FORMATTER)
        )
    }

    fun HabitData.toHabit(allCategories: List<HabitCategory>): Habit {
        val category = allCategories.find { it.id == categoryId } ?: HabitCategory.OTHER
        val completionMap = completionHistory.map {
            LocalDate.parse(it.key) to it.value
        }.toMap().toMutableMap()
        
        return Habit(
            id = id,
            title = title,
            description = description,
            category = category,
            priority = HabitPriority.values().find { it.level == priorityLevel } ?: HabitPriority.MEDIUM,
            frequency = HabitFrequency.valueOf(frequencyName),
            iconName = iconName.takeIf { it.isNotEmpty() },
            createdDate = LocalDateTime.parse(createdDate, DATE_TIME_FORMATTER),
            startDate = LocalDate.parse(startDate, DATE_FORMATTER),
            completionHistory = completionMap,
            reminderTime = reminderTime?.let { LocalDateTime.parse(it, DATE_TIME_FORMATTER) }
        )
    }

    fun HabitCategory.toCategoryData(): CategoryData {
        return CategoryData(
            id = id,
            displayName = displayName,
            colorHex = colorHex
        )
    }

    fun CategoryData.toHabitCategory(): HabitCategory {
        return HabitCategory(
            id = id,
            displayName = displayName,
            colorHex = colorHex
        )
    }
}

@kotlinx.serialization.Serializable
data class HabitData(
    val id: Int,
    val title: String,
    val description: String,
    val categoryId: String,
    val priorityLevel: Int,
    val frequencyName: String,
    val iconName: String,
    val createdDate: String,
    val startDate: String,
    val completionHistory: Map<String, Boolean>,
    val reminderTime: String?
)

@kotlinx.serialization.Serializable
data class CategoryData(
    val id: String,
    val displayName: String,
    val colorHex: String
)