package com.magic.habbittracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.magic.habbittracker.models.HabitCategory
import com.magic.habbittracker.viewmodels.HabitViewModel

@Composable
fun CategoryScreen(
    viewModel: HabitViewModel,
    innerPadding: PaddingValues
) {
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<HabitCategory?>(null) }
    
    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
    ) {
        // Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Manage categories to organize your habits",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { 
                        categoryToEdit = null
                        showAddCategoryDialog = true 
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add New Category")
                }
            }
        }
        
        // Categories list
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(viewModel.categories) { category ->
                val isDefaultCategory = category in HabitCategory.DEFAULT_CATEGORIES
                val categoryColor = Color(android.graphics.Color.parseColor(category.colorHex))
                val habitsWithCategory = viewModel.habits.count { it.category == category }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Category color indicator
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(categoryColor)
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = CircleShape
                                )
                        )
                        
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = category.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                color = categoryColor
                            )
                            
                            Text(
                                text = "$habitsWithCategory habits",
                                style = MaterialTheme.typography.bodySmall
                            )
                            
                            if (isDefaultCategory) {
                                Text(
                                    text = "Default category",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                        
                        // Edit button
                        IconButton(
                            onClick = {
                                categoryToEdit = category
                                showAddCategoryDialog = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit"
                            )
                        }
                        
                        // Delete button (only for custom categories)
                        if (!isDefaultCategory) {
                            IconButton(
                                onClick = {
                                    viewModel.deleteCategory(category.id)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Add/Edit Category Dialog
    if (showAddCategoryDialog) {
        val isEditing = categoryToEdit != null
        var categoryName by remember { mutableStateOf(categoryToEdit?.displayName ?: "") }
        var colorHex by remember { mutableStateOf(categoryToEdit?.colorHex ?: "#FF9800") }
        
        AlertDialog(
            onDismissRequest = { 
                showAddCategoryDialog = false
                categoryToEdit = null
            },
            title = { Text(if (isEditing) "Edit Category" else "Add New Category") },
            text = {
                Column {
                    OutlinedTextField(
                        value = categoryName,
                        onValueChange = { categoryName = it },
                        label = { Text("Category Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Color", style = MaterialTheme.typography.labelLarge)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Simple color picker with predefined colors
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val colors = listOf(
                            "#F44336", "#E91E63", "#9C27B0", "#673AB7", 
                            "#3F51B5", "#2196F3", "#03A9F4", "#00BCD4",
                            "#009688", "#4CAF50", "#8BC34A", "#CDDC39",
                            "#FFEB3B", "#FFC107", "#FF9800", "#FF5722"
                        )
                        
                        items(colors) { color ->
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(Color(android.graphics.Color.parseColor(color)))
                                    .border(
                                        width = 2.dp,
                                        color = if (colorHex == color) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = MaterialTheme.shapes.small
                                    )
                                    .clickable { colorHex = color }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Preview
                    Text("Preview:", style = MaterialTheme.typography.labelLarge)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        color = Color(android.graphics.Color.parseColor(colorHex)).copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(colorHex)))
                            )
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Text(
                                text = categoryName.ifEmpty { "Category Name" },
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(android.graphics.Color.parseColor(colorHex))
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (categoryName.isNotBlank()) {
                            if (isEditing && categoryToEdit != null) {
                                viewModel.updateCategory(categoryToEdit!!, categoryName, colorHex)
                            } else {
                                viewModel.addCategory(categoryName, colorHex)
                            }
                            showAddCategoryDialog = false
                            categoryToEdit = null
                        }
                    }
                ) {
                    Text(if (isEditing) "Update" else "Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showAddCategoryDialog = false
                        categoryToEdit = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}