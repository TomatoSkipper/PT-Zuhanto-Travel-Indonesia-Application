package com.example.appproject.ui.screens

import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appproject.R
import com.example.appproject.data.repository.FirebaseRepository
import com.example.appproject.ui.components.AdminListItemSkeleton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


enum class VoucherStatusType(@StringRes val labelRes: Int) {
    ALL(R.string.all),
    ACTIVE(R.string.active),
    UPCOMING(R.string.upcoming),
    EXPIRED(R.string.expired),
    DISABLED(R.string.disabled)
}

@Composable
fun AdminVouchersScreen(firebaseRepo: FirebaseRepository) {
    val context = LocalContext.current
    var adminVouchers by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Form inputs
    var editingVoucherCode by remember { mutableStateOf<String?>(null) }
    var inputVoucherCode by remember { mutableStateOf("") }
    var customPercentage by remember { mutableStateOf("") }
    var voucherDescription by remember { mutableStateOf("") }
    var startDateStr by remember { mutableStateOf("") }
    var endDateStr by remember { mutableStateOf("") }
    var voucherBgImageUri by remember { mutableStateOf<Uri?>(null) }
    var voucherBgImageBase64 by remember { mutableStateOf("") }

    // Search and Filter
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(VoucherStatusType.ALL) }

    val scope = rememberCoroutineScope()

    val voucherBgPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            voucherBgImageUri = uri
            scope.launch {
                voucherBgImageBase64 = compressAndEncodeImage(context, uri)
                if (voucherBgImageBase64.isBlank()) {
                    Toast.makeText(context, R.string.failed_to_process_image, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, R.string.voucher_picture_uploaded_successfully, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun showDatePicker(currentDateStr: String = "", onDateSelected: (String) -> Unit) {
        val cal = Calendar.getInstance()
        if (currentDateStr.isNotBlank()) {
            try {
                val parsed = SimpleDateFormat("dd-MM-yyyy", Locale.US).parse(currentDateStr.trim())
                if (parsed != null) cal.time = parsed
            } catch (_: Exception) {}
        }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedSdf = SimpleDateFormat("dd-MM-yyyy", Locale.US)
                val selectedCal = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }
                onDateSelected(selectedSdf.format(selectedCal.time))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun clearForm() {
        editingVoucherCode = null
        inputVoucherCode = ""
        customPercentage = ""
        voucherDescription = ""
        startDateStr = ""
        endDateStr = ""
        voucherBgImageUri = null
        voucherBgImageBase64 = ""
    }

    LaunchedEffect(Unit) {
        isLoading = true
        adminVouchers = firebaseRepo.getAllVouchers()
        isLoading = false
    }

    val filteredVouchers = adminVouchers.filter { voucher ->
        val code = voucher["code"]?.toString() ?: ""
        val desc = voucher["description"]?.toString() ?: ""

        val matchesSearch = searchQuery.isBlank() ||
                code.contains(searchQuery, ignoreCase = true) ||
                desc.contains(searchQuery, ignoreCase = true)

        val statusType = getVoucherStatusType(voucher)
        val matchesFilter = when (selectedFilter) {
            VoucherStatusType.ALL -> true
            else -> statusType == selectedFilter
        }

        matchesSearch && matchesFilter
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Section: Urus Baucar
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0265DC)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ConfirmationNumber,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = stringResource(R.string.manage_vouchers),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            fontSize = 26.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.create_promo_codes_and_manage_live_offers),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF64748B),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Create / Edit Voucher Card: Cipta Baucar
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF0265DC)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (editingVoucherCode != null) Icons.Default.Edit else Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = if (editingVoucherCode != null) stringResource(R.string.edit_voucher) else stringResource(R.string.create_voucher),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A),
                                    fontSize = 20.sp
                                )
                            }

                            if (editingVoucherCode != null) {
                                TextButton(onClick = { clearForm() }) {
                                    Text(stringResource(R.string.cancel), color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Kod Baucar & Diskaun (%) Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1.5f)) {
                                Text(
                                    text = stringResource(R.string.voucher_code),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF334155),
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = inputVoucherCode,
                                    onValueChange = { inputVoucherCode = it.uppercase() },
                                    placeholder = { Text("e.g : WELCOME15", color = Color(0xFF94A3B8), fontSize = 14.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color(0xFFCBD5E1),
                                        focusedBorderColor = Color(0xFF0265DC),
                                        unfocusedContainerColor = Color.White,
                                        focusedContainerColor = Color.White
                                    )
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.discount_percentage),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF334155),
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = customPercentage,
                                    onValueChange = { customPercentage = it },
                                    placeholder = { Text("15", color = Color(0xFF94A3B8), fontSize = 14.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color(0xFFCBD5E1),
                                        focusedBorderColor = Color(0xFF0265DC),
                                        unfocusedContainerColor = Color.White,
                                        focusedContainerColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Penerangan
                        Text(
                            text = stringResource(R.string.description),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = voucherDescription,
                            onValueChange = { voucherDescription = it },
                            placeholder = { Text(stringResource(R.string.enter_description), color = Color(0xFF94A3B8), fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color(0xFFCBD5E1),
                                focusedBorderColor = Color(0xFF0265DC),
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Tarikh Mula & Tarikh Tamat Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.start_date),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF334155),
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = startDateStr,
                                        onValueChange = {},
                                        placeholder = { Text(stringResource(R.string.choose_date), color = Color(0xFF94A3B8), fontSize = 14.sp) },
                                        modifier = Modifier.fillMaxWidth(),
                                        readOnly = true,
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp),
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.CalendarToday,
                                                contentDescription = stringResource(R.string.choose_starting_date),
                                                tint = Color(0xFF64748B),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = Color(0xFFCBD5E1),
                                            focusedBorderColor = Color(0xFF0265DC),
                                            unfocusedContainerColor = Color.White,
                                            focusedContainerColor = Color.White
                                        )
                                    )
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .clickable { showDatePicker(startDateStr) { startDateStr = it } }
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.end_date),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF334155),
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = endDateStr,
                                        onValueChange = {},
                                        placeholder = { Text(stringResource(R.string.choose_date), color = Color(0xFF94A3B8), fontSize = 14.sp) },
                                        modifier = Modifier.fillMaxWidth(),
                                        readOnly = true,
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp),
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.CalendarToday,
                                                contentDescription = "choose end date",
                                                tint = Color(0xFF64748B),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = Color(0xFFCBD5E1),
                                            focusedBorderColor = Color(0xFF0265DC),
                                            unfocusedContainerColor = Color.White,
                                            focusedContainerColor = Color.White
                                        )
                                    )
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .clickable { showDatePicker(endDateStr) { endDateStr = it } }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Imej Baucar (Pilihan)
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(R.string.voucher_image),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF334155),
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = stringResource(R.string.choices),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color(0xFF64748B),
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .dashedBorder(color = Color(0xFFCBD5E1), cornerRadius = 12.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFFAFAFA))
                                    .clickable { voucherBgPickerLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CloudUpload,
                                        contentDescription = null,
                                        tint = Color(0xFF0265DC),
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = if (voucherBgImageUri != null) stringResource(R.string.image_selected) else stringResource(R.string.upload_image),
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0265DC),
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "PNG, JPG (max. 2MB)",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Submit Button
                        Button(
                            onClick = {
                                val code = inputVoucherCode.trim().uppercase()
                                val pct = customPercentage.toIntOrNull() ?: 0
                                if (code.isBlank() || pct <= 0) {
                                    Toast.makeText(context, R.string.invalid_voucher_input, Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                scope.launch {
                                    firebaseRepo.saveVoucher(
                                        code = code,
                                        discountPercentage = pct,
                                        description = voucherDescription,
                                        startDate = startDateStr,
                                        endDate = endDateStr,
                                        backgroundPicture = voucherBgImageBase64
                                    )
                                    clearForm()
                                    adminVouchers = firebaseRepo.getAllVouchers()
                                    Toast.makeText(context, R.string.voucher_created_successfully, Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0265DC))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = if (editingVoucherCode != null) stringResource(R.string.edit_voucher) else stringResource(R.string.save_voucher),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // Existing Vouchers Header: Baucar Sedia Ada
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0265DC)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ConfirmationNumber,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = stringResource(R.string.existing_vouchers),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        fontSize = 22.sp
                    )
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
                            stringResource(R.string.find_voucher_code_or_description),
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
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedBorderColor = Color(0xFF0265DC)
                    )
                )
            }

            // Horizontal Filter Chips Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VoucherStatusType.entries.forEach { type ->
                        val count = when (type) {
                            VoucherStatusType.ALL -> adminVouchers.size
                            VoucherStatusType.ACTIVE -> adminVouchers.count { getVoucherStatusType(it) == VoucherStatusType.ACTIVE }
                            VoucherStatusType.UPCOMING -> adminVouchers.count { getVoucherStatusType(it) == VoucherStatusType.UPCOMING }
                            VoucherStatusType.EXPIRED -> adminVouchers.count { getVoucherStatusType(it) == VoucherStatusType.EXPIRED }
                            VoucherStatusType.DISABLED -> adminVouchers.count { getVoucherStatusType(it) == VoucherStatusType.DISABLED }
                        }

                        val isSelected = selectedFilter == type

                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) Color(0xFF0265DC) else Color.White,
                            border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.clickable { selectedFilter = type }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (!isSelected) {
                                    when (type) {
                                        VoucherStatusType.ACTIVE -> Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF16A34A))
                                        )
                                        VoucherStatusType.UPCOMING -> Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint = Color(0xFF0284C7),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        VoucherStatusType.EXPIRED -> Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            tint = Color(0xFFDC2626),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        VoucherStatusType.DISABLED -> Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF64748B))
                                        )
                                        VoucherStatusType.ALL -> {}
                                    }
                                }

                                Text(
                                    text = "${stringResource(type.labelRes)} ($count)",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF475569),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // Voucher List Items
            if (isLoading) {
                items(3) {
                    AdminListItemSkeleton()
                }
            } else if (filteredVouchers.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            stringResource(R.string.no_existing_vouchers),
                            color = Color(0xFF64748B),
                            fontSize = 15.sp
                        )
                    }
                }
            } else {
                items(filteredVouchers, key = { it["code"]?.toString() ?: "" }) { voucher ->
                    val code = voucher["code"]?.toString() ?: ""
                    val discount = voucher["discountPercentage"]?.toString() ?: "0"
                    val desc = voucher["description"]?.toString() ?: ""
                    val start = voucher["startDate"]?.toString() ?: ""
                    val end = voucher["endDate"]?.toString() ?: ""
                    val statusType = getVoucherStatusType(voucher)

                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Header Row
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
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFE0F2FE)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ConfirmationNumber,
                                            contentDescription = null,
                                            tint = Color(0xFF0284C7),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = code,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF0F172A),
                                            fontSize = 18.sp
                                        )
                                        if (desc.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = desc,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFF64748B),
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Column(horizontalAlignment = Alignment.End) {
                                    // Status Badge Pill
                                    val badgeBg: Color
                                    val badgeFg: Color
                                    val statusLabel: String
                                    val badgeIcon: ImageVector?

                                    when (statusType) {
                                        VoucherStatusType.ACTIVE -> {
                                            badgeBg = Color(0xFFDCFCE7)
                                            badgeFg = Color(0xFF15803D)
                                            statusLabel = stringResource(R.string.active)
                                            badgeIcon = null
                                        }
                                        VoucherStatusType.UPCOMING -> {
                                            badgeBg = Color(0xFFE0F2FE)
                                            badgeFg = Color(0xFF0369A1)
                                            statusLabel = stringResource(R.string.upcoming)
                                            badgeIcon = Icons.Default.Schedule
                                        }
                                        VoucherStatusType.EXPIRED -> {
                                            badgeBg = Color(0xFFFEE2E2)
                                            badgeFg = Color(0xFFB91C1C)
                                            statusLabel = stringResource(R.string.expired)
                                            badgeIcon = Icons.Default.CalendarToday
                                        }
                                        VoucherStatusType.DISABLED -> {
                                            badgeBg = Color(0xFFF1F5F9)
                                            badgeFg = Color(0xFF64748B)
                                            statusLabel = stringResource(R.string.disabled)
                                            badgeIcon = null
                                        }
                                        VoucherStatusType.ALL -> {
                                            badgeBg = Color(0xFFDCFCE7)
                                            badgeFg = Color(0xFF15803D)
                                            statusLabel = stringResource(R.string.active)
                                            badgeIcon = null
                                        }
                                    }

                                    Surface(
                                        shape = CircleShape,
                                        color = badgeBg
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            if (badgeIcon != null) {
                                                Icon(
                                                    imageVector = badgeIcon,
                                                    contentDescription = null,
                                                    tint = badgeFg,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(badgeFg)
                                                )
                                            }
                                            Text(
                                                text = statusLabel,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = badgeFg,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = "$discount%",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF0265DC),
                                        fontSize = 22.sp
                                    )
                                    Text(
                                        text = stringResource(R.string.discount),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF64748B),
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(12.dp))

                            // Dates Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            tint = Color(0xFF64748B),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = stringResource(R.string.valid_from),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF64748B),
                                            fontSize = 12.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = formatVoucherDateDisplay(start),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A),
                                        fontSize = 14.sp
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .height(30.dp)
                                        .width(1.dp)
                                        .background(Color(0xFFF1F5F9))
                                )

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            tint = Color(0xFF64748B),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = stringResource(R.string.valid_until),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF64748B),
                                            fontSize = 12.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = formatVoucherDateDisplay(end),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A),
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Action Row: Sunting + Menu
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        editingVoucherCode = code
                                        inputVoucherCode = code
                                        customPercentage = discount
                                        voucherDescription = desc
                                        startDateStr = start
                                        endDateStr = end
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Color(0xFF0265DC)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0265DC)),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "edit",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(stringResource(R.string.edit), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }

                                Box {
                                    var menuExpanded by remember { mutableStateOf(false) }

                                    IconButton(onClick = { menuExpanded = true }) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "More options",
                                            tint = Color(0xFF64748B)
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = menuExpanded,
                                        onDismissRequest = { menuExpanded = false }
                                    ) {
                                        val isDisabled = statusType == VoucherStatusType.DISABLED
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = if (isDisabled) Icons.Default.CheckCircle else Icons.Default.Block,
                                                        contentDescription = null,
                                                        tint = if (isDisabled) Color(0xFF16A34A) else Color(0xFFDC2626),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        if (isDisabled) stringResource(R.string.activate) else stringResource(R.string.deactivate),
                                                        color = if (isDisabled) Color(0xFF16A34A) else Color(0xFFDC2626)
                                                    )
                                                }
                                            },
                                            onClick = {
                                                menuExpanded = false
                                                scope.launch {
                                                    firebaseRepo.updateVoucherDisabled(code, !isDisabled)
                                                    adminVouchers = firebaseRepo.getAllVouchers()
                                                }
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = null,
                                                        tint = Color(0xFFDC2626),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("Padam", color = Color(0xFFDC2626))
                                                }
                                            },
                                            onClick = {
                                                menuExpanded = false
                                                scope.launch {
                                                    firebaseRepo.deleteVoucher(code)
                                                    adminVouchers = firebaseRepo.getAllVouchers()
                                                    Toast.makeText(context, R.string.voucher_deleted, Toast.LENGTH_SHORT).show()
                                                }
                                            }
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
}

