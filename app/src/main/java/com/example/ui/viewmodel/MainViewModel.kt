package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class Screen {
    object Home : Screen()
    object Explore : Screen()
    object MyLearning : Screen()
    object Cart : Screen()
    object Profile : Screen()
    data class CourseDetail(val courseId: Long) : Screen()
    data class LessonPlayer(val courseId: Long, val lessonId: Long = 0L) : Screen()
    data class Quiz(val courseId: Long) : Screen()
    data class CertificateView(val certificate: CertificateEntity) : Screen()
    data class Checkout(val courseId: Long) : Screen()
    data class OrderTracking(val orderNumber: String) : Screen()
    object AdminLogin : Screen()
    object AdminDashboard : Screen()
    data class AdminEditCourse(val courseId: Long = 0L) : Screen()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    val courseRepo = CourseRepository(db)
    val orderRepo = OrderRepository(db)
    val studentRepo = StudentRepository(db)
    val adminRepo = AdminRepository(db)

    // Current User Session
    val currentUserId = "student@skillpulse.pk"
    val currentUserEmail = "student@skillpulse.pk"
    val currentUserName = "Sohail Ahmed"
    val currentUserPhone = "03001234567"

    // Navigation State
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val screenBackStack = mutableListOf<Screen>()

    fun navigateTo(screen: Screen) {
        screenBackStack.add(_currentScreen.value)
        _currentScreen.value = screen
    }

    fun navigateBack(): Boolean {
        if (screenBackStack.isNotEmpty()) {
            _currentScreen.value = screenBackStack.removeAt(screenBackStack.size - 1)
            return true
        }
        return false
    }

    // Courses & Categories
    val allPublishedCourses: StateFlow<List<CourseEntity>> = courseRepo.allPublishedCourses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studyCourses: StateFlow<List<CourseEntity>> = courseRepo.getCoursesByCategory("STUDY")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val youtubeCourses: StateFlow<List<CourseEntity>> = courseRepo.getCoursesByCategory("YOUTUBE")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Enrollments & Learning
    val userEnrollments: StateFlow<List<EnrollmentEntity>> = studentRepo.getEnrollments(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userOrders: StateFlow<List<OrderEntity>> = orderRepo.getOrdersForUser(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userCertificates: StateFlow<List<CertificateEntity>> = studentRepo.getCertificates(currentUserEmail)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartCount: StateFlow<Int> = studentRepo.cartCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val cartItems: StateFlow<List<CartItemEntity>> = studentRepo.cartItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Explore / Search Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow("ALL")
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter.asStateFlow()

    private val _selectedLevelFilter = MutableStateFlow("ALL")
    val selectedLevelFilter: StateFlow<String> = _selectedLevelFilter.asStateFlow()

    private val _selectedPriceFilter = MutableStateFlow("ALL")
    val selectedPriceFilter: StateFlow<String> = _selectedPriceFilter.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String) {
        _selectedCategoryFilter.value = category
    }

    fun setLevelFilter(level: String) {
        _selectedLevelFilter.value = level
    }

    fun setPriceFilter(price: String) {
        _selectedPriceFilter.value = price
    }

    val filteredCourses: StateFlow<List<CourseEntity>> = combine(
        allPublishedCourses,
        searchQuery,
        selectedCategoryFilter,
        selectedLevelFilter,
        selectedPriceFilter
    ) { courses, query, cat, level, price ->
        courses.filter { course ->
            val matchQuery = query.isBlank() ||
                    course.title.contains(query, ignoreCase = true) ||
                    course.subject.contains(query, ignoreCase = true) ||
                    course.instructor.contains(query, ignoreCase = true) ||
                    course.description.contains(query, ignoreCase = true)

            val matchCategory = cat == "ALL" || course.category == cat
            val matchLevel = level == "ALL" || course.level.contains(level, ignoreCase = true)
            val matchPrice = when (price) {
                "FREE" -> course.isFree
                "PAID" -> !course.isFree
                else -> true
            }

            matchQuery && matchCategory && matchLevel && matchPrice
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Admin State
    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    private val _adminEmail = MutableStateFlow("msuhailsodhar@gmail.com")
    val adminEmail: StateFlow<String> = _adminEmail.asStateFlow()

    val allOrdersAdmin: StateFlow<List<OrderEntity>> = orderRepo.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingOrdersAdmin: StateFlow<List<OrderEntity>> = orderRepo.pendingOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalRevenue: StateFlow<Double?> = orderRepo.totalRevenue
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val adminAuditLogs: StateFlow<List<AuditLogEntity>> = adminRepo.auditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val announcements: StateFlow<List<AnnouncementEntity>> = adminRepo.announcements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loginAdmin(email: String, pass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            if (adminRepo.isLocked()) {
                onResult(false, "Too many failed attempts. Locked for ${adminRepo.remainingLockSeconds()}s.")
                return@launch
            }
            val ok = adminRepo.verifyAdminLogin(email, pass)
            if (ok) {
                _isAdminLoggedIn.value = true
                _adminEmail.value = email
                _currentScreen.value = Screen.AdminDashboard
                onResult(true, "Authentication successful")
            } else {
                onResult(false, "Invalid administrator credentials")
            }
        }
    }

    fun logoutAdmin() {
        _isAdminLoggedIn.value = false
        _currentScreen.value = Screen.Home
    }

    fun approvePayment(order: OrderEntity) {
        viewModelScope.launch {
            orderRepo.approveOrder(order, _adminEmail.value)
        }
    }

    fun rejectPayment(order: OrderEntity, reason: String) {
        viewModelScope.launch {
            orderRepo.rejectOrder(order, reason, _adminEmail.value)
        }
    }

    fun saveCourse(course: CourseEntity, onComplete: () -> Unit) {
        viewModelScope.launch {
            courseRepo.saveCourse(course)
            onComplete()
        }
    }

    fun deleteCourse(courseId: Long) {
        viewModelScope.launch {
            courseRepo.deleteCourse(courseId)
        }
    }

    fun deleteAllCourses(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            courseRepo.clearAllCoursesData()
            onComplete()
        }
    }

    // Checkout & Payment submission
    fun submitManualPayment(
        course: CourseEntity,
        senderName: String,
        senderPhone: String,
        paymentMethod: String,
        amountPaid: Double,
        transactionId: String,
        screenshotNote: String,
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            val orderNum = orderRepo.createManualOrder(
                course = course,
                userId = currentUserId,
                userEmail = currentUserEmail,
                senderName = senderName,
                senderPhone = senderPhone,
                paymentMethod = paymentMethod,
                amountPaid = amountPaid,
                transactionId = transactionId,
                screenshotNote = screenshotNote
            )
            // Remove from cart if it was there
            studentRepo.removeFromCart(course.id)
            onSuccess(orderNum)
        }
    }

    // Cart actions
    fun toggleCart(courseId: Long, isInCart: Boolean) {
        viewModelScope.launch {
            if (isInCart) studentRepo.removeFromCart(courseId) else studentRepo.addToCart(courseId)
        }
    }

    fun clearCart() {
        viewModelScope.launch { studentRepo.clearCart() }
    }

    // Course & Lesson actions
    fun markLessonCompleted(courseId: Long, lessonId: Long) {
        viewModelScope.launch {
            studentRepo.updateLessonProgress(currentUserId, courseId, lessonId)
        }
    }

    fun savePersonalNote(courseId: Long, lessonId: Long, title: String, content: String) {
        viewModelScope.launch {
            studentRepo.addNote(courseId, lessonId, title, content)
        }
    }

    fun deletePersonalNote(noteId: Long) {
        viewModelScope.launch {
            studentRepo.deleteNote(noteId)
        }
    }

    fun toggleWishlist(courseId: Long, isCurrentlyWishlisted: Boolean) {
        viewModelScope.launch {
            studentRepo.toggleWishlist(courseId, isCurrentlyWishlisted)
        }
    }

    fun postAnnouncement(title: String, message: String, category: String = "GENERAL") {
        viewModelScope.launch {
            adminRepo.postAnnouncement(title, message, category)
        }
    }

    fun issueCertificate(courseId: Long, courseTitle: String, onIssued: (CertificateEntity) -> Unit) {
        viewModelScope.launch {
            val cert = studentRepo.generateCertificate(courseId, courseTitle, currentUserName, currentUserEmail)
            onIssued(cert)
        }
    }
}
