package com.example.appproject.ui.screens

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.text.TextUtils
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import java.util.Locale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.HighlightOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.appproject.R
import com.example.appproject.data.model.Booking
import com.example.appproject.data.model.getBookingStatusDisplay
import com.example.appproject.data.model.getPaymentStatusDisplay
import com.example.appproject.data.model.normalizeBookingStatus
import com.example.appproject.data.model.normalizePaymentStatus
import com.example.appproject.data.model.parseBookingDate
import com.example.appproject.data.remote.resolveTripImageRes
import com.example.appproject.data.repository.FirebaseRepository
import com.example.appproject.ui.components.BookingCardSkeleton
import com.example.appproject.ui.theme.PrimaryBlue
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.launch
import java.util.Calendar

enum class StepStatus {
    COMPLETED,
    CANCELLED,
    PENDING
}

data class ParsedBookingDetails(
    val title: String,
    val paxInfo: String,
    val noteText: String
)

@Composable
private fun StepCircle(status: StepStatus) {
    val backgroundColor = when (status) {
        StepStatus.COMPLETED -> Color(0xFF2563EB)
        StepStatus.CANCELLED -> Color(0xFFDC2626)
        StepStatus.PENDING -> Color.Transparent
    }

    val borderColor = when (status) {
        StepStatus.PENDING -> Color(0xFFCBD5E1)
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(2.dp, borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        when (status) {
            StepStatus.COMPLETED -> Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Completed",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            StepStatus.CANCELLED -> Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cancelled",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            StepStatus.PENDING -> Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF94A3B8))
            )
        }
    }
}

