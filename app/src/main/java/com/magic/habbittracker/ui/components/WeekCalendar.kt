package com.magic.habbittracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.*

@Composable
fun WeekCalendar(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    habitsForDate: Map<LocalDate, Int> = emptyMap(),
    completedHabitsForDate: Map<LocalDate, Int> = emptyMap()
) {
    var currentWeekStart by remember { mutableStateOf(getStartOfWeek(selectedDate)) }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        // Month and navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                currentWeekStart = currentWeekStart.minusWeeks(1)
            }) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Previous Week"
                )
            }
            
            Text(
                text = getMonthYearDisplay(currentWeekStart),
                style = MaterialTheme.typography.titleMedium
            )
            
            IconButton(onClick = {
                currentWeekStart = currentWeekStart.plusWeeks(1)
            }) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "Next Week"
                )
            }
        }
        
        // Days of week
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            for (i in 0..6) {
                val day = currentWeekStart.plusDays(i.toLong())
                val isSelected = day.isEqual(selectedDate)
                val hasHabits = habitsForDate[day] ?: 0 > 0
                val allCompleted = (completedHabitsForDate[day] ?: 0) == (habitsForDate[day] ?: 0) && hasHabits
                val someCompleted = (completedHabitsForDate[day] ?: 0) > 0 && !allCompleted
                
                val backgroundColor = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    day.isEqual(LocalDate.now()) -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    else -> Color.Transparent
                }
                
                val textColor = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    day.isEqual(LocalDate.now()) -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                }
                
                val borderColor = when {
                    day.isEqual(LocalDate.now()) && !isSelected -> MaterialTheme.colorScheme.primary
                    else -> Color.Transparent
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp)
                ) {
                    // Day name (Mon, Tue, etc)
                    Text(
                        text = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                        style = MaterialTheme.typography.labelMedium,
                        color = textColor
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Date circle
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(backgroundColor)
                            .border(1.dp, borderColor, CircleShape)
                            .clickable { onDateSelected(day) }
                    ) {
                        Text(
                            text = day.dayOfMonth.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor,
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Indicator dot
                    if (hasHabits) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        allCompleted -> MaterialTheme.colorScheme.tertiary
                                        someCompleted -> MaterialTheme.colorScheme.secondary
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                )
                        )
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

private fun getStartOfWeek(date: LocalDate): LocalDate {
    val weekFields = WeekFields.of(Locale.getDefault())
    val dayOfWeek = date.get(weekFields.dayOfWeek())
    return date.minusDays((dayOfWeek - 1).toLong())
}

private fun getMonthYearDisplay(weekStart: LocalDate): String {
    val weekEnd = weekStart.plusDays(6)
    val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
    
    return if (weekStart.month == weekEnd.month) {
        weekStart.format(formatter)
    } else if (weekStart.year == weekEnd.year) {
        "${weekStart.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())} - ${weekEnd.format(formatter)}"
    } else {
        "${weekStart.format(formatter)} - ${weekEnd.format(formatter)}"
    }
}
