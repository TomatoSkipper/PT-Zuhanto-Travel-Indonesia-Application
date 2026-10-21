package com.example.appproject.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.appproject.R
import com.example.appproject.data.model.HelpRequest
import com.example.appproject.data.repository.FirebaseRepository
import com.example.appproject.ui.theme.PrimaryBlue
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AskQuestionScreen(
    loggedInUser: String? = null,
    displayUsername: String? = null,
    firebaseRepo: FirebaseRepository? = null,
    onBack: () -> Unit
) {
    var selectedCategory by rememberSaveable { mutableStateOf("") }
    var selectedTrip by rememberSaveable { mutableStateOf("") }
    var tripDropdownExpanded by remember { mutableStateOf(false) }

    var customerName by rememberSaveable { mutableStateOf(displayUsername ?: loggedInUser ?: "") }
    var customerEmail by rememberSaveable { mutableStateOf("") }
    
    var selectedCountryCode by rememberSaveable { mutableStateOf("+60") }
    var selectedCountryFlag by rememberSaveable { mutableStateOf("🇲🇾") }
    var countryDropdownExpanded by remember { mutableStateOf(false) }
    var customerPhone by rememberSaveable { mutableStateOf("") }

    var customerQuestion by rememberSaveable { mutableStateOf("") }

    var hasAttemptedSubmit by rememberSaveable { mutableStateOf(false) }
    val categoryError = hasAttemptedSubmit && selectedCategory.isBlank()
    val nameError = hasAttemptedSubmit && customerName.isBlank()
    val emailError = hasAttemptedSubmit && customerEmail.isBlank()
    val phoneError = hasAttemptedSubmit && customerPhone.isBlank()
    val questionError = hasAttemptedSubmit && customerQuestion.isBlank()

    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val countryOptions = listOf(
        Pair("🇲🇾", "+60"),
        Pair("🇮🇩", "+62"),
        Pair("🇺🇸", "+1"),
        Pair("🇸🇬", "+65"),
        Pair("🇦🇺", "+61")
    )

    val tripOptions = listOf(
        context.getString(R.string.not_about_trip),
        context.getString(R.string.pkg_bromo_sunrise),
        context.getString(R.string.pkg_tumpak_sewu),
        context.getString(R.string.pkg_ijen_bluefire),
        context.getString(R.string.pkg_jogja_dieng),
        context.getString(R.string.pkg_mount_rinjani),
        context.getString(R.string.pkg_yogyakarta_2d1n),
        context.getString(R.string.pkg_bromo_midnight),
        context.getString(R.string.pkg_malang_batu),
        context.getString(R.string.pkg_lembang_bandung),
        context.getString(R.string.pkg_jakarta_bogor_golf),
        context.getString(R.string.mount_bromo),
        context.getString(R.string.borobudur_temple),
        context.getString(R.string.ijen_crater),
        context.getString(R.string.bali),
        context.getString(R.string.labuan_bajo),
        context.getString(R.string.raja_ampat)
    )

    if (showSuccessDialog) {
        Dialog(
            onDismissRequest = {
                showSuccessDialog = false
                onBack()
            }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Surface(
                            modifier = Modifier.size(92.dp),
                            shape = CircleShape,
                            color = Color(0xFFDCFCE7)
                        ) {}
                        Surface(
                            modifier = Modifier.size(68.dp),
                            shape = CircleShape,
                            color = Color(0xFF059669)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = stringResource(R.string.request_sent),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A),
                        textAlign = TextAlign.Center
                    )

                    val annotatedMessage = buildAnnotatedString {
                        append(stringResource(R.string.thank_you_for_reach_out))
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))) {
                            append(stringResource(R.string.our_team_will_contact_you))
                        }
                        append(stringResource(R.string.by_whatsapp_or_email))
                    }

                    Text(
                        text = annotatedMessage,
                        fontSize = 14.sp,
                        color = Color(0xFF475569),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFFEEF2FF)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Outlined.AccessTime,
                                        contentDescription = null,
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = stringResource(R.string.expected_reply),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = stringResource(R.string.within_24_hours),
                                    fontSize = 13.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = {
                            showSuccessDialog = false
                            onBack()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8))
                    ) {
                        Text(
                            text = stringResource(R.string.done),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8FAFC),
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Button(
                        onClick = {
                            hasAttemptedSubmit = true
                            if (selectedCategory.isBlank() || customerName.isBlank() || customerEmail.isBlank() || customerPhone.isBlank() || customerQuestion.isBlank()) {
                                Toast.makeText(context, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            scope.launch {
                                isSubmitting = true
                                try {
                                    val req = HelpRequest(
                                        id = System.currentTimeMillis().toString(),
                                        category = selectedCategory,
                                        question = customerQuestion,
                                        customerName = customerName,
                                        customerEmail = customerEmail,
                                        customerPhone = "$selectedCountryCode $customerPhone".trim(),
                                        tripPackage = selectedTrip,
                                        timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
                                        status = "Pending",
                                        customerUid = loggedInUser ?: ""
                                    )
                                    val success = firebaseRepo?.saveHelpRequest(req) ?: false
                                    isSubmitting = false
                                    if (success) {
                                        showSuccessDialog = true
                                    } else {
                                        Toast.makeText(context, "Failed to submit help request. Please check Firestore security rules or connection.", Toast.LENGTH_LONG).show()
                                    }
                                } catch (e: Exception) {
                                    isSubmitting = false
                                    Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D5BD8)),
                        enabled = !isSubmitting
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.NearMe,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isSubmitting) stringResource(R.string.submitting) else stringResource(R.string.send_question),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8FAFC))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Top Navigation Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    onClick = onBack,
                    shape = CircleShape,
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shadowElevation = 2.dp,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = stringResource(R.string.ask_us_a_question),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }

            // Card 1: Question Details Category & Trip
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // What is it about?
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = stringResource(R.string.what_is_it_about),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val categories = listOf(stringResource(R.string.trip), stringResource(R.string.bookings), stringResource(R.string.payment), stringResource(R.string.something_else))
                            categories.forEach { cat ->
                                val isSelected = selectedCategory == cat
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSelected) Color(0xFFEFF6FF) else Color.White,
                                    border = BorderStroke(
                                        1.dp,
                                        if (categoryError && !isSelected) MaterialTheme.colorScheme.error
                                        else if (isSelected) Color(0xFF1D5BD8) 
                                        else Color(0xFFE2E8F0)
                                    ),
                                    modifier = Modifier.clickable { selectedCategory = cat }
                                ) {
                                    Text(
                                        text = cat,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected) Color(0xFF1D5BD8) else Color(0xFF334155),
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                                    )
                                }
                            }
                        }
                        if (categoryError) {
                            Text(
                                text = "Please select a category",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                            )
                        }
                    }

                    // Which trip? (optional)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(R.string.which_trip),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.optional),
                                fontSize = 13.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        Box(modifier = Modifier.fillMaxWidth()) {
                            Surface(
                                onClick = { tripDropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFF1D5BD8))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = Color(0xFF1D5BD8),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = if (selectedTrip.isBlank()) stringResource(R.string.not_about_trip) else selectedTrip,
                                            fontSize = 15.sp,
                                            color = Color(0xFF1D5BD8),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = Color(0xFF1D5BD8)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = tripDropdownExpanded,
                                onDismissRequest = { tripDropdownExpanded = false },
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .heightIn(max = 280.dp)
                                    .background(Color.White)
                            ) {
                                val scrollState = rememberScrollState()
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 280.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .verticalScroll(scrollState)
                                            .drawWithContent {
                                                drawContent()
                                                val needScrollbar = scrollState.maxValue > 0
                                                if (needScrollbar) {
                                                    val totalHeight = (scrollState.maxValue + size.height).coerceAtLeast(1f)
                                                    val scrollbarHeight = (size.height / totalHeight * size.height).coerceAtLeast(36.dp.toPx())
                                                    val scrollbarOffsetY = (scrollState.value.toFloat() / scrollState.maxValue.toFloat()) * (size.height - scrollbarHeight)

                                                    drawRoundRect(
                                                        color = Color(0xFF94A3B8),
                                                        topLeft = Offset(size.width - 6.dp.toPx(), scrollbarOffsetY),
                                                        size = Size(4.dp.toPx(), scrollbarHeight),
                                                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                                                        alpha = 0.8f
                                                    )
                                                }
                                            }
                                    ) {
                                        tripOptions.forEach { trip ->
                                            val isSelected = (selectedTrip == trip) || (selectedTrip.isBlank())
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = trip,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSelected) PrimaryBlue else Color(0xFF0F172A)
                                                    )
                                                },
                                                onClick = {
                                                    selectedTrip = trip
                                                    tripDropdownExpanded = false
                                                },
                                                modifier = Modifier.background(
                                                    if (isSelected) Color(0xFFEFF6FF) else Color.Transparent
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Card 2: Your Contact Details
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.your_contact_details),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.so_our_team_knows_how_to_contact_you),
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    // Your Name
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(R.string.your_name),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        OutlinedTextField(
                            value = customerName,
                            onValueChange = { customerName = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            isError = nameError,
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF1D5BD8))
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF1D5BD8),
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                errorBorderColor = MaterialTheme.colorScheme.error,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                errorContainerColor = Color.White
                            ),
                            singleLine = true,
                            placeholder = { Text("e.g: John Doe") }
                        )
                        if (nameError) {
                            Text(
                                text = "Please enter your name",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                            )
                        }
                    }

                    // Email
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Email",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        OutlinedTextField(
                            value = customerEmail,
                            onValueChange = { customerEmail = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            isError = emailError,
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF1D5BD8))
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF1D5BD8),
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                errorBorderColor = MaterialTheme.colorScheme.error,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                errorContainerColor = Color.White
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            placeholder = { Text("e.g: test123@gmail.com") }
                        )
                        if (emailError) {
                            Text(
                                text = "Please enter your email",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                            )
                        }
                    }

                    // WhatsApp Number
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(R.string.whatsapp_number),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Country code selector box
                            Box {
                                Surface(
                                    onClick = { countryDropdownExpanded = true },
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier.height(56.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(text = selectedCountryFlag, fontSize = 18.sp)
                                        Text(
                                            text = selectedCountryCode,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                            tint = Color(0xFF64748B),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = countryDropdownExpanded,
                                    onDismissRequest = { countryDropdownExpanded = false },
                                    modifier = Modifier.background(Color.White)
                                ) {
                                    countryOptions.forEach { (flag, code) ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(text = flag, fontSize = 18.sp)
                                                    Text(text = code, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                                }
                                            },
                                            onClick = {
                                                selectedCountryFlag = flag
                                                selectedCountryCode = code
                                                countryDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Phone input field
                            OutlinedTextField(
                                value = customerPhone,
                                onValueChange = { customerPhone = it },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                isError = phoneError,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF1D5BD8),
                                    unfocusedBorderColor = Color(0xFFE2E8F0),
                                    errorBorderColor = MaterialTheme.colorScheme.error,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    errorContainerColor = Color.White
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                placeholder = { Text("123456789") }
                            )
                        }
                        if (phoneError) {
                            Text(
                                text = "Please enter your WhatsApp number",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.we_can_reply_to_you_on_this_number),
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }

            // Card 3: Your Question
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.your_question),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    OutlinedTextField(
                        value = customerQuestion,
                        onValueChange = {
                            if (it.length <= 500) customerQuestion = it
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        shape = RoundedCornerShape(14.dp),
                        isError = questionError,
                        placeholder = { Text(stringResource(R.string.type_your_question_here), color = Color(0xFF94A3B8)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1D5BD8),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            errorBorderColor = MaterialTheme.colorScheme.error,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            errorContainerColor = Color.White
                        )
                    )
                    if (questionError) {
                        Text(
                            text = "Please enter your question",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${customerQuestion.length} / 500",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            // Info Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFEEF2FF)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = null,
                        tint = Color(0xFF1D5BD8),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(R.string.we_will_reply_within_24_hours_by_whatsapp_or_email),
                        fontSize = 13.sp,
                        color = Color(0xFF1E40AF),
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
