package com.example.appproject.ui.screens

import android.app.DatePickerDialog
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutQuad
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.window.core.layout.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.appproject.R
import com.example.appproject.data.model.Booking
import com.example.appproject.data.remote.FetchPromoTripsFromWebsite
import com.example.appproject.data.repository.FirebaseRepository
import com.example.appproject.data.model.PricingTier
import com.example.appproject.data.model.TripPromo
import com.example.appproject.data.model.User
import com.example.appproject.data.remote.getDefaultItinerariesForTrip
import com.example.appproject.data.model.getFormattedTierPrice
import com.example.appproject.data.remote.getSavedPromoTrips
import com.example.appproject.data.model.parseBookingDate
import com.example.appproject.data.remote.resolveTripImageRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import androidx.compose.ui.text.font.Font

val BebasNeue = FontFamily(
    Font(R.font.bebas_naue, FontWeight.Normal)
)

@Composable
fun PromoTripScreen(
    username: String,
    firebaseRepo: FirebaseRepository,
    onViewTripDetail: (TripPromo) -> Unit = {},
    onRequireLogin: () -> Unit = {}
) {
    val context = LocalContext.current
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val isExpanded = adaptiveInfo.windowSizeClass.windowWidthSizeClass != WindowWidthSizeClass.COMPACT

    var selectedBookingTrip by remember { mutableStateOf<TripPromo?>(null) }
    var showSignInPrompt by remember { mutableStateOf(false) }
    var trips by remember { mutableStateOf<List<TripPromo>>(emptyList()) }
    var isSyncing by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
        trips = withContext(Dispatchers.IO) { getSavedPromoTrips(context) }
        isSyncing = true
        try {
            val fresh = FetchPromoTripsFromWebsite(context)
            if (fresh.isNotEmpty()) trips = fresh
        } catch (e: Exception) {
            Log.e("PromoTripScreen", "Error auto syncing with website", e)
        } finally {
            isSyncing = false
        }
    }

    val filteredTrips = remember(trips, searchQuery) {
        trips.filter { trip ->
            searchQuery.isBlank() ||
                    trip.name.contains(searchQuery, ignoreCase = true) ||
                    trip.description.contains(searchQuery, ignoreCase = true) ||
                    trip.subtext.contains(searchQuery, ignoreCase = true) ||
                    trip.price.contains(searchQuery, ignoreCase = true)
        }
    }

    val columns = if (isExpanded) 2 else 1
    val horizontalPadding = if (isExpanded) 24.dp else 16.dp

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(800)) + slideInVertically(
                    initialOffsetY = { 40 },
                    animationSpec = tween(600, easing = EaseOutQuad)
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        AsyncImage(
                            model = R.drawable.backgroudmountain,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            alpha = 0.7f
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.45f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = stringResource(R.string.promo_trip),
                                    style = MaterialTheme.typography.displayLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = BebasNeue
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.all_travel_packages_special_offers),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = horizontalPadding),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SectionHeader(
                            title = stringResource(R.string.indonesian_travel_icons)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(R.string.promo_trip_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text(stringResource(R.string.search_promo_package)) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (filteredTrips.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.no_package_match),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }

        itemsIndexed(filteredTrips, key = { _, trip -> trip.name }) { index, trip ->
            val startPad = if (columns == 1) horizontalPadding else if (index % 2 == 0) horizontalPadding else 8.dp
            val endPad = if (columns == 1) horizontalPadding else if (index % 2 == 0) 8.dp else horizontalPadding

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = startPad, end = endPad)
            ) {
                TripCard(
                    trip = trip,
                    modifier = Modifier.fillMaxWidth(),
                    onViewDetails = { onViewTripDetail(trip) },
                    onBook = {
                        if (username.isBlank() || username == "Traveler") {
                            showSignInPrompt = true
                        } else {
                            selectedBookingTrip = trip
                        }
                    }
                )
            }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    selectedBookingTrip?.let { trip ->
        BookingDialog(
            trip = trip,
            username = username,
            firebaseRepo = firebaseRepo,
            onDismiss = { selectedBookingTrip = null }
        )
    }

    if (showSignInPrompt) {
        SignInPromptDialog(
            onDismiss = { showSignInPrompt = false },
            onSignIn = {
                showSignInPrompt = false
                onRequireLogin()
            }
        )
    }
}