private fun getVoucherStatusType(voucher: Map<String, Any>): VoucherStatusType {
    val isDisabled = voucher["isDisabled"] as? Boolean ?: false
    if (isDisabled) return VoucherStatusType.DISABLED

    val startStr = voucher["startDate"]?.toString()?.trim() ?: ""
    val endStr = voucher["endDate"]?.toString()?.trim() ?: ""

    val todayCal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val todayMs = todayCal.timeInMillis

    val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.US)

    val startDateMs = try {
        if (startStr.isNotBlank()) sdf.parse(startStr)?.time else null
    } catch (_: Exception) { null }

    val endDateMs = try {
        if (endStr.isNotBlank()) sdf.parse(endStr)?.time else null
    } catch (_: Exception) { null }

    if (startDateMs != null && todayMs < startDateMs) {
        return VoucherStatusType.UPCOMING
    }

    if (endDateMs != null && todayMs > endDateMs + 86400000L - 1L) {
        return VoucherStatusType.EXPIRED
    }

    return VoucherStatusType.ACTIVE
}

private fun formatVoucherDateDisplay(dateStr: String): String {
    if (dateStr.isBlank()) return "-"
    return try {
        val sdfIn = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        val date = sdfIn.parse(dateStr.trim())
        if (date != null) {
            val sdfOut = SimpleDateFormat("dd MMM yyyy", Locale.US)
            sdfOut.format(date)
        } else {
            dateStr
        }
    } catch (_: Exception) {
        dateStr
    }
}

