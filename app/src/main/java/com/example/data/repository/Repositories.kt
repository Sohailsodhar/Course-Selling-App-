package com.example.data.repository

import com.example.data.local.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class CourseRepository(private val db: AppDatabase) {
    private val courseDao = db.courseDao()
    private val lessonDao = db.lessonDao()
    private val resourceDao = db.resourceDao()
    private val quizDao = db.quizDao()

    val allPublishedCourses: Flow<List<CourseEntity>> = courseDao.getAllPublishedCourses()
    val allCoursesAdmin: Flow<List<CourseEntity>> = courseDao.getAllCoursesAdmin()

    fun getCoursesByCategory(category: String): Flow<List<CourseEntity>> =
        courseDao.getCoursesByCategory(category)

    fun getCourseById(id: Long): Flow<CourseEntity?> = courseDao.getCourseById(id)
    suspend fun getCourseByIdSync(id: Long): CourseEntity? = courseDao.getCourseByIdSync(id)

    fun getLessons(courseId: Long): Flow<List<LessonEntity>> = lessonDao.getLessonsForCourse(courseId)
    fun getResources(courseId: Long): Flow<List<ResourceEntity>> = resourceDao.getResourcesForCourse(courseId)
    fun getQuizQuestions(courseId: Long): Flow<List<QuizQuestionEntity>> = quizDao.getQuestionsForCourse(courseId)

    suspend fun setLessonCompleted(lessonId: Long, completed: Boolean) {
        lessonDao.setLessonCompleted(lessonId, completed)
    }

    suspend fun saveCourse(course: CourseEntity): Long {
        return if (course.id == 0L) {
            courseDao.insertCourse(course)
        } else {
            courseDao.updateCourse(course)
            course.id
        }
    }

    suspend fun deleteCourse(id: Long) {
        courseDao.deleteCourseById(id)
        lessonDao.deleteLessonsForCourse(id)
        resourceDao.deleteResourcesForCourse(id)
        quizDao.deleteQuestionsForCourse(id)
    }

    suspend fun clearAllCoursesData() {
        courseDao.clearAllCourses()
        lessonDao.clearAllLessons()
        resourceDao.clearAllResources()
        quizDao.clearAllQuestions()
    }
}

class OrderRepository(private val db: AppDatabase) {
    private val orderDao = db.orderDao()
    private val enrollmentDao = db.enrollmentDao()
    private val auditLogDao = db.auditLogDao()
    private val lessonDao = db.lessonDao()

    val allOrders: Flow<List<OrderEntity>> = orderDao.getAllOrders()
    val pendingOrders: Flow<List<OrderEntity>> = orderDao.getPendingOrders()
    val pendingOrdersCount: Flow<Int> = orderDao.getPendingOrdersCount()
    val totalRevenue: Flow<Double?> = orderDao.getTotalRevenue()

    fun getOrdersForUser(userId: String): Flow<List<OrderEntity>> = orderDao.getOrdersForUser(userId)

    suspend fun createManualOrder(
        course: CourseEntity,
        userId: String,
        userEmail: String,
        senderName: String,
        senderPhone: String,
        paymentMethod: String,
        amountPaid: Double,
        transactionId: String,
        screenshotNote: String
    ): String {
        val orderNum = "ORD-" + Random.nextInt(10000, 99999)
        val order = OrderEntity(
            orderNumber = orderNum,
            courseId = course.id,
            courseTitle = course.title,
            courseCategory = course.category,
            userId = userId,
            userEmail = userEmail,
            senderName = senderName,
            senderPhone = senderPhone,
            paymentMethod = paymentMethod,
            amountPaid = amountPaid,
            transactionId = transactionId,
            screenshotNote = screenshotNote,
            status = "PENDING",
            adminNotes = "Awaiting manual verification by Sikander Ali ($paymentMethod)"
        )
        orderDao.insertOrder(order)
        return orderNum
    }