fun parseBookingDestinationDetails(destination: String): ParsedBookingDetails {
    var rawTitle = destination.trim()
    var noteText = ""
    var paxInfo = ""

    if (rawTitle.contains(" - Note: ")) {
        val parts = rawTitle.split(" - Note: ", limit = 2)
        rawTitle = parts[0].trim()
        noteText = parts.getOrNull(1)?.trim().orEmpty()
    } else if (rawTitle.contains(" Note: ")) {
        val parts = rawTitle.split(" Note: ", limit = 2)
        rawTitle = parts[0].trim()
        noteText = parts.getOrNull(1)?.trim().orEmpty()
    }

    if (rawTitle.contains("(") && rawTitle.contains(")")) {
        val startIndex = rawTitle.lastIndexOf("(")
        val endIndex = rawTitle.lastIndexOf(")")
        if (endIndex > startIndex) {
            val extractedPax = rawTitle.substring(startIndex + 1, endIndex).trim()
            paxInfo = extractedPax.lowercase()
            rawTitle = rawTitle.substring(0, startIndex).trim()
        }
    }

    if (paxInfo.isBlank()) {
        paxInfo = "1 pax"
    }

    return ParsedBookingDetails(
        title = if (rawTitle.isNotBlank()) rawTitle else destination,
        paxInfo = paxInfo,
        noteText = noteText
    )
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(
    username: String,
    firebaseRepo: FirebaseRepository,
    onViewReceipt: (String) -> Unit,
    onViewInvoice: (String) -> Unit,
    onPayNow: ((String) -> Unit)? = null,
    onBack: () -> Unit = {},
    isGuest: Boolean = false,
    onLoginClick: (() -> Unit)? = null,
    onBookAgain: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Active, 1 = History
    var searchQuery by remember { mutableStateOf("") }

    var bookingToCancel by remember { mutableStateOf<Booking?>(null) }
    var bookingToPay by remember { mutableStateOf<Booking?>(null) }
    var selectedBookingForMenu by remember { mutableStateOf<Booking?>(null) }
    var showMenuBottomSheet by remember { mutableStateOf(false) }

    var showRefundNotice by remember { mutableStateOf(false) }
    var showDeletedNotice by remember { mutableStateOf(false) }
    var connectionStatus by remember { mutableStateOf("Connected") }
    var isLoading by remember { mutableStateOf(!isGuest) }
    val scope = rememberCoroutineScope()

    DisposableEffect(username, isGuest) {
        if (isGuest) {
            onDispose {}
        } else {
            isLoading = true
            scope.launch {
                val test = firebaseRepo.testConnection()
                if (test != "Connected") {
                    connectionStatus = test
                }
            }
            val listener = firebaseRepo.listenToBookingsForUser(username) { updatedList ->
                bookings = updatedList
                isLoading = false
            }
            onDispose {
                listener?.remove()
            }
        }
    }

    val today = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val (pastBookings, currentBookings) = remember(bookings) {
        bookings.partition {
            val isCancelled = normalizeBookingStatus(it.status) == "Cancelled"
            val isCompleted = normalizeBookingStatus(it.status) == "Completed"
            try {
                val date = parseBookingDate(it.bookingDate)?.time ?: 0L
                date < today || isCancelled || isCompleted
            } catch (_: Exception) {
                isCancelled || isCompleted
            }
        }
    }

    val displayedBookings = remember(selectedTab, currentBookings, pastBookings, searchQuery) {
        val targetList = if (selectedTab == 0) currentBookings else pastBookings
        if (searchQuery.isBlank()) {
            targetList
        } else {
            targetList.filter {
                it.destination.contains(searchQuery, ignoreCase = true) ||
                it.bookingId.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Top Header Title
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp)
        ) {
            Text(
                text = stringResource(R.string.my_bookings),
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0F172A)
            )
        }

        // Pill Segmented Tab Selector (Active vs History)
        SegmentedPillTab(
            activeCount = currentBookings.size,
            historyCount = pastBookings.size,
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
        )

        // Sync Error Notice
        if (connectionStatus != "Connected" && !isGuest) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.sync_error, connectionStatus),
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        if (isGuest) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Luggage,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = PrimaryBlue
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.to_see_the_bookings_please_login),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF0F172A)
                    )
                    if (onLoginClick != null) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onLoginClick,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 28.dp, vertical = 12.dp)
                        ) {
                            Text(stringResource(R.string.login), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        } else if (isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                repeat(3) {
                    BookingCardSkeleton()
                }
            }
        } else if (bookings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.no_booking_message),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else if (displayedBookings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (selectedTab == 0) "No active bookings found" else "No history bookings found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(displayedBookings, key = { it.bookingId }) { booking ->
                    UserBookingCard(
                        booking = booking,
                        firebaseRepo = firebaseRepo,
                        onViewReceipt = onViewReceipt,
                        onViewInvoice = onViewInvoice,
                        onCancel = { bookingToCancel = booking },
                        onPayNow = {
                            if (onPayNow != null) {
                                onPayNow(booking.bookingId)
                            } else {
                                bookingToPay = booking
                            }
                        },
                        onOpenMenu = {
                            selectedBookingForMenu = it
                            showMenuBottomSheet = true
                        },
                        onBookAgain = {
                            onBookAgain?.invoke()
                        },
                        isHistory = selectedTab == 1
                    )
                }
            }
        }
    }

    // 3-Dots Options Bottom Sheet (Image 3)
    if (showMenuBottomSheet && selectedBookingForMenu != null) {
        val menuBooking = selectedBookingForMenu!!
        val menuDetails = remember(menuBooking.destination) { parseBookingDestinationDetails(menuBooking.destination) }
        val isPaid = normalizePaymentStatus(menuBooking.paymentStatus) == "Paid"
        val isCancelled = normalizeBookingStatus(menuBooking.status) == "Cancelled"

        ModalBottomSheet(
            onDismissRequest = {
                showMenuBottomSheet = false
                selectedBookingForMenu = null
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 4.dp)
                        .width(36.dp)
                        .height(4.dp)
                        .background(Color(0xFFCBD5E1), CircleShape)
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Details
                Column {
                    Text(
                        text = menuDetails.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${menuDetails.paxInfo}, ${menuBooking.bookingDate}, Code #${menuBooking.bookingId}",
                        fontSize = 13.5.sp,
                        color = Color(0xFF64748B)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Menu Item 1: Print receipt/invoice
                BottomSheetMenuItem(
                    icon = Icons.Default.Print,
                    iconBackgroundColor = Color(0xFFF1F5F9),
                    iconTintColor = Color(0xFF334155),
                    label = if (isPaid) "Print receipt" else "Print invoice",
                    onClick = {
                        showMenuBottomSheet = false
                        selectedBookingForMenu = null
                        if (isPaid) {
                            printReceipt(context, menuBooking)
                        } else {
                            printInvoice(context, menuBooking)
                        }
                    }
                )

                // Menu Item 2: Cancel booking
                if (!isCancelled) {
                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    BottomSheetMenuItem(
                        icon = Icons.Default.HighlightOff,
                        iconBackgroundColor = Color(0xFFFEE2E2),
                        iconTintColor = Color(0xFFDC2626),
                        label = "Cancel booking",
                        labelColor = Color(0xFFDC2626),
                        onClick = {
                            showMenuBottomSheet = false
                            bookingToCancel = menuBooking
                            selectedBookingForMenu = null
                        }
                    )
                }
            }
        }
    }

    // Cancel Booking Bottom Sheet Modal
    bookingToCancel?.let { booking ->
        val isConfirmed = normalizeBookingStatus(booking.status) == "Confirmed"
        val isPaid = normalizePaymentStatus(booking.paymentStatus) == "Paid"
        val shouldDelete = !isConfirmed && !isPaid

        val (tripTitle, paxLabelText) = remember(booking.destination) {
            val destClean = booking.destination.split(" - Note:").firstOrNull() ?: booking.destination
            if (destClean.contains(" (") && destClean.endsWith(")")) {
                val parts = destClean.split(" (")
                val name = parts[0]
                val pax = parts[1].removeSuffix(")")
                Pair(name, pax)
            } else {
                Pair(destClean, "")
            }
        }

        val descriptionText = remember(isConfirmed, isPaid) {
            if (!isConfirmed && !isPaid) {
                context.getString(R.string.cancel_desc_unconfirmed_unpaid)
            } else if (isPaid) {
                context.getString(R.string.cancel_desc_confirmed_paid)
            } else {
                context.getString(R.string.cancel_desc_confirmed_unpaid)
            }
        }

        ModalBottomSheet(
            onDismissRequest = { bookingToCancel = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 4.dp)
                        .width(36.dp)
                        .height(4.dp)
                        .background(Color(0xFFCBD5E1), CircleShape)
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Warning Icon
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFEE2E2),
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Warning,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                // Title
                Text(
                    text = stringResource(R.string.cancel_this_booking),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )

                // Trip Summary Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = tripTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        if (paxLabelText.isNotBlank()) {
                            Text(
                                text = paxLabelText,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val bookingTagText = if (isConfirmed) stringResource(R.string.confirmed) else stringResource(R.string.not_confirmed_yet)
                            val bookingTagColor = if (isConfirmed) Color(0xFF047857) else Color(0xFFD97706)
                            Text(
                                text = bookingTagText,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = bookingTagColor
                            )

                            val paymentTagText = if (isPaid) stringResource(R.string.paid) else stringResource(R.string.not_paid)
                            val paymentTagColor = if (isPaid) Color(0xFF047857) else Color(0xFF2563EB)
                            Text(
                                text = paymentTagText,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = paymentTagColor
                            )
                        }
                    }
                }

                Text(
                    text = descriptionText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF475569),
                    lineHeight = 20.sp
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            scope.launch {
                                if (shouldDelete) {
                                    firebaseRepo.deleteBooking(booking.bookingId)
                                    bookings = bookings.filter { it.bookingId != booking.bookingId }
                                    bookingToCancel = null
                                    showDeletedNotice = true
                                } else {
                                    val cancelledBooking = booking.copy(status = "Cancelled")
                                    firebaseRepo.updateBooking(cancelledBooking)
                                    bookings = bookings.map { if (it.bookingId == cancelledBooking.bookingId) cancelledBooking else it }
                                    bookingToCancel = null
                                    showRefundNotice = true
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDC2626),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.yes_cancel_booking),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = { bookingToCancel = null },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Text(
                            text = stringResource(R.string.keep_my_booking),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB)
                        )
                    }
                }
            }
        }
    }

    if (showRefundNotice) {
        Dialog(
            onDismissRequest = { showRefundNotice = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color(0xFFDBEAFE), CircleShape)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color(0xFF2563EB), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.booking_cancelled),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.cancel_success_refund_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )
                    }

                    Button(
                        onClick = { showRefundNotice = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1D4ED8),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.done),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showDeletedNotice) {
        Dialog(
            onDismissRequest = { showDeletedNotice = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color(0xFFDBEAFE), CircleShape)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color(0xFF2563EB), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.booking_cancelled),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.cancel_success_deleted_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )
                    }

                    Button(
                        onClick = { showDeletedNotice = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1D4ED8),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.done),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    bookingToPay?.let { booking ->
        PaymentDialog(
            booking = booking,
            onDismiss = { bookingToPay = null },
            onConfirmPayment = { paidBooking ->
                scope.launch {
                    firebaseRepo.updateBooking(paidBooking)
                    bookings = bookings.map {
                        if (it.bookingId == paidBooking.bookingId) paidBooking else it
                    }
                    bookingToPay = null
                    Toast.makeText(context, context.getString(R.string.payment_successful), Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}

@Composable
fun SegmentedPillTab(
    activeCount: Int,
    historyCount: Int,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFE2E8F0)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (selectedTab == 0) Color.White else Color.Transparent)
                    .clickable { onTabSelected(0) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.active)+" ($activeCount)",
                    fontSize = 15.sp,
                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (selectedTab == 0) Color(0xFF2563EB) else Color(0xFF64748B)
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (selectedTab == 1) Color.White else Color.Transparent)
                    .clickable { onTabSelected(1) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.history)+ " ($historyCount)",
                    fontSize = 15.sp,
                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (selectedTab == 1) Color(0xFF2563EB) else Color(0xFF64748B)
                )
            }
        }
    }
}

