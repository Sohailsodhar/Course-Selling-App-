package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.LessonEntity
import com.example.ui.components.InteractiveVideoPlayer
import com.example.ui.theme.BrandAmberGold
import com.example.ui.theme.BrandEmeraldGreen
import com.example.ui.theme.BrandStudyBlue
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.delay

@Composable
fun LessonPlayerScreen(
    courseId: Long,
    initialLessonId: Long,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val courseFlow = remember(courseId) { viewModel.courseRepo.getCourseById(courseId) }
    val course by courseFlow.collectAsState(initial = null)

    val lessonsFlow = remember(courseId) { viewModel.courseRepo.getLessons(courseId) }
    val lessons by lessonsFlow.collectAsState(initial = emptyList())

    val resourcesFlow = remember(courseId) { viewModel.courseRepo.getResources(courseId) }
    val resources by resourcesFlow.collectAsState(initial = emptyList())

    val notesFlow = remember(courseId) { viewModel.studentRepo.getNotes(courseId) }
    val notes by notesFlow.collectAsState(initial = emptyList())

    val enrollmentFlow = remember(courseId) { viewModel.studentRepo.getEnrollment(viewModel.currentUserId, courseId) }
    val enrollment by enrollmentFlow.collectAsState(initial = null)

    var currentLessonId by remember { mutableStateOf(initialLessonId) }

    // Resolve active lesson
    val activeLesson = remember(lessons, currentLessonId) {
        if (currentLessonId != 0L) {
            lessons.firstOrNull { it.id == currentLessonId } ?: lessons.firstOrNull()
        } else {
            lessons.firstOrNull()
        }
    }

    LaunchedEffect(activeLesson) {
        if (activeLesson != null && currentLessonId == 0L) {
            currentLessonId = activeLesson.id
        }
    }

    // Video Player Interactive State
    var isPlaying by remember { mutableStateOf(true) }
    var progressFraction by remember { mutableFloatStateOf(0.15f) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }

    // Auto-advance progress simulation when playing
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(1000)
            progressFraction = (progressFraction + 0.015f).coerceAtMost(1.0f)
            if (progressFraction >= 0.98f && activeLesson != null) {
                // Automatically mark completed
                viewModel.markLessonCompleted(courseId, activeLesson.id)
            }
        }
    }

    var selectedTab by remember { mutableStateOf(0) } // 0: Curriculum, 1: Resources, 2: Notes, 3: Quiz
    var newNoteTitle by remember { mutableStateOf("") }
    var newNoteContent by remember { mutableStateOf("") }
    var showAddNoteDialog by remember { mutableStateOf(false) }

    val currentLessonIndex = lessons.indexOfFirst { it.id == activeLesson?.id }
    val hasPrev = currentLessonIndex > 0
    val hasNext = currentLessonIndex >= 0 && currentLessonIndex < lessons.size - 1

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = course?.title ?: "Course Player",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                maxLines = 1
                            )
                            Text(
                                text = activeLesson?.moduleName ?: "",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Certificate Icon if eligible
                    if (enrollment?.isCompleted == true || (enrollment?.progressPercent ?: 0) >= 80) {
                        IconButton(
                            onClick = {
                                viewModel.issueCertificate(courseId, course?.title ?: "Course") { cert ->
                                    viewModel.navigateTo(Screen.CertificateView(cert))
                                }
                            }
                        ) {
                            Icon(Icons.Default.WorkspacePremium, contentDescription = "Certificate", tint = BrandAmberGold)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Interactive Video Player
            InteractiveVideoPlayer(
                lessonTitle = activeLesson?.title ?: "Playing Lesson",
                durationMinutes = activeLesson?.durationMinutes ?: 20,
                isPlaying = isPlaying,
                progressFraction = progressFraction,
                playbackSpeed = playbackSpeed,
                onTogglePlay = { isPlaying = !isPlaying },
                onSeek = { progressFraction = it },
                onSpeedChange = { playbackSpeed = it }
            )

            // Lesson Title & Navigation Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = activeLesson?.title ?: "",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Progress row
                    val prog = enrollment?.progressPercent ?: 0
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Course Progress: $prog%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = activeLesson?.isCompleted == true,
                                onCheckedChange = {
                                    if (activeLesson != null) {
                                        viewModel.markLessonCompleted(courseId, activeLesson.id)
                                    }
                                }
                            )
                            Text("Completed", fontSize = 12.sp)
                        }
                    }

                    LinearProgressIndicator(
                        progress = { (enrollment?.progressPercent ?: 0) / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = BrandEmeraldGreen
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Previous / Next Lesson Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (hasPrev) {
                                    val prevLesson = lessons[currentLessonIndex - 1]
                                    currentLessonId = prevLesson.id
                                    progressFraction = 0.05f
                                }
                            },
                            enabled = hasPrev,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.SkipPrevious, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Previous", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                if (hasNext) {
                                    val nextLesson = lessons[currentLessonIndex + 1]
                                    currentLessonId = nextLesson.id
                                    progressFraction = 0.05f
                                } else {
                                    // Finished course! Launch Quiz
                                    viewModel.navigateTo(Screen.Quiz(courseId))
                                }
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (hasNext) "Next Lesson" else "Take Course Quiz", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(if (hasNext) Icons.Default.SkipNext else Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Learning Interface Tabs (Curriculum, Resources, Notes, Quiz)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Lessons") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Resources") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Notes (${notes.size})") })
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Quiz") })
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // Lessons List
                        items(lessons) { lesson ->
                            val isCurrent = lesson.id == activeLesson?.id
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        currentLessonId = lesson.id
                                        progressFraction = 0.05f
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isCurrent) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    }
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(
                                                if (lesson.isCompleted) BrandEmeraldGreen.copy(alpha = 0.2f)
                                                else MaterialTheme.colorScheme.surfaceVariant,
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (lesson.isCompleted) Icons.Default.Check else Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = if (lesson.isCompleted) BrandEmeraldGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = lesson.title,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 13.sp,
                                            color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${lesson.durationMinutes} mins • ${lesson.moduleName}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    if (isCurrent) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "PLAYING",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        // Resources
                        items(resources) { res ->
                            ResourceListItem(resource = res)
                        }
                    }
                    2 -> {
                        // Personal Notes
                        item {
                            Button(
                                onClick = { showAddNoteDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Personal Study Note")
                            }
                        }

                        if (notes.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No notes yet for this course. Take notes while watching lessons!",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            items(notes) { note ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = note.noteTitle,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            IconButton(
                                                onClick = {
                                                    viewModel.deletePersonalNote(note.id)
                                                }
                                            ) {
                                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = note.content,
                                            fontSize = 12.sp,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                    3 -> {
                        // Quiz Section
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(BrandAmberGold, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Quiz, contentDescription = null, tint = Color.White)
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "Course Final Assessment",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                            Text(
                                                text = "Test your knowledge to earn your Certificate",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Text(
                                        text = "• 3 Challenging Multiple Choice Questions\n• Passing Score: 66% (2/3 correct)\n• Immediate explanation for every question\n• Official certificate issued instantly upon passing",
                                        fontSize = 12.sp,
                                        lineHeight = 20.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Button(
                                        onClick = { viewModel.navigateTo(Screen.Quiz(courseId)) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = BrandEmeraldGreen)
                                    ) {
                                        Text("Start Quiz Now", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Note Dialog
    if (showAddNoteDialog) {
        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = { Text("Take Lesson Note") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newNoteTitle,
                        onValueChange = { newNoteTitle = it },
                        label = { Text("Note Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newNoteContent,
                        onValueChange = { newNoteContent = it },
                        label = { Text("Note Content") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newNoteTitle.isNotBlank()) {
                            viewModel.savePersonalNote(
                                courseId,
                                activeLesson?.id ?: 0L,
                                newNoteTitle,
                                newNoteContent
                            )
                            newNoteTitle = ""
                            newNoteContent = ""
                            showAddNoteDialog = false
                        }
                    }
                ) {
                    Text("Save Note")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNoteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
