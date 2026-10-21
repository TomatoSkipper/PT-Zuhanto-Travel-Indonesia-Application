package com.example.appproject.ui.screens

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import com.example.appproject.ui.theme.PrimaryBlue
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import coil.compose.AsyncImage
import com.example.appproject.R
import com.example.appproject.data.model.CountryCodeOption
import com.example.appproject.data.repository.FirebaseRepository
import com.example.appproject.data.model.User
import com.example.appproject.data.model.defaultCountryCodes
import com.example.appproject.data.model.isValidEmail
import com.example.appproject.data.model.isValidPhone
import com.example.appproject.data.model.isUserAdmin
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Brands
import compose.icons.fontawesomeicons.brands.Google
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun CountryCodePhoneInput(
    phoneValue: String,
    onPhoneValueChange: (String) -> Unit,
    selectedCountry: CountryCodeOption,
    onCountrySelected: (CountryCodeOption) -> Unit,
    enabled: Boolean = true,
    isError: Boolean = false,
    label: String = stringResource(R.string.phone),
    modifier: Modifier = Modifier
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = phoneValue,
            onValueChange = onPhoneValueChange,
            enabled = enabled,
            label = { Text(label) },
            placeholder = { Text("123456789") },
            leadingIcon = {
                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable(enabled = enabled) { dropdownExpanded = true }
                            .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
                    ) {
                        Text(
                            text = "${selectedCountry.flag} ${selectedCountry.code}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = if (dropdownExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Select Country Code",
                            tint = PrimaryBlue
                        )
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        defaultCountryCodes.forEach { country ->
                            val isSelected = selectedCountry == country
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${country.flag} ${country.countryName} (${country.code})",
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) PrimaryBlue else Color(0xFF0F172A)
                                        )
                                        if (isSelected) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryBlue)
                                        }
                                    }
                                },
                                onClick = {
                                    onCountrySelected(country)
                                    dropdownExpanded = false
                                },
                                modifier = Modifier.background(if (isSelected) PrimaryBlue.copy(alpha = 0.1f) else Color.Transparent)
                            )
                        }
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            isError = isError,
            modifier = Modifier.fillMaxWidth().testTag("phoneInput"),
            shape = RoundedCornerShape(16.dp)
        )
    }
}

fun getWebClientId(context: Context): String {
    return try {
        val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        if (resId != 0) {
            context.getString(resId)
        } else {
            "815723458802-bfg6omi650936erccj1arsbes2fsjoeg.apps.googleusercontent.com"
        }
    } catch (_: Exception) {
        "815723458802-bfg6omi650936erccj1arsbes2fsjoeg.apps.googleusercontent.com"
    }
}

