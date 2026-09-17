package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PakistanPaymentAccountCard
import com.example.ui.theme.BrandAmberGold
import com.example.ui.theme.BrandEmeraldGreen
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun ProfileScreen(
    viewModel: MainViewModel
) {
    val enrollments by viewModel.userEnrollments.collectAsState()
    val certificates by viewModel.userCertificates.collectAsState()
    val orders by viewModel.userOrders.collectAsState()

    var showRefundDialog by remember { mutableStateOf(false) }
    var showPaymentDetailsDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // User Profile Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = viewModel.currentUserName.take(1),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = viewModel.currentUserName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = viewModel.currentUserEmail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = viewModel.currentUserPhone,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Student Stats
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatMetricCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.School,
                    value = "${enrollments.size}",
                    label = "Enrolled",
                    iconColor = MaterialTheme.colorScheme.primary
                )
                StatMetricCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.WorkspacePremium,
                    value = "${certificates.size}",
                    label = "Certificates",
                    iconColor = BrandAmberGold
                )
                StatMetricCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.ReceiptLong,
                    value = "${orders.size}",
                    label = "Orders",
                    iconColor = BrandEmeraldGreen
                )
            }
        }

        // Section: Learning & Account
        item {
            Text("Learning & Purchases", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    ProfileMenuRow(Icons.Default.School, "My Courses & Progress") {
                        viewModel.navigateTo(Screen.MyLearning)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ProfileMenuRow(Icons.Default.WorkspacePremium, "My Certificates") {
                        viewModel.navigateTo(Screen.MyLearning)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ProfileMenuRow(Icons.Default.History, "Payment & Order Status") {
                        val latest = orders.firstOrNull()
                        if (latest != null) {
                            viewModel.navigateTo(Screen.OrderTracking(latest.orderNumber))
                        } else {
                            viewModel.navigateTo(Screen.MyLearning)
                        }
                    }
                }
            }
        }

        // Section: Policies & Support
        item {
            Text("Support & Information", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    ProfileMenuRow(Icons.Default.Payment, "Official Pakistan Payment Accounts") {
                        showPaymentDetailsDialog = true
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ProfileMenuRow(Icons.Default.Policy, "7-Day Refund Policy") {
                        showRefundDialog = true
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ProfileMenuRow(Icons.Default.VerifiedUser, "SkillPulse Education Manifesto") {
                        showRefundDialog = true
                    }
                }
            }
        }

        // Section: Administrator Portal
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Platform Administration",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Access secure course management, verify manual JazzCash & Easypaisa payments, review student enrollments, and check security audit logs.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.navigateTo(Screen.AdminLogin) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Open Admin Portal")
                    }
                }
            }
        }
    }

    if (showRefundDialog) {
        AlertDialog(
            onDismissRequest = { showRefundDialog = false },
            title = { Text("7-Day Refund Policy & Guarantee") },
            text = {
                Text(
                    text = "SkillPulse is built strictly for genuine educational outcomes and practical skills. If you are not satisfied with any course curriculum within 7 days of purchase, request a full refund by contacting support or Sikander Ali on WhatsApp/Call with your Order Number and TID.\n\nRefunds are processed back to your original JazzCash or Easypaisa account within 24 hours.",
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(onClick = { showRefundDialog = false }) {
                    Text("Understood")
                }
            }
        )
    }

    if (showPaymentDetailsDialog) {
        AlertDialog(
            onDismissRequest = { showPaymentDetailsDialog = false },
            title = { Text("Payment Accounts (Pakistan)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("1. JAZZCASH\nAccount Holder: Sikander Ali\nNumber: 03073896980\n\n2. EASYPAISA\nAccount Holder: Sikander Ali\nNumber: 03493818917", fontSize = 13.sp)
                }
            },
            confirmButton = {
                Button(onClick = { showPaymentDetailsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun StatMetricCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    iconColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ProfileMenuRow(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
