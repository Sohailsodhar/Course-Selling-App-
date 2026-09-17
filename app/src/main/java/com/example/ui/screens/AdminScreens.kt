package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CourseEntity
import com.example.data.local.OrderEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun AdminLoginScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("admin@skillpulse.pk") }
    var password by remember { mutableStateOf("Admin@2026") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Admin Authentication", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SkillPulse Admin Portal",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Manual Payment Verification & Course Control",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Administrator Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Master Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) }
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    isLoading = true
                    viewModel.loginAdmin(email.trim(), password) { success, msg ->
                        isLoading = false
                        if (success) {
                            Toast.makeText(context, "Welcome Administrator!", Toast.LENGTH_SHORT).show()
                        } else {
                            errorMessage = msg
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text("Secure Login", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "ℹ️ Demo Admin Credentials:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Email: admin@skillpulse.pk\nPassword: Admin@2026",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun AdminDashboardScreen(
    viewModel: MainViewModel,
    onBackToApp: () -> Unit
) {
    val context = LocalContext.current
    val allOrders by viewModel.allOrdersAdmin.collectAsState()
    val pendingOrders by viewModel.pendingOrdersAdmin.collectAsState()
    val allCourses by viewModel.allPublishedCourses.collectAsState()
    val auditLogs by viewModel.adminAuditLogs.collectAsState()
    val announcements by viewModel.announcements.collectAsState()

    var activeTab by remember { mutableIntStateOf(0) } // 0: Orders, 1: Courses, 2: Announcements, 3: Security Logs
    var orderStatusFilter by remember { mutableStateOf("ALL") } // ALL, PENDING, APPROVED, REJECTED

    // Reject order dialog state
    var selectedOrderForReject by remember { mutableStateOf<OrderEntity?>(null) }
    var rejectReason by remember { mutableStateOf("") }

    // Course edit dialog state
    var showAddCourseDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 3.dp) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Admin Console", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Logged in as ${viewModel.adminEmail.value}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Row {
                            TextButton(onClick = { viewModel.logoutAdmin() }) {
                                Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Exit", fontSize = 12.sp)
                            }
                        }
                    }

                    TabRow(
                        selectedTabIndex = activeTab,
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        Tab(
                            selected = activeTab == 0,
                            onClick = { activeTab = 0 },
                            text = { Text("Orders (${pendingOrders.size} pending)") }
                        )
                        Tab(
                            selected = activeTab == 1,
                            onClick = { activeTab = 1 },
                            text = { Text("Courses (${allCourses.size})") }
                        )
                        Tab(
                            selected = activeTab == 2,
                            onClick = { activeTab = 2 },
                            text = { Text("Broadcast") }
                        )
                        Tab(
                            selected = activeTab == 3,
                            onClick = { activeTab = 3 },
                            text = { Text("Audit Logs") }
                        )
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
            when (activeTab) {
                0 -> {
                    // Orders & Payment Verification Tab
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Summary Metrics
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MetricBox("Total Orders", "${allOrders.size}", BrandStudyBlue, Modifier.weight(1f))
                                MetricBox("Pending Review", "${pendingOrders.size}", BrandAmberGold, Modifier.weight(1f))
                                val approvedCount = allOrders.count { it.status == "APPROVED" }
                                MetricBox("Approved", "$approvedCount", BrandEmeraldGreen, Modifier.weight(1f))
                            }
                        }

                        // Filter Chips
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = orderStatusFilter == "ALL",
                                    onClick = { orderStatusFilter = "ALL" },
                                    label = { Text("All") }
                                )
                                FilterChip(
                                    selected = orderStatusFilter == "PENDING",
                                    onClick = { orderStatusFilter = "PENDING" },
                                    label = { Text("Pending (${pendingOrders.size})") }
                                )
                                FilterChip(
                                    selected = orderStatusFilter == "APPROVED",
                                    onClick = { orderStatusFilter = "APPROVED" },
                                    label = { Text("Approved") }
                                )
                                FilterChip(
                                    selected = orderStatusFilter == "REJECTED",
                                    onClick = { orderStatusFilter = "REJECTED" },
                                    label = { Text("Rejected") }
                                )
                            }
                        }

                        val filteredOrders = allOrders.filter {
                            orderStatusFilter == "ALL" || it.status == orderStatusFilter
                        }

                        if (filteredOrders.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No orders in this category", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        } else {
                            items(filteredOrders) { order ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = order.orderNumber,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            )

                                            Surface(
                                                color = when (order.status) {
                                                    "APPROVED" -> BrandEmeraldGreen.copy(alpha = 0.15f)
                                                    "PENDING" -> BrandAmberGold.copy(alpha = 0.15f)
                                                    else -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                                },
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = order.status,
                                                    color = when (order.status) {
                                                        "APPROVED" -> BrandEmeraldGreen
                                                        "PENDING" -> BrandAmberGold
                                                        else -> MaterialTheme.colorScheme.error
                                                    },
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(text = order.courseTitle, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = "Method: ${order.paymentMethod} • Amount: PKR ${order.amountPaid.toInt()}",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        Text(
                                            text = "TID: ${order.transactionId}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Text(
                                            text = "Sender: ${order.senderName} (${order.senderPhone})",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        if (order.screenshotNote.isNotBlank()) {
                                            Text(
                                                text = "Note: ${order.screenshotNote}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        if (order.status == "PENDING") {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = {
                                                        viewModel.approvePayment(order)
                                                        Toast.makeText(context, "Order ${order.orderNumber} APPROVED. Course unlocked!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = BrandEmeraldGreen),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Approve & Unlock", fontSize = 12.sp)
                                                }

                                                OutlinedButton(
                                                    onClick = {
                                                        selectedOrderForReject = order
                                                        rejectReason = ""
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Reject", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Course Management Tab
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Button(
                                onClick = { showAddCourseDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add New Course")
                            }
                        }

                        items(allCourses) { course ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                color = if (course.category == "STUDY") BrandStudyBlue else BrandPurpleAccent,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = course.category,
                                                    color = Color.White,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "PKR ${course.price.toInt()}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = course.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(text = "${course.instructor} • ${course.studentsCount} students", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }

                                    IconButton(
                                        onClick = { viewModel.deleteCourse(course.id) }
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Announcements
                    var announceTitle by remember { mutableStateOf("") }
                    var announceMsg by remember { mutableStateOf("") }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Broadcast Announcement to Students", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = announceTitle,
                                        onValueChange = { announceTitle = it },
                                        label = { Text("Title") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = announceMsg,
                                        onValueChange = { announceMsg = it },
                                        label = { Text("Message") },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 2
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = {
                                            if (announceTitle.isNotBlank()) {
                                                viewModel.postAnnouncement(announceTitle, announceMsg, "GENERAL")
                                                announceTitle = ""
                                                announceMsg = ""
                                                Toast.makeText(context, "Announcement Broadcasted!", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Publish Announcement")
                                    }
                                }
                            }
                        }

                        items(announcements) { ann ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(text = ann.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = ann.message, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = ann.date, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
                3 -> {
                    // Audit Logs Tab
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text("Real-Time Security & Action Audit Trail", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        items(auditLogs) { log ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = log.action,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = log.timestamp.toString().takeLast(8),
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = log.details, fontSize = 11.sp)
                                    Text(text = "By: ${log.adminEmail}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Reject Dialog
    if (selectedOrderForReject != null) {
        AlertDialog(
            onDismissRequest = { selectedOrderForReject = null },
            title = { Text("Reject Payment Verification") },
            text = {
                Column {
                    Text("Order #${selectedOrderForReject?.orderNumber} • TID: ${selectedOrderForReject?.transactionId}")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        label = { Text("Rejection Reason") },
                        placeholder = { Text("e.g. TID not found in statement") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.rejectPayment(selectedOrderForReject!!, rejectReason)
                        Toast.makeText(context, "Order marked as REJECTED", Toast.LENGTH_SHORT).show()
                        selectedOrderForReject = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Confirm Reject")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedOrderForReject = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Course Dialog
    if (showAddCourseDialog) {
        var title by remember { mutableStateOf("") }
        var subject by remember { mutableStateOf("Physics") }
        var category by remember { mutableStateOf("STUDY") }
        var instructor by remember { mutableStateOf("Dr. Sikander Ali") }
        var priceStr by remember { mutableStateOf("2999") }
        var desc by remember { mutableStateOf("Comprehensive conceptual mastery course.") }

        AlertDialog(
            onDismissRequest = { showAddCourseDialog = false },
            title = { Text("Add New Course") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Course Title") }, modifier = Modifier.fillMaxWidth())
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { category = "STUDY" },
                            colors = ButtonDefaults.buttonColors(containerColor = if (category == "STUDY") BrandStudyBlue else Color.Gray),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("📚 Study")
                        }
                        Button(
                            onClick = { category = "YOUTUBE" },
                            colors = ButtonDefaults.buttonColors(containerColor = if (category == "YOUTUBE") BrandPurpleAccent else Color.Gray),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🎬 YouTube")
                        }
                    }
                    OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject / Topic") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = instructor, onValueChange = { instructor = it }, label = { Text("Instructor") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = priceStr, onValueChange = { priceStr = it }, label = { Text("Price (PKR)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val p = priceStr.toDoubleOrNull() ?: 2999.0
                        val newCourse = CourseEntity(
                            title = title,
                            subject = subject,
                            category = category,
                            description = desc,
                            instructor = instructor,
                            instructorRole = "Lead Educator",
                            price = p,
                            originalPrice = p * 1.5,
                            isFeatured = true,
                            duration = "10 hours",
                            level = "All Levels",
                            language = "Urdu / English",
                            learnPoints = "Practical skills|Concept mastery|Exam strategy",
                            requirements = "Dedication and note-taking",
                            thumbnailKey = "study_default"
                        )
                        viewModel.saveCourse(newCourse) {
                            showAddCourseDialog = false
                            Toast.makeText(context, "Course published successfully!", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Save Course")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCourseDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MetricBox(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = color)
            Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
