package com.magic.habbittracker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.magic.habbittracker.data.HabitIcons
import com.magic.habbittracker.models.Habit
import com.magic.habbittracker.models.HabitPriority
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitCard(
    habit: Habit,
    isCompletedForSelectedDate: Boolean,
    onToggleCompleted: (Int) -> Unit,
    onEdit: (Habit) -> Unit,
    onDelete: (Int) -> Unit
) {
    val categoryColor = Color(android.graphics.Color.parseColor(habit.category.colorHex))
    val backgroundColor = animateColorAsState(
        targetValue = if (isCompletedForSelectedDate) {
            categoryColor.copy(alpha = 0.1f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(300)
    )
    
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor.value
        ),
        onClick = { onToggleCompleted(habit.id) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Habit icon with priority indicator
            Box(
                contentAlignment = Alignment.Center
            ) {
                // Priority indicator as background
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            when (habit.priority) {
                                HabitPriority.HIGH -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                HabitPriority.MEDIUM -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                                HabitPriority.LOW -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                            }
                        )
                )
                
                // Habit icon
                Icon(
                    imageVector = HabitIcons.getIconByName(habit.iconName),
                    contentDescription = habit.title,
                    modifier = Modifier.size(24.dp),
                    tint = when (habit.priority) {
                        HabitPriority.HIGH -> MaterialTheme.colorScheme.error
                        HabitPriority.MEDIUM -> MaterialTheme.colorScheme.tertiary
                        HabitPriority.LOW -> MaterialTheme.colorScheme.secondary
                    }
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Checkbox
            Checkbox(
                checked = isCompletedForSelectedDate,
                onCheckedChange = { onToggleCompleted(habit.id) },
                colors = CheckboxDefaults.colors(
                    checkedColor = categoryColor
                )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = habit.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (isCompletedForSelectedDate) TextDecoration.LineThrough else TextDecoration.None,
                    modifier = Modifier.alpha(if (isCompletedForSelectedDate) 0.7f else 1f)
                )
                
                Text(
                    text = habit.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.alpha(0.7f)
                )
                
                // Row for category and start date
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category chip
                    Surface(
                        modifier = Modifier.height(24.dp),
                        color = categoryColor.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = habit.category.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = categoryColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Start date indicator - only if not starting from today
                    if (habit.startDate != LocalDate.now()) {
                        Text(
                            text = "From ${habit.startDate.format(DateTimeFormatter.ofPattern("MMM d"))}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.alpha(0.7f)
                        )
                    }
                }
            }
            
            // Menu
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Options"
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            onEdit(habit)
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit"
                            )
                        }
                    )
                    
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            onDelete(habit.id)
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete"
                            )
                        }
                    )
                }
            }
        }
        
        // Progress indicator for completion rate
        val completionRate = habit.getCompletionRate()
        if (habit.completionHistory.isNotEmpty()) {
            LinearProgressIndicator(
                progress = { completionRate },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = categoryColor,
                trackColor = categoryColor.copy(alpha = 0.1f),
                strokeCap = StrokeCap.Round
            )
        }
    }
}
