package com.example.appproject.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.appproject.R
import com.example.appproject.data.model.Booking
import com.example.appproject.data.model.StatusOption
import com.example.appproject.data.model.getBookingStatusDisplay
import com.example.appproject.data.model.getPaymentStatusDisplay
import com.example.appproject.data.model.normalizeBookingStatus
import com.example.appproject.data.model.normalizePaymentStatus
import com.example.appproject.data.repository.FirebaseRepository
import com.example.appproject.ui.components.BookingCardSkeleton
import com.example.appproject.ui.theme.PrimaryBlue
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun AdminScreen(
    firebaseRepo: FirebaseRepository
) {
    val context = LocalContext.current
    var bookingList by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var bookingBeingEdited by remember { mutableStateOf<Booking?>(null) }
    var bookingToDelete by remember { mutableStateOf<Booking?>(null) }
    var filterStatus by rememberSaveable { mutableStateOf("All") }
    var connectionStatus by remember { mutableStateOf(context.getString(R.string.testing)) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isLoading = true
        connectionStatus = firebaseRepo.testConnection()
        bookingList = firebaseRepo.getAllBookings()
        isLoading = false
    }

    val filteredList = bookingList.filter {
        val name = it.customerName.ifBlank { it.customerUsername }
        val dest = it.destination ?: ""
        val bId = it.bookingId ?: ""
        val status = it.status ?: ""

        val matchesSearch = searchQuery.isBlank() ||
                name.contains(searchQuery, ignoreCase = true) ||
                dest.contains(searchQuery, ignoreCase = true) ||
                bId.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (filterStatus) {
            "All" -> true
            else -> normalizeBookingStatus(status) == filterStatus
        }

        matchesSearch && matchesFilter
    }

    val totalBookings = bookingList.size
    val confirmedBookings = bookingList.count { normalizeBookingStatus(it.status) == "Confirmed" }
    val pendingPayments = bookingList.count { normalizeBookingStatus(it.status) == "Pending" }
    val cancelledBookingsCount = bookingList.count { normalizeBookingStatus(it.status) == "Cancelled" }

    val filterOptions = listOf(
        Pair("All", "${stringResource(R.string.all)} ($totalBookings)"),
        Pair("Pending", "${stringResource(R.string.pending)} ($pendingPayments)"),
        Pair("Confirmed", "${stringResource(R.string.confirmed)} ($confirmedBookings)"),
        Pair("Cancelled", "${stringResource(R.string.cancelled)} ($cancelledBookingsCount)")
    )

    Scaffold(
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Page Title Header
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.admin_dashboard),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        fontSize = 28.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.manage_bookings_easily),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF64748B),
                        fontSize = 15.sp
                    )
                }
            }

            // 2x2 KPI Grid Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        KpiCard(
                            title = stringResource(R.string.total_bookings),
                            count = totalBookings,
                            icon = Icons.Default.CalendarToday,
                            containerColor = Color(0xFFF0F7FF),
                            borderColor = Color(0xFFDBEAFE),
                            iconBgColor = Color(0xFFDBEAFE),
                            iconTint = Color(0xFF0284C7),
                            modifier = Modifier.weight(1f)
                        )
                        KpiCard(
                            title = stringResource(R.string.confirmed),
                            count = confirmedBookings,
                            icon = Icons.Default.Check,
                            containerColor = Color(0xFFF0FDF4),
                            borderColor = Color(0xFFDCFCE7),
                            iconBgColor = Color(0xFFDCFCE7),
                            iconTint = Color(0xFF16A34A),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        KpiCard(
                            title = stringResource(R.string.pending),
                            count = pendingPayments,
                            icon = Icons.Default.Schedule,
                            containerColor = Color(0xFFFFFBEB),
                            borderColor = Color(0xFFFEF3C7),
                            iconBgColor = Color(0xFFFEF3C7),
                            iconTint = Color(0xFFD97706),
                            modifier = Modifier.weight(1f)
                        )
                        KpiCard(
                            title = stringResource(R.string.cancelled),
                            count = cancelledBookingsCount,
                            icon = Icons.Default.Close,
                            containerColor = Color(0xFFFEF2F2),
                            borderColor = Color(0xFFFEE2E2),
                            iconBgColor = Color(0xFFFEE2E2),
                            iconTint = Color(0xFFDC2626),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            stringResource(R.string.search_by_name_destination_or_id),
                            color = Color(0xFF94A3B8),
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = Color(0xFF64748B)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = Color(0xFF64748B)
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedBorderColor = Color(0xFF0284C7)
                    )
                )
            }

            // Horizontal Filter Pills Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    filterOptions.forEach { (canonicalKey, labelText) ->
                        val isSelected = filterStatus == canonicalKey
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) Color(0xFF0265DC) else Color(0xFFE2E8F0),
                            modifier = Modifier.clickable { filterStatus = canonicalKey }
                        ) {
                            Text(
                                text = labelText,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF475569),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }

            // Booking Cards List
            if (isLoading) {
                items(3) {
                    BookingCardSkeleton(modifier = Modifier.padding(bottom = 12.dp))
                }
            } else if (filteredList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(R.string.no_bookings_found),
                            color = Color(0xFF64748B),
                            fontSize = 15.sp
                        )
                    }
                }
            } else {
                items(filteredList, key = { it.bookingId }) { booking ->
                    AdminBookingCard(
                        booking = booking,
                        onEdit = { bookingBeingEdited = booking },
                        onDelete = { bookingToDelete = booking },
                        onConfirmBooking = {
                            scope.launch {
                                val ts = if (booking.confirmationTimestamp == 0L) System.currentTimeMillis() else booking.confirmationTimestamp
                                val updatedBooking = booking.copy(status = "Confirmed", confirmationTimestamp = ts)
                                firebaseRepo.updateBooking(updatedBooking)
                                bookingList = bookingList.map {
                                    if (it.bookingId == updatedBooking.bookingId) updatedBooking else it
                                }
                                Toast.makeText(context, R.string.booking_updated_successfully, Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }

    bookingBeingEdited?.let { booking ->
        BookingEditDialog(
            booking = booking,
            onDismiss = { bookingBeingEdited = null },
            onSave = { updatedBooking ->
                scope.launch {
                    firebaseRepo.updateBooking(updatedBooking)
                    bookingList = bookingList.map {
                        if (it.bookingId == updatedBooking.bookingId) updatedBooking else it
                    }
                    bookingBeingEdited = null
                    Toast.makeText(context, R.string.booking_updated_successfully, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    bookingToDelete?.let { booking ->
        DeleteBookingDialog(
            booking = booking,
            onDismiss = { bookingToDelete = null },
            onConfirmDelete = {
                scope.launch {
                    firebaseRepo.deleteBooking(booking.bookingId)
                    bookingList = bookingList.filterNot { it.bookingId == booking.bookingId }
                    Toast.makeText(context, R.string.booking_deleted_successfully, Toast.LENGTH_SHORT).show()
                    bookingToDelete = null
                }
            }
        )
    }
}

@Composable
fun KpiCard(
    title: String,
    count: Int,
    icon: ImageVector,
    containerColor: Color,
    borderColor: Color,
    iconBgColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF475569),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A),
                    fontSize = 28.sp
                )
            }
        }
    }
}

@Composable
fun AdminBookingCard(
    booking: Booking,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onConfirmBooking: () -> Unit
) {
    val context = LocalContext.current
    val details = remember(booking.destination) { parseBookingDestinationDetails(booking.destination) }
    val normStatus = normalizeBookingStatus(booking.status)
    val statusText = getBookingStatusDisplay(booking.status, context)

    val (statusBg, statusFg, statusIcon) = when (normStatus) {
        "Confirmed" -> Triple(Color(0xFFDCFCE7), Color(0xFF15803D), Icons.Default.CheckCircle)
        "Cancelled" -> Triple(Color(0xFFFEE2E2), Color(0xFFB91C1C), Icons.Default.Cancel)
        else -> Triple(Color(0xFFFEF3C7), Color(0xFFB45309), Icons.Default.Schedule)
    }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row: Avatar Icon + Title/Pax + Status Badge Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0F2FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Landscape,
                            contentDescription = null,
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = details.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A),
                            fontSize = 17.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${details.paxInfo}  •  ID #${booking.bookingId}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B),
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Top Right Pill Badge
                Surface(
                    shape = CircleShape,
                    color = statusBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusFg,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelLarge,
                            color = statusFg,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(14.dp))

            // 2-Column Details Layout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                // Left Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Tarikh percutian
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = Color(0xFF475569),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.travel_date_label),
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        Text(
                            text = booking.bookingDate.ifBlank { "-" },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    // Pelanggan
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF475569),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.customer),
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        Text(
                            text = booking.customerName.ifBlank { booking.customerUsername.ifBlank { "User" } },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    // Nombor Telefon
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = Color(0xFF475569),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.phone_number),
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        Text(
                            text = booking.customerPhone.ifBlank { "-" },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    // Email
                    Column{
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = Color(0xFF475569),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.email),
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                    }
                        Text(
                            text = booking.customerEmail.ifBlank { "-" },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                }

                // Vertical Divider Line
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(Color(0xFFF1F5F9))
                )

                // Right Column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Jumlah harga
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AttachMoney,
                                contentDescription = null,
                                tint = Color(0xFF475569),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.total_price),
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        Text(
                            text = booking.finalPrice.ifBlank { booking.basePrice.ifBlank { "IDR 0" } },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }

                    // Status Pembayaran
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CreditCard,
                                contentDescription = null,
                                tint = Color(0xFF475569),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.payment_status),
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        val normPayment = normalizePaymentStatus(booking.paymentStatus)
                        val paymentDisplay = getPaymentStatusDisplay(booking.paymentStatus, context)
                        val paymentColor = if (normPayment == "Paid") Color(0xFF047857) else Color(0xFFD97706)
                        Text(
                            text = paymentDisplay,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = paymentColor
                        )
                    }

                    // Baucar
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ConfirmationNumber,
                                contentDescription = null,
                                tint = Color(0xFF475569),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.voucher),
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                        val voucherDisplay = if (booking.voucherUsed.isNotBlank()) {
                            val discountText = if (booking.discountAmount.isNotBlank()) " (-${booking.discountAmount})" else ""
                            "${booking.voucherUsed}$discountText"
                        } else {
                            stringResource(R.string.no_voucher_used)
                        }
                        val voucherColor = if (booking.voucherUsed.isNotBlank()) Color(0xFF0284C7) else Color(0xFF64748B)
                        Text(
                            text = voucherDisplay,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = voucherColor
                        )
                    }
                }
            }

            // Customer Note Banner
            if (details.noteText.isNotBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF0F7FF)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.customer_note),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0284C7)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = details.noteText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF0F172A)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Action Button: Confirm Booking (if pending)
            if (normStatus == "Pending") {
                Button(
                    onClick = onConfirmBooking,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0265DC))
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.confirm_booking),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Action Buttons Row: Sunting & Padam
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, Color(0xFF0284C7)),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF0284C7)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.edit),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0284C7)
                    )
                }

                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFEF4444)),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFEF4444)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.delete),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF4444)
                    )
                }
            }
        }
    }
}

