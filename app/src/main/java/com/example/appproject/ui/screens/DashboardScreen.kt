package com.example.appproject.ui.screens

import android.content.Intent
import android.net.Uri
import android.view.MotionEvent
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Festival
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Hiking
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.SafetyCheck
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import com.example.appproject.data.model.TripPromo
import com.example.appproject.data.remote.FetchPromoTripsFromWebsite
import com.example.appproject.data.remote.getSavedPromoTrips
import androidx.compose.runtime.Composable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.window.core.layout.WindowWidthSizeClass
import coil.compose.AsyncImage
import com.example.appproject.R
import com.example.appproject.data.model.Feature
import com.example.appproject.data.model.GuestStory
import com.example.appproject.data.model.User
import com.example.appproject.data.repository.FirebaseRepository
import com.example.appproject.ui.theme.PrimaryBlue
import com.example.appproject.ui.theme.PrimaryBlueDark
import com.example.appproject.ui.theme.WarmAmberGold
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Brands
import compose.icons.fontawesomeicons.brands.Facebook
import compose.icons.fontawesomeicons.brands.Instagram
import compose.icons.fontawesomeicons.brands.Tiktok
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

fun height(dp: Dp): Modifier = Modifier.height(dp)
fun width(dp: Dp): Modifier = Modifier.width(dp)
fun size(dp: Dp): Modifier = Modifier.size(dp)
fun padding(all: Dp): Modifier = Modifier.padding(all)
fun padding(horizontal: Dp = 0.dp, vertical: Dp = 0.dp): Modifier = Modifier.padding(horizontal = horizontal, vertical = vertical)
fun padding(start: Dp = 0.dp, top: Dp = 0.dp, end: Dp = 0.dp, bottom: Dp = 0.dp): Modifier = Modifier.padding(start = start, top = top, end = end, bottom = bottom)