@Composable
fun BottomSheetMenuItem(
    icon: ImageVector,
    iconBackgroundColor: Color,
    iconTintColor: Color,
    label: String,
    labelColor: Color = Color(0xFF0F172A),
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = iconBackgroundColor,
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTintColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = labelColor
        )
    }
}

@Composable
fun PaymentCountdownTimer(confirmationTimestamp: Long, modifier: Modifier = Modifier) {
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(confirmationTimestamp) {
        if (confirmationTimestamp > 0L) {
            while (true) {
                currentTime = System.currentTimeMillis()
                kotlinx.coroutines.delay(1000L)
            }
        }
    }

    val deadline = confirmationTimestamp + 2L * 24 * 60 * 60 * 1000L
    val timeLeftMs = deadline - currentTime

    if (timeLeftMs > 0) {
        val totalSeconds = timeLeftMs / 1000L
        val days = totalSeconds / (24 * 3600)
        val hours = (totalSeconds % (24 * 3600)) / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        val timeString = buildString {
            if (days > 0) append("${days}d ")
            append(String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds))
        }

        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFFEF3C7),
            border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(18.dp)
                )
                Column {
                    Text(
                        text = "Payment Time Remaining",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB45309)
                    )
                    Text(
                        text = timeString,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF92400E)
                    )
                }
            }
        }
    } else {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFFEE2E2),
            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = stringResource(R.string.payment_time_expired),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB91C1C)
                )
            }
        }
    }
}