@Composable
fun BookingDetailRow(
    label: String,
    value: String,
    valueColor: Color = Color(0xFF0F172A),
    showDot: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF64748B),
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            if (showDot) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(valueColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
fun DeleteBookingDialog(
    booking: Booking,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    val context = LocalContext.current
    var isUnderstandChecked by remember { mutableStateOf(false) }

    val normStatus = normalizeBookingStatus(booking.status)
    val normPayment = normalizePaymentStatus(booking.paymentStatus)
    val statusText = getBookingStatusDisplay(booking.status, context)
    val statusColor = when (normStatus) {
        "Confirmed" -> Color(0xFF047857)
        "Cancelled" -> Color(0xFFB91C1C)
        else -> Color(0xFFD97706)
    }

    val paymentDisplay = getPaymentStatusDisplay(booking.paymentStatus, context)
    val paymentColor = if (normPayment == "Paid") Color(0xFF047857) else Color(0xFFD97706)

    val isConfirmedOrPaid = normStatus == "Confirmed" || normPayment == "Paid"

    val warningMsg = when {
        normStatus == "Confirmed" && normPayment == "Paid" ->
            stringResource(R.string.delete_booking_warn_confirmed_paid)
        normStatus == "Confirmed" ->
            stringResource(R.string.delete_booking_warn_confirmed)
        else ->
            stringResource(R.string.delete_booking_warn_paid)
    }

    Dialog(
        onDismissRequest = onDismiss,
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
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFEE2E2),
                    modifier = Modifier.size(52.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.delete_this_booking),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = stringResource(R.string.this_cant_be_undone),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF64748B)
                    )
                }

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
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        BookingDetailRow(
                            label = stringResource(R.string.booking_id),
                            value = "#${booking.bookingId}"
                        )
                        HorizontalDivider(color = Color(0xFFE2E8F0))
                        BookingDetailRow(
                            label = stringResource(R.string.customer_name),
                            value = booking.customerName.ifBlank { booking.customerUsername.ifBlank { "User" } }
                        )
                        if (booking.customerPhone.isNotBlank()) {
                            BookingDetailRow(
                                label = stringResource(R.string.phone),
                                value = booking.customerPhone
                            )
                        }
                        HorizontalDivider(color = Color(0xFFE2E8F0))
                        BookingDetailRow(
                            label = stringResource(R.string.package_label),
                            value = booking.destination.ifBlank { stringResource(R.string.tour_package) }
                        )
                        if (booking.bookingDate.isNotBlank()) {
                            BookingDetailRow(
                                label = stringResource(R.string.travel_date_label),
                                value = booking.bookingDate
                            )
                        }
                        if (booking.finalPrice.isNotBlank()) {
                            BookingDetailRow(
                                label = stringResource(R.string.total_label),
                                value = booking.finalPrice
                            )
                        }
                        BookingDetailRow(
                            label = stringResource(R.string.applied_voucher),
                            value = if (booking.voucherUsed.isNotBlank()) "${booking.voucherUsed}${if (booking.discountAmount.isNotBlank()) " (-${booking.discountAmount})" else ""}" else stringResource(R.string.no_voucher_used)
                        )
                        BookingDetailRow(
                            label = stringResource(R.string.booking_label),
                            value = statusText,
                            valueColor = statusColor,
                            showDot = true
                        )
                        BookingDetailRow(
                            label = stringResource(R.string.payment_label),
                            value = paymentDisplay,
                            valueColor = paymentColor,
                            showDot = true
                        )
                    }
                }

                if (isConfirmedOrPaid) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFFFBEB),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Warning,
                                contentDescription = null,
                                tint = Color(0xFFB45309),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = warningMsg,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF92400E),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isUnderstandChecked = !isUnderstandChecked }
                ) {
                    Checkbox(
                        checked = isUnderstandChecked,
                        onCheckedChange = { isUnderstandChecked = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFFEF4444)
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.i_understand_delete),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1E293B)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onConfirmDelete,
                        enabled = isUnderstandChecked,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE57373),
                            disabledContainerColor = Color(0xFFFCA5A5).copy(alpha = 0.5f),
                            contentColor = Color.White,
                            disabledContentColor = Color.White.copy(alpha = 0.7f)
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.delete_booking_button),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Text(
                            text = stringResource(R.string.keep_booking),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2563EB)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BookingEditDialog(
    booking: Booking,
    onDismiss: () -> Unit,
    onSave: (Booking) -> Unit
) {
    val context = LocalContext.current
    val details = remember(booking.destination) { parseBookingDestinationDetails(booking.destination) }

    var customerName by remember(booking) { mutableStateOf(booking.customerName.ifBlank { booking.customerUsername }) }
    var customerPhone by remember(booking) { mutableStateOf(booking.customerPhone) }
    var bookingDate by remember(booking) { mutableStateOf(booking.bookingDate) }
    var bookingStatus by remember(booking) { mutableStateOf(normalizeBookingStatus(booking.status)) }
    var paymentStatus by remember(booking) { mutableStateOf(normalizePaymentStatus(booking.paymentStatus)) }

    val bookingStatusOptions = listOf(
        StatusOption("Pending", stringResource(R.string.pending)),
        StatusOption("Confirmed", stringResource(R.string.confirmed)),
        StatusOption("Cancelled", stringResource(R.string.cancelled))
    )

    val paymentStatusOptions = listOf(
        StatusOption("Pending", stringResource(R.string.pending)),
        StatusOption("Paid", stringResource(R.string.paid)),
        StatusOption("Cancelled", stringResource(R.string.cancelled))
    )

    val openDatePicker = {
        val calendar = Calendar.getInstance()
        try {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
            val parsed = sdf.parse(bookingDate)
            if (parsed != null) {
                calendar.time = parsed
            }
        } catch (_: Exception) {}

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                bookingDate = SimpleDateFormat("dd MMM yyyy", Locale.US).format(selectedCalendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Column {
                Text("Update booking", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
                Spacer(modifier = Modifier.height(2.dp))
                Text("Booking #${booking.bookingId}", fontSize = 13.sp, color = Color(0xFF64748B))
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column {
                    Text(stringResource(R.string.customer_name), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedBorderColor = Color(0xFF0284C7)
                        )
                    )
                }

                Column {
                    Text(stringResource(R.string.phone_number), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedBorderColor = Color(0xFF0284C7)
                        )
                    )
                }

                Column {
                    Text(stringResource(R.string.package_read_only), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = "${details.title}, ${details.paxInfo}",
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF1F5F9),
                            focusedContainerColor = Color(0xFFF1F5F9),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedBorderColor = Color(0xFFE2E8F0)
                        )
                    )
                }

                Column {
                    Text(stringResource(R.string.customer_note_read_only), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = details.noteText.ifBlank { "" },
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF1F5F9),
                            focusedContainerColor = Color(0xFFF1F5F9),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedBorderColor = Color(0xFFE2E8F0)
                        )
                    )
                }

                Column {
                    Text(stringResource(R.string.applied_voucher), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = if (booking.voucherUsed.isNotBlank()) "${booking.voucherUsed}${if (booking.discountAmount.isNotBlank()) " (-${booking.discountAmount})" else ""}" else stringResource(R.string.no_voucher_used),
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFF1F5F9),
                            focusedContainerColor = Color(0xFFF1F5F9),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedBorderColor = Color(0xFFE2E8F0)
                        )
                    )
                }

                Column {
                    Text(stringResource(R.string.travel_date), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = bookingDate,
                        onValueChange = { bookingDate = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { openDatePicker() },
                        readOnly = true,
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        trailingIcon = {
                            IconButton(onClick = { openDatePicker() }) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Select Date",
                                    tint = Color(0xFF2563EB)
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White,
                            unfocusedBorderColor = Color(0xFF64748B),
                            focusedBorderColor = Color(0xFF0284C7)
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        BookingDropdown(
                            label = stringResource(R.string.booking_status),
                            selectedCanonical = bookingStatus,
                            options = bookingStatusOptions,
                            onValueSelected = { bookingStatus = it }
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        BookingDropdown(
                            label = stringResource(R.string.payment_status),
                            selectedCanonical = paymentStatus,
                            options = paymentStatusOptions,
                            onValueSelected = { paymentStatus = it }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val wasConfirmed = normalizeBookingStatus(booking.status) == "Confirmed"
                    val isConfirmedNow = normalizeBookingStatus(bookingStatus) == "Confirmed"
                    val ts = if (isConfirmedNow && (!wasConfirmed || booking.confirmationTimestamp == 0L)) {
                        System.currentTimeMillis()
                    } else {
                        booking.confirmationTimestamp
                    }
                    onSave(
                        booking.copy(
                            customerName = customerName,
                            customerPhone = customerPhone,
                            bookingDate = bookingDate,
                            status = bookingStatus,
                            paymentStatus = paymentStatus,
                            confirmationTimestamp = ts
                        )
                    )
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D5C91)),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Text(stringResource(R.string.save_changes), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Text(stringResource(R.string.cancel), color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    )
}

@Composable
fun BookingDropdown(
    label: String,
    selectedCanonical: String,
    options: List<StatusOption>,
    onValueSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val currentDisplay = options.find { it.canonical == selectedCanonical }?.display
        ?: getBookingStatusDisplay(selectedCanonical)

    val currentStatusColor = when (normalizeBookingStatus(selectedCanonical)) {
        "Confirmed", "Paid" -> Color(0xFF047857)
        "Cancelled" -> Color(0xFFB91C1C)
        else -> Color(0xFFD97706)
    }

    Column {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
        Spacer(modifier = Modifier.height(4.dp))
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, if (expanded) PrimaryBlue else Color(0xFFCBD5E1)),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(currentStatusColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(currentDisplay, color = Color(0xFF0F172A), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = Color(0xFF64748B)
                    )
                }
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .background(Color.White)
            ) {
                options.forEach { option ->
                    val isSelected = option.canonical == selectedCanonical
                    val optionStatusColor = when (normalizeBookingStatus(option.canonical)) {
                        "Confirmed", "Paid" -> Color(0xFF047857)
                        "Cancelled" -> Color(0xFFB91C1C)
                        else -> Color(0xFFD97706)
                    }
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(optionStatusColor)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = option.display,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) PrimaryBlue else Color(0xFF0F172A)
                                    )
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryBlue)
                                }
                            }
                        },
                        onClick = {
                            onValueSelected(option.canonical)
                            expanded = false
                        },
                        modifier = Modifier.background(if (isSelected) PrimaryBlue.copy(alpha = 0.1f) else Color.Transparent)
                    )
                }
            }
        }
    }
}
