package com.magic.habbittracker.viewmodels

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.magic.habbittracker.data.*
import com.magic.habbittracker.data.HabitData
import com.magic.habbittracker.data.CategoryData
import com.magic.habbittracker.models.Habit
import com.magic.habbittracker.models.HabitCategory
import com.magic.habbittracker.models.HabitFrequency
import com.magic.habbittracker.models.HabitPriority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import android.util.Log
import com.magic.habbittracker.data.jsonSerializer as Json
import java.time.LocalDate

class HabitViewModel(application: Application) : AndroidViewModel(application) {
    private val userPreferencesRepository = UserPreferencesRepository(application)
    private val habitDataRepository = HabitDataRepository(application)
    private val notificationHelper = NotificationHelper(application)
    
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode
    
    private val _areNotificationsEnabled = MutableStateFlow(true)
    val areNotificationsEnabled: StateFlow<Boolean> = _areNotificationsEnabled
    
    private val _reminderTime = MutableStateFlow("08:00")
    val reminderTime: StateFlow<String> = _reminderTime
    
    private val _userNickname = MutableStateFlow("Habit Master")
    val userNickname: StateFlow<String> = _userNickname
    
    private val _motivationalPhrase = MutableStateFlow("Keep up the good work!")
    val motivationalPhrase: StateFlow<String> = _motivationalPhrase
    
    private val _versionClickCount = MutableStateFlow(0)
    val versionClickCount: StateFlow<Int> = _versionClickCount
    
    private val _habits = mutableStateListOf<Habit>()
    val habits: List<Habit> get() = _habits
    
    private val _categories = mutableStateListOf<HabitCategory>()
    val categories: List<HabitCategory> get() = _categories
    
    val selectedDate = mutableStateOf(LocalDate.now())
    val selectedCategory = mutableStateOf<HabitCategory?>(null)
    
    val selectedScreen = mutableStateOf(Screen.CALENDAR)
    
    init {
        notificationHelper.createNotificationChannel()
        
        _categories.addAll(HabitCategory.DEFAULT_CATEGORIES)
        
        loadSavedData()
        
        viewModelScope.launch {
            userPreferencesRepository.isDarkMode.collect { isDarkMode ->
                _isDarkMode.value = isDarkMode
            }
        }
        
        viewModelScope.launch {
            userPreferencesRepository.areNotificationsEnabled.collect { areEnabled ->
                _areNotificationsEnabled.value = areEnabled
                updateNotificationSchedule()
            }
        }
        
        viewModelScope.launch {
            userPreferencesRepository.reminderTime.collect { time ->
                _reminderTime.value = time
                updateNotificationSchedule()
            }
        }
        
        viewModelScope.launch {
            userPreferencesRepository.userNickname.collect { nickname ->
                _userNickname.value = nickname
            }
        }
        
        viewModelScope.launch {
            userPreferencesRepository.motivationalPhrase.collect { phrase ->
                _motivationalPhrase.value = phrase
            }
        }
        
        viewModelScope.launch {
            userPreferencesRepository.versionClickCount.collect { count ->
                _versionClickCount.value = count
            }
        }
        
        viewModelScope.launch {
            userPreferencesRepository.updateMotivationalPhrase()
        }
    }
    
