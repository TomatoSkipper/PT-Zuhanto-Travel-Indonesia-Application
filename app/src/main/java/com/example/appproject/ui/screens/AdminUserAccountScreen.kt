package com.example.appproject.ui.screens

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.appproject.R
import com.example.appproject.data.model.User
import com.example.appproject.data.repository.FirebaseRepository
import com.example.appproject.ui.components.AdminListItemSkeleton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminUsersScreen(firebaseRepo: FirebaseRepository) {
    val context = LocalContext.current
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var searchKey by rememberSaveable { mutableStateOf("") }
    var userPendingAction by remember { mutableStateOf<User?>(null) }
    var pendingAction by remember { mutableStateOf("") }
    var connectionStatus by remember { mutableStateOf(context.getString(R.string.testing)) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        isLoading = true
        connectionStatus = firebaseRepo.testConnection()
        users = firebaseRepo.getAllUsers()
        isLoading = false
    }

    val filteredUsers = users.filter { user ->
        val key = searchKey.trim()
        key.isEmpty() ||
                (user.id.toString()).contains(key, ignoreCase = true) ||
                (user.username).contains(key, ignoreCase = true) ||
                (user.email).contains(key, ignoreCase = true) ||
                (user.phone).contains(key, ignoreCase = true) ||
                (user.name).contains(key, ignoreCase = true)
    }
    val activeUsers = users.count { !it.isBanned }
    val bannedUsers = users.count { it.isBanned }

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
            // Screen Title Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.user_accounts),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            fontSize = 22.sp
                        )
                    }
                }
            }

            // Stat Summary KPI Cards
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiStatCard(
                        modifier = Modifier.width(130.dp),
                        title = stringResource(R.string.total),
                        count = users.size,
                        subtitle = stringResource(R.string.user_accounts),
                        icon = Icons.Default.Group,
                        containerColor = Color(0xFFF0F7FF),
                        borderColor = Color(0xFFDBEAFE),
                        iconBgColor = Color(0xFFE0F2FE),
                        accentColor = Color(0xFF0284C7)
                    )
                    KpiStatCard(
                        modifier = Modifier.width(130.dp),
                        title = stringResource(R.string.active),
                        count = activeUsers,
                        subtitle = stringResource(R.string.user_accounts),
                        icon = Icons.Default.Group,
                        containerColor = Color(0xFFF0FDF4),
                        borderColor = Color(0xFFDCFCE7),
                        iconBgColor = Color(0xFFDCFCE7),
                        accentColor = Color(0xFF16A34A)
                    )
                    KpiStatCard(
                        modifier = Modifier.width(130.dp),
                        title = stringResource(R.string.banned),
                        count = bannedUsers,
                        subtitle = stringResource(R.string.user_accounts),
                        icon = Icons.Default.Block,
                        containerColor = Color(0xFFFEF2F2),
                        borderColor = Color(0xFFFEE2E2),
                        iconBgColor = Color(0xFFFEE2E2),
                        accentColor = Color(0xFFDC2626)
                    )
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchKey,
                    onValueChange = { searchKey = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.find_using_id_username_email_or_phone),
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchKey.isNotEmpty()) {
                            IconButton(onClick = { searchKey = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
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
                        focusedBorderColor = Color(0xFF0284C7)
                    )
                )
            }

            // User Accounts Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE0F2FE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = stringResource(R.string.user_accounts_list_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // User Accounts Cards List
            if (isLoading) {
                items(4) {
                    AdminListItemSkeleton(modifier = Modifier.padding(bottom = 8.dp))
                }
            } else if (filteredUsers.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp)
                                .heightIn(min = 140.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = Color(0xFF94A3B8)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (connectionStatus != "Connected") stringResource(R.string.sync_failed, connectionStatus)
                                    else if (users.isEmpty()) stringResource(R.string.no_user_accounts_found)
                                    else stringResource(R.string.no_matching_user_accounts),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
            } else {
                items(filteredUsers, key = { it.uid.ifBlank { "${it.id}_${it.username}" } }) { user ->
                    UserAccountCard(
                        user = user,
                        onBanOrUnban = {
                            userPendingAction = user
                            pendingAction = if (user.isBanned) context.getString(R.string.unban) else context.getString(R.string.ban)
                        },
                        onDelete = {
                            userPendingAction = user
                            pendingAction = context.getString(R.string.delete)
                        }
                    )
                }
            }
        }
    }

    userPendingAction?.let { user ->
        when (pendingAction) {
            context.getString(R.string.ban) -> {
                BanAccountDialog(
                    user = user,
                    onDismiss = { userPendingAction = null },
                    onConfirmBan = { reason ->
                        scope.launch {
                            val currentDate = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH).format(Date())
                            val updatedUser = user.copy(
                                isBanned = true,
                                bannedReason = reason,
                                bannedDate = currentDate
                            )
                            firebaseRepo.updateUser(updatedUser)
                            users = users.map { if (it.uid == updatedUser.uid) updatedUser else it }
                            userPendingAction = null
                        }
                    }
                )
            }
            context.getString(R.string.unban) -> {
                UnbanAccountDialog(
                    user = user,
                    onDismiss = { userPendingAction = null },
                    onConfirmUnban = {
                        scope.launch {
                            val updatedUser = user.copy(
                                isBanned = false,
                                bannedReason = "",
                                bannedDate = ""
                            )
                            firebaseRepo.updateUser(updatedUser)
                            users = users.map { if (it.uid == updatedUser.uid) updatedUser else it }
                            userPendingAction = null
                        }
                    }
                )
            }
            context.getString(R.string.delete) -> {
                DeleteAccountDialog(
                    user = user,
                    onDismiss = { userPendingAction = null },
                    onConfirmDelete = {
                        scope.launch {
                            firebaseRepo.deleteUser(user.uid.ifBlank { user.id.toString() })
                            users = users.filterNot { it.uid == user.uid && it.id == user.id }
                            userPendingAction = null
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun BanAccountDialog(
    user: User,
    onDismiss: () -> Unit,
    onConfirmBan: (reason: String) -> Unit
) {
    var banReason by remember { mutableStateOf("") }

    val nameString = user.name.ifBlank { user.username }.ifBlank { "User" }
    val initials = remember(nameString) {
        val parts = nameString.trim().split("\\s+".toRegex())
        if (parts.size >= 2 && parts[0].isNotEmpty() && parts[1].isNotEmpty()) {
            "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
        } else {
            nameString.take(2).uppercase()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Badge & Category Text
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFEDD5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = null,
                            tint = Color(0xFFC2410C),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.dialog_ban_account_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A),
                        fontSize = 16.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                // Title & Description
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.dialog_ban_this_account_q),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        fontSize = 20.sp
                    )
                    Text(
                        text = stringResource(R.string.dialog_ban_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF64748B),
                        fontSize = 13.sp
                    )
                }

                // User Profile Preview Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1D4ED8)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = nameString.uppercase(),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                fontSize = 14.sp
                            )
                            if (user.email.isNotBlank()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = user.email,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF475569),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            if (user.phone.isNotBlank()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = user.phone,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF475569),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Reason Input Section
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.dialog_reason_label),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0F172A),
                        fontSize = 13.sp
                    )
                    OutlinedTextField(
                        value = banReason,
                        onValueChange = { banReason = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 90.dp),
                        placeholder = {
                            Text(
                                text = stringResource(R.string.dialog_reason_placeholder),
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White,
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedBorderColor = Color(0xFF0284C7)
                        )
                    )
                }

                // Action Buttons
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { onConfirmBan(banReason) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC2410C))
                    ) {
                        Text(
                            text = stringResource(R.string.dialog_ban_account_btn),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                    ) {
                        Text(
                            text = stringResource(R.string.dialog_cancel_btn),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0284C7)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteAccountDialog(
    user: User,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    var isConfirmed by remember { mutableStateOf(false) }

    val nameString = user.name.ifBlank { user.username }.ifBlank { "User" }
    val initials = remember(nameString) {
        val parts = nameString.trim().split("\\s+".toRegex())
        if (parts.size >= 2 && parts[0].isNotEmpty() && parts[1].isNotEmpty()) {
            "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
        } else {
            nameString.take(2).uppercase()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Badge & Category Text
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEE2E2)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.dialog_delete_account_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A),
                        fontSize = 16.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                // Title & Description
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.dialog_delete_this_account_q),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        fontSize = 20.sp
                    )
                    Text(
                        text = stringResource(R.string.dialog_delete_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF64748B),
                        fontSize = 13.sp
                    )
                }

                // User Profile Preview Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1D4ED8)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = nameString.uppercase(),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                fontSize = 14.sp
                            )
                            if (user.email.isNotBlank()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = user.email,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF475569),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            if (user.phone.isNotBlank()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = user.phone,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF475569),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Checkbox Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.clickable { isConfirmed = !isConfirmed }
                ) {
                    Checkbox(
                        checked = isConfirmed,
                        onCheckedChange = { isConfirmed = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFFDC2626))
                    )
                    Text(
                        text = stringResource(R.string.dialog_understand_cannot_be_undone),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF0F172A),
                        fontSize = 13.sp
                    )
                }

                // Action Buttons
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onConfirmDelete,
                        enabled = isConfirmed,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF4444),
                            disabledContainerColor = Color(0xFFFCA5A5)
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.dialog_delete_account_btn),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                    ) {
                        Text(
                            text = stringResource(R.string.dialog_keep_account_btn),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0284C7)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UnbanAccountDialog(
    user: User,
    onDismiss: () -> Unit,
    onConfirmUnban: () -> Unit
) {
    val nameString = user.name.ifBlank { user.username }.ifBlank { "User" }
    val initials = remember(nameString) {
        val parts = nameString.trim().split("\\s+".toRegex())
        if (parts.size >= 2 && parts[0].isNotEmpty() && parts[1].isNotEmpty()) {
            "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
        } else {
            nameString.take(2).uppercase()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Badge & Close Button Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEE2E2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Text(
                            text = stringResource(R.string.dialog_unban_account_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A),
                            fontSize = 18.sp
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF64748B)
                        )
                    }
                }

                // Title & Description
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.dialog_unban_this_account_q),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        fontSize = 16.sp
                    )
                    Text(
                        text = stringResource(R.string.dialog_unban_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF64748B),
                        fontSize = 13.sp
                    )
                }

                // User Profile Preview Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1D4ED8)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = nameString.uppercase(),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                fontSize = 14.sp
                            )
                            if (user.email.isNotBlank()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = user.email,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF475569),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                            if (user.phone.isNotBlank()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = user.phone,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF475569),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Alasan Pemblokiran Display Box
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEF2F2),
                    border = BorderStroke(1.dp, Color(0xFFFEE2E2))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Block,
                                contentDescription = null,
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = stringResource(R.string.dialog_banned_reason_label),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626),
                                fontSize = 13.sp
                            )
                        }
                        Text(
                            text = user.bannedReason.ifBlank { stringResource(R.string.no_reason) },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF334155),
                            fontSize = 12.sp
                        )
                    }
                }

                // Tanggal Diblokir Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.dialog_banned_date_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF64748B),
                            fontSize = 11.sp
                        )
                        Text(
                            text = user.bannedDate.ifBlank { "12 Okt 2026, 10:24 AM" },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            fontSize = 13.sp
                        )
                    }
                }

                // Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                    ) {
                        Text(
                            text = stringResource(R.string.dialog_cancel_btn),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0284C7)
                        )
                    }

                    Button(
                        onClick = onConfirmUnban,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = stringResource(R.string.dialog_unban_account_btn),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KpiStatCard(
    modifier: Modifier = Modifier,
    title: String,
    count: Int,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    borderColor: Color,
    iconBgColor: Color,
    accentColor: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$count",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0F172A),
                fontSize = 24.sp,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF64748B),
                fontSize = 11.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
fun UserAccountCard(
    user: User,
    onBanOrUnban: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val nameString = user.name.ifBlank { user.username }.ifBlank { "User" }
    val initials = remember(nameString) {
        val parts = nameString.trim().split("\\s+".toRegex())
        if (parts.size >= 2 && parts[0].isNotEmpty() && parts[1].isNotEmpty()) {
            "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
        } else {
            nameString.take(2).uppercase()
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row: Avatar + Name/ID + Status Badge + 3-dots Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3B82F6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }

                    Column {
                        Text(
                            text = nameString,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            fontSize = 14.sp,
                            maxLines = 1
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Status Badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (user.isBanned) Color(0xFFFEE2E2) else Color(0xFFDCFCE7)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (user.isBanned) Color(0xFFDC2626) else Color(0xFF16A34A))
                            )
                            Text(
                                text = if (user.isBanned) stringResource(R.string.banned) else stringResource(R.string.active),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (user.isBanned) Color(0xFFB91C1C) else Color(0xFF15803D),
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Action Menu
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9))
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Actions",
                                tint = Color(0xFF475569),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = Color(0xFF334155),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            if (user.isBanned) stringResource(R.string.unban) else stringResource(R.string.ban),
                                            color = Color(0xFF334155),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    onBanOrUnban()
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = Color(0xFFE11D48),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            stringResource(R.string.delete),
                                            color = Color(0xFFE11D48),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

            // Details Row: Email and Phone
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (user.email.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF334155),
                            fontSize = 13.sp
                        )
                    }
                }
                if (user.phone.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = user.phone,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF334155),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