@Composable
fun UserBookingCard(
    booking: Booking,
    firebaseRepo: FirebaseRepository,
    onViewReceipt: (String) -> Unit = {},
    onViewInvoice: (String) -> Unit = {},
    onCancel: () -> Unit = {},
    onPayNow: () -> Unit = {},
    onOpenMenu: (Booking) -> Unit = {},
    onBookAgain: (Booking) -> Unit = {},
    isHistory: Boolean = false
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val details = remember(booking.destination) { parseBookingDestinationDetails(booking.destination) }
    val normStatus = normalizeBookingStatus(booking.status)
    val normPayment = normalizePaymentStatus(booking.paymentStatus)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Row: Image Thumbnail + Details + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Trip Image Thumbnail
                val imageRes = resolveTripImageRes(booking.destination)
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = details.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .alpha(if (normStatus == "Cancelled") 0.6f else 1f)
                )

                // Main Trip Info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = details.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = details.paxInfo,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = booking.bookingDate,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155)
                    )
                }

                // Top Right Status Badge
                val (statusText, statusBgColor, statusTextColor) = when {
                    normStatus == "Cancelled" -> Triple(stringResource(R.string.cancelled), Color(0xFFF1F5F9), Color(0xFF64748B))
                    normStatus == "Completed" -> Triple(stringResource(R.string.completed), Color(0xFFF1F5F9), Color(0xFF0F172A))
                    normPayment == "Paid" -> Triple(stringResource(R.string.paid), Color.Transparent, Color(0xFF334155))
                    normStatus == "Confirmed" -> Triple(stringResource(R.string.confirmed), Color(0xFFECFDF5), Color(0xFF047857))
                    else -> Triple(stringResource(R.string.pending), Color(0xFFFEF3C7), Color(0xFFD97706))
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusBgColor
                ) {
                    Text(
                        text = statusText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusTextColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Code Pill Badge (Click to copy code)
            Surface(
                onClick = {
                    clipboardManager.setText(AnnotatedString(booking.bookingId))
                    Toast.makeText(context, "Copied code #${booking.bookingId} to clipboard", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFF1F5F9))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Code #${booking.bookingId}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155)
                    )
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy code",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            // 3-Segment Progress Indicator Bars
            val (step1, step2, step3, statusLabel, statusColor) = when {
                normStatus == "Cancelled" -> Tuple5(
                    StepStatus.COMPLETED,
                    StepStatus.CANCELLED,
                    StepStatus.PENDING,
                    stringResource(R.string.this_booking_was_cancelled),
                    Color(0xFFDC2626)
                )
                normStatus == "Completed" || normPayment == "Paid" -> Tuple5(
                    StepStatus.COMPLETED,
                    StepStatus.COMPLETED,
                    StepStatus.COMPLETED,
                    if (normStatus == "Completed") stringResource(R.string.trip_completed) else stringResource(R.string.confirmed_and_paid),
                    Color(0xFF047857)
                )
                normStatus == "Confirmed" -> Tuple5(
                    StepStatus.COMPLETED,
                    StepStatus.COMPLETED,
                    StepStatus.PENDING,
                    stringResource(R.string.confirmed_payment_required),
                    Color(0xFF2563EB)
                )
                else -> Tuple5(
                    StepStatus.COMPLETED,
                    StepStatus.PENDING,
                    StepStatus.PENDING,
                    stringResource(R.string.pending_admin_confirmation),
                    Color(0xFFD97706)
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val steps = listOf(step1, step2, step3)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    steps.forEachIndexed { index, status ->
                        // Check Circle Component
                        StepCircle(status = status)

                        // Connecting Line between steps
                        if (index < steps.size - 1) {
                            val nextStatus = steps[index + 1]
                            val lineColor = when {
                                nextStatus == StepStatus.CANCELLED -> Color(0xFFEF4444)
                                status == StepStatus.COMPLETED && nextStatus != StepStatus.PENDING -> Color(0xFF2563EB)
                                else -> Color(0xFFE2E8F0)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(3.dp)
                                    .background(lineColor)
                            )
                        }
                    }
                }

                Text(
                    text = statusLabel,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }

            // Payment Countdown Timer for Confirmed Unpaid Bookings
            if (normStatus == "Confirmed" && normPayment != "Paid" && booking.confirmationTimestamp > 0L) {
                PaymentCountdownTimer(
                    confirmationTimestamp = booking.confirmationTimestamp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Review section for completed trips in History
            if (normStatus == "Completed" || (normStatus == "Confirmed" && isHistory && normPayment == "Paid")) {
                RateTripSection(booking = booking, firebaseRepo = firebaseRepo, isHistory = isHistory)
            }

            // Bottom Actions
            if (normStatus != "Cancelled") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Main Action Button (View receipt / Pay now / View invoice)
                    OutlinedButton(
                        onClick = {
                            if (normPayment == "Paid") onViewReceipt(booking.bookingId)
                            else if (normStatus == "Confirmed") onPayNow()
                            else onViewInvoice(booking.bookingId)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        val buttonIcon = if (normPayment == "Paid") Icons.Default.Description else if (normStatus == "Confirmed") Icons.Default.CreditCard else Icons.Default.Description
                        val buttonLabel = if (normPayment == "Paid") stringResource(R.string.view_receipt) else if (normStatus == "Confirmed") stringResource(R.string.pay_now) else stringResource(R.string.view_invoice)

                        Icon(
                            imageVector = buttonIcon,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = buttonLabel,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB)
                        )
                    }

                    // 3-Dots Menu Button
                    if (normStatus != "Cancelled" && normStatus != "Completed" && !isHistory) {
                        OutlinedButton(
                            onClick = { onOpenMenu(booking) },
                            modifier = Modifier.size(48.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options",
                                tint = Color(0xFF334155),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class Tuple5<A, B, C, D, E>(
    val a: A,
    val b: B,
    val c: C,
    val d: D,
    val e: E
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptScreen(
    bookingId: String,
    firebaseRepo: FirebaseRepository,
    loggedInUser: String?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var booking by remember { mutableStateOf<Booking?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isAuthorized by remember { mutableStateOf(true) }

    LaunchedEffect(bookingId, loggedInUser) {
        booking = firebaseRepo.getBooking(bookingId)
        if (booking != null) {
            isAuthorized = firebaseRepo.isUserAuthorizedForBooking(booking, loggedInUser)
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.booking_receipt)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (booking != null) {
                        IconButton(onClick = { printReceipt(context, booking!!) }) {
                            Icon(Icons.Default.Print, contentDescription = "Print")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                BookingCardSkeleton()
            }
        } else if (booking == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.receipt_not_found))
            }
        } else if (!isAuthorized) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.access_denied))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.zti_logo),
                            contentDescription = null,
                            modifier = Modifier.size(80.dp).clip(CircleShape).background(Color.White).padding(8.dp)
                        )
                        Text(
                            "PT ZUHANTO TRAVEL INDONESIA",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        ReceiptRow(stringResource(R.string.booking_id), "#${booking!!.bookingId}")
                        ReceiptRow(stringResource(R.string.customer), booking!!.customerName)
                        ReceiptRow(stringResource(R.string.destination_label), booking!!.destination)
                        ReceiptRow(stringResource(R.string.date_label), booking!!.bookingDate)
                        ReceiptRow(stringResource(R.string.phone_label), booking!!.customerPhone)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))

                        ReceiptRow(stringResource(R.string.normal_price), if (booking!!.basePrice.isNotBlank()) booking!!.basePrice else booking!!.finalPrice)

                        if (booking!!.voucherUsed.isNotBlank()) {
                            ReceiptRow(
                                label = "${stringResource(R.string.discount)} (${booking!!.voucherUsed})",
                                value = "- ${booking!!.discountAmount}",
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        ReceiptRow(stringResource(R.string.total_paid), booking!!.finalPrice, isTotal = true)
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            stringResource(R.string.thank_you),
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Paid",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(stringResource(R.string.paid_upper), fontWeight = FontWeight.ExtraBold, color = Color(0xFF4CAF50))
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = { printReceipt(context, booking!!) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.print_receipt))
                }
            }
        }
    }
}