    private fun loadSavedData() {
        viewModelScope.launch {
            try {
                val savedCategories = habitDataRepository.categories.first()
                
                val customCategories = savedCategories.map { categoryData ->
                    habitDataRepository.run { categoryData.toHabitCategory() }
                }
                
                _categories.removeAll { it !in HabitCategory.DEFAULT_CATEGORIES }
                
                _categories.addAll(customCategories)
                
                val savedHabits = habitDataRepository.habits.first()
                
                val habits = savedHabits.map { habitData ->
                    habitDataRepository.run { habitData.toHabit(_categories) }
                }
                
                if (habits.isNotEmpty()) {
                    _habits.clear()
                    _habits.addAll(habits)
                } else {
                    addSampleHabits()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                addSampleHabits()
            }
        }
    }
    
    private fun addSampleHabits() {
        _habits.clear()
    }
    
    private fun saveHabits() {
        viewModelScope.launch {
            val habitDataList = _habits.map { habitDataRepository.run { it.toHabitData() } }
            habitDataRepository.saveHabits(habitDataList)
        }
    }
    
    private fun saveCategories() {
        viewModelScope.launch {
            val customCategories = _categories.filter { it !in HabitCategory.DEFAULT_CATEGORIES }
            val categoryDataList = customCategories.map { habitDataRepository.run { it.toCategoryData() } }
            habitDataRepository.saveCategories(categoryDataList)
        }
    }
    
    fun getFilteredHabits(): List<Habit> {
        return habits.filter { habit ->
            selectedCategory.value?.let { habit.category == it } ?: true
        }
    }
    
    fun getAvailableHabitsForDate(date: LocalDate): List<Habit> {
        return habits.filter { habit ->
            habit.isAvailableForDate(date) && (selectedCategory.value?.let { habit.category == it } ?: true)
        }
    }
    
    fun toggleHabitCompletion(habitId: Int) {
        val index = _habits.indexOfFirst { it.id == habitId }
        if (index != -1) {
            val habit = _habits[index]
            val updatedHabit = habit.toggleCompletionForDate(selectedDate.value)
            _habits[index] = updatedHabit
            saveHabits()
        }
    }
    
    fun addHabit(habit: Habit) {
        _habits.add(habit.copy(id = getNextId()))
        saveHabits()
    }
    
    fun updateHabit(habit: Habit) {
        val index = _habits.indexOfFirst { it.id == habit.id }
        if (index != -1) {
            _habits[index] = habit
            saveHabits()
        }
    }
    
    fun deleteHabit(habitId: Int) {
        _habits.removeIf { it.id == habitId }
        saveHabits()
    }
    
    fun setSelectedDate(date: LocalDate) {
        selectedDate.value = date
    }
    
    fun setSelectedCategory(category: HabitCategory?) {
        selectedCategory.value = category
    }
    
    fun setSelectedScreen(screen: Screen) {
        selectedScreen.value = screen
    }
    
    private fun getNextId(): Int {
        return if (_habits.isEmpty()) 1 else _habits.maxOf { it.id } + 1
    }
    
    fun addCategory(displayName: String, colorHex: String) {
        val id = displayName.lowercase().replace(" ", "_") + "_" + System.currentTimeMillis()
        val newCategory = HabitCategory(id, displayName, colorHex)
        if (!_categories.contains(newCategory)) {
            _categories.add(newCategory)
            saveCategories()
        }
    }
    
    fun updateCategory(category: HabitCategory, newDisplayName: String, newColorHex: String) {
        val index = _categories.indexOfFirst { it.id == category.id }
        if (index != -1) {
            val updatedCategory = category.copy(displayName = newDisplayName, colorHex = newColorHex)
            _categories[index] = updatedCategory
            
            // Update all habits with this category
            _habits.forEachIndexed { habitIndex, habit ->
                if (habit.category.id == category.id) {
                    _habits[habitIndex] = habit.copy(category = updatedCategory)
                }
            }
            
            // Update selected category if needed
            if (selectedCategory.value?.id == category.id) {
                selectedCategory.value = updatedCategory
            }
            
            saveCategories()
            saveHabits()
        }
    }
    
    fun deleteCategory(categoryId: String) {
        val index = _categories.indexOfFirst { it.id == categoryId }
        if (index != -1) {
            val category = _categories[index]
            
            // Don't delete default categories
            if (category in HabitCategory.DEFAULT_CATEGORIES) return
            
            _categories.removeAt(index)
            
            // Move habits with this category to "Other"
            _habits.forEachIndexed { habitIndex, habit ->
                if (habit.category.id == categoryId) {
                    _habits[habitIndex] = habit.copy(category = HabitCategory.OTHER)
                }
            }
            
            // Reset selected category if needed
            if (selectedCategory.value?.id == categoryId) {
                selectedCategory.value = null
            }
            
            saveCategories()
            saveHabits()
        }
    }
    
    fun resetAllData() {
        _habits.clear()
        
        _categories.clear()
        _categories.addAll(HabitCategory.DEFAULT_CATEGORIES)
        
        viewModelScope.launch {
            try {
                userPreferencesRepository.resetAllPreferences()
                
                userPreferencesRepository.setDarkMode(false)
                userPreferencesRepository.setNotificationsEnabled(true)
                userPreferencesRepository.setReminderTime("08:00")
                userPreferencesRepository.setUserNickname("Habit Master")
                userPreferencesRepository.updateMotivationalPhrase()
                userPreferencesRepository.resetVersionClickCount()
                userPreferencesRepository.setFirstRunCompleted()
                userPreferencesRepository.setOnboardingCompleted()
                
                Log.d("HabitViewModel", "Clearing all saved data")
                habitDataRepository.clearAllData()
                
                habitDataRepository.saveHabits(emptyList())
                habitDataRepository.saveCategories(emptyList())
                
                _isDarkMode.value = false
                _areNotificationsEnabled.value = true
                _reminderTime.value = "08:00"
                _versionClickCount.value = 0
                
                selectedDate.value = LocalDate.now()
                selectedCategory.value = null
                
                updateNotificationSchedule()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setDarkMode(enabled)
            _isDarkMode.value = enabled
        }
    }
    
    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setNotificationsEnabled(enabled)
            _areNotificationsEnabled.value = enabled
            updateNotificationSchedule()
        }
    }
    
