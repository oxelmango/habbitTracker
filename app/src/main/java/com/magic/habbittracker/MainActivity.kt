package com.magic.habbittracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState

import com.magic.habbittracker.data.HabitIcons
import com.magic.habbittracker.data.IconData
import com.magic.habbittracker.models.Habit
import com.magic.habbittracker.models.HabitCategory
import com.magic.habbittracker.ui.components.CategoryChips
import com.magic.habbittracker.ui.components.HabitCard
import com.magic.habbittracker.ui.components.WeekCalendar
import com.magic.habbittracker.ui.screens.CategoryScreen
import com.magic.habbittracker.ui.screens.EmptyState
import com.magic.habbittracker.ui.screens.ProfileScreen
import com.magic.habbittracker.ui.theme.HabbitTrackerTheme
import com.magic.habbittracker.viewmodels.HabitViewModel
import com.magic.habbittracker.viewmodels.HabitViewModelFactory
import com.magic.habbittracker.viewmodels.Screen
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                val viewModel = HabitViewModel(application)
                viewModel.updateNotificationSchedule()
            }
        }
        
        checkNotificationPermission()
        
        setContent {
            val viewModel: HabitViewModel = viewModel(
                factory = HabitViewModelFactory(application)
            )
            
            HabbitTrackerTheme(
                darkTheme = viewModel.isDarkMode.collectAsState().value
            ) {
                HabitTrackerApp(viewModel)
            }
        }
    }
    
    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    
                }
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTrackerApp(viewModel: HabitViewModel) {
    val habits = viewModel.habits
    val selectedDate = viewModel.selectedDate.value
    val selectedCategory = viewModel.selectedCategory.value
    val currentScreen = viewModel.selectedScreen.value
    
    var showAddEditDialog by remember { mutableStateOf(false) }
    var habitToEdit by remember { mutableStateOf<Habit?>(null) }
    
    Scaffold(
        floatingActionButton = {
            if (currentScreen == Screen.CALENDAR) {
                FloatingActionButton(
                    onClick = { 
                        habitToEdit = null
                        showAddEditDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Habit",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentScreen == Screen.CALENDAR,
                    onClick = { viewModel.setSelectedScreen(Screen.CALENDAR) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Calendar"
                        )
                    },
                    label = { Text("Calendar") }
                )
                NavigationBarItem(
                    selected = currentScreen == Screen.CATEGORIES,
                    onClick = { viewModel.setSelectedScreen(Screen.CATEGORIES) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = "Categories"
                        )
                    },
                    label = { Text("Categories") }
                )
                NavigationBarItem(
                    selected = currentScreen == Screen.PROFILE,
                    onClick = { viewModel.setSelectedScreen(Screen.PROFILE) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile"
                        )
                    },
                    label = { Text("Profile") }
                )
            }
        }
    ) { innerPadding ->
        when (currentScreen) {
            Screen.CALENDAR -> CalendarScreen(
                viewModel = viewModel, 
                innerPadding = innerPadding,
                showAddEditDialog = { show -> showAddEditDialog = show },
                setHabitToEdit = { habit -> habitToEdit = habit }
            )
            Screen.CATEGORIES -> CategoryScreen(
                viewModel = viewModel,
                innerPadding = innerPadding
            )
            Screen.PROFILE -> ProfileScreen(
                viewModel = viewModel,
                onNavigateToCalendar = { viewModel.setSelectedScreen(Screen.CALENDAR) },
                innerPadding = innerPadding
            )
        }
    }
    
    if (showAddEditDialog) {
        val isEditing = habitToEdit != null
        var title by remember { mutableStateOf(habitToEdit?.title ?: "") }
        var description by remember { mutableStateOf(habitToEdit?.description ?: "") }
        var category by remember { mutableStateOf(habitToEdit?.category ?: HabitCategory.PERSONAL) }
        var iconName by remember { mutableStateOf(habitToEdit?.iconName ?: "check") }
        var selectedIcon by remember { mutableStateOf(HabitIcons.getIconByName(iconName)) }
        var showIconPicker by remember { mutableStateOf(false) }
        
        var startFromToday by remember { mutableStateOf(habitToEdit?.startDate == LocalDate.now() || habitToEdit == null) }
        var startDate by remember { mutableStateOf(habitToEdit?.startDate ?: LocalDate.now()) }
        
        AlertDialog(
            onDismissRequest = { showAddEditDialog = false },
            title = { Text(if (isEditing) "Edit Habit" else "Add New Habit") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    // Icon picker button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        FilledTonalButton(
                            onClick = { showIconPicker = true },
                            modifier = Modifier.size(64.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ),
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            Icon(
                                imageVector = selectedIcon,
                                contentDescription = "Habit Icon",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Start date section
                    Text("Start tracking from", style = MaterialTheme.typography.labelLarge)
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = startFromToday,
                            onCheckedChange = { startFromToday = it },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        
                        Column {
                            Text(
                                text = if (startFromToday) "Today onwards" else "Custom date",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            if (!startFromToday) {
                                Text(
                                    text = startDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        if (!startFromToday) {
                            FilledTonalIconButton(
                                onClick = {
                                    startDate = startDate.minusDays(1)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowLeft,
                                    contentDescription = "Previous Day"
                                )
                            }
                            
                            FilledTonalIconButton(
                                onClick = {
                                    val today = LocalDate.now()
                                    if (startDate.isBefore(today)) {
                                        startDate = startDate.plusDays(1)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowRight,
                                    contentDescription = "Next Day"
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Category", style = MaterialTheme.typography.labelLarge)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(viewModel.categories) { cat ->
                            val color = android.graphics.Color.parseColor(cat.colorHex)
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat.displayName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(color).copy(alpha = 0.2f),
                                    selectedLabelColor = Color(color)
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            val finalStartDate = if (startFromToday) LocalDate.now() else startDate
                            
                            if (isEditing && habitToEdit != null) {
                                viewModel.updateHabit(
                                    habitToEdit!!.copy(
                                        title = title,
                                        description = description,
                                        category = category,
                                        iconName = HabitIcons.getIconName(selectedIcon),
                                        startDate = finalStartDate
                                    )
                                )
                            } else {
                                viewModel.addHabit(
                                    Habit(
                                        id = 0, // Will be assigned by ViewModel
                                        title = title,
                                        description = description,
                                        category = category,
                                        iconName = HabitIcons.getIconName(selectedIcon),
                                        startDate = finalStartDate
                                    )
                                )
                            }
                            showAddEditDialog = false
                        }
                    }
                ) {
                    Text(if (isEditing) "Update" else "Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAddEditDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
        
        // Icon picker dialog
        if (showIconPicker) {
            AlertDialog(
                onDismissRequest = { showIconPicker = false },
                title = { Text("Select Icon") },
                text = {
                    Column(
                        modifier = Modifier.height(350.dp)
                    ) {
                        var searchQuery by remember { mutableStateOf("") }
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search icons...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            leadingIcon = { 
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search"
                                )
                            },
                            singleLine = true
                        )
                        
                        val filteredIcons = remember(searchQuery) {
                            if (searchQuery.isEmpty()) {
                                HabitIcons.allIcons
                            } else {
                                HabitIcons.allIcons.filter { 
                                    it.name.contains(searchQuery, ignoreCase = true)
                                }
                            }
                        }
                        
                        val iconCategories = listOf(
                            "Health & Fitness",
                            "Nutrition",
                            "Personal Development",
                            "Work & Productivity",
                            "Finances",
                            "Home & Life",
                            "Wellbeing"
                        )
                        
                        if (searchQuery.isEmpty()) {
                            LazyRow(
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                items(iconCategories) { category ->
                                    FilterChip(
                                        selected = false,
                                        onClick = { },
                                        label = { Text(category) },
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                }
                            }
                        }
                        
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(5),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 8.dp)
                        ) {
                            items(filteredIcons) { iconData ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(MaterialTheme.shapes.medium)
                                            .background(
                                                if (iconData.icon == selectedIcon) 
                                                    MaterialTheme.colorScheme.primaryContainer 
                                                else 
                                                    MaterialTheme.colorScheme.surfaceVariant
                                            )
                                            .clickable {
                                                selectedIcon = iconData.icon
                                                iconName = iconData.name
                                                showIconPicker = false
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = iconData.icon,
                                            contentDescription = iconData.name,
                                            tint = if (iconData.icon == selectedIcon) 
                                                MaterialTheme.colorScheme.onPrimaryContainer 
                                            else 
                                                MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = iconData.name.replaceFirstChar { it.uppercase() },
                                            style = MaterialTheme.typography.labelSmall,
                                            maxLines = 1,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .padding(top = 4.dp)
                                                .width(56.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showIconPicker = false }
                    ) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

@Composable
private fun CalendarScreen(
    viewModel: HabitViewModel, 
    innerPadding: PaddingValues,
    showAddEditDialog: (Boolean) -> Unit,
    setHabitToEdit: (Habit?) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
    ) {
        WeekCalendar(
            selectedDate = viewModel.selectedDate.value,
            onDateSelected = { date ->
                viewModel.setSelectedDate(date)
            },
            habitsForDate = remember(viewModel.habits, viewModel.selectedDate.value) {
                viewModel.getAvailableHabitsForDate(viewModel.selectedDate.value).groupBy { habit ->
                    viewModel.selectedDate.value
                }.mapValues { it.value.size }
            },
            completedHabitsForDate = remember(viewModel.habits, viewModel.selectedDate.value) {
                viewModel.habits.filter { habit ->
                    habit.isAvailableForDate(viewModel.selectedDate.value) && 
                    habit.completionHistory[viewModel.selectedDate.value] == true
                }.groupBy { habit ->
                    viewModel.selectedDate.value
                }.mapValues { it.value.size }
            }
        )
        
        CategoryChips(
            categories = viewModel.categories,
            selectedCategory = viewModel.selectedCategory.value,
            onCategorySelected = { category ->
                viewModel.setSelectedCategory(category)
            },
            onAddCategoryClick = {
                viewModel.selectedCategory.value = null
            }
        )
        
        val availableHabits = viewModel.getAvailableHabitsForDate(viewModel.selectedDate.value)
        val filteredHabits = availableHabits.filter { habit ->
            viewModel.selectedCategory.value?.let { habit.category == it } ?: true
        }
        val completedCount = filteredHabits.count { it.completionHistory[viewModel.selectedDate.value] == true }
        val progress = if (filteredHabits.isNotEmpty()) completedCount.toFloat() / filteredHabits.size else 0f
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = if (viewModel.selectedCategory.value != null) {
                        "${viewModel.selectedCategory.value!!.displayName} Habits"
                    } else {
                        "All Habits"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "$completedCount/${filteredHabits.size} habits completed",
                    style = MaterialTheme.typography.labelMedium
                )
                
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        if (filteredHabits.isEmpty()) {
            EmptyState(
                title = if (viewModel.selectedCategory.value != null) {
                    "No ${viewModel.selectedCategory.value!!.displayName.lowercase()} habits"
                } else {
                    "No habits yet"
                },
                message = if (viewModel.selectedCategory.value != null) {
                    "Try selecting a different category or add a new habit"
                } else {
                    "Tap the + button to create your first habit"
                }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredHabits) { habit ->
                    HabitCard(
                        habit = habit,
                        isCompletedForSelectedDate = habit.completionHistory[viewModel.selectedDate.value] == true,
                        onToggleCompleted = { habitId ->
                            viewModel.toggleHabitCompletion(habitId)
                        },
                        onEdit = { 
                            setHabitToEdit(it)
                            showAddEditDialog(true)
                        },
                        onDelete = { habitId ->
                            viewModel.deleteHabit(habitId)
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HabitTrackerPreview() {
    HabbitTrackerTheme {
        HabitTrackerApp(viewModel())
    }
}