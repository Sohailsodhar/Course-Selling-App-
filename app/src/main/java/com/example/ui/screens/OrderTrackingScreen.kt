package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrandAmberGold
import com.example.ui.theme.BrandEmeraldGreen
import com.example.ui.theme.BrandTealPrimary
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun OrderTrackingScreen(
    orderNumber: String,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val orders by viewModel.userOrders.collectAsState()
    val order = orders.firstOrNull { it.orderNumber == orderNumber } ?: orders.firstOrNull()

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
                    Text(
                        text = "Order Details & Status",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    ) { innerPadding ->
        if (order == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading order information...")
            }
            return@Scaffold
        }

        val isApproved = order.status == "APPROVED"
        val isPending = order.status == "PENDING"
        val isRejected = order.status == "REJECTED"

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isApproved -> BrandEmeraldGreen.copy(alpha = 0.12f)
                            isPending -> BrandAmberGold.copy(alpha = 0.12f)
                            else -> MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    when {
                                        isApproved -> BrandEmeraldGreen
                                        isPending -> BrandAmberGold
                                        else -> MaterialTheme.colorScheme.error
                                    },
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when {
                                    isApproved -> Icons.Default.CheckCircle
                                    isPending -> Icons.Default.HourglassTop
                                    else -> Icons.Default.Cancel
                                },
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = when {
                                isApproved -> "Payment Verified & Approved"
                                isPending -> "Payment Under Verification"
                                else -> "Payment Rejected"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = when {
                                isApproved -> BrandEmeraldGreen
                                isPending -> BrandAmberGold
                                else -> MaterialTheme.colorScheme.error
                            }
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = when {
                                isApproved -> "Course access has been unlocked and added to your learning dashboard!"
                                isPending -> "Your payment details have been sent to Administrator Sikander Ali. Verification typically takes 10 to 30 minutes."
                                else -> order.adminNotes
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            // Timeline Steps
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Verification Progress",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        TimelineStep(1, "Order Generated", "Order #${order.orderNumber}", isDone = true)
                        TimelineStep(2, "Payment Submitted", "${order.paymentMethod} • TID: ${order.transactionId}", isDone = true)
                        TimelineStep(
                            3,
                            "Manual Statement Check",
                            "Sikander Ali verifies transaction in account app",
                            isDone = isApproved || isRejected,
                            isActive = isPending
                        )
                        TimelineStep(4, "Course Unlocked", "Immediate access to video lessons & PDFs", isDone = isApproved)
                    }
                }
            }

            // Order Metadata Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Order Details",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        DetailRow("Order Number", order.orderNumber)
                        DetailRow("Course Title", order.courseTitle)
                        DetailRow("Payment Method", order.paymentMethod)
                        DetailRow("Transaction ID (TID)", order.transactionId)
                        DetailRow("Sender Name", order.senderName)
                        DetailRow("Sender Phone", order.senderPhone)
                        DetailRow("Amount Paid", "PKR ${order.amountPaid.toInt()}")
                    }
                }
            }

            // Action Buttons
            item {
                if (isApproved) {
                    Button(
                        onClick = { viewModel.navigateTo(Screen.LessonPlayer(order.courseId)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandEmeraldGreen)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Start Learning Course", fontWeight = FontWeight.Bold)
                    }
                } else if (isPending) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.navigateTo(Screen.MyLearning) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Go to My Learning Dashboard")
                        }

                        // Quick link to admin dashboard for testing approval
                        OutlinedButton(
                            onClick = { viewModel.navigateTo(Screen.AdminLogin) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open Admin Portal to Review Orders")
                        }
                    }
                } else {
                    Button(
                        onClick = { viewModel.navigateTo(Screen.Checkout(order.courseId)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Resubmit Payment")
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineStep(
    stepNumber: Int,
    title: String,
    subtitle: String,
    isDone: Boolean,
    isActive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(
                    when {
                        isDone -> BrandEmeraldGreen
                        isActive -> BrandAmberGold
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            } else {
                Text(
                    text = "$stepNumber",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                fontWeight = if (isDone || isActive) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp,
                color = if (isDone || isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}