@Composable
fun DashboardHomeScreen(
    username: String,
    firebaseRepo: FirebaseRepository,
    onBookNow: () -> Unit = {}
) {
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val columnCount = when (adaptiveInfo.windowSizeClass.windowWidthSizeClass) {
        WindowWidthSizeClass.COMPACT -> 1
        WindowWidthSizeClass.MEDIUM -> 2
        WindowWidthSizeClass.EXPANDED -> 3
        else -> 1
    }

    var user by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(username) {
        user = firebaseRepo.getUser(username)
    }

    val displayName = remember(user, username) {
        user?.username?.ifBlank { user?.name }?.takeIf { it.isNotBlank() }
            ?: username.takeIf { it.isNotBlank() && it.length < 20 }
            ?: "Traveler"
    }

    val scrollState = rememberScrollState()
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(
                initialOffsetY = { 30 },
                animationSpec = tween(1000, easing = EaseOutQuad)
            )
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Immersive Travel Hero Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (columnCount > 1) 380.dp else 300.dp)
                ) {
                    AsyncImage(
                        model = R.drawable.background_dashboard,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.2f),
                                        Color.Black.copy(alpha = 0.5f),
                                        PrimaryBlueDark.copy(alpha = 0.95f)
                                    ),
                                    startY = 0f
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(if (columnCount > 1) 40.dp else 20.dp),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Explore,
                                    contentDescription = null,
                                    tint = WarmAmberGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.indonesia_premier_travel),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        Text(
                            text = stringResource(R.string.welcome_user, displayName),
                            style = if (columnCount > 1) MaterialTheme.typography.displayMedium else MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = stringResource(R.string.company_slogan),
                            style = MaterialTheme.typography.titleMedium,
                            color = WarmAmberGold,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Modern Interactive Search Card
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onBookNow() },
                            shape = RoundedCornerShape(18.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.35f)),
                            shadowElevation = 8.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(R.string.where_would_you_like_to_go),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = stringResource(R.string.search_destination),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = PrimaryBlue,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Place,
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

                Spacer(modifier = height(20.dp))

                // Modern Category Chips Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (columnCount > 1) 32.dp else 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val iconSize = if (columnCount > 1) 64.dp else 52.dp
                    CategoryItem(icon = Icons.Default.Hiking, label = stringResource(R.string.category_adventure), iconSize = iconSize)
                    CategoryItem(icon = Icons.Default.BeachAccess, label = stringResource(R.string.category_beaches), iconSize = iconSize)
                    CategoryItem(icon = Icons.Default.Festival, label = stringResource(R.string.category_cultural), iconSize = iconSize)
                    CategoryItem(icon = Icons.Default.Forest, label = stringResource(R.string.category_nature), iconSize = iconSize)
                }

                Spacer(modifier = height(28.dp))

                // Featured Destinations Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (columnCount > 1) 48.dp else 20.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    SectionHeader(title = stringResource(R.string.explore_indonesia))
                }

                Spacer(modifier = height(16.dp))

                // Hero Carousel
                val images: List<Int> = listOf(
                    R.drawable.background_dashboard,
                    R.drawable.backgroudmountain,
                    R.drawable.borobudurtemple,
                    R.drawable.ijencrater,
                    R.drawable.labuanbajo,
                    R.drawable.tumpak_sewu
                )
                val autoScrollDelayMs: Long = 6000L
                val size = images.size
                val initialPage = Int.MAX_VALUE / 2 - ((Int.MAX_VALUE / 2) % size)
                val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { Int.MAX_VALUE })

                LaunchedEffect(pagerState) {
                    while (true) {
                        delay(autoScrollDelayMs)
                        if (!pagerState.isScrollInProgress) {
                            pagerState.animateScrollToPage(
                                pagerState.currentPage + 1,
                                animationSpec = tween(durationMillis = 1000, easing = EaseOutQuad)
                            )
                        }
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (columnCount > 1) 360.dp else 220.dp),
                    contentPadding = PaddingValues(horizontal = if (columnCount > 1) 100.dp else 20.dp),
                    pageSpacing = 16.dp
                ) { page ->
                    val actualIndex = page % size
                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                val pageOffset = (
                                    (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                                ).absoluteValue
                                alpha = lerp(
                                    start = 0.6f,
                                    stop = 1f,
                                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                )
                                scaleX = lerp(
                                    start = 0.88f,
                                    stop = 1f,
                                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                )
                                scaleY = scaleX
                            },
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.35f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        AsyncImage(
                            model = images[actualIndex],
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = height(12.dp))

                // Pager Indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(size) { iteration ->
                        val color = if (pagerState.currentPage % size == iteration)
                            PrimaryBlue
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                        val indicatorWidth = if (pagerState.currentPage % size == iteration) 24.dp else 8.dp
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .height(8.dp)
                                .width(indicatorWidth)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }

                Spacer(modifier = height(32.dp))

                // Introduction Section Card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (columnCount > 1) 48.dp else 20.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 3.dp
                ) {
                    if (columnCount > 1) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.redefining_travel),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryBlue
                                )
                                Text(
                                    text = stringResource(R.string.affordable_price),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Spacer(modifier = width(32.dp))
                            Text(
                                text = stringResource(R.string.company_intro),
                                modifier = Modifier.weight(1.8f),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 26.sp
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = stringResource(R.string.redefining_travel),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = PrimaryBlue
                            )
                            Text(
                                text = stringResource(R.string.affordable_price),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = height(10.dp))
                            Text(
                                text = stringResource(R.string.company_intro),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }

                Spacer(modifier = height(36.dp))

                val context = LocalContext.current
                var promoTrips by remember { mutableStateOf<List<TripPromo>>(emptyList()) }
                LaunchedEffect(Unit) {
                    promoTrips = withContext(Dispatchers.IO) { getSavedPromoTrips(context) }
                    try {
                        val fresh = FetchPromoTripsFromWebsite(context)
                        if (fresh.isNotEmpty()) promoTrips = fresh
                    } catch (_: Exception) {}
                }

                val featuredTrips: List<Triple<String, Int, Int>> = listOf(
                    Triple(stringResource(R.string.mount_bromo), R.drawable.mountbromo, R.string.mount_bromo_price),
                    Triple(stringResource(R.string.borobudur_temple), R.drawable.borobudurtemple, R.string.borobudur_temple_price),
                    Triple(stringResource(R.string.ijen_crater), R.drawable.ijencrater, R.string.ijen_crater_price),
                    Triple(stringResource(R.string.bali), R.drawable.bali, R.string.bali_price),
                    Triple(stringResource(R.string.labuan_bajo), R.drawable.labuanbajo, R.string.labuan_bajo_price),
                    Triple(stringResource(R.string.raja_ampat), R.drawable.rajaampat, R.string.raja_ampat_price)
                )

                SectionHeader(
                    title = stringResource(R.string.featured_destinations),
                    modifier = Modifier.padding(horizontal = if (columnCount > 1) 48.dp else 20.dp)
                )

                Spacer(modifier = height(16.dp))

                val featuredSize = featuredTrips.size
                val featuredInitialPage = Int.MAX_VALUE / 2 - ((Int.MAX_VALUE / 2) % featuredSize)
                val featuredPagerState = rememberPagerState(initialPage = featuredInitialPage, pageCount = { Int.MAX_VALUE })
                LaunchedEffect(featuredPagerState) {
                    while (true) {
                        delay(6000L)
                        if (!featuredPagerState.isScrollInProgress) {
                            featuredPagerState.animateScrollToPage(
                                featuredPagerState.currentPage + 1,
                                animationSpec = tween(durationMillis = 1000, easing = EaseOutQuad)
                            )
                        }
                    }
                }

                HorizontalPager(
                    state = featuredPagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (columnCount > 1) 360.dp else 270.dp),
                    contentPadding = PaddingValues(horizontal = if (columnCount > 1) 100.dp else 20.dp),
                    pageSpacing = if (columnCount > 1) 24.dp else 16.dp
                ) { page ->
                    val actualIndex = page % featuredSize
                    val (name, imageRes, priceRes) = featuredTrips[actualIndex]
                    val matchedPromo = promoTrips.find { promo ->
                        promo.name.equals(name, ignoreCase = true)
                    } ?: promoTrips.find { promo ->
                        val pName = promo.name.lowercase(Locale.ROOT)
                        val fName = name.lowercase(Locale.ROOT)
                        pName.contains(fName) && !pName.contains("package") && !pName.contains("trip")
                    }
                    val livePrice = if (matchedPromo != null && matchedPromo.price.isNotBlank() && matchedPromo.price != "IDR 0") {
                        matchedPromo.price
                    } else {
                        stringResource(priceRes)
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                val pageOffset = (
                                    (featuredPagerState.currentPage - page) + featuredPagerState.currentPageOffsetFraction
                                ).absoluteValue
                                alpha = lerp(
                                    start = 0.6f,
                                    stop = 1f,
                                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                )
                                scaleX = lerp(
                                    start = 0.88f,
                                    stop = 1f,
                                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                )
                                scaleY = scaleX
                            },
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.35f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        FeaturedTripContent(name, imageRes, livePrice, onBookNow)
                    }
                }

                Spacer(modifier = height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(featuredSize) { index ->
                        val color = if (featuredPagerState.currentPage % featuredSize == index) {
                            PrimaryBlue
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                        }
                        val indicatorWidth = if (featuredPagerState.currentPage % featuredSize == index) 24.dp else 8.dp
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .height(8.dp)
                                .width(indicatorWidth)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }

                Spacer(modifier = height(32.dp))

                // Leadership Section
                SectionHeader(
                    title = stringResource(R.string.leadership_team),
                    modifier = Modifier.padding(horizontal = if (columnCount > 1) 48.dp else 20.dp)
                )

                Spacer(modifier = height(16.dp))

                val leadershipImages = listOf(
                    R.drawable.leadership_profile,
                    R.drawable.leadership_profile2
                )
                val leadershipSize = leadershipImages.size
                val leadershipInitialPage = Int.MAX_VALUE / 2 - ((Int.MAX_VALUE / 2) % leadershipSize)
                val leadershipPagerState = rememberPagerState(initialPage = leadershipInitialPage, pageCount = { Int.MAX_VALUE })

                LaunchedEffect(leadershipPagerState) {
                    while (leadershipImages.size > 1) {
                        delay(6000L)
                        if (!leadershipPagerState.isScrollInProgress) {
                            leadershipPagerState.animateScrollToPage(
                                leadershipPagerState.currentPage + 1,
                                animationSpec = tween(durationMillis = 1000, easing = EaseOutQuad)
                            )
                        }
                    }
                }

                HorizontalPager(
                    state = leadershipPagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (columnCount > 1) 400.dp else 250.dp),
                    contentPadding = PaddingValues(horizontal = if (columnCount > 1) 80.dp else 20.dp),
                    pageSpacing = 16.dp
                ) { page ->
                    val actualIndex = page % leadershipSize
                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                val pageOffset = (
                                    (leadershipPagerState.currentPage - page) + leadershipPagerState.currentPageOffsetFraction
                                ).absoluteValue
                                alpha = lerp(
                                    start = 0.6f,
                                    stop = 1f,
                                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                )
                                scaleX = lerp(
                                    start = 0.88f,
                                    stop = 1f,
                                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                )
                                scaleY = scaleX
                            },
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.35f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        AsyncImage(
                            model = leadershipImages[actualIndex],
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(leadershipSize) { index ->
                        val color = if (leadershipPagerState.currentPage % leadershipSize == index) {
                            PrimaryBlue
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                        }
                        Box(
                            modifier = padding(4.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                    }
                }

                Spacer(modifier = height(36.dp))

                // Private Services Section - Modern Grid Cards
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PrimaryBlue.copy(alpha = 0.04f))
                        .padding(vertical = 36.dp, horizontal = if (columnCount > 1) 48.dp else 20.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    SectionHeader(title = stringResource(R.string.our_private_services))

                    Spacer(modifier = height(4.dp))
                    Text(
                        text = stringResource(R.string.private_services_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Start,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = height(20.dp))

                    val services = listOf(
                        Feature(
                            stringResource(R.string.prof_expertise_experience),
                            stringResource(R.string.prof_expertise_experience_desc),
                            Icons.Default.Work
                        ),
                        Feature(
                            stringResource(R.string.pricing_transparency),
                            stringResource(R.string.pricing_transparency_desc),
                            Icons.Default.AttachMoney
                        ),
                        Feature(
                            stringResource(R.string.customer_support_24_7),
                            stringResource(R.string.customer_support_desc),
                            Icons.Default.SupportAgent
                        ),
                        Feature(
                            stringResource(R.string.safety_commitment),
                            stringResource(R.string.safety_commitment_desc),
                            Icons.Default.SafetyCheck
                        ),
                        Feature(
                            stringResource(R.string.customized_solutions),
                            stringResource(R.string.customized_solutions_desc),
                            Icons.Default.Info
                        ),
                        Feature(
                            stringResource(R.string.trusted_by_corporate),
                            stringResource(R.string.trusted_by_corporate_desc),
                            Icons.Default.Business
                        )
                    )

                    val serviceCols = if (columnCount == 1) 2 else 3
                    services.chunked(serviceCols).forEach { rowServices ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowServices.forEach { service ->
                                ServiceItem(
                                    title = service.title,
                                    description = service.description,
                                    icon = service.icon,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            repeat(serviceCols - rowServices.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = height(12.dp))
                    }
                }

                Spacer(modifier = height(32.dp))

                // Travel Gallery Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (columnCount > 1) 48.dp else 20.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    SectionHeader(title = stringResource(R.string.travel_galery))

                    Spacer(modifier = height(16.dp))
                    val galleryImages = listOf(
                        R.drawable.traveler_gallery1,
                        R.drawable.traveler_gallery2,
                        R.drawable.traveler_gallery3,
                        R.drawable.traveler_gallery4,
                        R.drawable.traveler_gallery5,
                        R.drawable.traveler_gallery6
                    )

                    TravelGalleryCarousel(
                        images = galleryImages,
                        height = if (columnCount > 1) 420.dp else 230.dp,
                        contentScale = ContentScale.Crop,
                        horizontalPadding = if (columnCount > 1) 48.dp else 16.dp
                    )
                }
            }
        }

        Spacer(modifier = height(36.dp))

        GuestStoriesCarousel(isWideLayout = columnCount > 1)
    }
}