fun printReceipt(context: Context, booking: Booking) {
    val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
    val jobName = "Receipt_${booking.bookingId}"
    
    val officialReceiptText = context.getString(R.string.official_receipt)
    val bookingIdText = context.getString(R.string.booking_id)
    val customerText = context.getString(R.string.customer)
    val destinationText = context.getString(R.string.destination_label)
    val dateText = context.getString(R.string.date_label)
    val phoneText = context.getString(R.string.phone_label)
    val normalPriceText = context.getString(R.string.normal_price)
    val discountText = context.getString(R.string.discount)
    val totalPaidText = context.getString(R.string.total_paid)
    val paidLabelText = context.getString(R.string.paid_label)
    val footerThankYouText = context.getString(R.string.footer_thank_you)

    val webView = WebView(context)
    val htmlContent = """
        <html>
        <body style="font-family: sans-serif; padding: 40px; color: #333;">
            <div style="text-align: center; margin-bottom: 30px;">
                <h1 style="color: #063763; margin-bottom: 5px;">PT ZUHANTO TRAVEL INDONESIA</h1>
                <p style="margin: 0; color: #666;">$officialReceiptText</p>
            </div>
            <div style="border: 1px solid #eee; padding: 20px; border-radius: 10px;">
                <table style="width: 100%; border-collapse: collapse; margin-bottom: 20px;">
                    <tr><td style="padding: 8px 0; border-bottom: 1px solid #f9f9f9;"><b>$bookingIdText</b></td><td style="text-align: right;">#${TextUtils.htmlEncode(booking.bookingId)}</td></tr>
                    <tr><td style="padding: 8px 0; border-bottom: 1px solid #f9f9f9;"><b>$customerText</b></td><td style="text-align: right;">${TextUtils.htmlEncode(booking.customerName)}</td></tr>
                    <tr><td style="padding: 8px 0; border-bottom: 1px solid #f9f9f9;"><b>$destinationText</b></td><td style="text-align: right;">${TextUtils.htmlEncode(booking.destination)}</td></tr>
                    <tr><td style="padding: 8px 0; border-bottom: 1px solid #f9f9f9;"><b>$dateText</b></td><td style="text-align: right;">${TextUtils.htmlEncode(booking.bookingDate)}</td></tr>
                    <tr><td style="padding: 8px 0;"><b>$phoneText</b></td><td style="text-align: right;">${TextUtils.htmlEncode(booking.customerPhone)}</td></tr>
                </table>
                
                <div style="background: #fcfcfc; padding: 15px; border-radius: 5px;">
                    <table style="width: 100%;">
                        <tr><td style="padding: 5px 0;">$normalPriceText</td><td style="text-align: right;">${TextUtils.htmlEncode(if (booking.basePrice.isNotBlank()) booking.basePrice else booking.finalPrice)}</td></tr>
                        ${if (booking.voucherUsed.isNotBlank()) "<tr><td style='padding: 5px 0;'>$discountText (${TextUtils.htmlEncode(booking.voucherUsed)})</td><td style='text-align: right; color: red;'>- ${TextUtils.htmlEncode(booking.discountAmount)}</td></tr>" else ""}
                        <tr style="font-size: 1.3em; font-weight: bold; color: #063763;">
                            <td style="padding-top: 10px;">$totalPaidText</td><td style="text-align: right; padding-top: 10px;">${TextUtils.htmlEncode(booking.finalPrice)}</td>
                        </tr>
                    </table>
                </div>
            </div>
            
            <div style="text-align: center; margin-top: 40px;">
                <div style="display: inline-block; padding: 10px 20px; border: 2px solid #4CAF50; color: #4CAF50; font-weight: bold; border-radius: 5px;">
                    $paidLabelText
                </div>
                <p style="margin-top: 20px; color: #888;"><i>$footerThankYouText</i></p>
            </div>
        </body>
        </html>
    """.trimIndent()
    
    webView.webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            val printAdapter = webView.createPrintDocumentAdapter(jobName)
            printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
        }
    }
    webView.loadDataWithBaseURL(null, htmlContent, "text/HTML", "UTF-8", null)
}