@Composable
fun LoginScreen(
    firebaseRepo: FirebaseRepository,
    onLoginSuccess: (String) -> Unit = {},
    onSignUpClick: () -> Unit = {}
) {
    var selectedCountry by remember { mutableStateOf(defaultCountryCodes[0]) }
    var phoneInput by remember { mutableStateOf("") }
    var tacCode by remember { mutableStateOf("") }
    var staySignedIn by rememberSaveable { mutableStateOf(false) }

    var verificationId by remember { mutableStateOf<String?>(null) }
    var isTacSent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as? Activity
    val auth = remember { FirebaseAuth.getInstance() }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val webClientId = remember(context) { getWebClientId(context) }
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                scope.launch {
                    isLoading = true
                    try {
                        val authResult = auth.signInWithCredential(credential).await()
                        val firebaseUser = authResult.user

                        val email = firebaseUser?.email ?: account.email ?: ""
                        val displayName = firebaseUser?.displayName ?: account.displayName ?: "Traveler"
                        val phone = firebaseUser?.phoneNumber ?: ""
                        val (username, _) = handleGoogleSignInUser(firebaseRepo, context, email, displayName, phone)

                        val preferences = context.getSharedPreferences(
                            "user_session",
                            Context.MODE_PRIVATE
                        )
                        preferences.edit().putString("signed_in_username", username).apply()

                        isLoading = false
                        onLoginSuccess(username)
                    } catch (e: Exception) {
                        isLoading = false
                        errorMessage = e.localizedMessage ?: context.getString(R.string.error_invalid_credentials)
                    }
                }
            } else {
                isLoading = false
                errorMessage = "Google Sign-In failed: No ID Token"
            }
        } catch (e: Exception) {
            isLoading = false
            if (e is ApiException) {
                if (e.statusCode == 12501) {
                    return@rememberLauncherForActivityResult
                }
                val msg = e.localizedMessage ?: ""
                if (msg.contains("Long live credential", ignoreCase = true)) {
                    errorMessage = "Google credentials unavailable on this device. Please sign in with phone number."
                } else {
                    errorMessage = "Google Sign-In failed (${e.statusCode})"
                }
            } else {
                errorMessage = e.localizedMessage ?: "Google Sign-In failed"
            }
        }
    }

    fun launchGoogleSignIn() {
        isLoading = true
        errorMessage = ""
        scope.launch {
            try {
                if (activity != null) {
                    val credentialManager = CredentialManager.create(context)
                    val googleIdOption = GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(webClientId)
                        .setAutoSelectEnabled(false)
                        .build()

                    val request = GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()

                    val result = credentialManager.getCredential(context = activity, request = request)
                    val credential = result.credential
                    if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken
                        val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                        val authResult = auth.signInWithCredential(authCredential).await()
                        val firebaseUser = authResult.user

                        val email = firebaseUser?.email ?: googleIdTokenCredential.id ?: ""
                        val displayName = firebaseUser?.displayName ?: googleIdTokenCredential.displayName ?: "Traveler"
                        val phone = firebaseUser?.phoneNumber ?: ""
                        val (username, _) = handleGoogleSignInUser(firebaseRepo, context, email, displayName, phone)

                        val preferences = context.getSharedPreferences(
                            "user_session",
                            Context.MODE_PRIVATE
                        )
                        preferences.edit().putString("signed_in_username", username).apply()

                        isLoading = false
                        onLoginSuccess(username)
                        return@launch
                    }
                }
            } catch (e: GetCredentialCancellationException) {
                isLoading = false
                return@launch
            } catch (e: Exception) {
                Log.w("GoogleSignIn", "CredentialManager failed, trying legacy GoogleSignInIntent: ${e.message}")
            }

            try {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(webClientId)
                    .requestEmail()
                    .build()
                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                googleSignInClient.signOut()
                googleSignInLauncher.launch(googleSignInClient.signInIntent)
            } catch (e: Exception) {
                isLoading = false
                errorMessage = e.localizedMessage ?: "Failed to start Google Sign-In"
            }
        }
    }

    fun getFullPhoneNumber(): String {
        val trimmed = phoneInput.trim()
        return if (trimmed.startsWith("+")) {
            trimmed
        } else {
            val digits = trimmed.removePrefix("0")
            "${selectedCountry.code}$digits"
        }
    }

    fun completeLogin(credential: PhoneAuthCredential) {
        scope.launch {
            isLoading = true
            try {
                val authResult = auth.signInWithCredential(credential).await()
                val firebaseUser = authResult.user
                val userPhone = firebaseUser?.phoneNumber ?: getFullPhoneNumber()

                val tokenResult = firebaseUser?.getIdToken(true)?.await()
                val isAdmin = isUserAdmin(firebaseUser?.uid, tokenResult?.claims)

                val userUid = firebaseUser?.uid ?: ""
                val targetUsername = if (isAdmin) {
                    // Set a default username for admin users
                    "Admin"
                } else {
                    var currentUser = if (userUid.isNotBlank()) firebaseRepo.getUser(userUid) else null
                    if (currentUser == null) {
                        currentUser = firebaseRepo.getUser(userPhone)
                    }
                    if (currentUser == null) {
                        isLoading = false
                        errorMessage = context.getString(R.string.error_phone_not_registered)
                        return@launch
                    }

                    if (currentUser.isBanned) {
                        isLoading = false
                        errorMessage = context.getString(R.string.error_account_banned)
                        return@launch
                    }

                    val updated = currentUser.copy(isPhoneVerified = true)
                    firebaseRepo.updateUser(updated)
                    currentUser.username
                }

                val preferences = context.getSharedPreferences(
                    "user_session",
                    Context.MODE_PRIVATE
                )

                if (staySignedIn) {
                    preferences.edit()
                        .putString("signed_in_username", targetUsername)
                        .apply()
                } else {
                    preferences.edit().remove("signed_in_username").apply()
                }

                isLoading = false
                onLoginSuccess(targetUsername)
            } catch (e: Exception) {
                errorMessage = context.getString(R.string.error_invalid_credentials)
                isLoading = false
            }
        }
    }

    fun requestTacCode() {
        if (activity == null) {
            errorMessage = "Activity Reference Error"
            return
        }

        isLoading = true
        errorMessage = ""

        val fullPhone = getFullPhoneNumber()

        scope.launch {
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(fullPhone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        completeLogin(credential)
                    }

                    override fun onVerificationFailed(p0: FirebaseException) {
                        isLoading = false
                        errorMessage = p0.localizedMessage ?: "Verification Failed"
                    }

                    override fun onCodeSent(
                        vId: String,
                        token: PhoneAuthProvider.ForceResendingToken
                    ) {
                        isLoading = false
                        isTacSent = true
                        verificationId = vId
                    }
                }).build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        AsyncImage(
            model = R.drawable.borobudur_temple_explore,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.25f),
                            Color.Black.copy(alpha = 0.55f),
                            Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Header Section (Logo badge, Welcome back, Subtitle)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.size(88.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.zti_logo),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = stringResource(R.string.welcome_back),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stringResource(R.string.login_to_manage_your_trips_and_bookings),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )
            }

            // Bottom White Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = Color.White,
                shadowElevation = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Log in header
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(R.string.login),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = stringResource(R.string.sign_in_using_phone_number_or_google),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF64748B)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Phone number section
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.phone_number),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )

                        CountryCodePhoneInput(
                            phoneValue = phoneInput,
                            onPhoneValueChange = {
                                phoneInput = it
                                errorMessage = ""
                            },
                            selectedCountry = selectedCountry,
                            onCountrySelected = { selectedCountry = it },
                            enabled = !isTacSent,
                            label = "",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Helper text with padlock
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = stringResource(R.string.tac_helper_text),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B)
                        )
                    }

                    if (isTacSent) {
                        OutlinedTextField(
                            value = tacCode,
                            onValueChange = {
                                if (it.length <= 6) tacCode = it
                                errorMessage = ""
                            },
                            label = { Text(stringResource(R.string.tac_code)) },
                            placeholder = { Text(stringResource(R.string.enter_tac)) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("tacInput"),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }

                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Keep me logged in checkbox
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { staySignedIn = !staySignedIn }
                    ) {
                        Checkbox(
                            checked = staySignedIn,
                            onCheckedChange = { staySignedIn = it }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.keep_me_logged_in),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1E293B)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Request TAC Button
                    Button(
                        onClick = {
                            if (!isTacSent) {
                                val fullPhone = getFullPhoneNumber()
                                if (isValidPhone(fullPhone)) {
                                    requestTacCode()
                                } else {
                                    errorMessage = context.getString(R.string.error_invalid_phone)
                                }
                            } else {
                                if (tacCode.length != 6) {
                                    errorMessage = context.getString(R.string.error_invalid_tac)
                                } else if (verificationId != null) {
                                    val credential = PhoneAuthProvider.getCredential(verificationId!!, tacCode.trim())
                                    completeLogin(credential)
                                } else {
                                    errorMessage = context.getString(R.string.invalid_tac)
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8))
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = if (!isTacSent) stringResource(R.string.request_tac) else stringResource(R.string.login),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (isTacSent) {
                        TextButton(
                            onClick = {
                                isTacSent = false
                                tacCode = ""
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(stringResource(R.string.change_phone_tac))
                        }
                    }

                    // OR Divider
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
                        Text(
                            text = stringResource(R.string.or),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8)
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
                    }

                    // Continue with Google Button
                    OutlinedButton(
                        onClick = { launchGoogleSignIn() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = FontAwesomeIcons.Brands.Google,
                                contentDescription = "Google Logo",
                                tint = Color(0xFFEA4335),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stringResource(R.string.continue_with_google),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Footer: New here? Create an account
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.new_here),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = stringResource(R.string.create_an_account),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue,
                            modifier = Modifier.clickable { onSignUpClick() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RegisterScreen(
    firebaseRepo: FirebaseRepository,
    onRegisterSuccess: (String) -> Unit = {},
    onBackToLogin: () -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedCountry by remember { mutableStateOf(defaultCountryCodes[0]) }
    var phoneInput by remember { mutableStateOf("") }

    var isloading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showRegistrationSuccess by remember { mutableStateOf(false) }
    var registeredUsername by remember { mutableStateOf("") }
    var registrationSuccessTitle by remember { mutableStateOf("Registration Successful") }
    var registrationSuccessMessage by remember { mutableStateOf("Your account has been successfully created.") }
    var isTacSent by remember { mutableStateOf(false) }
    var verificationId by remember { mutableStateOf<String?>(null) }
    var tacCode by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as? Activity
    val auth = remember { FirebaseAuth.getInstance() }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val webClientId = remember(context) { getWebClientId(context) }
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                scope.launch {
                    isloading = true
                    try {
                        val authResult = auth.signInWithCredential(credential).await()
                        val firebaseUser = authResult.user

                        val userEmail = firebaseUser?.email ?: account.email ?: ""
                        val displayName = firebaseUser?.displayName ?: account.displayName ?: "Traveler"
                        val userPhone = firebaseUser?.phoneNumber ?: ""
                        val (username, isExisting) = handleGoogleSignInUser(firebaseRepo, context, userEmail, displayName, userPhone)

                        val preferences = context.getSharedPreferences(
                            "user_session",
                            Context.MODE_PRIVATE
                        )
                        preferences.edit().putString("signed_in_username", username).apply()

                        isloading = false
                        if (isExisting) {
                            onRegisterSuccess(username)
                        } else {
                            registeredUsername = username
                            registrationSuccessTitle = context.getString(R.string.registration_successful)
                            registrationSuccessMessage = context.getString(R.string.your_account_has_been_created)
                            showRegistrationSuccess = true
                        }
                    } catch (e: Exception) {
                        isloading = false
                        errorMessage = e.localizedMessage ?: context.getString(R.string.registration_failed)
                    }
                }
            } else {
                isloading = false
                errorMessage = "Google Sign-In failed: No ID Token"
            }
        } catch (e: Exception) {
            isloading = false
            if (e is ApiException) {
                if (e.statusCode == 12501) {
                    return@rememberLauncherForActivityResult
                }
                val msg = e.localizedMessage ?: ""
                if (msg.contains("Long live credential", ignoreCase = true)) {
                    errorMessage = "Google credentials unavailable on this device. Please register with phone number."
                } else {
                    errorMessage = "Google Sign-In failed (${e.statusCode})"
                }
            } else {
                errorMessage = e.localizedMessage ?: "Google Sign-In failed"
            }
        }
    }

    fun launchGoogleSignIn() {
        isloading = true
        errorMessage = ""
        scope.launch {
            val activity = context as? Activity
            try {
                if (activity != null) {
                    val credentialManager = CredentialManager.create(context)
                    val googleIdOption = GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(webClientId)
                        .setAutoSelectEnabled(false)
                        .build()

                    val request = GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()

                    val result = credentialManager.getCredential(context = activity, request = request)
                    val credential = result.credential
                    if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken
                        val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                        val authResult = auth.signInWithCredential(authCredential).await()
                        val firebaseUser = authResult.user

                        val userEmail = firebaseUser?.email ?: googleIdTokenCredential.id ?: ""
                        val displayName = firebaseUser?.displayName ?: googleIdTokenCredential.displayName ?: "Traveler"
                        val userPhone = firebaseUser?.phoneNumber ?: ""
                        val (username, isExisting) = handleGoogleSignInUser(firebaseRepo, context, userEmail, displayName, userPhone)

                        val preferences = context.getSharedPreferences(
                            "user_session",
                            Context.MODE_PRIVATE
                        )
                        preferences.edit().putString("signed_in_username", username).apply()

                        isloading = false
                        if (isExisting) {
                            onRegisterSuccess(username)
                        } else {
                            registeredUsername = username
                            registrationSuccessTitle = "Registration Successful"
                            registrationSuccessMessage = "Your account has been successfully created."
                            showRegistrationSuccess = true
                        }
                        return@launch
                    }
                }
            } catch (e: GetCredentialCancellationException) {
                isloading = false
                return@launch
            } catch (e: Exception) {
                Log.w("GoogleSignIn", "CredentialManager failed, trying legacy GoogleSignInIntent: ${e.message}")
            }

            try {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(webClientId)
                    .requestEmail()
                    .build()
                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                googleSignInClient.signOut()
                googleSignInLauncher.launch(googleSignInClient.signInIntent)
            } catch (e: Exception) {
                isloading = false
                errorMessage = e.localizedMessage ?: "Failed to start Google Sign-In"
            }
        }
    }

    fun getFullPhoneNumber(): String {
        val trimmed = phoneInput.trim()
        return if (trimmed.startsWith("+")) {
            trimmed
        } else {
            val digits = trimmed.removePrefix("0")
            "${selectedCountry.code}$digits"
        }
    }

    fun saveNewUserAndFinish(fullPhone: String, username: String, isVerified: Boolean) {
        scope.launch {
            try {
                val currentUserUid = auth.currentUser?.uid ?: ""
                if (firebaseRepo.isPhoneRegistered(fullPhone, currentUserUid)) {
                    isloading = false
                    errorMessage = "This phone number is already registered to another account"
                    return@launch
                }

                val fullName = name.trim()
                val parts = fullName.split("\\s+".toRegex()).filter { it.isNotBlank() }
                val derivedFirstTwoWords = when {
                    parts.size >= 2 -> "${parts[0]} ${parts[1]}"
                    parts.size == 1 -> parts[0]
                    else -> email.trim().substringBefore("@")
                }
                val finalUsername = derivedFirstTwoWords.ifBlank { fullPhone }
                val newUser = User(
                    uid = currentUserUid,
                    username = finalUsername,
                    email = email.trim(),
                    phone = fullPhone,
                    isPhoneVerified = isVerified,
                    name = fullName
                )
                firebaseRepo.saveUser(newUser)

                registeredUsername = finalUsername
                isloading = false
                showRegistrationSuccess = true
            } catch (e: Exception) {
                isloading = false
                errorMessage = e.message ?: context.getString(R.string.registration_failed)
            }
        }
    }

    fun completeRegistrationWithCredential(credential: PhoneAuthCredential) {
        scope.launch {
            isloading = true
            try {
                auth.signInWithCredential(credential).await()
                val fullPhone = getFullPhoneNumber()
                val username = name.trim()
                saveNewUserAndFinish(fullPhone, username, true)
            } catch (e: Exception) {
                isloading = false
                errorMessage = e.localizedMessage ?: context.getString(R.string.registration_failed)
            }
        }
    }

    fun verifyTacAndRegister() {
        if (tacCode.length != 6) {
            errorMessage = context.getString(R.string.error_tac_message)
            return
        }

        if (verificationId != null) {
            isloading = true
            val credential = PhoneAuthProvider.getCredential(verificationId!!, tacCode.trim())
            completeRegistrationWithCredential(credential)
        } else {
            errorMessage = context.getString(R.string.invalid_verification_request)
        }
    }

    fun requestTacCode() {
        val fullPhone = getFullPhoneNumber()
        if (name.isBlank() || email.isBlank() || phoneInput.isBlank()) {
            errorMessage = context.getString(R.string.error_fill_all_fields)
            return
        }
        if (!isValidEmail(email.trim())) {
            errorMessage = context.getString(R.string.invalid_email)
            return
        }
        if (!isValidPhone(fullPhone)) {
            errorMessage = context.getString(R.string.error_invalid_phone)
            return
        }

        isloading = true
        errorMessage = ""

        scope.launch {
            try {
                val cleanEmail = email.trim()
                val existingUser = if (cleanEmail.isNotBlank()) firebaseRepo.getUser(cleanEmail) else null
                if (existingUser != null) {
                    isloading = false
                    val username = existingUser.username
                    val preferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)
                    preferences.edit().putString("signed_in_username", username).apply()

                    onRegisterSuccess(username)
                    return@launch
                }

                val currentUserUid = auth.currentUser?.uid ?: ""
                if (firebaseRepo.isPhoneRegistered(fullPhone, currentUserUid)) {
                    isloading = false
                    errorMessage = "This phone number is already registered to another account"
                    return@launch
                }

                if (activity == null) {
                    isloading = false
                    errorMessage = "Activity Reference Error"
                    return@launch
                }

                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(fullPhone)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(activity)
                    .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                            completeRegistrationWithCredential(credential)
                        }

                        override fun onVerificationFailed(e: FirebaseException) {
                            isloading = false
                            errorMessage = e.message ?: "Verification Failed"
                        }

                        override fun onCodeSent(vId: String, token: PhoneAuthProvider.ForceResendingToken) {
                            isloading = false
                            isTacSent = true
                            verificationId = vId
                        }
                    }).build()

                PhoneAuthProvider.verifyPhoneNumber(options)
            } catch (e: Exception) {
                isloading = false
                errorMessage = e.localizedMessage ?: "Registration failed"
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        AsyncImage(
            model = R.drawable.borobudur_temple_explore,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.25f),
                            Color.Black.copy(alpha = 0.55f),
                            Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Header Section (Logo badge, Create your account, Subtitle)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.size(88.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.zti_logo),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = stringResource(R.string.create_your_account),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stringResource(R.string.book_trips_with_pt_zuhanto_travel_indonesia),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )
            }

            // Bottom White Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = Color.White,
                shadowElevation = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // FULL NAME section
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.full_name),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                errorMessage = ""
                            },
                            placeholder = { Text(stringResource(R.string.full_name_placeholder), color = Color(0xFF94A3B8)) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryBlue) },
                            singleLine = true,
                            enabled = !isTacSent,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                    }

                    // Email section
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.email),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                errorMessage = ""
                            },
                            placeholder = { Text("name@example.com", color = Color(0xFF94A3B8)) },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = PrimaryBlue) },
                            singleLine = true,
                            enabled = !isTacSent,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = Color(0xFFE2E8F0),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                    }

                    // Phone number section
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(R.string.phone_number),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )

                        CountryCodePhoneInput(
                            phoneValue = phoneInput,
                            onPhoneValueChange = {
                                phoneInput = it
                                errorMessage = ""
                            },
                            selectedCountry = selectedCountry,
                            onCountrySelected = { selectedCountry = it },
                            label = "",
                            enabled = !isTacSent,
                            isError = errorMessage == context.getString(R.string.error_invalid_phone),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Helper text
                    Text(
                        text = stringResource(R.string.tac_helper_text),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B)
                    )

                    if (isTacSent) {
                        OutlinedTextField(
                            value = tacCode,
                            onValueChange = {
                                if (it.length <= 6) tacCode = it
                                errorMessage = ""
                            },
                            label = { Text(stringResource(R.string.six_digit_tac)) },
                            placeholder = { Text(stringResource(R.string.enter_tac)) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("tacInput"),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }

                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Send TAC Code Button
                    Button(
                        onClick = {
                            if (!isTacSent) {
                                requestTacCode()
                            } else {
                                verifyTacAndRegister()
                            }
                        },
                        enabled = !isloading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8))
                    ) {
                        if (isloading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = if (!isTacSent) stringResource(R.string.send_tac_code) else stringResource(R.string.verify_and_register),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (isTacSent) {
                        TextButton(
                            onClick = {
                                isTacSent = false
                                tacCode = ""
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(stringResource(R.string.change_phone_tac))
                        }
                    }

                    // OR Divider
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
                        Text(
                            text = stringResource(R.string.or),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8)
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
                    }

                    // Sign up with Google Button
                    OutlinedButton(
                        onClick = { launchGoogleSignIn() },
                        enabled = !isloading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = FontAwesomeIcons.Brands.Google,
                                contentDescription = "Google Logo",
                                tint = Color(0xFFEA4335),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = stringResource(R.string.sign_up_with_google),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Footer: Already have an account? Log in
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.already_have_an_account),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = stringResource(R.string.login),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue,
                            modifier = Modifier.clickable { onBackToLogin() }
                        )
                    }
                }
            }
        }

        if (showRegistrationSuccess) {
            AlertDialog(
                onDismissRequest = {
                    showRegistrationSuccess = false
                    onRegisterSuccess(registeredUsername)
                },
                title = { Text(registrationSuccessTitle) },
                text = {
                    Text(registrationSuccessMessage)
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showRegistrationSuccess = false
                            onRegisterSuccess(registeredUsername)
                        }
                    ) {
                        Text(stringResource(R.string.continue_to_login))
                    }
                }
            )
        }
    }
}

