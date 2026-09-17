package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CourseEntity
import com.example.ui.components.CourseCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToCourse: (Long) -> Unit,
    onNavigateToExplore: (String) -> Unit
) {
    val allCourses by viewModel.allPublishedCourses.collectAsState()
    val studyCourses by viewModel.studyCourses.collectAsState()
    val youtubeCourses by viewModel.youtubeCourses.collectAsState()
    val enrollments by viewModel.userEnrollments.collectAsState()
    val cartCount by viewModel.cartCount.collectAsState()

    var homeCategoryTab by remember { mutableStateOf("ALL") } // ALL, STUDY, YOUTUBE

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // App Top Bar & Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(BrandTealPrimary, BrandPurpleAccent)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = "SkillPulse Logo",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "SkillPulse",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Learn • Build • Master",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Admin Portal Quick Action Button
                        IconButton(
                            onClick = { viewModel.navigateTo(Screen.AdminLogin) }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AdminPanelSettings,
                                contentDescription = "Admin Portal",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Cart Button with badge
                        BadgedBox(
                            badge = {
                                if (cartCount > 0) {
                                    Badge(
                                        containerColor = BrandCoralRose,
                                        contentColor = Color.White
                                    ) {
                                        Text("$cartCount")
                                    }
                                }
                            }
                        ) {
                            IconButton(onClick = { viewModel.navigateTo(Screen.Cart) }) {
                                Icon(
                                    imageVector = Icons.Outlined.ShoppingCart,
                                    contentDescription = "Cart",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search Bar Trigger
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onNavigateToExplore("") },
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Search courses, physics, YouTube SEO...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Welcome & Daily Motivation Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF0F2537))
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = BrandEmeraldGreen.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "🇵🇰 PAKISTAN'S PREMIER LEARNING",
                                    color = BrandEmeraldGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Welcome, ${viewModel.currentUserName}! 🚀",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Master verified academic subjects and build sustainable faceless YouTube channels with practical skills.",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onNavigateToExplore("STUDY") },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandStudyBlue),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("📚 Study Prep", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            FilledTonalButton(
                                onClick = { onNavigateToExplore("YOUTUBE") },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = BrandPurpleAccent,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("🎬 YouTube Setup", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Special Offers Section (Pakistan Payment Promo)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = BrandJazzCashRed.copy(alpha = 0.08f)
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        listOf(BrandJazzCashRed.copy(alpha = 0.4f), BrandEasypaisaGreen.copy(alpha = 0.4f))
                    )
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(BrandAmberGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Discount,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Up to 50% Off Courses This Month",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Direct payment via JazzCash & Easypaisa (Account: Sikander Ali) with fast manual approval.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Continue Learning Section (if student has enrolled courses)
        if (enrollments.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SectionHeader(
                    title = "Continue Learning",
                    subtitle = "Pick up where you left off",
                    actionText = "My Learning",
                    onActionClick = { viewModel.navigateTo(Screen.MyLearning) }
                )

                val activeEnrollment = enrollments.first()
                val activeCourse = allCourses.firstOrNull { it.id == activeEnrollment.courseId }

                if (activeCourse != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clickable {
                                viewModel.navigateTo(Screen.LessonPlayer(activeCourse.id))
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayCircleFilled,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = activeCourse.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Progress: ${activeEnrollment.progressPercent}% Completed",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { activeEnrollment.progressPercent / 100f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = BrandEmeraldGreen
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            FilledIconButton(
                                onClick = { viewModel.navigateTo(Screen.LessonPlayer(activeCourse.id)) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.ArrowForward, contentDescription = "Resume", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        // Category Filter Tabs (All, 📚 Study Courses, 🎬 YouTube Automation)
        item {
            Spacer(modifier = Modifier.height(14.dp))
            SectionHeader(
                title = "Explore Categories",
                subtitle = "Select your learning pathway"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = homeCategoryTab == "ALL",
                    onClick = { homeCategoryTab = "ALL" },
                    label = { Text("All Courses (${allCourses.size})") }
                )
                FilterChip(
                    selected = homeCategoryTab == "STUDY",
                    onClick = { homeCategoryTab = "STUDY" },
                    label = { Text("📚 Study (${studyCourses.size})") }
                )
                FilterChip(
                    selected = homeCategoryTab == "YOUTUBE",
                    onClick = { homeCategoryTab = "YOUTUBE" },
                    label = { Text("🎬 YouTube (${youtubeCourses.size})") }
                )
            }
        }

        // Featured Courses Horizontal Carousel
        item {
            val featured = allCourses.filter {
                it.isFeatured && (homeCategoryTab == "ALL" || it.category == homeCategoryTab)
            }
            if (featured.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                SectionHeader(
                    title = "Featured Courses",
                    subtitle = "Handpicked top-rated masterclasses",
                    actionText = "See All",
                    onActionClick = { onNavigateToExplore(homeCategoryTab) }
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(featured) { course ->
                        CourseCard(
                            course = course,
                            onClick = { onNavigateToCourse(course.id) },
                            modifier = Modifier.width(280.dp)
                        )
                    }
                }
            }
        }

        // Popular Courses Section
        item {
            val popular = allCourses.filter {
                it.isPopular && (homeCategoryTab == "ALL" || it.category == homeCategoryTab)
            }
            if (popular.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(
                    title = "Most Popular Courses",
                    subtitle = "Enrolled by thousands of students",
                    actionText = "View All",
                    onActionClick = { onNavigateToExplore(homeCategoryTab) }
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(popular) { course ->
                        CourseCard(
                            course = course,
                            onClick = { onNavigateToCourse(course.id) },
                            modifier = Modifier.width(280.dp)
                        )
                    }
                }
            }
        }

        // Free Lessons & Previews Section
        item {
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(
                title = "Free Lessons & Previews",
                subtitle = "Sample curriculum before purchasing"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                color = BrandEmeraldGreen,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "FREE PREVIEW",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "YouTube Automation: The 10-Second Hook Psychology",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Instructor: Hamza Malik • 21 Mins",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { onNavigateToCourse(7L) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Watch", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Student Success / Testimonials Section (Real Educational Reviews)
        item {
            Spacer(modifier = Modifier.height(18.dp))
            SectionHeader(
                title = "Student Success Stories",
                subtitle = "Real feedback from aspiring students & creators"
            )

            val testimonials = listOf(
                Triple(
                    "Muhammad Usman (Lahore)",
                    "MDCAT Physics Masterclass",
                    "The graphical vectors and momentum shortcuts cut my entry test question time from 2 minutes to under 40 seconds. Solved past papers with huge confidence!"
                ),
                Triple(
                    "Ayesha Noor (Karachi)",
                    "YouTube Automation Blueprint",
                    "No fake earnings claims or hype—just practical scripting, ethical monetization rules, and clean CapCut editing pipelines. Reached 10,000 subscribers in 4 months sustainably."
                ),
                Triple(
                    "Fahad Siddiqui (Islamabad)",
                    "Python & Computer Science",
                    "The OOP and data structures explanations in Urdu are crystal clear. Paid effortlessly with Easypaisa, course was unlocked within an hour."
                )
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(testimonials) { (name, courseName, quote) ->
                    Card(
                        modifier = Modifier.width(260.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(BrandTealPrimary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = name.take(1),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(text = name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(
                                        text = courseName,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row {
                                repeat(5) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = BrandAmberGold,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "\"$quote\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // Trust & Transparency Footer
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🛡️ Trust & Commitment to Education",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "SkillPulse prioritizes practical skills and rigorous learning. We do not make misleading financial promises or promote get-rich-quick schemes. 7-day course satisfaction policy.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