@Composable
fun ReceiptRow(label: String, value: String, isTotal: Boolean = false, color: Color? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(0.42f, fill = false),
            style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            modifier = Modifier.weight(0.58f, fill = false),
            textAlign = TextAlign.End,
            style = if (isTotal) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.SemiBold,
            color = color ?: if (isTotal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun PaymentDialog(
    booking: Booking,
    onDismiss: () -> Unit,
    onConfirmPayment: (Booking) -> Unit
) {
    val context = LocalContext.current
    var selectedMethod by remember { mutableStateOf("qris") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.payment_method)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "${stringResource(R.string.destination_label)}: ${booking.destination}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.payment_amount, booking.finalPrice),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold
                )

                HorizontalDivider()

                val methods = listOf(
                    "card" to stringResource(R.string.credit_card),
                    "bank" to stringResource(R.string.bank_transfer),
                    "ewallet" to stringResource(R.string.ewallet)
                )

                methods.forEach { (key, label) ->
                    Surface(
                        onClick = { selectedMethod = key },
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedMethod == key) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (selectedMethod == key) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedMethod == key,
                                onClick = { selectedMethod = key }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedBooking = booking.copy(
                        paymentStatus = "Paid",
                        paymentMethod = selectedMethod
                    )
                    onConfirmPayment(updatedBooking)
                }
            ) {
                Text(stringResource(R.string.confirm_payment))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreen(
    bookingId: String,
    firebaseRepo: FirebaseRepository,
    loggedInUser: String?,
    onBack: () -> Unit,
    onViewReceipt: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    var booking by remember { mutableStateOf<Booking?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isAuthorized by remember { mutableStateOf(true) }

    LaunchedEffect(bookingId, loggedInUser) {
        booking = firebaseRepo.getBooking(bookingId)
        if (booking != null) {
            isAuthorized = firebaseRepo.isUserAuthorizedForBooking(booking, loggedInUser)
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.booking_invoice)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (booking != null && normalizePaymentStatus(booking!!.paymentStatus) != "Paid") {
                        IconButton(onClick = { printInvoice(context, booking!!) }) {
                            Icon(Icons.Default.Print, contentDescription = "Print")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                BookingCardSkeleton()
            }
        } else if (booking == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.invoice_not_found))
            }
        } else if (!isAuthorized) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.access_denied))
            }
        } else if (normalizePaymentStatus(booking!!.paymentStatus) == "Paid") {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.zti_logo),
                            contentDescription = null,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .padding(8.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = Color(0xFF4CAF50)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.booking_paid_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.paid_booking_notice),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { onViewReceipt?.invoke(booking!!.bookingId) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.view_receipt))
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.zti_logo),
                            contentDescription = null,
                            modifier = Modifier.size(80.dp).clip(CircleShape).background(Color.White).padding(8.dp)
                        )
                        Text(
                            "PT ZUHANTO TRAVEL INDONESIA",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            stringResource(R.string.official_invoice),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))

                        ReceiptRow(stringResource(R.string.booking_id), "#${booking!!.bookingId}")
                        ReceiptRow(stringResource(R.string.customer), booking!!.customerName)
                        ReceiptRow(stringResource(R.string.destination_label), booking!!.destination)
                        ReceiptRow(stringResource(R.string.date_label), booking!!.bookingDate)
                        ReceiptRow(stringResource(R.string.phone_label), booking!!.customerPhone)
                        ReceiptRow(stringResource(R.string.booking_status),
                            getBookingStatusDisplay(booking!!.status)
                        )
                        ReceiptRow(stringResource(R.string.payment_status),
                            getPaymentStatusDisplay(booking!!.paymentStatus)
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))

                        ReceiptRow(stringResource(R.string.normal_price), if (booking!!.basePrice.isNotBlank()) booking!!.basePrice else booking!!.finalPrice)

                        if (booking!!.voucherUsed.isNotBlank()) {
                            ReceiptRow(
                                label = "${stringResource(R.string.discount)} (${booking!!.voucherUsed})",
                                value = "- ${booking!!.discountAmount}",
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        ReceiptRow(stringResource(R.string.total_paid), booking!!.finalPrice, isTotal = true)

                        Spacer(modifier = Modifier.height(24.dp))

                        val isConfirmed = normalizeBookingStatus(booking!!.status) == "Confirmed"
                        val isPaid = normalizePaymentStatus(booking!!.paymentStatus) == "Paid"

                        Surface(
                            color = when {
                                isPaid -> Color(0xFFDDF5E5)
                                isConfirmed -> Color(0xFFE3F2FD)
                                else -> MaterialTheme.colorScheme.errorContainer
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = when {
                                    isPaid -> stringResource(R.string.paid_upper)
                                    isConfirmed -> stringResource(R.string.confirmed_unpaid)
                                    else -> stringResource(R.string.awaiting_confirmation)
                                },
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    isPaid -> Color(0xFF176B36)
                                    isConfirmed -> Color(0xFF0D47A1)
                                    else -> MaterialTheme.colorScheme.onErrorContainer
                                }
                            )
                        }

                        if (isConfirmed && !isPaid && booking!!.confirmationTimestamp > 0L) {
                            val deadline = booking!!.confirmationTimestamp + 2L * 24 * 60 * 60 * 1000L
                            val timeLeftMs = deadline - System.currentTimeMillis()
                            val hoursLeft = maxOf(0L, timeLeftMs / (1000L * 60 * 60))
                            val daysLeft = hoursLeft / 24
                            val remHours = hoursLeft % 24
                            val timeLeftStr = if (daysLeft > 0) "$daysLeft days $remHours hours left" else "$hoursLeft hours left"

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Payment deadline: Complete within 2 days after admin confirmation ($timeLeftStr remaining)",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFD97706),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { printInvoice(context, booking!!) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.print_invoice))
                }
            }
        }
    }
}

