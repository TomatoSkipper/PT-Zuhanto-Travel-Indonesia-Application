package com.example.appproject.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookOnline
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appproject.R
import com.example.appproject.data.repository.FirebaseRepository
import com.example.appproject.ui.components.HelpScreenSkeleton
import kotlinx.coroutines.delay

@Suppress("UNUSED_PARAMETER")
@Composable
fun HelpScreen(
    loggedInUser: String? = null,
    displayUsername: String? = null,
    firebaseRepo: FirebaseRepository? = null,
    onNavigateToAskQuestion: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null
) {
    var isLoading by remember { mutableStateOf(true) }
    var selectedCategoryFilter by rememberSaveable { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    val faqList = remember {
        listOf(
            FaqData(
                id = 1,
                category = "Booking",
                question = context.getString(R.string.booking_faq),
                answer = context.getString(R.string.booking_faq_answer)
            ),
            FaqData(
                id = 2,
                category = "Payment",
                question = context.getString(R.string.payment_faq),
                answer = context.getString(R.string.payment_faq_answer)
            ),
            FaqData(
                id = 3,
                category = "Vouchers",
                question = context.getString(R.string.vouchers_faq),
                answer = context.getString(R.string.vouchers_faq_answer)
            ),
            FaqData(
                id = 4,
                category = "Cancelling",
                question = context.getString(R.string.cancel_faq),
                answer = context.getString(R.string.cancel_faq_answer)
            )
        )
    }

    val filteredFaqs = remember(selectedCategoryFilter) {
        if (selectedCategoryFilter == null) {
            faqList
        } else {
            faqList.filter { it.category.equals(selectedCategoryFilter, ignoreCase = true) }
        }
    }

    LaunchedEffect(Unit) {
        delay(500)
        isLoading = false
    }

    if (isLoading) {
        Scaffold(containerColor = Color(0xFFF8FAFC)) { innerPadding ->
            HelpScreenSkeleton(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState()),
                onBack = onBack
            )
        }
        return
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Screen Title
            SectionHeader(
                title = stringResource(R.string.help_and_support),
                modifier = Modifier.offset(x = (-10).dp)
            )

            // Banner Card ("Need a hand?")
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFF0E40A8)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.need_a_hand),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = stringResource(R.string.send_us_a_question_and_our_team_will_reply_within_24_hours),
                        fontSize = 14.sp,
                        color = Color(0xFFE2E8F0),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        onClick = { onNavigateToAskQuestion?.invoke() },
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.NearMe,
                                contentDescription = null,
                                tint = Color(0xFF0E40A8),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.ask_us_a_question),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0E40A8)
                            )
                        }
                    }
                }
            }

            // 2x2 Quick Category Grid Cards
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    QuickCategoryCard(
                        title = stringResource(R.string.bookings),
                        icon = Icons.Default.BookOnline,
                        isSelected = selectedCategoryFilter == "Booking",
                        onClick = {
                            selectedCategoryFilter = if (selectedCategoryFilter == "Booking") null else "Booking"
                        },
                        modifier = Modifier.weight(1f)
                    )

                    QuickCategoryCard(
                        title = stringResource(R.string.payment),
                        icon = Icons.Default.Payment,
                        isSelected = selectedCategoryFilter == "Payment",
                        onClick = {
                            selectedCategoryFilter = if (selectedCategoryFilter == "Payment") null else "Payment"
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    QuickCategoryCard(
                        title = stringResource(R.string.cancelling),
                        icon = Icons.Default.Cancel,
                        isSelected = selectedCategoryFilter == "Cancelling",
                        onClick = {
                            selectedCategoryFilter = if (selectedCategoryFilter == "Cancelling") null else "Cancelling"
                        },
                        modifier = Modifier.weight(1f)
                    )

                    QuickCategoryCard(
                        title = stringResource(R.string.voucher),
                        icon = Icons.Default.ConfirmationNumber,
                        isSelected = selectedCategoryFilter == "Vouchers",
                        onClick = {
                            selectedCategoryFilter = if (selectedCategoryFilter == "Vouchers") null else "Vouchers"
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Common Questions Section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.common_questions),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        filteredFaqs.forEachIndexed { index, faq ->
                            ExpandableFaqRow(
                                faq = faq,
                                defaultExpanded = (index == 0)
                            )
                            if (index < filteredFaqs.size - 1) {
                                HorizontalDivider(
                                    color = Color(0xFFF1F5F9),
                                    modifier = Modifier.padding(horizontal = 20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Prefer to talk to someone? Section
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.contact_us_directly),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ContactInfoRow(
                            icon = Icons.Default.Email,
                            label = stringResource(R.string.email),
                            detail = "info@ztiindo.id"
                        )

                        HorizontalDivider(
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        ContactInfoRow(
                            icon = Icons.Default.Phone,
                            label = stringResource(R.string.phone),
                            detail = "+62 813 3315 225"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun QuickCategoryCard(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(
            1.dp,
            if (isSelected) Color(0xFF0E40A8) else Color(0xFFE2E8F0)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) Color(0xFF0E40A8) else Color(0xFFEFF6FF),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else Color(0xFF0E40A8),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color(0xFF0E40A8) else Color(0xFF0F172A)
            )
        }
    }
}

@Composable
fun ContactInfoRow(
    icon: ImageVector,
    label: String,
    detail: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFEFF6FF),
            modifier = Modifier.size(42.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF0E40A8),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = detail,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        }
    }
}

data class FaqData(
    val id: Int,
    val category: String,
    val question: String,
    val answer: String
)

@Composable
fun ExpandableFaqRow(
    faq: FaqData,
    defaultExpanded: Boolean = false
) {
    var isExpanded by rememberSaveable { mutableStateOf(defaultExpanded) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = faq.question,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = Color(0xFF64748B)
            )
        }

        AnimatedVisibility(visible = isExpanded) {
            Column {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = faq.answer,
                    fontSize = 14.sp,
                    color = Color(0xFF475569),
                    lineHeight = 20.sp
                )
            }
        }
    }
}
