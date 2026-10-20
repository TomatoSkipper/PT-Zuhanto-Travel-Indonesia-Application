package com.example.appproject.ui.navigation

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.appproject.R
import com.example.appproject.data.model.isUserAdmin
import com.example.appproject.data.model.normalizeBookingStatus
import com.example.appproject.data.repository.FirebaseRepository
import com.example.appproject.ui.screens.AboutUsScreen
import com.example.appproject.ui.screens.AdminHelpDetailScreen
import com.example.appproject.ui.screens.AdminHelpScreen
import com.example.appproject.ui.screens.AdminScreen
import com.example.appproject.ui.screens.AdminUsersScreen
import com.example.appproject.ui.screens.AdminVouchersScreen
import com.example.appproject.ui.screens.AskQuestionScreen
import com.example.appproject.ui.screens.DashboardHomeScreen
import com.example.appproject.ui.screens.HelpScreen
import com.example.appproject.ui.screens.InvoiceScreen
import com.example.appproject.ui.screens.LoginScreen
import com.example.appproject.ui.screens.MyAccountScreen
import com.example.appproject.ui.screens.MyBookingsScreen
import com.example.appproject.ui.screens.PartnerScreen
import com.example.appproject.ui.screens.PaymentScreen
import com.example.appproject.ui.screens.ProfileScreen
import com.example.appproject.ui.screens.PromoTripDetailScreen
import com.example.appproject.ui.screens.PromoTripScreen
import com.example.appproject.ui.screens.ReceiptScreen
import com.example.appproject.ui.screens.RegisterScreen
import com.example.appproject.ui.screens.SettingsScreen
import com.example.appproject.ui.screens.TravelChatScreen
import com.example.appproject.ui.screens.UserVouchersScreen
import com.example.appproject.ui.theme.NavyStart
import com.example.appproject.ui.theme.PrimaryBlue
import com.example.appproject.worker.isAdminNewBookingNotified
import com.example.appproject.worker.isBookingConfirmedNotified
import com.example.appproject.worker.markAdminNewBookingNotified
import com.example.appproject.worker.markBookingConfirmedNotified
import com.example.appproject.worker.showAdminNewBookingNotification
import com.example.appproject.worker.showBookingConfirmedNotification
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigationDrawer() {
    val context = LocalContext.current
    val firebaseRepo = remember { FirebaseRepository() }
    val sessionPreferences = remember {
        context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
    }

    val scope = rememberCoroutineScope()

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var showSplash by remember { mutableStateOf(true) }

    var loggedInUser by rememberSaveable { mutableStateOf<String?>(null) }
    var displayUsername by rememberSaveable { mutableStateOf<String?>("Traveler") }
    var isAdminLoggedIn by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(loggedInUser, isAdminLoggedIn) {
        val uid = loggedInUser
        if (!uid.isNullOrBlank()) {
            if (isAdminLoggedIn) {
                displayUsername = "Admin"
            } else {
                val user = firebaseRepo.getUser(uid)
                if (user != null) {
                    displayUsername = user.username.ifBlank { user.name.ifBlank { "Traveler" } }
                } else {
                    displayUsername = "Traveler"
                }
            }
        } else {
            displayUsername = null
        }
    }

    LaunchedEffect(Unit) {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser != null) {
            val uid = firebaseUser.uid
            try {
                val tokenResult = firebaseUser.getIdToken(true).await()
                val isAdmin = isUserAdmin(uid, tokenResult?.claims)
                isAdminLoggedIn = isAdmin

                val user = firebaseRepo.getUser(uid)
                if (user != null && !user.isBanned) {
                    loggedInUser = uid
                } else if (isAdmin) {
                    loggedInUser = uid
                } else {
                    FirebaseAuth.getInstance().signOut()
                    loggedInUser = null
                    isAdminLoggedIn = false
                }
            } catch (_: Exception) {
                FirebaseAuth.getInstance().signOut()
                loggedInUser = null
                isAdminLoggedIn = false
            }
        }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            Log.d("NotificationPermission", "POST_NOTIFICATIONS granted: $isGranted")
        }
        LaunchedEffect(Unit) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    DisposableEffect(loggedInUser, isAdminLoggedIn) {
        val user = loggedInUser
        if (user.isNullOrEmpty()) {
            return@DisposableEffect onDispose {}
        }

        val listenerRegistration = if (isAdminLoggedIn) {
            firebaseRepo.listenToAllBookings { bookingList ->
                val prefs = context.getSharedPreferences("booking_notifications_prefs", Context.MODE_PRIVATE)
                val isInitialized = prefs.getBoolean("admin_booking_notifications_initialized", false)

                if (!isInitialized) {
                    val existingIds = bookingList.map { it.bookingId }.toSet()
                    prefs.edit()
                        .putStringSet("notified_admin_booking_ids", existingIds)
                        .putBoolean("admin_booking_notifications_initialized", true)
                        .apply()
                } else {
                    bookingList.forEach { booking ->
                        if (!isAdminNewBookingNotified(context, booking.bookingId)) {
                            showAdminNewBookingNotification(context, booking)
                            markAdminNewBookingNotified(context, booking.bookingId)
                        }
                    }
                }
            }
        } else {
            firebaseRepo.listenToBookingsForUser(user) { bookingList ->
                bookingList.forEach { booking ->
                    if (normalizeBookingStatus(booking.status) == "Confirmed") {
                        if (!isBookingConfirmedNotified(context, booking.bookingId)) {
                            showBookingConfirmedNotification(context, booking)
                            markBookingConfirmedNotified(context, booking.bookingId)
                        }
                    }
                }
            }
        }

        onDispose {
            listenerRegistration?.remove()
        }
    }

    LaunchedEffect(Unit) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.cancel(2002)
    }

    val activity = context as? AppCompatActivity
    val targetBookingId = activity?.intent?.getStringExtra("navigate_to_booking_id")
    val navigateToAdmin = activity?.intent?.getBooleanExtra("navigate_to_admin", false) ?: false
    LaunchedEffect(targetBookingId, navigateToAdmin, loggedInUser, isAdminLoggedIn, showSplash) {
        if (!showSplash && (!targetBookingId.isNullOrEmpty() || navigateToAdmin) && loggedInUser != null) {
            try {
                if (isAdminLoggedIn || navigateToAdmin) {
                    navController.navigate("admin") {
                        launchSingleTop = true
                    }
                } else {
                    navController.navigate("bookings/$loggedInUser") {
                        launchSingleTop = true
                    }
                }
            } catch (e: Exception) {
                Log.e("Navigation", "Failed to navigate from notification: ${e.message}", e)
            } finally {
                activity?.intent?.removeExtra("navigate_to_booking_id")
                activity?.intent?.removeExtra("navigate_to_admin")
            }
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    if (showSplash) {
        SplashWithCircularLogo(onSplashFinished = { showSplash = false })
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                if (isAdminLoggedIn) {
                    AdminSideNavbar(
                        currentRoute = currentRoute,
                        displayUsername = displayUsername,
                        onNavigate = { route ->
                            scope.launch { drawerState.close() }
                            navController.navigate(route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onLogout = {
                            scope.launch { drawerState.close() }
                            loggedInUser = null
                            isAdminLoggedIn = false
                            displayUsername = null
                            sessionPreferences.edit().remove("signed_in_username").apply()
                            FirebaseAuth.getInstance().signOut()
                            navController.navigate("dashboard") {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        navigationIcon = {
                            if (isAdminLoggedIn) {
                                androidx.compose.material3.IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.open_drawer))
                                }
                            }
                        },
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    shadowElevation = 3.dp,
                                    color = Color.White,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Image(
                                            painter = painterResource(id = R.drawable.zti_logo),
                                            contentDescription = "PT Zuhanto Travel Logo",
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = stringResource(R.string.company_name_zuhanto),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontSize = 15.sp,
                                        fontFamily = FontFamily.SansSerif,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = "INDONESIA",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.SansSerif,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Red
                                    )
                                }
                            }
                        },
                        actions = {
                            if (loggedInUser == null) {
                                Button(
                                    onClick = {
                                        navController.navigate("home") {
                                            launchSingleTop = true
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PrimaryBlue,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(stringResource(R.string.login), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    )
                },
                bottomBar = {
                    if (!isAdminLoggedIn && currentRoute?.startsWith("trip-detail/") != true) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        val isDashboardSelected = currentRoute == "dashboard" || currentRoute?.startsWith("dashboard/") == true
                        val isPromoTripSelected = currentRoute == "promotrip" || currentRoute?.startsWith("trip-detail/") == true
                        val isBookingsSelected = currentRoute?.startsWith("bookings/") == true
                        val isHelpSelected = currentRoute == "help"
                        val isMyAccountSelected = currentRoute == "myaccount" || currentRoute == "vouchers" || currentRoute?.startsWith("profile/") == true || currentRoute == "settings" || currentRoute == "about" || currentRoute == "partner"

                        NavigationBarItem(
                            selected = isDashboardSelected,
                            onClick = {
                                val target = if (loggedInUser != null) "dashboard/$loggedInUser" else "dashboard"
                                if (currentRoute != target) {
                                    navController.navigate(target) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                            label = { Text("Home") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryBlue,
                                selectedTextColor = PrimaryBlue,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )

                        NavigationBarItem(
                            selected = isPromoTripSelected,
                            onClick = {
                                if (currentRoute != "promotrip") {
                                    navController.navigate("promotrip") {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(Icons.Default.Place, contentDescription = "Promo Trip") },
                            label = { Text(
                                text = stringResource(R.string.promo_trip),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            ) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryBlue,
                                selectedTextColor = PrimaryBlue,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )

                        NavigationBarItem(
                            selected = isBookingsSelected,
                            onClick = {
                                val target = if (loggedInUser != null && !isAdminLoggedIn) "bookings/$loggedInUser" else "bookings/guest"
                                if (currentRoute != target) {
                                    navController.navigate(target) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(Icons.Filled.Luggage, contentDescription = "Bookings") },
                            label = { Text(stringResource(R.string.bookings)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryBlue,
                                selectedTextColor = PrimaryBlue,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )

                        NavigationBarItem(
                            selected = isHelpSelected,
                            onClick = {
                                if (currentRoute != "help") {
                                    navController.navigate("help") {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = "Help") },
                            label = { Text(stringResource(R.string.help)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryBlue,
                                selectedTextColor = PrimaryBlue,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )

                        NavigationBarItem(
                            selected = isMyAccountSelected,
                            onClick = {
                                if (currentRoute != "myaccount") {
                                    val popped = navController.popBackStack("myaccount", false)
                                    if (!popped) {
                                        navController.navigate("myaccount") {
                                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            },
                            icon = { Icon(Icons.Default.AccountCircle, contentDescription = "My Account") },
                            label = { Text(stringResource(R.string.my_account)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PrimaryBlue,
                                selectedTextColor = PrimaryBlue,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = when {
                    isAdminLoggedIn -> "admin"
                    loggedInUser != null -> "dashboard/$loggedInUser"
                    else -> "dashboard"
                },
                modifier = Modifier.padding(innerPadding),
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(350, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(350))
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(350, easing = FastOutSlowInEasing)
                    ) + fadeOut(animationSpec = tween(350))
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { -it },
                        animationSpec = tween(350, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(350))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(350, easing = FastOutSlowInEasing)
                    ) + fadeOut(animationSpec = tween(350))
                }
            ) {
                composable("home") {
                    LoginScreen(
                        firebaseRepo = firebaseRepo,
                        onLoginSuccess = { user ->
                            scope.launch {
                                val firebaseUser = FirebaseAuth.getInstance().currentUser
                                val tokenResult = firebaseUser?.getIdToken(true)?.await()
                                val isAdmin = isUserAdmin(firebaseUser?.uid, tokenResult?.claims)
                                isAdminLoggedIn = isAdmin
                                loggedInUser = user
                                val destination = if (isAdmin) "admin" else "dashboard/$user"
                                navController.navigate(destination) {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        },
                        onSignUpClick = {
                            navController.navigate("register")
                        }
                    )
                }
                composable("dashboard/{username}") { backStackEntry ->
                    val username = backStackEntry.arguments?.getString("username") ?: ""
                    DashboardHomeScreen(
                        username = username,
                        firebaseRepo = firebaseRepo,
                        onBookNow = {
                            navController.navigate("promotrip") {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable("dashboard") {
                    DashboardHomeScreen(
                        username = "Traveler",
                        firebaseRepo = firebaseRepo,
                        onBookNow = {
                            navController.navigate("promotrip") {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable("myaccount") {
                    MyAccountScreen(
                        loggedInUser = loggedInUser,
                        displayUsername = displayUsername,
                        isAdminLoggedIn = isAdminLoggedIn,
                        firebaseRepo = firebaseRepo,
                        onNavigateToBookings = {
                            val target = if (loggedInUser != null) "bookings/$loggedInUser" else "bookings/guest"
                            navController.navigate(target) { launchSingleTop = true }
                        },
                        onNavigateToVouchers = {
                            navController.navigate("vouchers") { launchSingleTop = true }
                        },
                        onNavigateToProfile = {
                            if (loggedInUser != null) {
                                navController.navigate("profile/$loggedInUser") { launchSingleTop = true }
                            } else {
                                navController.navigate("home") { launchSingleTop = true }
                            }
                        },
                        onNavigateToSettings = {
                            navController.navigate("settings") { launchSingleTop = true }
                        },
                        onNavigateToAbout = {
                            navController.navigate("about") { launchSingleTop = true }
                        },
                        onNavigateToPartner = {
                            navController.navigate("partner") { launchSingleTop = true }
                        },
                        onNavigateToHelp = {
                            navController.navigate("help") { launchSingleTop = true }
                        },
                        onNavigateToLogin = {
                            navController.navigate("home") { launchSingleTop = true }
                        },
                        onLogout = {
                            loggedInUser = null
                            isAdminLoggedIn = false
                            displayUsername = null
                            sessionPreferences.edit().remove("signed_in_username").apply()
                            FirebaseAuth.getInstance().signOut()
                            navController.navigate("dashboard") {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable("admin") {
                    AdminScreen(
                        firebaseRepo = firebaseRepo
                    )
                }
                composable("admin-vouchers") {
                    if (isAdminLoggedIn) {
                        AdminVouchersScreen(firebaseRepo = firebaseRepo)
                    } else {
                        LoginScreen(
                            firebaseRepo = firebaseRepo,
                            onLoginSuccess = { user ->
                                scope.launch {
                                    val firebaseUser = FirebaseAuth.getInstance().currentUser
                                    val tokenResult = firebaseUser?.getIdToken(true)?.await()
                                    val isAdmin = isUserAdmin(firebaseUser?.uid, tokenResult?.claims)
                                    isAdminLoggedIn = isAdmin
                                    loggedInUser = user
                                    val destination = if (isAdmin) "admin" else "dashboard/$user"
                                    navController.navigate(destination) {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            },
                            onSignUpClick = { navController.navigate("register") }
                        )
                    }
                }
                composable("admin-users") {
                    if (isAdminLoggedIn) {
                        AdminUsersScreen(firebaseRepo = firebaseRepo)
                    } else {
                        LoginScreen(
                            firebaseRepo = firebaseRepo,
                            onLoginSuccess = { user ->
                                scope.launch {
                                    val firebaseUser = FirebaseAuth.getInstance().currentUser
                                    val tokenResult = firebaseUser?.getIdToken(true)?.await()
                                    val isAdmin = isUserAdmin(firebaseUser?.uid, tokenResult?.claims)
                                    isAdminLoggedIn = isAdmin
                                    loggedInUser = user
                                    val destination = if (isAdmin) "admin" else "dashboard/$user"
                                    navController.navigate(destination) {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            },
                            onSignUpClick = { navController.navigate("register") }
                        )
                    }
                }
                composable("admin-help") {
                    if (isAdminLoggedIn) {
                        AdminHelpScreen(
                            firebaseRepo = firebaseRepo,
                            onBack = { navController.popBackStack() },
                            onNavigateToDetail = { requestId ->
                                navController.navigate("admin-help-detail/$requestId")
                            }
                        )
                    } else {
                        LoginScreen(
                            firebaseRepo = firebaseRepo,
                            onLoginSuccess = { user ->
                                scope.launch {
                                    val firebaseUser = FirebaseAuth.getInstance().currentUser
                                    val tokenResult = firebaseUser?.getIdToken(true)?.await()
                                    val isAdmin = isUserAdmin(firebaseUser?.uid, tokenResult?.claims)
                                    isAdminLoggedIn = isAdmin
                                    loggedInUser = user
                                    val destination = if (isAdmin) "admin" else "dashboard/$user"
                                    navController.navigate(destination) {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            },
                            onSignUpClick = { navController.navigate("register") }
                        )
                    }
                }
                composable("admin-help-detail/{requestId}") { backStackEntry ->
                    val requestId = backStackEntry.arguments?.getString("requestId") ?: ""
                    if (isAdminLoggedIn) {
                        AdminHelpDetailScreen(
                            requestId = requestId,
                            firebaseRepo = firebaseRepo,
                            onBack = { navController.popBackStack() }
                        )
                    } else {
                        LoginScreen(
                            firebaseRepo = firebaseRepo,
                            onLoginSuccess = { user ->
                                scope.launch {
                                    val firebaseUser = FirebaseAuth.getInstance().currentUser
                                    val tokenResult = firebaseUser?.getIdToken(true)?.await()
                                    val isAdmin = isUserAdmin(firebaseUser?.uid, tokenResult?.claims)
                                    isAdminLoggedIn = isAdmin
                                    loggedInUser = user
                                    val destination = if (isAdmin) "admin" else "dashboard/$user"
                                    navController.navigate(destination) {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            },
                            onSignUpClick = { navController.navigate("register") }
                        )
                    }
                }
                composable("promotrip") {
                    PromoTripScreen(
                        username = loggedInUser ?: "",
                        firebaseRepo = firebaseRepo,
                        onViewTripDetail = { trip ->
                            navController.navigate("trip-detail/${Uri.encode(trip.name)}") {
                                launchSingleTop = true
                            }
                        },
                        onRequireLogin = {
                            navController.navigate("home") {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable("trip-detail/{tripName}") { backStackEntry ->
                    val encodedName = backStackEntry.arguments?.getString("tripName") ?: ""
                    val tripName = Uri.decode(encodedName)
                    PromoTripDetailScreen(
                        tripName = tripName,
                        firebaseRepo = firebaseRepo,
                        loggedInUser = loggedInUser ?: "",
                        onBack = { navController.popBackStack() },
                        onRequireLogin = {
                            navController.navigate("home") {
                                launchSingleTop = true
                            }
                        },
                        onNavigateToChat = { destination ->
                            navController.navigate("chat/${Uri.encode(destination)}") {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable("chat") {
                    TravelChatScreen(
                        destination = "General Travel",
                        onBack = { navController.popBackStack() },
                        onNavigateToHelp = { navController.navigate("help") }
                    )
                }
                composable("chat/{destination}") { backStackEntry ->
                    val encodedDest = backStackEntry.arguments?.getString("destination") ?: ""
                    val destination = Uri.decode(encodedDest)
                    TravelChatScreen(
                        destination = destination,
                        onBack = { navController.popBackStack() },
                        onNavigateToHelp = { navController.navigate("help") }
                    )
                }
                composable("bookings/{username}") { backStackEntry ->
                    val username = backStackEntry.arguments?.getString("username") ?: ""
                    val isGuest = loggedInUser == null || username == "guest"
                    if (isGuest) {
                        MyBookingsScreen(
                            username = "guest",
                            firebaseRepo = firebaseRepo,
                            onViewReceipt = {},
                            onViewInvoice = {},
                            onBack = { navController.popBackStack() },
                            isGuest = true,
                            onLoginClick = {
                                navController.navigate("home") { launchSingleTop = true }
                            }
                        )
                    } else if (loggedInUser == null || loggedInUser != username || isAdminLoggedIn) {
                        LoginScreen(
                            firebaseRepo = firebaseRepo,
                            onLoginSuccess = { user ->
                                scope.launch {
                                    val firebaseUser = FirebaseAuth.getInstance().currentUser
                                    val tokenResult = firebaseUser?.getIdToken(true)?.await()
                                    val isAdmin = isUserAdmin(firebaseUser?.uid, tokenResult?.claims)
                                    isAdminLoggedIn = isAdmin
                                    loggedInUser = user
                                    val destination = if (isAdmin) "admin" else "dashboard/$user"
                                    navController.navigate(destination) {
                                        popUpTo("home") { inclusive = true }
                                    }
                                }
                            },
                            onSignUpClick = {
                                navController.navigate("register")
                            }
                        )
                    } else {
                        MyBookingsScreen(
                            username = username,
                            firebaseRepo = firebaseRepo,
                            isGuest = false,
                            onViewReceipt = { bId ->
                                navController.navigate("receipt/$bId")
                            },
                            onViewInvoice = { bId ->
                                navController.navigate("invoice/$bId")
                            },
                            onPayNow = { bId ->
                                navController.navigate("payment/$bId")
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
                composable("payment/{bookingId}") { backStackEntry ->
                    val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
                    PaymentScreen(
                        bookingId = bookingId,
                        firebaseRepo = firebaseRepo,
                        loggedInUser = loggedInUser,
                        onBack = { navController.popBackStack() },
                        onRedirectToBookings = {
                            navController.navigate("bookings/$loggedInUser") {
                                popUpTo("bookings/$loggedInUser") { inclusive = false }
                            }
                        },
                        onViewReceipt = { bId ->
                            navController.navigate("receipt/$bId") {
                                popUpTo("payment/$bId") { inclusive = true }
                            }
                        },
                        onViewInvoice = { bId ->
                            navController.navigate("invoice/$bId") {
                                popUpTo("payment/$bId") { inclusive = true }
                            }
                        }
                    )
                }
                composable("receipt/{bookingId}") { backStackEntry ->
                    val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
                    ReceiptScreen(
                        bookingId = bookingId,
                        firebaseRepo = firebaseRepo,
                        loggedInUser = loggedInUser,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("invoice/{bookingId}") { backStackEntry ->
                    val bookingId = backStackEntry.arguments?.getString("bookingId") ?: ""
                    InvoiceScreen(
                        bookingId = bookingId,
                        firebaseRepo = firebaseRepo,
                        loggedInUser = loggedInUser,
                        onBack = { navController.popBackStack() },
                        onViewReceipt = { bId ->
                            navController.navigate("receipt/$bId") {
                                popUpTo("invoice/$bId") { inclusive = true }
                            }
                        }
                    )
                }
                composable("vouchers") {
                    UserVouchersScreen(
                        firebaseRepo = firebaseRepo,
                        username = loggedInUser ?: "",
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("partner") {
                    PartnerScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("profile/{username}") { backStackEntry ->
                    val usernameArg = backStackEntry.arguments?.getString("username") ?: ""
                    if (isAdminLoggedIn) {
                        LaunchedEffect(Unit) {
                            navController.navigate("admin") {
                                popUpTo("admin") { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    } else {
                        ProfileScreen(
                            username = usernameArg,
                            firebaseRepo = firebaseRepo,
                            onBack = { navController.popBackStack() },
                            onProfileUpdated = { newUsername ->
                                if (newUsername != loggedInUser) {
                                    loggedInUser = newUsername
                                    navController.navigate("profile/$newUsername") {
                                        popUpTo("profile/$usernameArg") { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            }
                        )
                    }
                }
                composable("settings") {
                    SettingsScreen(
                        onNavigateToAdminReports = {
                            navController.navigate("admin-reports") {
                                launchSingleTop = true
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("about") {
                    AboutUsScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("help") {
                    HelpScreen(
                        loggedInUser = loggedInUser,
                        displayUsername = displayUsername,
                        firebaseRepo = firebaseRepo,
                        onNavigateToAskQuestion = { navController.navigate("ask-question") },
                        onBack = {
                            if (navController.previousBackStackEntry != null) {
                                navController.popBackStack()
                            } else {
                                val target = if (loggedInUser != null) "dashboard/$loggedInUser" else "dashboard"
                                navController.navigate(target) {
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }
                composable("ask-question") {
                    AskQuestionScreen(
                        loggedInUser = loggedInUser,
                        displayUsername = displayUsername,
                        firebaseRepo = firebaseRepo,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("register") {
                    RegisterScreen(
                        firebaseRepo = firebaseRepo,
                        onRegisterSuccess = { newUsername ->
                            scope.launch {
                                val firebaseUser = FirebaseAuth.getInstance().currentUser
                                val tokenResult = firebaseUser?.getIdToken(true)?.await()
                                val isAdmin = isUserAdmin(firebaseUser?.uid, tokenResult?.claims)
                                isAdminLoggedIn = isAdmin
                                loggedInUser = newUsername
                                val destination = if (isAdmin) "admin" else "dashboard/$newUsername"
                                navController.navigate(destination) {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        },
                        onBackToLogin = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
      }
    }
}

@Composable
fun SplashWithCircularLogo(onSplashFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1500)
        onSplashFinished()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(NavyStart, PrimaryBlue, Color(0xFF041C32))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                modifier = Modifier
                    .size(110.dp)
                    .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 12.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.zti_logo),
                        contentDescription = null,
                        modifier = Modifier.size(85.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "PT ZUHANTO TRAVEL",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "INDONESIA",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Red,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
    }
}

private data class AdminNavItemData(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val showChevron: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSideNavbar(
    currentRoute: String?,
    displayUsername: String?,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(285.dp),
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
        drawerContainerColor = Color(0xFFFAFCFF),
        drawerTonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header: Logo & Brand Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 3.dp,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.zti_logo),
                            contentDescription = "Company Logo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "PT ZUHANTO TRAVEL",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = Color(0xFF0F172A),
                            letterSpacing = 0.2.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "INDONESIA",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            }

            // User Profile Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFEBF3FE),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayUsername ?: "Admin",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF0F172A)
                        )
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF22C55E))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Online",
                            style = TextStyle(
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(20.dp)
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                color = Color(0xFFF1F5F9),
                thickness = 1.dp
            )

            // Side Navigation Items
            val items = listOf(
                AdminNavItemData(
                    route = "admin",
                    label = stringResource(R.string.admin_dashboard),
                    icon = Icons.Filled.Home,
                    showChevron = false
                ),
                AdminNavItemData(
                    route = "admin-users",
                    label = stringResource(R.string.user_accounts),
                    icon = Icons.Default.People,
                    showChevron = true
                ),
                AdminNavItemData(
                    route = "admin-help",
                    label = stringResource(R.string.help_and_support),
                    icon = Icons.AutoMirrored.Filled.Assignment,
                    showChevron = true
                ),
                AdminNavItemData(
                    route = "admin-vouchers",
                    label = stringResource(R.string.manage_vouchers),
                    icon = Icons.Default.ConfirmationNumber,
                    showChevron = true
                ),
                AdminNavItemData(
                    route = "settings",
                    label = stringResource(R.string.settings),
                    icon = Icons.Default.Settings,
                    showChevron = true
                )
            )

            items.forEach { item ->
                val isSelected = currentRoute == item.route
                AdminDrawerNavItem(
                    icon = item.icon,
                    label = item.label,
                    selected = isSelected,
                    showChevron = item.showChevron,
                    onClick = { onNavigate(item.route) }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                color = Color(0xFFF1F5F9),
                thickness = 1.dp
            )

            // Logout Button
            Surface(
                onClick = onLogout,
                shape = RoundedCornerShape(12.dp),
                color = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = stringResource(R.string.logout),
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(R.string.logout),
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AdminDrawerNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    showChevron: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (selected) Color(0xFFEFF6FF) else Color.Transparent
    val contentColor = if (selected) Color(0xFF2563EB) else Color(0xFF334155)
    val iconColor = if (selected) Color(0xFF2563EB) else Color(0xFF475569)
    val fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = fontWeight,
                    color = contentColor
                ),
                modifier = Modifier.weight(1f)
            )
            if (showChevron && !selected) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
