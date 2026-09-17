package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CourseCard
import com.example.ui.theme.BrandEmeraldGreen
import com.example.ui.viewmodel.MainViewModel

@Composable
fun ExploreScreen(
    viewModel: MainViewModel,
    onNavigateToCourse: (Long) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategoryFilter.collectAsState()
    val selectedLevel by viewModel.selectedLevelFilter.collectAsState()
    val selectedPrice by viewModel.selectedPriceFilter.collectAsState()
    val courses by viewModel.filteredCourses.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Search Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Find Your Next Skill",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Search by title, subject, instructor...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category Filter Pills
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == "ALL",
                            onClick = { viewModel.setCategoryFilter("ALL") },
                            label = { Text("All") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedCategory == "STUDY",
                            onClick = { viewModel.setCategoryFilter("STUDY") },
                            label = { Text("📚 Study Courses") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedCategory == "YOUTUBE",
                            onClick = { viewModel.setCategoryFilter("YOUTUBE") },
                            label = { Text("🎬 YouTube Automation") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Secondary Filter Row (Level & Price)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        AssistChip(
                            onClick = {
                                val nextPrice = when (selectedPrice) {
                                    "ALL" -> "PAID"
                                    "PAID" -> "FREE"
                                    else -> "ALL"
                                }
                                viewModel.setPriceFilter(nextPrice)
                            },
                            label = {
                                Text("Price: $selectedPrice")
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        )
                    }

                    item {
                        AssistChip(
                            onClick = {
                                val nextLevel = when (selectedLevel) {
                                    "ALL" -> "Beginner"
                                    "Beginner" -> "Intermediate"
                                    "Intermediate" -> "Advanced"
                                    else -> "ALL"
                                }
                                viewModel.setLevelFilter(nextLevel)
                            },
                            label = {
                                Text("Level: $selectedLevel")
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        )
                    }

                    if (searchQuery.isNotBlank() || selectedCategory != "ALL" || selectedLevel != "ALL" || selectedPrice != "ALL") {
                        item {
                            TextButton(
                                onClick = {
                                    viewModel.updateSearchQuery("")
                                    viewModel.setCategoryFilter("ALL")
                                    viewModel.setLevelFilter("ALL")
                                    viewModel.setPriceFilter("ALL")
                                }
                            ) {
                                Text("Reset Filters", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }

        // Results Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${courses.size} courses found",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (selectedCategory != "ALL") {
                Surface(
                    color = BrandEmeraldGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (selectedCategory == "STUDY") "📚 Academic" else "🎬 Creator",
                        color = BrandEmeraldGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        if (courses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No matching courses found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Try adjusting your search terms or clearing the filters.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            viewModel.updateSearchQuery("")
                            viewModel.setCategoryFilter("ALL")
                            viewModel.setLevelFilter("ALL")
                            viewModel.setPriceFilter("ALL")
                        }
                    ) {
                        Text("Reset All Filters")
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(courses) { course ->
                    CourseCard(
                        course = course,
                        onClick = { onNavigateToCourse(course.id) }
                    )
                }
            }
        }
    }
}
