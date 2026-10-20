package com.example.appproject.ui.screens

import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.appproject.ui.components.VoucherCardSkeleton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.appproject.R
import com.example.appproject.data.model.VoucherStatus
import com.example.appproject.data.model.parseBookingDate
import com.example.appproject.data.repository.FirebaseRepository

fun Modifier.dashedBorder(
    strokeWidth: Dp,
    color: Color,
    cornerRadius: Dp
) = this.drawWithContent {
    drawContent()
    val stroke = Stroke(
        width = strokeWidth.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
    )
    drawRoundRect(
        color = color,
        style = stroke,
        cornerRadius = CornerRadius(cornerRadius.toPx())
    )
}

@Composable
fun UserVouchersScreen(
    firebaseRepo: FirebaseRepository,
    username: String,
    onBack: () -> Unit = {}
) {
    var vouchersWithStatus by remember { mutableStateOf<List<VoucherStatus>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val fetchedVouchers = firebaseRepo.getAllVouchers()
        val currentTime = System.currentTimeMillis()

        val allVouchers = if (fetchedVouchers.isNotEmpty()) fetchedVouchers else listOf(
            mapOf(
                "code" to "NEWCOMER",
                "discountPercentage" to "10",
                "description" to (R.string.newcomer_voucher_description),
                "startDate" to "",
                "endDate" to ""
            )
        )

        vouchersWithStatus = allVouchers.map { vData ->
            val code = vData["code"]?.toString() ?: ""
            val endDateStr = vData["endDate"]?.toString() ?: ""

            val isUsed = if (code == "NEWCOMER" && username.isBlank()) false else firebaseRepo.isVoucherUsedByUser(code, username)

            var isExpired = false
            if (endDateStr.isNotBlank()) {
                try {
                    val endDate = parseBookingDate(endDateStr.trim())?.time ?: 0L
                    if (currentTime > endDate + 86400000) {
                        isExpired = true
                    }
                } catch (_: Exception) {}
            }

            VoucherStatus(vData, isUsed, isExpired)
        }
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(40.dp),
                onClick = onBack
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
            SectionHeader(title = stringResource(R.string.available_promo_vouchers))
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.apply_code_at_checkout_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (isLoading) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(3) {
                    VoucherCardSkeleton()
                }
            }
        } else if (vouchersWithStatus.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.no_active_voucher),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(vouchersWithStatus, key = { it.data["code"]?.toString() ?: "" }) { status ->
                    VoucherCardItem(status = status)
                }

                item {
                    HowToRedeemSection()
                }
            }
        }
    }
}

@Composable
fun VoucherCardItem(status: VoucherStatus) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val voucher = status.data
    val code = voucher["code"]?.toString() ?: "UNKNOWN"
    val discount = voucher["discountPercentage"]?.toString() ?: "0"
    val desc = voucher["description"]?.toString() ?: ""
    val isUsed = status.isUsed
    val isExpired = status.isExpired
    val isDisabled = isUsed || isExpired
    val bgPic = voucher["backgroundPicture"]?.toString() ?: ""

    val bgBytes = remember(bgPic) {
        try {
            val clean = bgPic.replace("\n", "").replace("\r", "").trim()
            if (clean.isNotBlank()) Base64.decode(clean, Base64.DEFAULT) else null
        } catch (_: Exception) {
            null
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isDisabled) 0.65f else 1f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Top Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(135.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF0F5A71),
                                Color(0xFF0C4355),
                                Color(0xFF082B38)
                            )
                        )
                    )
            ) {
                if (bgBytes != null) {
                    AsyncImage(
                        model = bgBytes,
                        contentDescription = null,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Black.copy(alpha = 0.45f))
                    )
                }

                // Top Right Badge
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Text(
                        text = when {
                            isUsed -> stringResource(R.string.used)
                            isExpired -> stringResource(R.string.expired)
                            else -> "$discount% off"
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F5A71)
                    )
                }

                // Bottom Left Title/Description
                Text(
                    text = if (desc.isNotBlank()) desc else "Special for new account",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                )
            }

            // Bottom Actions & Information Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Voucher Code Box
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFF4F7FC), RoundedCornerShape(8.dp))
                            .dashedBorder(1.dp, Color(0xFF90CAF9), 8.dp)
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LocalOffer,
                                contentDescription = null,
                                tint = Color(0xFF0B57D0),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = code,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0B57D0),
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    // Copy Code Button
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(code))
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B57D0)),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(R.string.copy_code),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Validity & Status Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val start = voucher["startDate"]?.toString() ?: ""
                    val end = voucher["endDate"]?.toString() ?: ""
                    val validText = when {
                        end.isNotBlank() -> stringResource(R.string.valid_until, end)
                        start.isNotBlank() -> "Valid from $start"
                        else -> stringResource(R.string.no_expiration)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = validText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF475569)
                        )
                    }

                    // Status Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = when {
                                        isUsed -> Color.Gray
                                        isExpired -> Color.Red
                                        else -> Color(0xFF10B981)
                                    },
                                    shape = CircleShape
                                )
                        )
                        Text(
                            text = when {
                                isUsed -> stringResource(R.string.used)
                                isExpired -> stringResource(R.string.expired)
                                else -> stringResource(R.string.active)
                            },
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isUsed -> Color.Gray
                                isExpired -> Color.Red
                                else -> Color(0xFF059669)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HowToRedeemSection() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.how_to_redeem),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(14.dp))

            // Step 1
            RedeemStepItem(
                stepNumber = "1",
                title = stringResource(R.string.redeem_step_1_title),
                description = stringResource(R.string.redeem_step_1_desc)
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(12.dp))

            // Step 2
            RedeemStepItem(
                stepNumber = "2",
                title = stringResource(R.string.redeem_step_2_title),
                description = stringResource(R.string.redeem_step_2_desc)
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(12.dp))

            // Step 3
            RedeemStepItem(
                stepNumber = "3",
                title = stringResource(R.string.redeem_step_3_title),
                description = stringResource(R.string.redeem_step_3_desc)
            )
        }
    }
}

@Composable
fun RedeemStepItem(
    stepNumber: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color(0xFFE0F2FE), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0284C7)
            )
        }

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF64748B)
            )
        }
    }
}