fun printInvoice(context: Context, booking: Booking) {
    val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
    val jobName = "Invoice_${booking.bookingId}"

    val officialInvoiceText = context.getString(R.string.official_invoice)
    val bookingIdText = context.getString(R.string.booking_id)
    val customerText = context.getString(R.string.customer)
    val destinationText = context.getString(R.string.destination_label)
    val dateText = context.getString(R.string.date_label)
    val phoneText = context.getString(R.string.phone_label)
    val statusText = context.getString(R.string.booking_status)
    val paymentStatusText = context.getString(R.string.payment_status)
    val statusDisplay = getBookingStatusDisplay(booking.status, context)
    val paymentStatusDisplay = getPaymentStatusDisplay(booking.paymentStatus, context)
    val normalPriceText = context.getString(R.string.normal_price)
    val discountText = context.getString(R.string.discount)
    val totalPaidText = context.getString(R.string.total_paid)
    val footerThankYouText = context.getString(R.string.footer_thank_you)

    val webView = WebView(context)
    val htmlContent = """
        <html>
        <body style="font-family: sans-serif; padding: 40px; color: #333;">
            <div style="text-align: center; margin-bottom: 30px;">
                <h1 style="color: #063763; margin-bottom: 5px;">PT ZUHANTO TRAVEL INDONESIA</h1>
                <p style="margin: 0; color: #666;">$officialInvoiceText</p>
            </div>
            <div style="border: 1px solid #eee; padding: 20px; border-radius: 10px;">
                <table style="width: 100%; border-collapse: collapse; margin-bottom: 20px;">
                    <tr><td style="padding: 8px 0; border-bottom: 1px solid #f9f9f9;"><b>$bookingIdText</b></td><td style="text-align: right;">#${TextUtils.htmlEncode(booking.bookingId)}</td></tr>
                    <tr><td style="padding: 8px 0; border-bottom: 1px solid #f9f9f9;"><b>$customerText</b></td><td style="text-align: right;">${TextUtils.htmlEncode(booking.customerName)}</td></tr>
                    <tr><td style="padding: 8px 0; border-bottom: 1px solid #f9f9f9;"><b>$destinationText</b></td><td style="text-align: right;">${TextUtils.htmlEncode(booking.destination)}</td></tr>
                    <tr><td style="padding: 8px 0; border-bottom: 1px solid #f9f9f9;"><b>$dateText</b></td><td style="text-align: right;">${TextUtils.htmlEncode(booking.bookingDate)}</td></tr>
                    <tr><td style="padding: 8px 0; border-bottom: 1px solid #f9f9f9;"><b>$phoneText</b></td><td style="text-align: right;">${TextUtils.htmlEncode(booking.customerPhone)}</td></tr>
                    <tr><td style="padding: 8px 0; border-bottom: 1px solid #f9f9f9;"><b>$statusText</b></td><td style="text-align: right;">${TextUtils.htmlEncode(statusDisplay)}</td></tr>
                    <tr><td style="padding: 8px 0;"><b>$paymentStatusText</b></td><td style="text-align: right;">${TextUtils.htmlEncode(paymentStatusDisplay)}</td></tr>
                </table>
                <div style="background: #fcfcfc; padding: 15px; border-radius: 5px;">
                    <table style="width: 100%;">
                        <tr><td style="padding: 5px 0;">$normalPriceText</td><td style="text-align: right;">${TextUtils.htmlEncode(if (booking.basePrice.isNotBlank()) booking.basePrice else booking.finalPrice)}</td></tr>
                        ${if (booking.voucherUsed.isNotBlank()) "<tr><td style='padding: 5px 0;'>$discountText (${TextUtils.htmlEncode(booking.voucherUsed)})</td><td style='text-align: right; color: red;'>- ${TextUtils.htmlEncode(booking.discountAmount)}</td></tr>" else ""}
                        <tr style="font-size: 1.3em; font-weight: bold; color: #063763;">
                            <td style="padding-top: 10px;">$totalPaidText</td><td style="text-align: right; padding-top: 10px;">${TextUtils.htmlEncode(booking.finalPrice)}</td>
                        </tr>
                    </table>
                </div>
            </div>
            <div style="text-align: center; margin-top: 30px; color: #888;">
                <p><i>$footerThankYouText</i></p>
            </div>
        </body>
        </html>
    """.trimIndent()

    webView.webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            val printAdapter = webView.createPrintDocumentAdapter(jobName)
            printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
        }
    }
    webView.loadDataWithBaseURL(null, htmlContent, "text/HTML", "UTF-8", null)
}