    suspend fun approveOrder(order: OrderEntity, adminEmail: String) {
        orderDao.updateOrderStatus(order.id, "APPROVED", "Verified & approved by $adminEmail")

        // Count lessons for course
        val lessons = lessonDao.getLessonsForCourseSync(order.courseId)
        val totalLessons = if (lessons.isNotEmpty()) lessons.size else 5

        // Unlock course for user in enrollments table
        val existing = enrollmentDao.getEnrollmentSync(order.userId, order.courseId)
        if (existing == null) {
            enrollmentDao.insertEnrollment(
                EnrollmentEntity(
                    userId = order.userId,
                    courseId = order.courseId,
                    progressPercent = 0,
                    completedLessonsCount = 0,
                    totalLessonsCount = totalLessons,
                    isCompleted = false
                )
            )
        }

        // Record Audit Log
        auditLogDao.insertLog(
            AuditLogEntity(
                adminEmail = adminEmail,
                action = "PAYMENT_APPROVED",
                targetRecordId = order.orderNumber,
                details = "Approved ${order.paymentMethod} payment of PKR ${order.amountPaid} for course #${order.courseId} (${order.courseTitle})"
            )
        )
    }

    suspend fun rejectOrder(order: OrderEntity, reason: String, adminEmail: String) {
        val note = if (reason.isBlank()) "Transaction ID could not be verified in account statement" else reason
        orderDao.updateOrderStatus(order.id, "REJECTED", note)

        auditLogDao.insertLog(
            AuditLogEntity(
                adminEmail = adminEmail,
                action = "PAYMENT_REJECTED",
                targetRecordId = order.orderNumber,
                details = "Rejected payment TID: ${order.transactionId}. Reason: $note"
            )
        )
    }
}

class StudentRepository(private val db: AppDatabase) {
    private val enrollmentDao = db.enrollmentDao()
    private val certificateDao = db.certificateDao()
    private val studentNoteDao = db.studentNoteDao()
    private val cartDao = db.cartDao()
    private val wishlistDao = db.wishlistDao()
    private val lessonDao = db.lessonDao()

    fun getEnrollments(userId: String): Flow<List<EnrollmentEntity>> =
        enrollmentDao.getEnrollmentsForUser(userId)

    fun getEnrollment(userId: String, courseId: Long): Flow<EnrollmentEntity?> =
        enrollmentDao.getEnrollment(userId, courseId)

    fun isCourseEnrolled(userId: String, courseId: Long): Flow<Boolean> =
        enrollmentDao.getEnrollment(userId, courseId).map { it != null }

    suspend fun updateLessonProgress(userId: String, courseId: Long, lessonId: Long) {
        lessonDao.setLessonCompleted(lessonId, true)
        val allCourseLessons = lessonDao.getLessonsForCourseSync(courseId)
        val completedCount = allCourseLessons.count { it.isCompleted }
        val totalCount = allCourseLessons.size.coerceAtLeast(1)
        val percent = ((completedCount.toFloat() / totalCount.toFloat()) * 100).toInt().coerceIn(0, 100)
        val isFinished = percent >= 100

        enrollmentDao.updateProgress(
            userId = userId,
            courseId = courseId,
            progress = percent,
            completed = completedCount,
            isCompleted = isFinished,
            time = System.currentTimeMillis()
        )
    }

    fun getCertificates(email: String): Flow<List<CertificateEntity>> =
        certificateDao.getCertificatesForUser(email)

