package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String, // "STUDY" or "YOUTUBE"
    val subject: String,
    val instructor: String,
    val instructorRole: String,
    val rating: Float = 4.8f,
    val studentsCount: Int = 120,
    val duration: String = "12 Hours",
    val level: String = "Beginner", // "Beginner", "Intermediate", "Advanced"
    val language: String = "Urdu / English",
    val lastUpdated: String = "September 2026",
    val price: Double = 2499.0,
    val originalPrice: Double = 4999.0,
    val isFree: Boolean = false,
    val isPublished: Boolean = true,
    val isFeatured: Boolean = false,
    val isPopular: Boolean = false,
    val isNew: Boolean = false,
    val description: String,
    val learnPoints: String, // Pipe-separated points
    val requirements: String, // Pipe-separated
    val thumbnailKey: String = "default_banner"
)

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long,
    val moduleName: String,
    val title: String,
    val durationMinutes: Int,
    val videoUrl: String = "",
    val summary: String = "",
    val isPreview: Boolean = false,
    val isCompleted: Boolean = false,
    val sortOrder: Int = 0
)

@Entity(tableName = "resources")
data class ResourceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long,
    val title: String,
    val type: String, // "PDF Notes", "Formula Sheet", "Script Template"
    val fileSize: String,
    val downloadUrl: String = ""
)

@Entity(tableName = "quiz_questions")
data class QuizQuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long,
    val question: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctOption: Int, // 0..3
    val explanation: String
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderNumber: String,
    val courseId: Long,
    val courseTitle: String,
    val courseCategory: String,
    val userId: String,
    val userEmail: String,
    val senderName: String,
    val senderPhone: String,
    val paymentMethod: String, // "JazzCash" or "Easypaisa"
    val amountPaid: Double,
    val transactionId: String,
    val screenshotNote: String = "Bank SMS Verified",
    val status: String, // "PENDING", "APPROVED", "REJECTED"
    val adminNotes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "enrollments")
data class EnrollmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val courseId: Long,
    val progressPercent: Int = 0,
    val completedLessonsCount: Int = 0,
    val totalLessonsCount: Int = 0,
    val isCompleted: Boolean = false,
    val enrolledAt: Long = System.currentTimeMillis(),
    val lastAccessedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "certificates")
data class CertificateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val certificateNumber: String,
    val courseId: Long,
    val courseTitle: String,
    val studentName: String,
    val studentEmail: String,
    val issuedDate: String,
    val grade: String = "Distinction"
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val adminEmail: String,
    val action: String,
    val targetRecordId: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "student_notes")
data class StudentNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long,
    val lessonId: Long,
    val noteTitle: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val date: String,
    val category: String = "General"
)

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "wishlist_items")
data class WishlistItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long,
    val addedAt: Long = System.currentTimeMillis()
)