@Composable
fun PhoneVerificationBookingDialog(
    initialPhone: String,
    username: String,
    firebaseRepo: FirebaseRepository,
    onVerificationSuccess: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCountry by remember { mutableStateOf(defaultCountryCodes[0]) }
    var phoneInput by remember { mutableStateOf(initialPhone.removePrefix(selectedCountry.code).removePrefix("+")) }
    var tacCode by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf<String?>(null) }
    var isTacSent by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val context = LocalContext.current
    val activity = context as? Activity
    val auth = remember { FirebaseAuth.getInstance() }
    val scope = rememberCoroutineScope()

    fun getFullPhoneNumber(): String {
        val trimmed = phoneInput.trim()
        return if (trimmed.startsWith("+")) trimmed else {
            val digits = trimmed.removePrefix("0")
            "${selectedCountry.code}$digits"
        }
    }

    fun requestTacCode() {
        if (activity == null) {
            errorMessage = "Activity Reference Error"
            return
        }

        isLoading = true
        errorMessage = ""
        val fullPhone = getFullPhoneNumber()

        scope.launch {
            val customer = if (username.isNotBlank()) firebaseRepo.getUser(username) else null
            val currentUserUid = customer?.uid ?: FirebaseAuth.getInstance().currentUser?.uid ?: ""
            if (firebaseRepo.isPhoneRegistered(fullPhone, currentUserUid)) {
                isLoading = false
                errorMessage = "This phone number is already registered to another account"
                return@launch
            }

            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(fullPhone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        scope.launch {
                            isLoading = false
                            val verifiedPhone = fullPhone
                            val customerCheck = if (username.isNotBlank()) firebaseRepo.getUser(username) else null
                            val uidCheck = customerCheck?.uid ?: FirebaseAuth.getInstance().currentUser?.uid ?: ""
                            if (firebaseRepo.isPhoneRegistered(verifiedPhone, uidCheck)) {
                                errorMessage = "This phone number is already registered to another account"
                                return@launch
                            }
                            onVerificationSuccess(verifiedPhone)
                        }
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        isLoading = false
                        errorMessage = e.message ?: context.getString(R.string.verification_failed)
                    }

                    override fun onCodeSent(vId: String, token: PhoneAuthProvider.ForceResendingToken) {
                        isLoading = false
                        isTacSent = true
                        verificationId = vId
                    }
                }).build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }

    fun verifyTac() {
        val fullPhone = getFullPhoneNumber()
        if (tacCode.length != 6) {
            errorMessage = context.getString(R.string.error_tac_message)
            return
        }
        scope.launch {
            val customer = if (username.isNotBlank()) firebaseRepo.getUser(username) else null
            val currentUserUid = customer?.uid ?: FirebaseAuth.getInstance().currentUser?.uid ?: ""
            if (firebaseRepo.isPhoneRegistered(fullPhone, currentUserUid)) {
                errorMessage = "This phone number is already registered to another account"
                return@launch
            }

            if (verificationId != null) {
                isLoading = true
                val credential = PhoneAuthProvider.getCredential(verificationId!!, tacCode.trim())
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        isLoading = false
                        if (task.isSuccessful) {
                            onVerificationSuccess(fullPhone)
                        } else {
                            errorMessage = task.exception?.message ?: context.getString(R.string.invalid_tac)
                        }
                    }
            } else {
                errorMessage = context.getString(R.string.invalid_verification_request)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.phone_verification_required)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.phone_verfication_required_desc),
                    style = MaterialTheme.typography.bodyMedium
                )

                CountryCodePhoneInput(
                    phoneValue = phoneInput,
                    onPhoneValueChange = {
                        phoneInput = it
                        errorMessage = ""
                    },
                    selectedCountry = selectedCountry,
                    onCountrySelected = { selectedCountry = it },
                    enabled = !isTacSent,
                    label = stringResource(R.string.phone),
                    modifier = Modifier.fillMaxWidth()
                )

                if (isTacSent) {
                    OutlinedTextField(
                        value = tacCode,
                        onValueChange = {
                            if (it.length <= 6) tacCode = it
                            errorMessage = ""
                        },
                        label = { Text(stringResource(R.string.six_digit_tac)) },
                        placeholder = { Text(stringResource(R.string.enter_tac)) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                }

                if (errorMessage.isNotEmpty()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (!isTacSent) {
                        val fullPhone = getFullPhoneNumber()
                        if (isValidPhone(fullPhone)) {
                            requestTacCode()
                        } else {
                            errorMessage = context.getString(R.string.error_invalid_phone)
                        }
                    } else {
                        verifyTac()
                    }
                },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text(if (!isTacSent) stringResource(R.string.send_tac_code) else stringResource(
                        R.string.verify_tac_complete_booking))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

private suspend fun handleGoogleSignInUser(
    firebaseRepo: FirebaseRepository,
    context: Context,
    email: String,
    displayName: String,
    phone: String = ""
): Pair<String, Boolean> {
    val cleanEmail = email.trim()
    val existingUser = if (cleanEmail.isNotBlank()) firebaseRepo.getUser(cleanEmail) else null

    if (existingUser != null && existingUser.isBanned) {
        throw IllegalStateException(context.getString(R.string.your_account_has_been_banned))
    }

    if (existingUser != null) {
        // User already exists with this email! Do NOT overwrite anything or change username.
        return Pair(existingUser.username, true)
    }

    val nameToUse = if (displayName.isNotBlank()) displayName else cleanEmail.substringBefore("@")
    val parts = nameToUse.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
    val targetUsername = when {
        parts.size >= 2 -> "${parts[0]} ${parts[1]}"
        parts.size == 1 -> parts[0]
        else -> cleanEmail.substringBefore("@").ifBlank { "Traveler" }
    }

    val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val newUser = User(
        uid = currentUserUid,
        username = targetUsername,
        email = cleanEmail,
        phone = phone,
        name = displayName
    )

    firebaseRepo.saveUser(newUser)
    return Pair(targetUsername, false)
}
