package com.magic.habbittracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import com.magic.habbittracker.models.Habit
import com.magic.habbittracker.models.HabitCategory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

/**
 * Displays detailed statistics for a category
 */
@Composable
fun CategoryStatisticsCard(
    category: HabitCategory,
    habits: List<Habit>,
    isExpanded: Boolean = false,
    onToggleExpand: () -> Unit
) {
    val categoryHabits = habits.filter { it.category == category }
    
    if (categoryHabits.isEmpty()) return
    
    val completionRate = categoryHabits.map { it.getCompletionRate() }.average().toFloat()
    val completedToday = categoryHabits.count { it.isCompletedToday() }
    val totalHabits = categoryHabits.size
    val streak = calculateCategoryStreak(categoryHabits)
    
    val categoryColor = Color(android.graphics.Color.parseColor(category.colorHex))
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = categoryColor.copy(alpha = 0.1f)
        ),
        onClick = onToggleExpand
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Category header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category color indicator
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(categoryColor)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = category.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = categoryColor
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    text = "$completedToday/$totalHabits today",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Completion rate bar
            LinearProgressIndicator(
                progress = { completionRate },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = categoryColor,
                trackColor = categoryColor.copy(alpha = 0.2f)
            )
            
            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Detailed stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        value = "${(completionRate * 100).roundToInt()}%",
                        label = "Completion",
                        color = categoryColor
                    )
                    
                    StatItem(
                        value = streak.toString(),
                        label = "Streak",
                        color = categoryColor
                    )
                    
                    StatItem(
                        value = categoryHabits.size.toString(),
                        label = "Habits",
                        color = categoryColor
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // List of habits in this category
                categoryHabits.forEach { habit ->
                    HabitStatItem(
                        habit = habit,
                        categoryColor = categoryColor
                    )
                }
            }
        }
    }
}

@Composable
fun StatItem(
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun HabitStatItem(
    habit: Habit,
    categoryColor: Color
) {
    val completionRate = habit.getCompletionRate()
    val isCompletedToday = habit.isCompletedToday()
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Completion indicator
        Icon(
            imageVector = if (isCompletedToday) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = if (isCompletedToday) "Completed" else "Not completed",
            tint = if (isCompletedToday) categoryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Habit title
        Text(
            text = habit.title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        
        // Completion rate
        Text(
            text = "${(completionRate * 100).roundToInt()}%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * A component to display overall statistics
 */
@Composable
fun OverallStatisticsCard(habits: List<Habit>) {
    if (habits.isEmpty()) return
    
    val overallRate = habits.map { it.getCompletionRate() }.average().toFloat()
    val completedToday = habits.count { it.isCompletedToday() }
    val totalHabits = habits.size
    val streak = calculateStreak(habits)
    val longestHabit = habits.maxByOrNull { it.createdDate }
    val oldestHabit = habits.minByOrNull { it.createdDate }
    
    val longestHabitDate = longestHabit?.createdDate?.format(DateTimeFormatter.ofPattern("MMM d, yyyy")) ?: "N/A"
    val oldestHabitDate = oldestHabit?.createdDate?.format(DateTimeFormatter.ofPattern("MMM d, yyyy")) ?: "N/A"
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Overall Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = "${(overallRate * 100).roundToInt()}%",
                    label = "Completion",
                    color = MaterialTheme.colorScheme.primary
                )
                
                StatItem(
                    value = streak.toString(),
                    label = "Streak",
                    color = MaterialTheme.colorScheme.primary
                )
                
                StatItem(
                    value = "$completedToday/$totalHabits",
                    label = "Today",
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Additional stats
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatRow("Total Habits", totalHabits.toString())
                StatRow("Newest Habit", longestHabit?.title ?: "None")
                StatRow("Started On", longestHabitDate)
                StatRow("Oldest Habit", oldestHabit?.title ?: "None")
                StatRow("Created On", oldestHabitDate)
            }
        }
    }
}

@Composable
fun StatRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Helper function to calculate streak for a specific category
 */
fun calculateCategoryStreak(habits: List<Habit>): Int {
    if (habits.isEmpty()) return 0
    
    var streak = 0
    var currentDate = LocalDate.now()
    
    while (true) {
        val allCompletedForDate = habits.all { habit -> 
            habit.completionHistory[currentDate] == true 
        }
        
        if (!allCompletedForDate) break
        
        streak++
        currentDate = currentDate.minusDays(1)
    }
    
    return streak
}

/**
 * Helper function to calculate overall streak
 */
fun calculateStreak(habits: List<Habit>): Int {
    if (habits.isEmpty()) return 0
    
    var streak = 0
    var currentDate = LocalDate.now()
    
    while (true) {
        val allCompletedForDate = habits.all { habit -> 
            habit.completionHistory[currentDate] == true 
        }
        
        if (!allCompletedForDate) break
        
        streak++
        currentDate = currentDate.minusDays(1)
    }
    
    return streak
}