package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses WHERE isPublished = 1 ORDER BY id ASC")
    fun getAllPublishedCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses ORDER BY id ASC")
    fun getAllCoursesAdmin(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE id = :id LIMIT 1")
    fun getCourseById(id: Long): Flow<CourseEntity?>

    @Query("SELECT * FROM courses WHERE id = :id LIMIT 1")
    suspend fun getCourseByIdSync(id: Long): CourseEntity?

    @Query("SELECT * FROM courses WHERE category = :category AND isPublished = 1")
    fun getCoursesByCategory(category: String): Flow<List<CourseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<CourseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseEntity): Long

    @Update
    suspend fun updateCourse(course: CourseEntity)

    @Query("DELETE FROM courses WHERE id = :id")
    suspend fun deleteCourseById(id: Long)

    @Query("SELECT COUNT(*) FROM courses")
    suspend fun getCourseCount(): Int
}

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons WHERE courseId = :courseId ORDER BY sortOrder ASC, id ASC")
    fun getLessonsForCourse(courseId: Long): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE courseId = :courseId ORDER BY sortOrder ASC, id ASC")
    suspend fun getLessonsForCourseSync(courseId: Long): List<LessonEntity>

    @Query("SELECT * FROM lessons WHERE id = :lessonId LIMIT 1")
    suspend fun getLessonById(lessonId: Long): LessonEntity?

    @Query("UPDATE lessons SET isCompleted = :completed WHERE id = :lessonId")
    suspend fun setLessonCompleted(lessonId: Long, completed: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<LessonEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: LessonEntity): Long
}

@Dao
interface ResourceDao {
    @Query("SELECT * FROM resources WHERE courseId = :courseId")
    fun getResourcesForCourse(courseId: Long): Flow<List<ResourceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResources(resources: List<ResourceEntity>)
}

@Dao
interface QuizDao {
    @Query("SELECT * FROM quiz_questions WHERE courseId = :courseId")
    fun getQuestionsForCourse(courseId: Long): Flow<List<QuizQuestionEntity>>

    @Query("SELECT * FROM quiz_questions WHERE courseId = :courseId")
    suspend fun getQuestionsForCourseSync(courseId: Long): List<QuizQuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuizQuestionEntity>)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY createdAt DESC")
    fun getOrdersForUser(userId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE status = 'PENDING' ORDER BY createdAt DESC")
    fun getPendingOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    suspend fun getOrderById(orderId: Long): OrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Query("UPDATE orders SET status = :status, adminNotes = :adminNotes WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Long, status: String, adminNotes: String)

    @Query("SELECT COUNT(*) FROM orders WHERE status = 'PENDING'")
    fun getPendingOrdersCount(): Flow<Int>

    @Query("SELECT SUM(amountPaid) FROM orders WHERE status = 'APPROVED'")
    fun getTotalRevenue(): Flow<Double?>
}

@Dao
interface EnrollmentDao {
    @Query("SELECT * FROM enrollments WHERE userId = :userId")
    fun getEnrollmentsForUser(userId: String): Flow<List<EnrollmentEntity>>

    @Query("SELECT * FROM enrollments WHERE userId = :userId AND courseId = :courseId LIMIT 1")
    fun getEnrollment(userId: String, courseId: Long): Flow<EnrollmentEntity?>

    @Query("SELECT * FROM enrollments WHERE userId = :userId AND courseId = :courseId LIMIT 1")
    suspend fun getEnrollmentSync(userId: String, courseId: Long): EnrollmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnrollment(enrollment: EnrollmentEntity)

    @Query("UPDATE enrollments SET progressPercent = :progress, completedLessonsCount = :completed, isCompleted = :isCompleted, lastAccessedAt = :time WHERE userId = :userId AND courseId = :courseId")
    suspend fun updateProgress(userId: String, courseId: Long, progress: Int, completed: Int, isCompleted: Boolean, time: Long)

    @Query("SELECT COUNT(DISTINCT userId) FROM enrollments")
    fun getTotalStudentsCount(): Flow<Int>
}

@Dao
interface CertificateDao {
    @Query("SELECT * FROM certificates WHERE studentEmail = :email ORDER BY id DESC")
    fun getCertificatesForUser(email: String): Flow<List<CertificateEntity>>

    @Query("SELECT * FROM certificates ORDER BY id DESC")
    fun getAllCertificates(): Flow<List<CertificateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCertificate(cert: CertificateEntity)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 50")
    fun getRecentLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLogEntity)
}

@Dao
interface StudentNoteDao {
    @Query("SELECT * FROM student_notes WHERE courseId = :courseId ORDER BY createdAt DESC")
    fun getNotesForCourse(courseId: Long): Flow<List<StudentNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: StudentNoteEntity)

    @Query("DELETE FROM student_notes WHERE id = :id")
    suspend fun deleteNote(id: Long)
}

@Dao
interface AnnouncementDao {
    @Query("SELECT * FROM announcements ORDER BY id DESC")
    fun getAllAnnouncements(): Flow<List<AnnouncementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: AnnouncementEntity)
}

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items ORDER BY addedAt DESC")
    fun getAllCartItems(): Flow<List<CartItemEntity>>

    @Query("SELECT COUNT(*) FROM cart_items")
    fun getCartCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addToCart(item: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE courseId = :courseId")
    suspend fun removeFromCart(courseId: Long)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()

    @Query("SELECT EXISTS(SELECT 1 FROM cart_items WHERE courseId = :courseId)")
    fun isCourseInCart(courseId: Long): Flow<Boolean>
}

@Dao
interface WishlistDao {
    @Query("SELECT * FROM wishlist_items ORDER BY addedAt DESC")
    fun getAllWishlistItems(): Flow<List<WishlistItemEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addToWishlist(item: WishlistItemEntity)

    @Query("DELETE FROM wishlist_items WHERE courseId = :courseId")
    suspend fun removeFromWishlist(courseId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM wishlist_items WHERE courseId = :courseId)")
    fun isWishlisted(courseId: Long): Flow<Boolean>
}
