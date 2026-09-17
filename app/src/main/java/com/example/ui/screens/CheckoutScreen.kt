package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PakistanPaymentAccountCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.Screen
import kotlin.random.Random

@Composable
fun CheckoutScreen(
    courseId: Long,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val courseFlow = remember(courseId) { viewModel.courseRepo.getCourseById(courseId) }
    val course by courseFlow.collectAsState(initial = null)

    var selectedMethod by remember { mutableStateOf("JazzCash") } // "JazzCash" or "Easypaisa"
    var senderName by remember { mutableStateOf(viewModel.currentUserName) }
    var senderPhone by remember { mutableStateOf(viewModel.currentUserPhone) }
    var transactionId by remember { mutableStateOf("") }
    var screenshotNote by remember { mutableStateOf("Verified Bank SMS confirmation") }
    var isSubmitting by remember { mutableStateOf(false) }

    val tempOrderId = remember { "ORD-" + Random.nextInt(10000, 99999) }

    if (course == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val currentCourse = course!!
    val finalPrice = currentCourse.price
    val originalPrice = currentCourse.originalPrice
    val discount = originalPrice - finalPrice

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
                        text = "Secure Course Checkout",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Payable:",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (currentCourse.isFree) "FREE" else "PKR ${finalPrice.toInt()}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (currentCourse.isFree) {
                                // Direct instant unlock for free courses
                                viewModel.submitManualPayment(
                                    course = currentCourse,
                                    senderName = senderName,
                                    senderPhone = senderPhone,
                                    paymentMethod = "FREE_ENROLLMENT",
                                    amountPaid = 0.0,
                                    transactionId = "FREE-ENROLL",
                                    screenshotNote = "Instant Free Enrollment"
                                ) { orderNum ->
                                    Toast.makeText(context, "Enrolled successfully!", Toast.LENGTH_SHORT).show()
                                    viewModel.navigateTo(Screen.OrderTracking(orderNum))
                                }
                            } else {
                                if (transactionId.trim().length < 6) {
                                    Toast.makeText(context, "Please enter a valid Transaction ID (TID) from your payment receipt SMS.", Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                                if (senderName.isBlank()) {
                                    Toast.makeText(context, "Please enter your sender name.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                isSubmitting = true
                                viewModel.submitManualPayment(
                                    course = currentCourse,
                                    senderName = senderName,
                                    senderPhone = senderPhone,
                                    paymentMethod = selectedMethod,
                                    amountPaid = finalPrice,
                                    transactionId = transactionId.trim(),
                                    screenshotNote = screenshotNote
                                ) { orderNum ->
                                    isSubmitting = false
                                    Toast.makeText(context, "Payment submitted! Pending verification by Sikander Ali.", Toast.LENGTH_LONG).show()
                                    viewModel.navigateTo(Screen.OrderTracking(orderNum))
                                }
                            }
                        },
                        enabled = !isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedMethod == "JazzCash") BrandJazzCashRed else BrandEasypaisaGreen
                        )
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                        } else {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (currentCourse.isFree) "Complete Free Enrollment" else "Submit Payment for Verification",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Step 1: Order Summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "ORDER SUMMARY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = tempOrderId,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = currentCourse.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )

                        Text(
                            text = "Category: ${if (currentCourse.category == "STUDY") "📚 Study Course" else "🎬 YouTube Automation"}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Original Price", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "PKR ${originalPrice.toInt()}",
                                fontSize = 13.sp,
                                style = MaterialTheme.typography.bodyMedium.copy(textDecoration = TextDecoration.LineThrough)
                            )
                        }

                        if (discount > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Promotional Discount", fontSize = 13.sp, color = BrandEmeraldGreen)
                                Text("- PKR ${discount.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BrandEmeraldGreen)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Final Payable Amount", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "PKR ${finalPrice.toInt()}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            if (!currentCourse.isFree) {
                // Step 2: Select Pakistan Payment Method
                item {
                    Text(
                        text = "Select Payment Destination",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // JazzCash Selector
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = if (selectedMethod == "JazzCash") 2.dp else 1.dp,
                                    color = if (selectedMethod == "JazzCash") BrandJazzCashRed else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedMethod = "JazzCash" },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedMethod == "JazzCash") BrandJazzCashRed.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedMethod == "JazzCash",
                                    onClick = { selectedMethod = "JazzCash" },
                                    colors = RadioButtonDefaults.colors(selectedColor = BrandJazzCashRed)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "JazzCash",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (selectedMethod == "JazzCash") BrandJazzCashRed else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Easypaisa Selector
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = if (selectedMethod == "Easypaisa") 2.dp else 1.dp,
                                    color = if (selectedMethod == "Easypaisa") BrandEasypaisaGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedMethod = "Easypaisa" },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedMethod == "Easypaisa") BrandEasypaisaGreen.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedMethod == "Easypaisa",
                                    onClick = { selectedMethod = "Easypaisa" },
                                    colors = RadioButtonDefaults.colors(selectedColor = BrandEasypaisaGreen)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Easypaisa",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (selectedMethod == "Easypaisa") BrandEasypaisaGreen else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Step 3: Account Holder & Number Display Card
                item {
                    PakistanPaymentAccountCard(
                        method = selectedMethod,
                        onCopied = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                    )
                }

                // Step 4: Submission Form
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Submit Payment Verification Details",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Admin verifies transaction before unlocking course",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = transactionId,
                                onValueChange = { transactionId = it },
                                label = { Text("Transaction ID / TID (from SMS)*") },
                                placeholder = { Text("e.g. 02938472910") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                leadingIcon = {
                                    Icon(Icons.Default.ReceiptLong, contentDescription = null)
                                }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = senderName,
                                onValueChange = { senderName = it },
                                label = { Text("Sender Account Holder Name*") },
                                placeholder = { Text("Name on your JazzCash/Easypaisa account") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null)
                                }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = senderPhone,
                                onValueChange = { senderPhone = it },
                                label = { Text("Sender Mobile Number*") },
                                placeholder = { Text("03XXXXXXXXX") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                leadingIcon = {
                                    Icon(Icons.Default.Phone, contentDescription = null)
                                }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = screenshotNote,
                                onValueChange = { screenshotNote = it },
                                label = { Text("Payment Proof Note (Optional)") },
                                placeholder = { Text("SMS confirmation time, last digits, etc.") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                leadingIcon = {
                                    Icon(Icons.Default.Attachment, contentDescription = null)
                                }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Surface(
                                color = BrandEmeraldGreen.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = BrandEmeraldGreen, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Payment security: We never store your passwords, PINs, or banking keys. Orders are verified directly against incoming statement.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