@Composable
fun StarRatingBar(
    rating: Int,
    onRatingChanged: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (i in 1..5) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Star $i",
                tint = if (i <= rating) Color(0xFFFFA51E) else Color(0xFFCBD5E1),
                modifier = Modifier
                    .size(24.dp)
                    .clickable(enabled = onRatingChanged != null) {
                        onRatingChanged?.invoke(i)
                    }
            )
        }
    }
}

@Composable
fun RateTripSection(
    booking: Booking,
    firebaseRepo: FirebaseRepository,
    isHistory: Boolean
) {
    var currentRating by remember(booking.userRating) { mutableIntStateOf(booking.userRating) }
    var reviewText by remember(booking.userReview) { mutableStateOf(booking.userReview) }
    var isSubmitted by remember { mutableStateOf(booking.userRating > 0 || booking.userReview.isNotBlank()) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, Color(0xFFF1F5F9))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isSubmitted || booking.userRating > 0) {
                Text(
                    text = stringResource(R.string.thank_you_for_your_review),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF047857),
                    textAlign = TextAlign.Center
                )
                StarRatingBar(
                    rating = if (currentRating > 0) currentRating else 5,
                    onRatingChanged = null
                )
                if (reviewText.isNotBlank()) {
                    Text(
                        text = "\"$reviewText\"",
                        fontSize = 13.5.sp,
                        fontStyle = FontStyle.Italic,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.rate_and_review_completed),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                StarRatingBar(
                    rating = currentRating,
                    onRatingChanged = { newRating ->
                        currentRating = newRating
                    }
                )

                OutlinedTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    placeholder = { Text(stringResource(R.string.write_review)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 2
                )

                Button(
                    onClick = {
                        if (currentRating == 0) {
                            Toast.makeText(context, context.getString(R.string.please_select_one_star), Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        scope.launch {
                            val updatedBooking = booking.copy(
                                userRating = currentRating,
                                userReview = reviewText.trim()
                            )
                            firebaseRepo.updateBooking(updatedBooking)
                            isSubmitted = true
                            Toast.makeText(context, context.getString(R.string.review_submitted_successfully), Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text(stringResource(R.string.submit_rating_and_review))
                }
            }
        }
    }
}