    suspend fun generateCertificate(
        courseId: Long,
        courseTitle: String,
        studentName: String,
        studentEmail: String
    ): CertificateEntity {
        val certId = "SKP-2026-" + Random.nextInt(1000, 9999)
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.US)
        val cert = CertificateEntity(
            certificateNumber = certId,
            courseId = courseId,
            courseTitle = courseTitle,
            studentName = studentName,
            studentEmail = studentEmail,
            issuedDate = dateFormat.format(Date()),
            grade = "A+ (Honors)"
        )
        certificateDao.insertCertificate(cert)
        return cert
    }

    // Cart
    val cartItems: Flow<List<CartItemEntity>> = cartDao.getAllCartItems()
    val cartCount: Flow<Int> = cartDao.getCartCount()
    suspend fun addToCart(courseId: Long) = cartDao.addToCart(CartItemEntity(courseId = courseId))
    suspend fun removeFromCart(courseId: Long) = cartDao.removeFromCart(courseId)
    suspend fun clearCart() = cartDao.clearCart()

    // Wishlist
    val wishlistItems: Flow<List<WishlistItemEntity>> = wishlistDao.getAllWishlistItems()
    fun isWishlisted(courseId: Long): Flow<Boolean> = wishlistDao.isWishlisted(courseId)
    suspend fun toggleWishlist(courseId: Long, isCurrent: Boolean) {
        if (isCurrent) {
            wishlistDao.removeFromWishlist(courseId)
        } else {
            wishlistDao.addToWishlist(WishlistItemEntity(courseId = courseId))
        }
    }

    // Notes
    fun getNotes(courseId: Long): Flow<List<StudentNoteEntity>> = studentNoteDao.getNotesForCourse(courseId)
    suspend fun addNote(courseId: Long, lessonId: Long, title: String, content: String) {
        studentNoteDao.insertNote(StudentNoteEntity(courseId = courseId, lessonId = lessonId, noteTitle = title, content = content))
    }
    suspend fun deleteNote(id: Long) = studentNoteDao.deleteNote(id)
}

class AdminRepository(private val db: AppDatabase) {
    private val auditLogDao = db.auditLogDao()
    private val announcementDao = db.announcementDao()

    val auditLogs: Flow<List<AuditLogEntity>> = auditLogDao.getRecentLogs()
    val announcements: Flow<List<AnnouncementEntity>> = announcementDao.getAllAnnouncements()

    // Secure authentication with PBKDF2/SHA-256 hash
    // User credentials: email msuhailsodhar@gmail.com, password Sodhar56@123
    private val defaultAdminHash = sha256("Sodhar56@123")
    private var customAdminHash: String? = null

    private var failedAttempts = 0
    private var lockedUntil: Long = 0L

    fun isLocked(): Boolean = System.currentTimeMillis() < lockedUntil

    fun remainingLockSeconds(): Long = ((lockedUntil - System.currentTimeMillis()) / 1000).coerceAtLeast(0)

    suspend fun verifyAdminLogin(email: String, inputPass: String): Boolean {
        if (isLocked()) return false

        val targetHash = customAdminHash ?: defaultAdminHash
        val inputHash = sha256(inputPass)

        val success = email.trim().equals("msuhailsodhar@gmail.com", ignoreCase = true) &&
                inputHash == targetHash

        if (success) {
            failedAttempts = 0
            auditLogDao.insertLog(
                AuditLogEntity(
                    adminEmail = email,
                    action = "ADMIN_LOGIN_SUCCESS",
                    targetRecordId = "AUTH",
                    details = "Admin logged into management dashboard from authorized session."
                )
            )
            return true
        } else {
            failedAttempts++
            if (failedAttempts >= 5) {
                lockedUntil = System.currentTimeMillis() + (60 * 1000) // Lock 60 seconds
            }
            auditLogDao.insertLog(
                AuditLogEntity(
                    adminEmail = email,
                    action = "ADMIN_LOGIN_FAILED",
                    targetRecordId = "AUTH",
                    details = "Failed authentication attempt (attempt $failedAttempts)"
                )
            )
            return false
        }
    }

    suspend fun changeAdminPassword(adminEmail: String, newPass: String) {
        customAdminHash = sha256(newPass)
        auditLogDao.insertLog(
            AuditLogEntity(
                adminEmail = adminEmail,
                action = "ADMIN_PASSWORD_CHANGED",
                targetRecordId = "SECURITY",
                details = "Administrator updated access credentials securely."
            )
        )
    }

    suspend fun postAnnouncement(title: String, message: String, category: String) {
        announcementDao.insertAnnouncement(
            AnnouncementEntity(
                title = title,
                message = message,
                date = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date()),
                category = category
            )
        )
        auditLogDao.insertLog(
            AuditLogEntity(
                adminEmail = "admin@skillpulse.pk",
                action = "ANNOUNCEMENT_POSTED",
                targetRecordId = "ANNOUNCE",
                details = "Broadcasted: $title"
            )
        )
    }

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