private fun Modifier.dashedBorder(
    color: Color,
    strokeWidth: Dp = 1.dp,
    dashLength: Dp = 6.dp,
    gapLength: Dp = 4.dp,
    cornerRadius: Dp = 12.dp
) = this.drawWithCache {
    val stroke = Stroke(
        width = strokeWidth.toPx(),
        pathEffect = PathEffect.dashPathEffect(
            floatArrayOf(dashLength.toPx(), gapLength.toPx()),
            0f
        )
    )
    onDrawBehind {
        drawRoundRect(
            color = color,
            style = stroke,
            cornerRadius = CornerRadius(cornerRadius.toPx())
        )
    }
}

private suspend fun compressAndEncodeImage(context: Context, uri: Uri): String {
    return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext ""
            val originalBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            if (originalBitmap == null) return@withContext ""

            val maxDimension = 800
            val width = originalBitmap.width
            val height = originalBitmap.height
            val (targetW, targetH) = if (width > height) {
                if (width > maxDimension) maxDimension to (height * maxDimension / width) else width to height
            } else {
                if (height > maxDimension) (width * maxDimension / height) to maxDimension else width to height
            }

            val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(originalBitmap, targetW, targetH, true)
            val outputStream = java.io.ByteArrayOutputStream()
            scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outputStream)
            val bytes = outputStream.toByteArray()
            Base64.encodeToString(bytes, Base64.DEFAULT)
        } catch (_: Exception) {
            ""
        }
    }
}