    fun setReminderTime(time: String) {
        viewModelScope.launch {
            userPreferencesRepository.setReminderTime(time)
            _reminderTime.value = time
            updateNotificationSchedule()
        }
    }
    
    fun updateNotificationSchedule() {
        notificationHelper.scheduleReminder(
            _reminderTime.value,
            _areNotificationsEnabled.value
        )
    }
    
    fun setUserNickname(nickname: String) {
        viewModelScope.launch {
            userPreferencesRepository.setUserNickname(nickname)
            _userNickname.value = nickname
        }
    }
    
    fun handleVersionClick(): Boolean {
        viewModelScope.launch {
            val newCount = userPreferencesRepository.incrementVersionClickCount()
            _versionClickCount.value = newCount
            
            if (newCount >= 5) {
                notificationHelper.showNotification()
                userPreferencesRepository.resetVersionClickCount()
                _versionClickCount.value = 0
                return@launch
            }
        }
        
        return _versionClickCount.value >= 5
    }
    
    fun exportData(): String {
        val habitDataList = _habits.map { habitDataRepository.run { it.toHabitData() } }
        val categoryDataList = _categories
            .filter { it !in HabitCategory.DEFAULT_CATEGORIES }
            .map { habitDataRepository.run { it.toCategoryData() } }
        
        val exportData = ExportData(
            habits = habitDataList,
            categories = categoryDataList
        )
        
        return Json.encodeToString(exportData)
    }
    
    fun importData(jsonData: String): Boolean {
        return try {
            val importedData = Json.decodeFromString<ExportData>(jsonData)
            
            val importedCategories = importedData.categories.map { categoryData ->
                habitDataRepository.run { categoryData.toHabitCategory() }
            }
            
            _categories.removeAll { it !in HabitCategory.DEFAULT_CATEGORIES }
            _categories.addAll(importedCategories)
            
            val importedHabits = importedData.habits.map { habitData ->
                habitDataRepository.run { habitData.toHabit(_categories) }
            }
            
            _habits.clear()
            _habits.addAll(importedHabits)
            
            saveHabits()
            saveCategories()
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    fun sendTestNotification() {
        notificationHelper.showNotification()
    }
}

@kotlinx.serialization.Serializable
data class ExportData(
    val habits: List<HabitData>,
    val categories: List<CategoryData>
)

enum class Screen {
    CALENDAR, CATEGORIES, PROFILE
}