@Composable
fun UserInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = size(18.dp), tint = PrimaryBlue)
        Spacer(modifier = width(12.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun FeaturedTripContent(
    name: String,
    imageRes: Int,
    priceText: String,
    onBookNow: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = imageRes,
            contentDescription = name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.2f),
                            Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        // Rating & Price Badge at Top
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = PrimaryBlue
            ) {
                Text(
                    text = priceText,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Bottom Info & CTA Button
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = WarmAmberGold,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Indonesia",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = height(10.dp))
            Button(
                onClick = onBookNow,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.book_now), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TravelGalleryCarousel(
    images: List<Int>,
    height: Dp,
    contentScale: ContentScale,
    horizontalPadding: Dp
) {
    val size = images.size
    val initialPage = Int.MAX_VALUE / 2 - ((Int.MAX_VALUE / 2) % size)
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { Int.MAX_VALUE })

    LaunchedEffect(pagerState) {
        while (size > 1) {
            delay(6000L)
            if (!pagerState.isScrollInProgress) {
                pagerState.animateScrollToPage(
                    pagerState.currentPage + 1,
                    animationSpec = tween(durationMillis = 1000, easing = EaseOutQuad)
                )
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(height),
            contentPadding = PaddingValues(horizontal = horizontalPadding),
            pageSpacing = 16.dp
        ) { page ->
            val actualIndex = page % size
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val pageOffset = (
                            (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                        ).absoluteValue
                        alpha = lerp(
                            start = 0.6f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )
                        scaleX = lerp(
                            start = 0.88f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )
                        scaleY = scaleX
                    },
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.35f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                AsyncImage(
                    model = images[actualIndex],
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale
                )
            }
        }

        Spacer(modifier = height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(size) { index ->
                val color = if (pagerState.currentPage % size == index) {
                    PrimaryBlue
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                }
                Box(
                    modifier = padding(4.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}

@Composable
fun GuestStoriesCarousel(isWideLayout: Boolean = false) {
    val stories = listOf(
        GuestStory(
            quote = (stringResource(R.string.johndoe_review)),
            guestName = (stringResource(R.string.johndoe_name)),
            country = (stringResource(R.string.johndoe_country))
        ),
        GuestStory(
            quote = (stringResource(R.string.emilysmith_review)),
            guestName = (stringResource(R.string.emilysmith_name)),
            country = (stringResource(R.string.emilysmith_country))
        ),
        GuestStory(
            quote = (stringResource(R.string.davidsarah_review)),
            guestName = (stringResource(R.string.davidsarah_name)),
            country = (stringResource(R.string.davidsarah_country))
        )
    )
    val size = stories.size
    val initialPage = Int.MAX_VALUE / 2 - ((Int.MAX_VALUE / 2) % size)
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { Int.MAX_VALUE })

    LaunchedEffect(pagerState) {
        while (true) {
            delay(6000L)
            if (!pagerState.isScrollInProgress) {
                pagerState.animateScrollToPage(
                    pagerState.currentPage + 1,
                    animationSpec = tween(durationMillis = 1000, easing = EaseOutQuad)
                )
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.Start
    ) {
        SectionHeader(title = stringResource(R.string.guest_stories))

        Spacer(modifier = height(16.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isWideLayout) 360.dp else 380.dp),
            contentPadding = PaddingValues(horizontal = if (isWideLayout) 80.dp else 0.dp),
            pageSpacing = if (isWideLayout) 24.dp else 0.dp
        ) { page ->
            val actualIndex = page % size
            val story = stories[actualIndex]
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val pageOffset = (
                            (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                        ).absoluteValue
                        alpha = lerp(
                            start = 0.6f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )
                        scaleX = lerp(
                            start = 0.88f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )
                        scaleY = scaleX
                    },
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.35f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Surface(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .size(70.dp),
                        shape = CircleShape,
                        color = PrimaryBlueDark,
                        shadowElevation = 6.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.FormatQuote,
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = WarmAmberGold
                            )
                        }
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 24.dp, end = 24.dp, top = 90.dp, bottom = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row {
                                repeat(5) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = WarmAmberGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = height(16.dp))
                            Text(
                                text = "\"${story.quote}\"",
                                style = MaterialTheme.typography.bodyLarge,
                                fontFamily = FontFamily.Serif,
                                fontStyle = FontStyle.Italic,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 24.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = story.guestName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                            Text(
                                text = story.country,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(size) { index ->
                val indicatorWidth = if (pagerState.currentPage % size == index) 40.dp else 10.dp
                val indicatorColor = if (pagerState.currentPage % size == index) PrimaryBlue else Color(0xFFE1E5E9)
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .height(8.dp)
                        .width(indicatorWidth)
                        .clip(CircleShape)
                        .background(indicatorColor)
                )
            }
        }
    }
}
