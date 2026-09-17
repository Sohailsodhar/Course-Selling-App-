package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CourseEntity::class,
        LessonEntity::class,
        ResourceEntity::class,
        QuizQuestionEntity::class,
        OrderEntity::class,
        EnrollmentEntity::class,
        CertificateEntity::class,
        AuditLogEntity::class,
        StudentNoteEntity::class,
        AnnouncementEntity::class,
        CartItemEntity::class,
        WishlistItemEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun lessonDao(): LessonDao
    abstract fun resourceDao(): ResourceDao
    abstract fun quizDao(): QuizDao
    abstract fun orderDao(): OrderDao
    abstract fun enrollmentDao(): EnrollmentDao
    abstract fun certificateDao(): CertificateDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun studentNoteDao(): StudentNoteDao
    abstract fun announcementDao(): AnnouncementDao
    abstract fun cartDao(): CartDao
    abstract fun wishlistDao(): WishlistDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "skillpulse_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    suspend fun seedInitialData() {
        if (courseDao().getCourseCount() > 0) return

        // Seed Courses
        val courses = SeedData.initialCourses
        courseDao().insertCourses(courses)

        // Seed Lessons
        lessonDao().insertLessons(SeedData.initialLessons)

        // Seed Resources
        resourceDao().insertResources(SeedData.initialResources)

        // Seed Quizzes
        quizDao().insertQuestions(SeedData.initialQuizQuestions)

        // Seed Sample Student Enrollment in one course to showcase "Continue Learning"
        enrollmentDao().insertEnrollment(
            EnrollmentEntity(
                userId = "student@skillpulse.pk",
                courseId = 1L, // Physics Masterclass
                progressPercent = 45,
                completedLessonsCount = 2,
                totalLessonsCount = 5,
                isCompleted = false
            )
        )

        // Seed an initial announcement
        announcementDao().insertAnnouncement(
            AnnouncementEntity(
                title = "Welcome to SkillPulse Pakistan!",
                message = "Explore verified Study and YouTube Automation courses. Pay smoothly with JazzCash or Easypaisa.",
                date = "September 2026",
                category = "Welcome"
            )
        )

        // Seed initial audit log
        auditLogDao().insertLog(
            AuditLogEntity(
                adminEmail = "system@skillpulse.pk",
                action = "SYSTEM_INITIALIZED",
                targetRecordId = "INIT",
                details = "Platform initialized with verified study & video automation curriculum."
            )
        )
    }
}
