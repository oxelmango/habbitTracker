package com.magic.habbittracker.data

object MotivationalPhrases {
    private val phrases = listOf(
        "Keep up the good work!",
        "You're making great progress!",
        "One habit at a time!",
        "Small steps, big changes!",
        "Consistency is key!",
        "Building a better you!",
        "Every check counts!",
        "On the right track!",
        "Making it happen!",
        "Habit by habit, day by day!",
        "Progress over perfection!",
        "You've got this!",
        "Each day is a new opportunity!",
        "Building momentum!",
        "Celebrate your progress!",
        "Habits shape who you become!",
        "Stick with it!",
        "Creating lasting change!",
        "The journey continues!",
        "Your future self thanks you!"
    )
    
    fun getRandomPhrase(): String {
        return phrases.random()
    }
}