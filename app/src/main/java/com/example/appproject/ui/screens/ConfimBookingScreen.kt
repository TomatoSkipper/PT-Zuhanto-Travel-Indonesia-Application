package com.example.appproject.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.appproject.R
import com.example.appproject.data.model.Booking
import com.example.appproject.data.model.PricingTier
import com.example.appproject.data.model.TripPromo
import com.example.appproject.data.model.User
import com.example.appproject.data.model.getFormattedTierPrice
import com.example.appproject.data.model.parseBookingDate
import com.example.appproject.data.remote.resolveTripImageRes
import com.example.appproject.data.repository.FirebaseRepository
import com.example.appproject.ui.theme.PrimaryBlue
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun BookingDialog(
    trip: TripPromo,
    username: String,
    firebaseRepo: FirebaseRepository,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var bookingDate by remember { mutableStateOf("") }
    var dateError by remember { mutableStateOf(false) }

    val customPaxMin = remember(trip.name) { getCustomPaxMin(trip.name) }
    val isCustomPaxTrip = customPaxMin != null

    var customPaxCount by remember(customPaxMin) { mutableIntStateOf(customPaxMin ?: 5) }

    val availableTiers = remember(trip) {
        if (trip.pricingTiers.isNotEmpty()) {
            trip.pricingTiers
        } else {
            val digits = trip.price.filter { it.isDigit() }
            val pInt = digits.toIntOrNull() ?: 0
            val pFormatted = if (trip.price.isNotBlank()) trip.price else "IDR 0"
            listOf(PricingTier(1, "Standard Package", pInt, pFormatted))
        }
    }

    var selectedTier by remember { mutableStateOf(availableTiers.first()) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    var voucherCode by remember { mutableStateOf("") }
    var appliedDiscount by remember { mutableStateOf(0) }
    var voucherStatusMessage by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }

    var showPhoneVerificationDialog by remember { mutableStateOf(false) }
    var currentUserPhone by remember { mutableStateOf("") }

    val effectivePaxCount = if (isCustomPaxTrip) customPaxCount else selectedTier.paxCount

    val totalBasePriceInt = remember(selectedTier, customPaxCount, isCustomPaxTrip) {
        if (isCustomPaxTrip) {
            selectedTier.pricePerPaxInt * customPaxCount
        } else {
            selectedTier.pricePerPaxInt * selectedTier.paxCount
        }
    }

    val finalPriceString = remember(totalBasePriceInt, appliedDiscount) {
        if (appliedDiscount > 0) {
            val discountedPrice = (totalBasePriceInt * (100 - appliedDiscount)) / 100
            String.format(Locale.US, "IDR %,d", discountedPrice)
        } else {
            String.format(Locale.US, "IDR %,d", totalBasePriceInt)
        }
    }

    fun completeBookingCreation(customerPhone: String) {
        scope.launch {
            val customer = firebaseRepo.getUser(username)
            val customerUid = customer?.uid?.ifBlank { null } ?: FirebaseAuth.getInstance().currentUser?.uid ?: ""
            if (appliedDiscount > 0) {
                firebaseRepo.recordVoucherUsage(voucherCode, customerUid)
            }
            val paxLabelText = if (isCustomPaxTrip) {
                "$customPaxCount Pax" + (if (selectedTier.paxLabel.contains("Package", ignoreCase = true)) " - ${selectedTier.paxLabel}" else "")
            } else {
                selectedTier.paxLabel.ifEmpty { "${selectedTier.paxCount} Pax" }
            }
            val destinationWithNote = if (noteText.isNotBlank()) {
                "${trip.name} ($paxLabelText) - Note: ${noteText.trim()}"
            } else {
                "${trip.name} ($paxLabelText)"
            }
            firebaseRepo.saveBooking(
                Booking(
                    bookingId = System.currentTimeMillis().toString(),
                    customerUid = customerUid,
                    customerUsername = username,
                    customerName = customer?.username ?: username,
                    customerPhone = customerPhone,
                    destination = destinationWithNote,
                    bookingDate = bookingDate,
                    status = "Pending",
                    paymentStatus = "Pending",
                    finalPrice = finalPriceString,
                    basePrice = "IDR " + String.format(Locale.US, "%,d", totalBasePriceInt),
                    discountAmount = if (appliedDiscount > 0) {
                        String.format(
                            Locale.US,
                            "IDR %,d",
                            (totalBasePriceInt * appliedDiscount) / 100
                        )
                    } else "IDR 0",
                    voucherUsed = if (appliedDiscount > 0) voucherCode.trim().uppercase() else ""
                )
            )
            Toast.makeText(context, context.getString(R.string.booking_confirmed_success), Toast.LENGTH_SHORT).show()
            onDismiss()
        }
    }

    if (showPhoneVerificationDialog) {
        PhoneVerificationBookingDialog(
            initialPhone = currentUserPhone,
            onVerificationSuccess = { verifiedPhone ->
                scope.launch {
                    val customer = firebaseRepo.getUser(username)
                    val updatedUser = (customer ?: User(username = username, name = username)).copy(
                        phone = verifiedPhone,
                        isPhoneVerified = true
                    )
                    firebaseRepo.updateUser(updatedUser)
                    showPhoneVerificationDialog = false
                    completeBookingCreation(verifiedPhone)
                }
            },
            onDismiss = { showPhoneVerificationDialog = false }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF4F6FB))
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Scrollable Content Column (Top Banner + Form Cards)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Top Header Banner with Trip Image
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 260.dp)
                    ) {
                        AsyncImage(
                            model = resolveTripImageRes(trip.name, trip.imageRes),
                            contentDescription = trip.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize()
                        )

                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.35f),
                                            Color.Black.copy(alpha = 0.65f),
                                            Color.Black.copy(alpha = 0.85f)
                                        )
                                    )
                                )
                        )

                        // Back button at top-left
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier
                                .padding(20.dp)
                                .size(40.dp)
                                .align(Alignment.TopStart),
                            onClick = onDismiss
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Trip Title at bottom-left over the dark gradient
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(20.dp)
                        ) {
                            Text(
                                text = trip.name,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }

                    // Form Cards Padding Container
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                    // 1. "Who is coming?" Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.who_is_coming),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = if (isCustomPaxTrip) stringResource(R.string.we_need_at_least) + " ${customPaxMin ?: 1} " + stringResource(R.string.travelers) else stringResource(R.string.pick_your_package_and_options),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B)
                            )

                            // Show Dropdown Box if trip has multiple package options/tiers
                            if (availableTiers.size > 1) {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    Surface(
                                        onClick = { dropdownExpanded = true },
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color.White,
                                        border = BorderStroke(1.dp, PrimaryBlue),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.LocationOn,
                                                    contentDescription = null,
                                                    tint = PrimaryBlue
                                                )
                                                Column {
                                                    Text(
                                                        text = selectedTier.paxLabel.ifEmpty { "${selectedTier.paxCount}" + stringResource(R.string.pax) },
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        fontWeight = FontWeight.Bold,
                                                        color = PrimaryBlue
                                                    )
                                                    Text(
                                                        text = getFormattedTierPrice(selectedTier),
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color(0xFF64748B)
                                                    )
                                                }
                                            }
                                            Icon(
                                                imageVector = if (dropdownExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                contentDescription = "Select Package",
                                                tint = PrimaryBlue
                                            )
                                        }
                                    }

                                    DropdownMenu(
                                        expanded = dropdownExpanded,
                                        onDismissRequest = { dropdownExpanded = false },
                                        modifier = Modifier.fillMaxWidth(0.85f).background(Color.White)
                                    ) {
                                        availableTiers.forEach { tier ->
                                            val isSelected = selectedTier == tier
                                            DropdownMenuItem(
                                                text = {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                text = tier.paxLabel.ifEmpty { "${tier.paxCount}" + stringResource(R.string.pax) },
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (isSelected) PrimaryBlue else Color(0xFF0F172A)
                                                            )
                                                            Text(
                                                                text = getFormattedTierPrice(tier),
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = Color(0xFF64748B)
                                                            )
                                                        }
                                                        if (isSelected) {
                                                            Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryBlue)
                                                        }
                                                    }
                                                },
                                                onClick = {
                                                    selectedTier = tier
                                                    dropdownExpanded = false
                                                },
                                                modifier = Modifier.background(if (isSelected) PrimaryBlue.copy(alpha = 0.1f) else Color.Transparent)
                                            )
                                        }
                                    }
                                }
                            }

                            // Show Traveler Counter Box if trip supports custom pax or counter
                            if (isCustomPaxTrip) {
                                val minPax = customPaxMin ?: 1
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFF8FAFC),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = stringResource(R.string.travelers),
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F172A)
                                            )
                                            val rateStr = if (selectedTier.pricePerPaxInt > 0) String.format(Locale.US, "IDR %,d " + stringResource(R.string.per_person), selectedTier.pricePerPaxInt) else ""
                                            Text(
                                                text = rateStr,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFF64748B)
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            OutlinedIconButton(
                                                onClick = { if (customPaxCount > minPax) customPaxCount-- },
                                                enabled = customPaxCount > minPax,
                                                modifier = Modifier.size(38.dp),
                                                shape = CircleShape
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Remove,
                                                    contentDescription = "Decrease",
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Text(
                                                text = "$customPaxCount",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF0F172A)
                                            )
                                            Spacer(modifier = Modifier.width(16.dp))
                                            OutlinedIconButton(
                                                onClick = { if (customPaxCount < 100) customPaxCount++ },
                                                enabled = customPaxCount < 100,
                                                modifier = Modifier.size(38.dp),
                                                shape = CircleShape
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = "Increase",
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2. "When do you want to go?" Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.when_do_you_want_to_go),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = stringResource(R.string.pick_your_journey_date),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B)
                            )

                            Surface(
                                onClick = {
                                    val calendar = Calendar.getInstance()
                                    if (bookingDate.isNotBlank()) {
                                        try {
                                            val parsed = parseBookingDate(bookingDate)
                                            if (parsed != null) calendar.time = parsed
                                        } catch (_: Exception) {}
                                    }
                                    DatePickerDialog(
                                        context,
                                        { _, year, month, day ->
                                            val selectedCalendar = Calendar.getInstance().apply {
                                                set(year, month, day)
                                            }
                                            bookingDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(selectedCalendar.time)
                                            dateError = false
                                        },
                                        calendar.get(Calendar.YEAR),
                                        calendar.get(Calendar.MONTH),
                                        calendar.get(Calendar.DAY_OF_MONTH)
                                    ).apply {
                                        datePicker.minDate = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(7)
                                    }.show()
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, if (dateError) MaterialTheme.colorScheme.error else Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CalendarToday,
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = if (bookingDate.isBlank()) stringResource(R.string.select_a_date) else bookingDate,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (bookingDate.isBlank()) Color(0xFF94A3B8) else Color(0xFF0F172A),
                                        fontWeight = if (bookingDate.isNotBlank()) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }

                            if (dateError) {
                                Text(
                                    text = stringResource(R.string.please_select_a_booking_date),
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    // 3. "Have a promo code?" Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.have_a_promo_code),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = stringResource(R.string.enter_it_here_to_save_on_your_trip),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = voucherCode,
                                    onValueChange = { voucherCode = it },
                                    placeholder = { Text("e.g. NEWCOMER", color = Color(0xFF94A3B8)) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Button(
                                    onClick = {
                                        if (voucherCode.isNotBlank()) {
                                            scope.launch {
                                                val customer = firebaseRepo.getUser(username)
                                                val customerUid = customer?.uid?.ifBlank { null } ?: FirebaseAuth.getInstance().currentUser?.uid ?: username
                                                val isUsed = firebaseRepo.isVoucherUsedByUser(voucherCode, customerUid)
                                                if (isUsed) {
                                                    appliedDiscount = 0
                                                    voucherStatusMessage = context.getString(R.string.used_voucher_message)
                                                } else {
                                                    val vData = firebaseRepo.getVoucherData(voucherCode)
                                                    val pct = vData?.get("discountPercentage")?.toString()?.toIntOrNull() ?: 0

                                                    if (pct > 0) {
                                                        appliedDiscount = pct
                                                        voucherStatusMessage = context.getString(R.string.discount_applied_success, pct)
                                                    } else {
                                                        appliedDiscount = 0
                                                        voucherStatusMessage = context.getString(R.string.invalid_voucher)
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.height(56.dp)
                                ) {
                                    Text(stringResource(R.string.apply), fontWeight = FontWeight.Bold)
                                }
                            }

                            if (voucherStatusMessage.isNotEmpty()) {
                                Text(
                                    text = voucherStatusMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (appliedDiscount > 0) Color(0xFF16A34A) else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    // 4. "Anything we should know?" Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.anything_we_should_know),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = stringResource(R.string.add_a_note_for_our_team),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B)
                            )

                            OutlinedTextField(
                                value = noteText,
                                onValueChange = { if (it.length <= 300) noteText = it },
                                placeholder = { Text(stringResource(R.string.write_your_note_here), color = Color(0xFF94A3B8)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp),
                                shape = RoundedCornerShape(12.dp),
                                maxLines = 4
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${noteText.length} / 300",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }

                    // 5. "Price summary" Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.price_summary),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(R.string.normal_price),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = "IDR " + String.format(Locale.US, "%,d", totalBasePriceInt),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = stringResource(R.string.promo_discount),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = if (appliedDiscount > 0) "- IDR " + String.format(Locale.US, "%,d", (totalBasePriceInt * appliedDiscount) / 100) else "None",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (appliedDiscount > 0) FontWeight.Bold else FontWeight.Normal,
                                    color = if (appliedDiscount > 0) Color(0xFF16A34A) else Color(0xFF0F172A)
                                )
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = Color(0xFFE2E8F0)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.total_price),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0F172A)
                                )
                                Text(
                                    text = finalPriceString,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryBlue
                                )
                            }
                        }
                    }
                }

                // Bottom Sticky CTA Bar
                Surface(
                    color = Color.White,
                    shadowElevation = 12.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.total_for) + " $effectivePaxCount " + stringResource(R.string.travelers),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = finalPriceString,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Button(
                            onClick = {
                                if (bookingDate.isBlank()) {
                                    dateError = true
                                } else {
                                    scope.launch {
                                        val customer = firebaseRepo.getUser(username)
                                        currentUserPhone = customer?.phone.orEmpty()
                                        if (customer != null && customer.isPhoneVerified && customer.phone.isNotBlank()) {
                                            completeBookingCreation(customer.phone)
                                        } else {
                                            showPhoneVerificationDialog = true
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.book_now),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
}
