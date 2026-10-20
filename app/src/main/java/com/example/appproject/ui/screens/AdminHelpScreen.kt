package com.example.appproject.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appproject.R
import com.example.appproject.data.model.HelpRequest
import com.example.appproject.data.repository.FirebaseRepository
import com.example.appproject.ui.components.HelpRequestCardSkeleton
import com.example.appproject.ui.theme.PendingAmber
import com.example.appproject.ui.theme.PendingContainer
import com.example.appproject.ui.theme.PrimaryBlue
import kotlinx.coroutines.launch

@Suppress("UNUSED_PARAMETER")
@Composable
fun AdminHelpScreen(
    firebaseRepo: FirebaseRepository,
    onBack: (() -> Unit)? = null,
    onNavigateToDetail: (String) -> Unit
) {
    var helpRequests by remember { mutableStateOf<List<HelpRequest>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var filterStatus by rememberSaveable { mutableStateOf("All") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isLoading = true
        helpRequests = firebaseRepo.getAllHelpRequests()
        isLoading = false
    }

    val filteredList = helpRequests.filter { req ->
        val matchesSearch = searchQuery.isBlank() ||
                req.customerName.contains(searchQuery, ignoreCase = true) ||
                req.tripPackage.contains(searchQuery, ignoreCase = true) ||
                req.question.contains(searchQuery, ignoreCase = true) ||
                req.category.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (filterStatus) {
            "All" -> true
            "Pending" -> req.status.equals("Pending", ignoreCase = true) || req.status.equals("Still waiting", ignoreCase = true)
            "Working on it" -> req.status.equals("Working on it", ignoreCase = true)
            "Resolved" -> req.status.equals("Resolved", ignoreCase = true) || req.status.equals("All done", ignoreCase = true)
            else -> true
        }

        matchesSearch && matchesFilter
    }

    val totalCount = helpRequests.size
    val pendingCount = helpRequests.count { it.status.equals("Pending", ignoreCase = true) || it.status.equals("Still waiting", ignoreCase = true) }
    val workingCount = helpRequests.count { it.status.equals("Working on it", ignoreCase = true) }
    val resolvedCount = helpRequests.count { it.status.equals("Resolved", ignoreCase = true) || it.status.equals("All done", ignoreCase = true) }

    val filterOptions = listOf(
        Pair("All", "${stringResource(R.string.all)} ($totalCount)"),
        Pair("Pending", "${stringResource(R.string.pending)} ($pendingCount)"),
        Pair("Working on it", "${stringResource(R.string.working_on_it)} ($workingCount)"),
        Pair("Resolved", "${stringResource(R.string.resolved)} ($resolvedCount)")
    )

    Scaffold(
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.help_and_support),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.questions_sent_by_customers_from_the_help_screen),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B)
                    )
                }
            }

            // Search bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.search_by_name_trip_or_question), color = Color(0xFF94A3B8)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF64748B))
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        disabledContainerColor = Color.White,
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = Color(0xFFE2E8F0)
                    ),
                    singleLine = true
                )
            }

            // Filter Pills Row
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

            // List of Help Requests
            if (isLoading) {
                items(3) {
                    HelpRequestCardSkeleton()
                    Spacer(modifier = Modifier.height(12.dp))
                }
            } else if (filteredList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = stringResource(R.string.no_help_request_found), fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = stringResource(R.string.try_adjusting_your_search_query), color = Color(0xFF64748B), fontSize = 13.sp)
                        }
                    }
                }
            } else {
                items(filteredList) { request ->
                    HelpRequestCard(
                        request = request,
                        onClick = { onNavigateToDetail(request.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun HelpRequestCard(
    request: HelpRequest,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
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
                Text(
                    text = request.category,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B)
                )
                if (request.status.isNotBlank()) {
                    val isPending = request.status.equals("Pending", ignoreCase = true) ||
                                    request.status.equals("Still waiting", ignoreCase = true) ||
                                    request.status.equals(context.getString(R.string.pending), ignoreCase = true) ||
                                    request.status.equals(context.getString(R.string.still_waiting), ignoreCase = true)
                    val isWorking = request.status.equals("Working on it", ignoreCase = true) ||
                                    request.status.equals(context.getString(R.string.working_on_it), ignoreCase = true)

                    val statusText = when {
                        isPending -> stringResource(R.string.waiting_for_reply)
                        isWorking -> stringResource(R.string.working_on_it)
                        request.status.equals("Resolved", ignoreCase = true) ||
                        request.status.equals("All done", ignoreCase = true) ||
                        request.status.equals(context.getString(R.string.resolved), ignoreCase = true) ||
                        request.status.equals(context.getString(R.string.all_done), ignoreCase = true) -> stringResource(R.string.all_done)
                        else -> request.status
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isPending) PendingContainer else Color(0xFFF0FDF4)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        color = if (isPending) PendingAmber else Color(0xFF16A34A),
                                        shape = CircleShape
                                    )
                            )
                            Text(
                                text = statusText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPending) PendingAmber else Color(0xFF16A34A)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = request.question,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(15.dp))
                    Text(text = request.customerName, fontSize = 13.sp, color = Color(0xFF475569))
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(15.dp))
                    Text(text = request.tripPackage, fontSize = 13.sp, color = Color(0xFF475569))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = request.timestamp,
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.view_and_reply),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AdminHelpDetailScreen(
    requestId: String,
    firebaseRepo: FirebaseRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var request by remember { mutableStateOf<HelpRequest?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var replyChannel by remember { mutableStateOf("WhatsApp") }
    var replyText by remember { mutableStateOf("") }
    var selectedNextStatus by remember { mutableStateOf("Working on it") }
    val scope = rememberCoroutineScope()

    val updateStatus: (String) -> Unit = { newStatus ->
        selectedNextStatus = newStatus
        val req = request
        if (req != null) {
            val mappedStatus = when (newStatus) {
                "Still waiting" -> "Pending"
                "Working on it" -> "Working on it"
                "All done" -> "Resolved"
                else -> "Pending"
            }
            scope.launch {
                try {
                    firebaseRepo.updateHelpRequestStatus(req.id, mappedStatus)
                    request = req.copy(status = mappedStatus)
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(requestId) {
        isLoading = true
        val fetched = firebaseRepo.getHelpRequest(requestId)
        request = fetched
        if (fetched != null) {
            selectedNextStatus = when {
                fetched.status.equals("Working on it", ignoreCase = true) -> "Working on it"
                fetched.status.equals("Resolved", ignoreCase = true) || fetched.status.equals("All done", ignoreCase = true) -> "All done"
                else -> "Still waiting"
            }
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            Surface(
                color = Color(0xFFF8FAFC),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            onClick = onBack,
                            shape = CircleShape,
                            color = Color.White,
                            shadowElevation = 1.dp,
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color(0xFF0F172A),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Text(
                            text = stringResource(R.string.help_and_support),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (request != null) {
                Surface(
                    color = Color.White,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        val req = request!!
                        Button(
                            onClick = {
                                val phoneClean = req.customerPhone.replace("+", "").replace(" ", "").replace("-", "")
                                if (replyChannel == "WhatsApp") {
                                    val waUrl = if (replyText.isBlank()) {
                                        "https://wa.me/$phoneClean"
                                    } else {
                                        "https://wa.me/$phoneClean?text=${Uri.encode(replyText)}"
                                    }
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(waUrl)))
                                    } catch (_: Exception) {
                                        Toast.makeText(context, R.string.could_not_open_whatsapp, Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    val emailIntent = Intent(
                                        Intent.ACTION_SENDTO,
                                        Uri.parse("mailto:${req.customerEmail}?subject=${Uri.encode("Re: ${req.question}")}&body=${Uri.encode(replyText)}")
                                    )
                                    try {
                                        context.startActivity(emailIntent)
                                    } catch (_: Exception) {
                                        Toast.makeText(context, R.string.could_not_open_email, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.send_reply_on, replyChannel),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        if (isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                HelpRequestCardSkeleton()
            }
        } else if (request == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(R.string.no_help_request_found), color = Color(0xFF64748B))
            }
        } else {
            val req = request!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // 1. Customer Request Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val initials = getInitials(req.customerName)
                            Surface(
                                shape = CircleShape,
                                color = PrimaryBlue,
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = initials,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = req.customerName.uppercase(),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = stringResource(R.string.asked_on, req.timestamp),
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFEFF6FF),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = req.question,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E40AF),
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (req.category.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Sell,
                                            contentDescription = null,
                                            tint = PrimaryBlue,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = req.category,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF1E293B)
                                        )
                                    }
                                }
                            }

                            if (req.tripPackage.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = PrimaryBlue,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = req.tripPackage,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF1E293B)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Quick Direct Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        onClick = {
                            val phoneClean = req.customerPhone.replace("+", "").replace(" ", "").replace("-", "")
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$phoneClean")))
                            } catch (_: Exception) {
                                Toast.makeText(context, R.string.could_not_open_whatsapp, Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "WhatsApp",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                        }
                    }

                    Surface(
                        onClick = {
                            try {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_SENDTO,
                                        Uri.parse("mailto:${req.customerEmail}?subject=${Uri.encode("Re: ${req.question}")}")
                                    )
                                )
                            } catch (_: Exception) {
                                Toast.makeText(context, R.string.could_not_open_email, Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.email),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                        }
                    }
                }

                // 3. Write your reply Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.write_your_reply),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.choose_how_to_reply),
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Channel toggle tabs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val isWa = replyChannel == "WhatsApp"
                            Surface(
                                onClick = { replyChannel = "WhatsApp" },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isWa) Color(0xFFEFF6FF) else Color.White,
                                border = BorderStroke(1.dp, if (isWa) PrimaryBlue else Color(0xFFE2E8F0))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Chat,
                                        contentDescription = null,
                                        tint = if (isWa) PrimaryBlue else Color(0xFF475569),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "WhatsApp",
                                        fontSize = 13.sp,
                                        fontWeight = if (isWa) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isWa) PrimaryBlue else Color(0xFF475569)
                                    )
                                }
                            }

                            val isEmail = replyChannel == "Email"
                            Surface(
                                onClick = { replyChannel = "Email" },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isEmail) Color(0xFFEFF6FF) else Color.White,
                                border = BorderStroke(1.dp, if (isEmail) PrimaryBlue else Color(0xFFE2E8F0))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        tint = if (isEmail) PrimaryBlue else Color(0xFF475569),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = stringResource(R.string.email),
                                        fontSize = 13.sp,
                                        fontWeight = if (isEmail) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isEmail) PrimaryBlue else Color(0xFF475569)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Reply Input
                        OutlinedTextField(
                            value = replyText,
                            onValueChange = { replyText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 110.dp),
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.type_your_reply_here),
                                    color = Color(0xFF94A3B8),
                                    fontSize = 14.sp
                                )
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = Color(0xFFE2E8F0)
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Quick Answers
                        Text(
                            text = stringResource(R.string.quick_answers),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val quickAnswersList = listOf(
                            stringResource(R.string.quick_answer_1),
                            stringResource(R.string.quick_answer_2),
                            stringResource(R.string.quick_answer_3)
                        )

                        quickAnswersList.forEach { answer ->
                            Surface(
                                onClick = { replyText = answer },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Text(
                                    text = answer,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = PrimaryBlue,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }
                }

                // 4. What happens next? Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.what_happens_next),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.set_status_after_reply),
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        StatusOptionCard(
                            title = stringResource(R.string.still_waiting),
                            subtitle = stringResource(R.string.not_answered_yet),
                            icon = Icons.Default.AccessTime,
                            iconBgColor = Color(0xFFFEF3C7),
                            iconTint = Color(0xFFD97706),
                            isSelected = selectedNextStatus == "Still waiting",
                            onClick = { updateStatus("Still waiting") }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        StatusOptionCard(
                            title = stringResource(R.string.working_on_it),
                            subtitle = stringResource(R.string.handling_request),
                            icon = Icons.Default.Autorenew,
                            iconBgColor = Color(0xFFE0F2FE),
                            iconTint = Color(0xFF0284C7),
                            isSelected = selectedNextStatus == "Working on it",
                            onClick = { updateStatus("Working on it") }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        StatusOptionCard(
                            title = stringResource(R.string.all_done),
                            subtitle = stringResource(R.string.customer_has_answer),
                            icon = Icons.Default.Check,
                            iconBgColor = Color(0xFFDCFCE7),
                            iconTint = Color(0xFF15803D),
                            isSelected = selectedNextStatus == "All done",
                            onClick = { updateStatus("All done") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) Color(0xFFEFF6FF) else Color.White,
        border = BorderStroke(1.dp, if (isSelected) PrimaryBlue else Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(iconBgColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .background(PrimaryBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(14.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .border(1.5.dp, Color(0xFFCBD5E1), CircleShape)
                )
            }
        }
    }
}

private fun getInitials(name: String): String {
    val parts = name.trim().split("\\s+".toRegex())
    return when {
        parts.isEmpty() || parts[0].isEmpty() -> "CU"
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
    }
}
