package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.*
import com.example.ui.theme.BrandCoralRose
import com.example.ui.theme.SkillPulseTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SkillPulseTheme {
                SkillPulseApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun SkillPulseApp(viewModel: MainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val cartCount by viewModel.cartCount.collectAsState()

    // Handle back button
    BackHandler(enabled = currentScreen !is Screen.Home) {
        viewModel.navigateBack()
    }

    val isRootScreen = currentScreen is Screen.Home ||
            currentScreen is Screen.Explore ||
            currentScreen is Screen.MyLearning ||
            currentScreen is Screen.Profile

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (isRootScreen) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                ) {
                    NavigationBarItem(
                        selected = currentScreen is Screen.Home,
                        onClick = { viewModel.navigateTo(Screen.Home) },
                        icon = {
                            Icon(
                                if (currentScreen is Screen.Home) Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = "Home"
                            )
                        },
                        label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.Medium) }
                    )

                    NavigationBarItem(
                        selected = currentScreen is Screen.Explore,
                        onClick = { viewModel.navigateTo(Screen.Explore) },
                        icon = {
                            Icon(
                                if (currentScreen is Screen.Explore) Icons.Filled.Search else Icons.Outlined.Search,
                                contentDescription = "Explore"
                            )
                        },
                        label = { Text("Explore", fontSize = 11.sp, fontWeight = FontWeight.Medium) }
                    )

                    NavigationBarItem(
                        selected = currentScreen is Screen.MyLearning,
                        onClick = { viewModel.navigateTo(Screen.MyLearning) },
                        icon = {
                            Icon(
                                if (currentScreen is Screen.MyLearning) Icons.Filled.School else Icons.Outlined.School,
                                contentDescription = "My Learning"
                            )
                        },
                        label = { Text("Learning", fontSize = 11.sp, fontWeight = FontWeight.Medium) }
                    )

                    NavigationBarItem(
                        selected = currentScreen is Screen.Profile,
                        onClick = { viewModel.navigateTo(Screen.Profile) },
                        icon = {
                            Icon(
                                if (currentScreen is Screen.Profile) Icons.Filled.Person else Icons.Outlined.Person,
                                contentDescription = "Profile"
                            )
                        },
                        label = { Text("Profile", fontSize = 11.sp, fontWeight = FontWeight.Medium) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (isRootScreen) innerPadding.calculateBottomPadding() else 0.dp)
        ) {
            when (val screen = currentScreen) {
                is Screen.Home -> {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToCourse = { courseId ->
                            viewModel.navigateTo(Screen.CourseDetail(courseId))
                        },
                        onNavigateToExplore = { category ->
                            if (category.isNotBlank() && category != "ALL") {
                                viewModel.setCategoryFilter(category)
                            }
                            viewModel.navigateTo(Screen.Explore)
                        }
                    )
                }
                is Screen.Explore -> {
                    ExploreScreen(
                        viewModel = viewModel,
                        onNavigateToCourse = { courseId ->
                            viewModel.navigateTo(Screen.CourseDetail(courseId))
                        }
                    )
                }
                is Screen.MyLearning -> {
                    MyLearningScreen(
                        viewModel = viewModel,
                        onNavigateToCourse = { courseId ->
                            viewModel.navigateTo(Screen.CourseDetail(courseId))
                        }
                    )
                }
                is Screen.Profile -> {
                    ProfileScreen(viewModel = viewModel)
                }
                is Screen.Cart -> {
                    CartScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateBack() }
                    )
                }
                is Screen.CourseDetail -> {
                    CourseDetailScreen(
                        courseId = screen.courseId,
                        viewModel = viewModel,
                        onBack = { viewModel.navigateBack() }
                    )
                }
                is Screen.LessonPlayer -> {
                    LessonPlayerScreen(
                        courseId = screen.courseId,
                        initialLessonId = screen.lessonId,
                        viewModel = viewModel,
                        onBack = { viewModel.navigateBack() }
                    )
                }
                is Screen.Quiz -> {
                    QuizScreen(
                        courseId = screen.courseId,
                        viewModel = viewModel,
                        onBack = { viewModel.navigateBack() }
                    )
                }
                is Screen.CertificateView -> {
                    CertificateScreen(
                        certificate = screen.certificate,
                        onBack = { viewModel.navigateBack() }
                    )
                }
                is Screen.Checkout -> {
                    CheckoutScreen(
                        courseId = screen.courseId,
                        viewModel = viewModel,
                        onBack = { viewModel.navigateBack() }
                    )
                }
                is Screen.OrderTracking -> {
                    OrderTrackingScreen(
                        orderNumber = screen.orderNumber,
                        viewModel = viewModel,
                        onBack = { viewModel.navigateBack() }
                    )
                }
                is Screen.AdminLogin -> {
                    AdminLoginScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateBack() }
                    )
                }
                is Screen.AdminDashboard -> {
                    AdminDashboardScreen(
                        viewModel = viewModel,
                        onBackToApp = { viewModel.navigateTo(Screen.Home) }
                    )
                }
                else -> {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToCourse = { courseId ->
                            viewModel.navigateTo(Screen.CourseDetail(courseId))
                        },
                        onNavigateToExplore = { viewModel.navigateTo(Screen.Explore) }
                    )
                }
            }
        }
    }
}