@Composable
fun FacilityCard(
    title: String,
    items: List<String>,
    badgeText: String = "✓",
    isExcluded: Boolean = false
) {
    if (items.isEmpty()) return

    val containerColor = if (isExcluded) {
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
    }

    val borderColor = if (isExcluded) {
        MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    }

    val titleColor = if (isExcluded) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }

    val badgeBgColor = if (isExcluded) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.primary
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = titleColor
            )
            Spacer(modifier = Modifier.height(10.dp))
            items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        color = badgeBgColor,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = badgeText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun FacilitiesAndInclusionsSection(trip: TripPromo) {
    Text(
        text = stringResource(R.string.package_facilities_inclusions, "Package Facilities & Inclusions"),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(12.dp))

    if (trip.name.contains("Tumpak", ignoreCase = true)) {
        FacilityCard(
            title = stringResource(R.string.package_A, "Package A"),
            items = listOf(
                stringResource(R.string.inc_transport_ac_full, "Transportation (AC)"),
                stringResource(R.string.inc_tickets, "Tickets"),
                stringResource(R.string.inc_local_guide, "Local Guide"),
                stringResource(R.string.inc_mineral_water, "Mineral Water")
            ),
            badgeText = "✓"
        )

        FacilityCard(
            title = stringResource(R.string.package_B, "Package B"),
            items = listOf(
                stringResource(R.string.all_package_a, "All Package A inclusions"),
                stringResource(R.string.lunch_makan_siang, "Lunch"),
                stringResource(R.string.inc_snack_box, "Snack Box"),
                stringResource(R.string.inc_video_cinematic, "Video Cinematic")
            ),
            badgeText = "✓"
        )

        FacilityCard(
            title = stringResource(R.string.included, "INCLUDED"),
            items = listOf(
                stringResource(R.string.inc_transport_ac, "Transport AC"),
                stringResource(R.string.inc_driver_bbm, "Driver + BBM"),
                stringResource(R.string.inc_tickets, "Tickets"),
                stringResource(R.string.inc_local_guide, "Local Guide"),
                stringResource(R.string.inc_mineral_water, "Mineral Water"),
                stringResource(R.string.inc_lunch_pack_b, "Lunch (Pack B)"),
                stringResource(R.string.inc_documentation, "Documentation"),
                stringResource(R.string.inc_first_aid, "First Aid")
            ),
            badgeText = "✓"
        )

        if (trip.exclusions.isNotEmpty()) {
            FacilityCard(
                title = stringResource(R.string.excluded, "EXCLUDED"),
                items = trip.exclusions,
                badgeText = "✕",
                isExcluded = true
            )
        }
    } else {
        val effectiveInclusions = trip.inclusions.ifEmpty {
            emptyList()
        }

        val effectiveExclusions = trip.exclusions.ifEmpty {
            emptyList()
        }

        FacilityCard(
            title = stringResource(R.string.included, "INCLUDED"),
            items = effectiveInclusions,
            badgeText = "✓"
        )

        FacilityCard(
            title = stringResource(R.string.excluded, "EXCLUDED"),
            items = effectiveExclusions,
            badgeText = "✕",
            isExcluded = true
        )
    }
}

@Composable
private fun SignInPromptDialog(
    onDismiss: () -> Unit,
    onSignIn: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {},
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(modifier = Modifier.height(4.dp))

                    // Icon with decorative accents
                    Box(
                        modifier = Modifier.size(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(80.dp)) {
                            val strokeWidth = 2.dp.toPx()
                            val color = Color(0xFF0066FF).copy(alpha = 0.7f)
                            // Left dashes
                            drawLine(color, start = Offset(4.dp.toPx(), 28.dp.toPx()), end = Offset(12.dp.toPx(), 28.dp.toPx()), strokeWidth = strokeWidth)
                            drawLine(color, start = Offset(4.dp.toPx(), 52.dp.toPx()), end = Offset(12.dp.toPx(), 52.dp.toPx()), strokeWidth = strokeWidth)
                            // Right dashes
                            drawLine(color, start = Offset(68.dp.toPx(), 28.dp.toPx()), end = Offset(76.dp.toPx(), 28.dp.toPx()), strokeWidth = strokeWidth)
                            drawLine(color, start = Offset(68.dp.toPx(), 52.dp.toPx()), end = Offset(76.dp.toPx(), 52.dp.toPx()), strokeWidth = strokeWidth)
                            // Top dashes
                            drawLine(color, start = Offset(28.dp.toPx(), 4.dp.toPx()), end = Offset(28.dp.toPx(), 12.dp.toPx()), strokeWidth = strokeWidth)
                            drawLine(color, start = Offset(52.dp.toPx(), 4.dp.toPx()), end = Offset(52.dp.toPx(), 12.dp.toPx()), strokeWidth = strokeWidth)
                        }

                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE3F2FD)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Login,
                                contentDescription = null,
                                tint = Color(0xFF0066FF),
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    // Title
                    Text(
                        text = stringResource(R.string.login_required_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        textAlign = TextAlign.Center
                    )

                    // Message
                    Text(
                        text = stringResource(R.string.login_required_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = CircleShape,
                            border = BorderStroke(1.dp, Color(0xFF0066FF)),
                            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF0066FF)
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.cancel, "Batal"),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                        }

                        Button(
                            onClick = onSignIn,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = CircleShape,
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0066FF)
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.login, "Log Masuk"),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

