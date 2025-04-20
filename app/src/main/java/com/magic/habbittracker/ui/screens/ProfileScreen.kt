package com.magic.habbittracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.magic.habbittracker.data.NotificationHelper
import com.magic.habbittracker.models.Habit
import com.magic.habbittracker.models.HabitCategory
import com.magic.habbittracker.ui.components.*
import com.magic.habbittracker.viewmodels.HabitViewModel
import java.time.LocalDate
import androidx.compose.material.icons.filled.Check
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.Toast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun ProfileScreen(
    viewModel: HabitViewModel,
    onNavigateToCalendar: () -> Unit,
    innerPadding: PaddingValues
) {
    var selectedTab by remember { mutableStateOf(ProfileTab.STATISTICS) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    
    // Get preferences from ViewModel
    val isDarkMode = viewModel.isDarkMode.collectAsState().value
    val areNotificationsEnabled = viewModel.areNotificationsEnabled.collectAsState().value
    val reminderTime = viewModel.reminderTime.collectAsState().value
    
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)) {
        // Profile header
        ProfileHeader(viewModel)
        
        // Tabs
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            ProfileTab.values().forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = { Text(tab.title) },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title
                        )
                    }
                )
            }
        }
        
        // Tab content
        when (selectedTab) {
            ProfileTab.STATISTICS -> StatisticsTab(viewModel)
            ProfileTab.ACHIEVEMENTS -> AchievementsTab(viewModel)
            ProfileTab.SETTINGS -> SettingsTab(viewModel, onResetDataClick = { showResetConfirmDialog = true })
        }
        
        // Store context for toast
        val context = LocalContext.current
        
        // Show reset confirmation dialog if triggered
        ResetConfirmationDialog(
            showDialog = showResetConfirmDialog,
            onDismiss = { showResetConfirmDialog = false },
            onConfirm = {
                // Reset all data
                viewModel.resetAllData()
                showResetConfirmDialog = false
                
                // Show toast confirmation
                Toast.makeText(
                    context,
                    "All data has been reset",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }
}

@Composable
private fun ProfileHeader(viewModel: HabitViewModel) {
    val habits = viewModel.habits
    val totalHabits = habits.size
    val completedToday = habits.count { it.isCompletedToday() }
    val streakDays = calculateStreak(habits)
    
    // User nickname and motivational phrase
    val nickname = viewModel.userNickname.collectAsState().value
    val motivationalPhrase = viewModel.motivationalPhrase.collectAsState().value
    
    var showEditNicknameDialog by remember { mutableStateOf(false) }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar with edit option
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { showEditNicknameDialog = true }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(40.dp)
                )
                
                // Edit indicator
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .border(2.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = nickname,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = motivationalPhrase,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Stats summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = totalHabits.toString(),
                    label = "Total Habits",
                    icon = Icons.Default.List
                )
                
                StatItem(
                    value = "$completedToday/$totalHabits",
                    label = "Today's Progress",
                    icon = Icons.Default.CheckCircle
                )
                
                StatItem(
                    value = streakDays.toString(),
                    label = "Day Streak",
                    icon = Icons.Default.Star
                )
            }
        }
    }
    
    // Edit nickname dialog
    if (showEditNicknameDialog) {
        var newNickname by remember { mutableStateOf(nickname) }
        
        AlertDialog(
            onDismissRequest = { showEditNicknameDialog = false },
            title = { Text("Change Nickname") },
            text = {
                Column {
                    Text(
                        text = "Enter your new nickname:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = newNickname,
                        onValueChange = { newNickname = it },
                        label = { Text("Nickname") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newNickname.isNotBlank()) {
                            viewModel.setUserNickname(newNickname)
                        }
                        showEditNicknameDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEditNicknameDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatisticsTab(viewModel: HabitViewModel) {
    val habits = viewModel.habits
    val categories = viewModel.categories
    
    // Track expanded category cards
    val expandedCategories = remember { mutableStateListOf<String>() }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Statistics Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 12.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Overall statistics card
            OverallStatisticsCard(habits = habits)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Categories Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Category statistics cards
        items(categories) { category ->
            val isExpanded = expandedCategories.contains(category.id)
            
            CategoryStatisticsCard(
                category = category,
                habits = habits,
                isExpanded = isExpanded,
                onToggleExpand = {
                    if (isExpanded) {
                        expandedCategories.remove(category.id)
                    } else {
                        expandedCategories.add(category.id)
                    }
                }
            )
        }
        
        item {
            Text(
                text = "Recent Activity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Recent activity
            val today = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("MMM d")
            
            for (i in 0 downTo -6) {
                val date = today.plusDays(i.toLong())
                val dateStr = if (i == 0) "Today" else date.format(formatter)
                val completedOnDate = habits.count { habit -> 
                    habit.completionHistory[date] == true 
                }
                val totalForDate = habits.count { habit ->
                    habit.isAvailableForDate(date)
                }
                
                ActivityDayItem(
                    date = dateStr,
                    completed = completedOnDate,
                    total = totalForDate
                )
            }
            
            // Add bottom spacing
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
private fun CompletionRateBar(
    label: String,
    rate: Float,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "${(rate * 100).roundToInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        LinearProgressIndicator(
            progress = { rate },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

@Composable
private fun ActivityDayItem(
    date: String,
    completed: Int,
    total: Int
) {
    val progress = if (total > 0) completed.toFloat() / total else 0f
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = date,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(80.dp)
        )
        
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .weight(1f)
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.primaryContainer
        )
        
        Text(
            text = "$completed/$total",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(48.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun AchievementsTab(viewModel: HabitViewModel) {
    val habits = viewModel.habits
    
    val totalCompletions = habits.sumOf { habit -> 
        habit.completionHistory.count { it.value } 
    }
    val streak = calculateStreak(habits)
    val categoriesUsed = habits.map { it.category }.distinct().size
    val perfectDays = calculatePerfectDays(habits)
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Your Achievements",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 12.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Milestone achievements
        item {
            AchievementCard(
                title = "Habit Starter",
                description = "Complete your first habit",
                progress = if (totalCompletions > 0) 1f else 0f,
                icon = Icons.Default.Star
            )
        }
        
        item {
            AchievementCard(
                title = "Consistency Champion",
                description = "Maintain a 7-day streak",
                progress = (streak.toFloat() / 7f).coerceAtMost(1f),
                icon = Icons.Default.Refresh
            )
        }
        
        item {
            AchievementCard(
                title = "Variety Seeker",
                description = "Create habits in 5 different categories",
                progress = (categoriesUsed.toFloat() / 5f).coerceAtMost(1f),
                icon = Icons.Default.Star
            )
        }
        
        item {
            AchievementCard(
                title = "Perfect Week",
                description = "Complete all habits for 7 days in a row",
                progress = (perfectDays.toFloat() / 7f).coerceAtMost(1f),
                icon = Icons.Default.Star
            )
        }
        
        item {
            AchievementCard(
                title = "Habit Master",
                description = "Complete 100 habit check-ins",
                progress = (totalCompletions.toFloat() / 100f).coerceAtMost(1f),
                icon = Icons.Default.Star
            )
            
            // Add bottom spacing
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
private fun AchievementCard(
    title: String,
    description: String,
    progress: Float,
    icon: ImageVector
) {
    val isCompleted = progress >= 1f
    val backgroundColor = if (isCompleted) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isCompleted) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isCompleted) MaterialTheme.colorScheme.onPrimary
                          else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "${(progress * 100).roundToInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Completed",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun SettingsTab(viewModel: HabitViewModel, onResetDataClick: () -> Unit = {}) {
    // Get preferences from ViewModel
    val isDarkMode = viewModel.isDarkMode.collectAsState().value
    val areNotificationsEnabled = viewModel.areNotificationsEnabled.collectAsState().value
    val reminderTime = viewModel.reminderTime.collectAsState().value
    val context = LocalContext.current
    
    var showTimePickerDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportedData by remember { mutableStateOf("") }
    var importData by remember { mutableStateOf("") }
    var showImportResult by remember { mutableStateOf<Boolean?>(null) }
    
    // Track version clicks for secret feature
    val versionClickCount = viewModel.versionClickCount.collectAsState().value
    
    // Show time picker dialog if needed
    if (showTimePickerDialog) {
        TimePickerDialog(
            onDismiss = { showTimePickerDialog = false },
            onTimeSelected = { time ->
                viewModel.setReminderTime(time)
                showTimePickerDialog = false
            },
            initialTime = reminderTime
        )
    }
    
    // Show export dialog
    if (showExportDialog) {
        val exportJson = viewModel.exportData()
        
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export Data") },
            text = {
                Column {
                    Text(
                        text = "Your data has been exported. Copy the text below:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = exportJson,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .padding(8.dp)
                                .verticalScroll(rememberScrollState())
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    var copiedState by remember { mutableStateOf(false) }
                    val scope = rememberCoroutineScope()
                    
                    // Copy button that spans full width
                    Button(
                        onClick = {
                            // Copy to clipboard
                            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clipData = ClipData.newPlainText("Exported Habits Data", exportJson)
                            clipboardManager.setPrimaryClip(clipData)
                            
                            // Show copied state
                            copiedState = true
                            
                            // Reset after delay
                            scope.launch {
                                delay(2000)
                                copiedState = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (!copiedState) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Copy to Clipboard")
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Copied",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Copied!")
                        }
                    }
                }
            },
            confirmButton = { },
            dismissButton = {
                TextButton(
                    onClick = { showExportDialog = false }
                ) {
                    Text("Close")
                }
            }
        )
    }
    
    // Show import dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { 
                showImportDialog = false
                showImportResult = null
            },
            title = { Text("Import Data") },
            text = {
                Column {
                    Text(
                        text = "Paste your exported data here:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = importData,
                        onValueChange = { importData = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        placeholder = { Text("Paste JSON data here...") }
                    )
                    
                    // Show import result if available
                    showImportResult?.let { success ->
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Surface(
                            color = if (success) 
                                MaterialTheme.colorScheme.primaryContainer 
                            else 
                                MaterialTheme.colorScheme.errorContainer,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (success) 
                                        Icons.Default.CheckCircle 
                                    else 
                                        Icons.Default.Error,
                                    contentDescription = if (success) "Success" else "Error",
                                    tint = if (success) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.error
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    text = if (success) 
                                        "Data imported successfully!" 
                                    else 
                                        "Error: Invalid data format",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (success) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importData.isNotBlank()) {
                            val success = viewModel.importData(importData)
                            showImportResult = success
                            if (success) {
                                // Clear import data on success
                                importData = ""
                            }
                        }
                    }
                ) {
                    Text("Import")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showImportDialog = false
                        showImportResult = null 
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "App Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 12.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Theme settings
        item {
            SettingsSwitchItem(
                title = "Dark Mode",
                description = "Use dark theme",
                icon = Icons.Default.DarkMode,
                checked = isDarkMode,
                onCheckedChange = { enabled -> 
                    viewModel.setDarkMode(enabled)
                }
            )
        }
        
        // Notification settings
        item {
            // Dialog state
            var showPermissionDialog by remember { mutableStateOf(false) }
            
            // Permission dialog
            if (showPermissionDialog) {
                val activity = context as? ComponentActivity
                AlertDialog(
                    onDismissRequest = { 
                        showPermissionDialog = false 
                    },
                    title = { Text("Notification Permission") },
                    text = { 
                        Text("To show habit reminders, we need permission to send notifications. Please grant this permission in the next dialog.")
                    },
                    confirmButton = {
                        Button(onClick = {
                            if (activity != null) {
                                ActivityCompat.requestPermissions(
                                    activity,
                                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                                    0
                                )
                            }
                            viewModel.setNotificationsEnabled(true)
                            showPermissionDialog = false
                        }) {
                            Text("Continue")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { 
                            showPermissionDialog = false 
                        }) {
                            Text("Cancel")
                        }
                    }
                )
            }
            
            SettingsSwitchItem(
                title = "Notifications",
                description = "Enable habit reminders",
                icon = Icons.Default.Notifications,
                checked = areNotificationsEnabled,
                onCheckedChange = { enabled -> 
                    if (enabled) {
                        // Check if we need to request permission on Android 13+
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val activity = context as? ComponentActivity
                            if (activity != null) {
                                if (ContextCompat.checkSelfPermission(
                                    activity,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) != PackageManager.PERMISSION_GRANTED) {
                                    // Show permission dialog
                                    showPermissionDialog = true
                                    return@SettingsSwitchItem
                                }
                            }
                        }
                    }
                    
                    viewModel.setNotificationsEnabled(enabled)
                }
            )
        }
        
        // Reminder time
        item {
            SettingsItem(
                title = "Reminder Time",
                description = reminderTime,
                icon = Icons.Default.AccessTime,
                onClick = {
                    // Show the time picker dialog
                    showTimePickerDialog = true
                }
            )
        }
        
        // Data management
        item {
            Text(
                text = "Data Management",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 24.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        item {
            SettingsItem(
                title = "Export Data",
                description = "Save your habits to a file",
                icon = Icons.Default.SaveAlt,
                onClick = {
                    showExportDialog = true
                }
            )
        }
        
        item {
            SettingsItem(
                title = "Import Data",
                description = "Load habits from a file",
                icon = Icons.Default.FileUpload,
                onClick = {
                    showImportDialog = true
                    importData = ""
                    showImportResult = null
                }
            )
        }
        
        item {
            SettingsItem(
                title = "Reset All Data",
                description = "Delete all habits and settings",
                icon = Icons.Default.Delete,
                onClick = onResetDataClick,
                iconTint = MaterialTheme.colorScheme.error,
                textColor = MaterialTheme.colorScheme.error
            )
        }
        
        // About
        item {
            Text(
                text = "About",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            SettingsItem(
                title = "Version",
                description = "1.0.0" + if (versionClickCount > 0) " (${5-versionClickCount} more...)" else "",
                icon = Icons.Default.Info,
                onClick = {
                    viewModel.handleVersionClick()
                }
            )
        }

        item {
            SettingsItem(
                title = "Privacy Policy",
                description = "Read our privacy policy",
                icon = Icons.Default.Article,
                onClick = {
                    // In a real app, this would open a browser with the privacy policy URL
                }
            )

            // Add bottom spacing
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}


@Composable
private fun SettingsSwitchItem(
    title: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall
            )
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

@Composable
private fun SettingsItem(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = textColor
            )
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Icon(
            imageVector = Icons.Default.ArrowForwardIos,
            contentDescription = "Open",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
    }
}

// Helper functions
private fun calculateStreak(habits: List<Habit>): Int {
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

private fun calculatePerfectDays(habits: List<Habit>): Int {
    if (habits.isEmpty()) return 0
    
    var perfectDays = 0
    var currentDate = LocalDate.now()
    
    for (i in 0 until 30) { // Check last 30 days
        val date = currentDate.minusDays(i.toLong())
        val allCompletedForDate = habits.all { habit -> 
            habit.completionHistory[date] == true 
        }
        
        if (allCompletedForDate) {
            perfectDays++
        } else {
            // Break streak on first non-perfect day
            if (i < 7) break
        }
    }
    
    return perfectDays
}

enum class ProfileTab(val title: String, val icon: ImageVector) {
    STATISTICS("Stats", Icons.Default.BarChart),
    ACHIEVEMENTS("Achievements", Icons.Default.EmojiEvents),
    SETTINGS("Settings", Icons.Rounded.Settings)
}

// Reset confirmation dialog
@Composable
private fun ResetConfirmationDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (showDialog) {
        var isConfirming by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Reset All Data") },
            text = { 
                Column {
                    Text(
                        "Are you sure you want to delete all habits and settings? This action cannot be undone.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    if (isConfirming) {
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        if (!isConfirming) {
                            isConfirming = true
                            
                            // Add a small delay to show the operation is happening
                            scope.launch {
                                delay(500)
                                onConfirm()
                                isConfirming = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    enabled = !isConfirming
                ) {
                    if (isConfirming) {
                        Text("Resetting...")
                    } else {
                        Text("Reset")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    enabled = !isConfirming
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Time picker dialog for selecting reminder time
 */
@Composable
private fun TimePickerDialog(
    onDismiss: () -> Unit,
    onTimeSelected: (String) -> Unit,
    initialTime: String
) {
    val timeOptions = listOf("06:00", "07:00", "08:00", "09:00", "12:00", "16:00", "18:00", "20:00", "21:00", "22:00")
    var selectedTime by remember { mutableStateOf(initialTime) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Reminder Time") },
        text = {
            Column {
                Text(
                    "Choose the time for your daily habit reminders:",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Time options grid
                LazyColumn {
                    items(timeOptions.chunked(2)) { row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { time ->
                                val isSelected = time == selectedTime
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedTime = time },
                                    label = { Text(time) },
                                    leadingIcon = if (isSelected) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else null,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            
                            // If row has only one item, add an empty space
                            if (row.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onTimeSelected(selectedTime) }
            ) {
                Text("Set Time")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}
