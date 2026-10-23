package com.example.appproject.ui.screens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.appproject.MainActivity
import com.example.appproject.R
import com.example.appproject.data.model.Booking
import com.example.appproject.data.repository.FirebaseRepository
import com.example.appproject.ui.theme.AppTheme
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.rememberPaymentSheet
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

class PaymentScreen : ComponentActivity() {

    private val publishableKey = "pk_test_51UNOmW984JcO38MUl2fQbGvBbgnCyLVaPdPhwlUTMSR1dXkjrZzV8Gl5VV96wZKTtv2UUaVOqFcy48zCvgpr3FpH00mrOstu0x"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Stripe Configuration
        PaymentConfiguration.init(applicationContext, publishableKey)

        val bookingId = intent.getStringExtra("BOOKING_ID") ?: ""
        val loggedInUserExtra = intent.getStringExtra("LOGGED_IN_USER")
        val loggedInUser = if (!loggedInUserExtra.isNullOrBlank()) {
            loggedInUserExtra
        } else {
            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
        }
        val firebaseRepo = FirebaseRepository()

        setContent {
            AppTheme {
                PaymentScreen(
                    bookingId = bookingId,
                    firebaseRepo = firebaseRepo,
                    loggedInUser = loggedInUser,
                    onBack = { finish() },
                    onRedirectToBookings = {
                        val intent = Intent(this@PaymentScreen, MainActivity::class.java).apply {
                            putExtra("navigate_to_booking_id", bookingId)
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                        startActivity(intent)
                        finish()
                    },
                    onViewReceipt = { finish() },
                    onViewInvoice = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    bookingId: String,
    firebaseRepo: FirebaseRepository,
    loggedInUser: String?,
    onBack: () -> Unit,
    onRedirectToBookings: (() -> Unit)? = null,
    onViewReceipt: ((String) -> Unit)? = null,
    onViewInvoice: ((String) -> Unit)? = null,
    stripeClientSecret: String? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var booking by remember { mutableStateOf<Booking?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isAuthorized by remember { mutableStateOf(true) }
    var isProcessingPayment by remember { mutableStateOf(false) }
    var paymentSuccessDialog by remember { mutableStateOf(false) }

    // Proof file temporary selection
    var selectedProofUri by remember { mutableStateOf<Uri?>(null) }
    var selectedProofFileName by remember { mutableStateOf("") }

    // Promo / Voucher
    var voucherCodeInput by remember { mutableStateOf("") }
    var appliedVoucherCode by remember { mutableStateOf("") }
    var discountValue by remember { mutableDoubleStateOf(0.0) }
    var voucherDiscountPercent by remember { mutableDoubleStateOf(10.0) }
    var voucherMessage by remember { mutableStateOf<String?>(null) }
    var isVoucherValid by remember { mutableStateOf(false) }
    var isPromoExpanded by remember { mutableStateOf(false) }

    // Strings for callbacks & UI messages
    val paymentCancelledMsg = stringResource(R.string.payment_cancelled)
    val paymentFailedMsg = stringResource(R.string.payment_failed)
    val voucherAlreadyUsedMsg = stringResource(R.string.voucher_already_used)
    val invalidVoucherMsg = stringResource(R.string.invalid_voucher)

    val publishableKey = "pk_test_51UNOmW984JcO38MUl2fQbGvBbgnCyLVaPdPhwlUTMSR1dXkjrZzV8Gl5VV96wZKTtv2UUaVOqFcy48zCvgpr3FpH00mrOstu0x"

    // Stripe PaymentSheet integration
    val paymentSheet = rememberPaymentSheet { result ->
        when (result) {
            is PaymentSheetResult.Completed -> {
                isProcessingPayment = false
                scope.launch {
                    processPaymentCompletion(
                        booking = booking,
                        firebaseRepo = firebaseRepo,
                        methodName = "Stripe Credit/Debit Card",
                        appliedVoucher = appliedVoucherCode,
                        discountVal = discountValue,
                        onSuccess = { updated ->
                            booking = updated
                            paymentSuccessDialog = true
                        }
                    )
                }
            }
            is PaymentSheetResult.Canceled -> {
                isProcessingPayment = false
                Toast.makeText(context, paymentCancelledMsg, Toast.LENGTH_SHORT).show()
            }
            is PaymentSheetResult.Failed -> {
                isProcessingPayment = false
                Log.e("PaymentScreen", "Payment failed: ${result.error.localizedMessage}", result.error)
                Toast.makeText(
                    context,
                    "$paymentFailedMsg: ${result.error.localizedMessage ?: "Unknown error"}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    LaunchedEffect(bookingId, loggedInUser) {
        PaymentConfiguration.init(context, publishableKey)
        if (bookingId.isNotBlank()) {
            val fetched = firebaseRepo.getBooking(bookingId)
            booking = fetched
            if (fetched != null) {
                isAuthorized = firebaseRepo.isUserAuthorizedForBooking(fetched, loggedInUser)
                if (fetched.voucherUsed.isNotBlank()) {
                    appliedVoucherCode = fetched.voucherUsed
                    voucherCodeInput = fetched.voucherUsed
                    discountValue = parseCurrencyToDouble(fetched.discountAmount)
                    val basePriceStr = fetched.basePrice.ifBlank { fetched.finalPrice }
                    val rawPrice = if (basePriceStr.isNotBlank()) parseCurrencyToDouble(basePriceStr) else 3250000.0
                    if (rawPrice > 0 && discountValue > 0) {
                        voucherDiscountPercent = (discountValue / rawPrice) * 100.0
                    }
                    isPromoExpanded = true
                }
            }
        }
        isLoading = false
    }

    LaunchedEffect(paymentSuccessDialog) {
        if (paymentSuccessDialog) {
            Toast.makeText(context, context.getString(R.string.payment_successful), Toast.LENGTH_SHORT).show()
            delay(2500)
            if (paymentSuccessDialog) {
                paymentSuccessDialog = false
                if (onRedirectToBookings != null) {
                    onRedirectToBookings()
                } else if (onViewReceipt != null && booking != null) {
                    onViewReceipt(booking!!.bookingId)
                } else {
                    onBack()
                }
            }
        }
    }

    var selectedPaymentOption by remember { mutableStateOf(PaymentOption.STRIPE_CARD) }

    val currentBooking = booking
    val basePriceStr = currentBooking?.basePrice?.ifBlank { currentBooking.finalPrice } ?: ""
    val rawBasePrice = if (basePriceStr.isNotBlank()) parseCurrencyToDouble(basePriceStr) else 3250000.0
    val calculatedFinalPrice = (rawBasePrice - discountValue).coerceAtLeast(0.0)

    Scaffold(
        containerColor = Color(0xFFF5F7FA),
        topBar = {
            PaymentHeader(onBack = onBack)
        },
        bottomBar = {
            if (!isLoading && currentBooking != null && isAuthorized) {
                Surface(
                    color = Color.White,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        if (selectedPaymentOption == PaymentOption.STRIPE_CARD) {
                            Button(
                                onClick = {
                                    isProcessingPayment = true
                                    scope.launch {
                                        val targetCurrency = "idr"
                                        val (clientSecret, errorMsg) = if (!stripeClientSecret.isNullOrBlank()) {
                                            Pair(stripeClientSecret, null)
                                        } else {
                                            createStripePaymentIntent(
                                                calculatedFinalPrice,
                                                com.example.appproject.BuildConfig.STRIPE_SECRET_KEY,
                                                currency = targetCurrency
                                            )
                                        }

                                        if (!clientSecret.isNullOrBlank()) {
                                            isProcessingPayment = false
                                            try {
                                                val config = PaymentSheet.Configuration(
                                                    merchantDisplayName = "PT ZUHANTO TRAVEL INDONESIA"
                                                )
                                                paymentSheet.presentWithPaymentIntent(
                                                    clientSecret,
                                                    config
                                                )
                                            } catch (e: Exception) {
                                                isProcessingPayment = false
                                                Log.e("PaymentScreen", "Error launching PaymentSheet", e)
                                                Toast.makeText(
                                                    context,
                                                    "An error occurred while launching payment. Please try again.",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        } else {
                                            isProcessingPayment = false
                                            Log.e("PaymentScreen", "Stripe Error: ${errorMsg ?: "Failed to generate client secret"}")
                                            Toast.makeText(
                                                context,
                                                errorMsg ?: "Failed to initialize payment. Please try again later.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                shape = RoundedCornerShape(16.dp),
                                enabled = !isProcessingPayment,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1662D8))
                            ) {
                                if (isProcessingPayment) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Lock,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(R.string.pay) + " ${formatCurrency(calculatedFinalPrice)} via Card",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        } else {
                            Button(
                                onClick = {
                                    val b = currentBooking
                                    if (b != null) {
                                        val hasFileAttached = selectedProofUri != null || b.proofOfPaymentUrl.isNotBlank()
                                        if (!hasFileAttached) {
                                            Toast.makeText(context, R.string.please_select_a_file, Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }

                                        scope.launch {
                                            val fileUriToUse = selectedProofUri ?: if (b.proofOfPaymentUrl.isNotBlank()) Uri.parse(b.proofOfPaymentUrl) else null
                                            val fileNameToSave = selectedProofFileName.ifBlank { b.proofOfPaymentFileName.ifBlank { "proof_receipt" } }
                                            val updatedBooking = b.copy(
                                                proofOfPaymentUploaded = true,
                                                proofOfPaymentFileName = fileNameToSave,
                                                proofOfPaymentUrl = selectedProofUri?.toString() ?: b.proofOfPaymentUrl,
                                                paymentStatus = "Pending",
                                                paymentMethod = "Bank Transfer"
                                            )
                                            firebaseRepo.updateBooking(updatedBooking)
                                            booking = updatedBooking

                                            sendProofToWhatsApp(
                                                context = context,
                                                fileUri = fileUriToUse,
                                                bookingId = b.bookingId,
                                                customerName = b.customerName.ifBlank { b.customerUsername },
                                                finalPrice = b.finalPrice,
                                                destination = b.destination
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.send_proof_to_whatsapp),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF1662D8))
            }
        } else if (booking == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.receipt_not_found),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF0F172A)
                )
            }
        } else if (!isAuthorized) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.access_denied),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            val b = currentBooking!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 0. Payment Timer Banner
                if (b.confirmationTimestamp > 0L && b.paymentStatus != "Paid") {
                    if (b.proofOfPaymentUploaded) {

                    } else {
                        PaymentCountdownTimerCard(confirmationTimestamp = b.confirmationTimestamp)
                    }
                }

                // 1. Booking Summary Card
                BookingSummaryCard(booking = b)

                // 2. Promo Code Card
                PromoCodeCard(
                    voucherCodeInput = voucherCodeInput,
                    onVoucherCodeChange = { voucherCodeInput = it },
                    appliedVoucherCode = appliedVoucherCode,
                    voucherMessage = voucherMessage,
                    isVoucherValid = isVoucherValid,
                    isPromoExpanded = isPromoExpanded,
                    onToggleExpand = { isPromoExpanded = !isPromoExpanded },
                    voucherDiscountPercent = voucherDiscountPercent,
                    onApplyVoucher = {
                        scope.launch {
                            val code = voucherCodeInput.trim().uppercase()
                            if (code.isBlank()) return@launch
                            val allVouchers = firebaseRepo.getAllVouchers()
                            val matched = allVouchers.find {
                                it["code"]?.toString()?.equals(code, ignoreCase = true) == true
                            }
                            if (matched != null) {
                                val targetUserForVoucher = b.customerUid.ifBlank {
                                    b.customerUsername.ifBlank {
                                        com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                                    }
                                }
                                val isUsed = firebaseRepo.isVoucherUsedByUser(
                                    code,
                                    targetUserForVoucher
                                )
                                if (isUsed) {
                                    voucherMessage = voucherAlreadyUsedMsg
                                    isVoucherValid = false
                                } else {
                                    val discPercent =
                                        matched["discountPercentage"]?.toString()?.toDoubleOrNull()
                                            ?: matched["discount"]?.toString()?.toDoubleOrNull()
                                            ?: 10.0
                                    voucherDiscountPercent = discPercent
                                    val calcDisc = rawBasePrice * (discPercent / 100.0)
                                    discountValue = calcDisc
                                    appliedVoucherCode = code
                                    isVoucherValid = true
                                    voucherMessage = "$code ," + context.getString(R.string.applied) + "${discPercent.toInt()}% off"
                                }
                            } else {
                                voucherMessage = invalidVoucherMsg
                                isVoucherValid = false
                            }
                        }
                    },
                    onRemoveVoucher = {
                        appliedVoucherCode = ""
                        discountValue = 0.0
                        voucherCodeInput = ""
                        voucherMessage = null
                        isVoucherValid = false
                    }
                )

                // 3. Payment Method Section Selector
                PaymentMethodOptionSelector(
                    selectedOption = selectedPaymentOption,
                    onOptionSelected = { selectedPaymentOption = it }
                )

                // 4. Active Section Content: When Credit/Debit Card is selected, Bank Transfer & Upload Proof are HIDDEN.
                // When Bank Transfer is selected, Credit/Debit Card details are HIDDEN.
                if (selectedPaymentOption == PaymentOption.STRIPE_CARD) {
                    StripePaymentMethodCard()
                } else {
                    BankTransferSection(
                        booking = b,
                        firebaseRepo = firebaseRepo,
                        onBookingUpdated = { updated -> booking = updated },
                        onFilePicked = { uri, fileName ->
                            selectedProofUri = uri
                            selectedProofFileName = fileName
                        }
                    )
                }

                // 5. Price Breakdown Card
                PriceSummaryCard(
                    basePrice = rawBasePrice,
                    discount = discountValue,
                    appliedVoucherCode = appliedVoucherCode,
                    finalPrice = calculatedFinalPrice
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    // Success Dialog
    if (paymentSuccessDialog && booking != null) {
        val b = booking!!
        val paymentMethodDisplay = remember(b.paymentMethod) {
            val pm = b.paymentMethod.lowercase()
            when {
                pm.contains("card") || pm.contains("stripe") -> "Credit / debit card"
                pm.contains("bank") || pm.contains("transfer") -> "Bank transfer"
                else -> b.paymentMethod.ifBlank { "Credit / debit card" }
            }
        }

        Dialog(
            onDismissRequest = { paymentSuccessDialog = false },
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
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Top Double Circle Checkmark Badge
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color(0xFFE6F4EA), CircleShape)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color(0xFF059669), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    // Title & Subtitle
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.payment_successful),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.thank_you_paid),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Details Card Container
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF0F6FF),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Booking ID
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.booking_id),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = "#${b.bookingId}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                            }

                            HorizontalDivider(color = Color(0xFFE2E8F0))

                            // Package
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.package_label),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = b.destination.split(" - Note:").firstOrNull() ?: b.destination,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A),
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.widthIn(max = 180.dp)
                                )
                            }

                            HorizontalDivider(color = Color(0xFFE2E8F0))

                            // Paid with
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.paid_with),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF64748B)
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CreditCard,
                                        contentDescription = null,
                                        tint = Color(0xFF0F172A),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = paymentMethodDisplay,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                }
                            }

                            HorizontalDivider(color = Color(0xFFE2E8F0))

                            // Amount paid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.amount_paid),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = b.finalPrice,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1D4ED8)
                                )
                            }
                        }
                    }

                    // Primary Action Button (Go to my bookings)
                    Button(
                        onClick = {
                            paymentSuccessDialog = false
                            if (onRedirectToBookings != null) {
                                onRedirectToBookings()
                            } else if (onViewReceipt != null) {
                                onViewReceipt(b.bookingId)
                            } else {
                                onBack()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1D4ED8),
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.go_to_my_bookings),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Secondary Action Button (View invoice)
                    TextButton(
                        onClick = {
                            paymentSuccessDialog = false
                            if (onViewInvoice != null) {
                                onViewInvoice(b.bookingId)
                            } else {
                                onBack()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Description,
                                contentDescription = null,
                                tint = Color(0xFF1D4ED8),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(R.string.view_invoice),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D4ED8)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Back Button Circle
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFF0F172A),
                modifier = Modifier.size(20.dp)
            )
        }

        // Title
        Text(
            text = stringResource(R.string.payment),
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = Color(0xFF0F172A)
        )

        // Secure Payment Badge
        Surface(
            color = Color(0xFFE6F4EA),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = Color(0xFF166534),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = stringResource(R.string.secure_payment),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF166534)
                )
            }
        }
    }
}

@Composable
private fun BookingSummaryCard(booking: Booking) {
    val (packageTitle, paxInfo) = remember(booking.destination) {
        parseDestinationInfo(booking.destination)
    }
    val dateDisplay = booking.bookingDate.ifBlank { "" }
    val customerDisplay = booking.customerName.ifBlank { "" }.uppercase()
    val bookingCodeDisplay = if (booking.bookingId.isBlank()) "" else if (booking.bookingId.startsWith("#")) booking.bookingId else "#${booking.bookingId}"
    val statusDisplay = booking.status.ifBlank { "" }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Package title & Confirmed status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = packageTitle,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.weight(1f)
                )
            }

            // Subtitle: "2 pax · 24 Oct 2026"
            Text(
                text = "$paxInfo · $dateDisplay",
                fontSize = 14.sp,
                color = Color(0xFF64748B)
            )

            HorizontalDivider(
                color = Color(0xFFF1F5F9),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Customer & Booking Code
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.customer),
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = customerDisplay,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.booking_code),
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        textAlign = TextAlign.End
                    )
                    Text(
                        text = bookingCodeDisplay,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@Composable
private fun PromoCodeCard(
    voucherCodeInput: String,
    onVoucherCodeChange: (String) -> Unit,
    appliedVoucherCode: String,
    voucherMessage: String?,
    isVoucherValid: Boolean,
    isPromoExpanded: Boolean,
    onToggleExpand: () -> Unit,
    voucherDiscountPercent: Double,
    onApplyVoucher: () -> Unit,
    onRemoveVoucher: () -> Unit
) {
    val isExpanded = isPromoExpanded || appliedVoucherCode.isNotBlank()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row (Clickable when not applied to toggle expand/collapse)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFEFF6FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocalOffer,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = if (isExpanded) stringResource(R.string.promo_code) else stringResource(R.string.have_a_promo_code),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color(0xFF64748B)
                )
            }

            // Expanded content (Input + Apply button + Applied banner)
            if (isExpanded) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = voucherCodeInput,
                        onValueChange = onVoucherCodeChange,
                        placeholder = {
                            Text(
                                "e.g. NEWCOMER",
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Bold
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2563EB),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )

                    if (appliedVoucherCode.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF2563EB)),
                            color = Color.White,
                            modifier = Modifier.height(50.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 20.dp)
                                    .height(50.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.applied),
                                    color = Color(0xFF2563EB),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = onApplyVoucher,
                            modifier = Modifier.height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1662D8))
                        ) {
                            Text(
                                text = stringResource(R.string.apply),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Green Banner when applied
                if (appliedVoucherCode.isNotBlank()) {
                    Surface(
                        color = Color(0xFFE6F4EA),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF166534),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = stringResource(R.string.promo_code) + " $appliedVoucherCode " + stringResource(R.string.applied) + " " + stringResource(R.string.discount) + "(${voucherDiscountPercent.toInt()}%)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF166534)
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove voucher",
                                tint = Color(0xFF166534),
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { onRemoveVoucher() }
                            )
                        }
                    }
                }

                if (voucherMessage != null && !isVoucherValid) {
                    Text(
                        text = voucherMessage,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFDC2626)
                    )
                }
            }
        }
    }
}

@Composable
private fun StripePaymentMethodCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.5.dp, Color(0xFF2563EB))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEFF6FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CreditCard,
                        contentDescription = null,
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = stringResource(R.string.credit_debit_card),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = stringResource(R.string.secure_payment_by_stripe),
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            // Badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CardBadge(text = "Visa")
                CardBadge(text = "Mastercard")
                CardBadge(text = "AMEX")
                CardBadge(text = "JCB")
            }

            Surface(
                color = Color(0xFFECFDF5),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF059669),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = stringResource(R.string.instant_confirmation_no_waiting_time),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF047857)
                    )
                }
            }

            // Info note
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = stringResource(R.string.you_will_enter_your_card_details_on_the_next_step),
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}

@Composable
private fun CardBadge(text: String) {
    Surface(
        color = Color(0xFFEFF6FF),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2563EB),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun PriceSummaryCard(
    basePrice: Double,
    discount: Double,
    appliedVoucherCode: String,
    finalPrice: Double
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.price_summary),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Trip price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.trip_price),
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )
                Text(
                    text = formatCurrency(basePrice),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }

            // Taxes and service fees
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.taxes_and_fees),
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )
                Text(
                    text = stringResource(R.string.included),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF166534)
                )
            }

            // Discount row if applied
            if (discount > 0) {
                val discountLabel = if (appliedVoucherCode.isNotBlank()) "Promo $appliedVoucherCode" else stringResource(R.string.discount)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = discountLabel,
                        fontSize = 14.sp,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = "-${formatCurrency(discount)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF166534)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            DashedDivider(color = Color(0xFFCBD5E1))

            Spacer(modifier = Modifier.height(4.dp))

            // Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.total),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = formatCurrency(finalPrice),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1662D8)
                )
            }
        }
    }
}

@Composable
private fun DashedDivider(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFCBD5E1),
    dashWidth: Dp = 4.dp,
    dashGap: Dp = 4.dp,
    strokeWidth: Dp = 1.dp
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(strokeWidth)
    ) {
        val pathEffect = PathEffect.dashPathEffect(
            floatArrayOf(dashWidth.toPx(), dashGap.toPx()),
            0f
        )
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = strokeWidth.toPx(),
            pathEffect = pathEffect
        )
    }
}

private fun parseDestinationInfo(destination: String): Pair<String, String> {
    val clean = destination.split(" - Note:").firstOrNull()?.trim() ?: destination.trim()
    val openParen = clean.lastIndexOf('(')
    val closeParen = clean.lastIndexOf(')')
    if (openParen != -1 && closeParen > openParen) {
        val title = clean.substring(0, openParen).trim()
        val paxRaw = clean.substring(openParen + 1, closeParen).trim()
        val paxFormatted = if (paxRaw.contains("pax", ignoreCase = true)) paxRaw.lowercase() else "$paxRaw pax"
        return Pair(if (title.isNotBlank()) title else clean, paxFormatted)
    }
    return Pair(if (clean.isNotBlank()) clean else "Bromo Sunrise Package", "2 pax")
}

private suspend fun createStripePaymentIntent(
    amountInCurrency: Double,
    secretKey: String = com.example.appproject.BuildConfig.STRIPE_SECRET_KEY,
    currency: String = "idr",
    paymentMethodType: String? = null
): Pair<String?, String?> = withContext(Dispatchers.IO) {
    try {
        if (secretKey.isBlank()) {
            return@withContext Pair(null, "Stripe secret key is missing in local properties")
        }

        val stripeUrl = URL("https://api.stripe.com/v1/payment_intents")
        val stripeConn = stripeUrl.openConnection() as HttpURLConnection
        stripeConn.requestMethod = "POST"
        stripeConn.setRequestProperty("Authorization", "Bearer $secretKey")
        stripeConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        stripeConn.connectTimeout = 15000
        stripeConn.readTimeout = 15000
        stripeConn.doOutput = true

        val amountInt = (amountInCurrency * 100).toLong().coerceAtLeast(100L)
        val postData = if (!paymentMethodType.isNullOrEmpty()) {
            "amount=$amountInt&currency=$currency&payment_method_types[]=$paymentMethodType"
        } else {
            "amount=$amountInt&currency=$currency&automatic_payment_methods[enabled]=true"
        }

        stripeConn.outputStream.use { os ->
            os.write(postData.toByteArray(Charsets.UTF_8))
        }

        val responseCode = stripeConn.responseCode
        val stream = if (responseCode in 200..299) stripeConn.inputStream else stripeConn.errorStream
        val responseText = stream?.bufferedReader()?.use { it.readText() } ?: ""

        if (responseCode in 200..299) {
            val jsonObject = JSONObject(responseText)
            val clientSecret = jsonObject.optString("client_secret", "")
            if (clientSecret.isNotBlank()) {
                return@withContext Pair(clientSecret, null)
            }
        }

        val errorDetail = if (responseText.isNotBlank()) {
            try {
                val jsonErr = JSONObject(responseText)
                jsonErr.optJSONObject("error")?.optString("message", responseText) ?: responseText
            } catch (_: Exception) {
                responseText
            }
        } else "Stripe Error ($responseCode)"

        Pair(null, errorDetail)
    } catch (e: Exception) {
        Log.e("PaymentScreen", "Exception creating PaymentIntent", e)
        Pair(null, e.localizedMessage ?: "Network error")
    }
}

private suspend fun processPaymentCompletion(
    booking: Booking?,
    firebaseRepo: FirebaseRepository,
    methodName: String,
    appliedVoucher: String,
    discountVal: Double,
    onSuccess: (Booking) -> Unit
) {
    if (booking == null) return
    val basePriceStr = booking.basePrice.ifBlank { booking.finalPrice }
    val rawBasePrice = parseCurrencyToDouble(basePriceStr)
    val finalCalculated = (rawBasePrice - discountVal).coerceAtLeast(0.0)

    val updatedBooking = booking.copy(
        paymentStatus = "Paid",
        paymentMethod = methodName,
        voucherUsed = appliedVoucher,
        discountAmount = if (discountVal > 0) formatCurrency(discountVal) else booking.discountAmount,
        basePrice = formatCurrency(rawBasePrice),
        finalPrice = formatCurrency(finalCalculated)
    )

    firebaseRepo.updateBooking(updatedBooking)

    // Mark voucher as used if applicable
    val uidToRecord = booking.customerUid.ifBlank {
        booking.customerUsername.ifBlank {
            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
        }
    }
    if (appliedVoucher.isNotBlank() && uidToRecord.isNotBlank()) {
        try {
            firebaseRepo.recordVoucherUsage(appliedVoucher, uidToRecord)
        } catch (_: Exception) {}
    }

    onSuccess(updatedBooking)
}

private fun parseCurrencyToDouble(priceStr: String): Double {
    if (priceStr.isBlank()) return 0.0
    val cleaned = priceStr.replace("[^0-9]".toRegex(), "")
    return cleaned.toDoubleOrNull() ?: 0.0
}

private fun formatCurrency(amount: Double): String {
    return "IDR " + String.format(Locale.US, "%,d", amount.toLong())
}


@Composable
private fun PaymentCountdownTimerCard(confirmationTimestamp: Long) {
    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(confirmationTimestamp) {
        if (confirmationTimestamp > 0L) {
            while (true) {
                currentTime = System.currentTimeMillis()
                delay(1000L)
            }
        }
    }

    val deadline = confirmationTimestamp + 2L * 24 * 60 * 60 * 1000L
    val timeLeftMs = (deadline - currentTime).coerceAtLeast(0L)

    val totalSeconds = timeLeftMs / 1000L
    val days = totalSeconds / (24 * 3600)
    val hours = (totalSeconds % (24 * 3600)) / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    val timeString = buildString {
        if (days > 0) append("${days}d ")
        append(String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds))
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFEF3C7),
        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFDE68A)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = "Payment Time Remaining",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB45309)
                )
                Text(
                    text = timeString,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF92400E)
                )
            }
        }
    }
}

@Composable
private fun ProofOfPaymentCard(
    booking: Booking,
    firebaseRepo: FirebaseRepository,
    onBookingUpdated: (Booking) -> Unit,
    onFilePicked: (Uri?, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf(booking.proofOfPaymentFileName) }
    var uploadErrorMessage by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}

            var fileName = "proof_of_payment"
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIdx != -1) {
                        fileName = cursor.getString(nameIdx)
                    }
                }
            }

            val lower = fileName.lowercase()
            val mimeType = (context.contentResolver.getType(uri) ?: "").lowercase()
            val isPdfJpg = lower.endsWith(".pdf") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") ||
                    mimeType.contains("pdf") || mimeType.contains("jpeg") || mimeType.contains("jpg")

            if (isPdfJpg) {
                selectedUri = uri
                selectedFileName = fileName
                uploadErrorMessage = null
                onFilePicked(uri, fileName)
                Toast.makeText(context, R.string.proof_of_payment_saved, Toast.LENGTH_SHORT).show()
            } else {
                uploadErrorMessage = context.getString(R.string.invalid_file_type)
                Toast.makeText(context, uploadErrorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFEF3C7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = stringResource(R.string.upload_proof_of_payment),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = stringResource(R.string.supported_formats),
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            if (booking.proofOfPaymentUploaded) {
                Surface(
                    color = Color(0xFFECFDF5),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF059669),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.proof_of_payment_uploaded),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF047857)
                            )
                        }
                        if (booking.proofOfPaymentFileName.isNotBlank()) {
                            Text(
                                text = "File: ${booking.proofOfPaymentFileName}",
                                fontSize = 12.sp,
                                color = Color(0xFF065F46)
                            )
                        }
                        Text(
                            text = stringResource(R.string.payment_remaining_time_had_been_paused),
                            fontSize = 12.sp,
                            color = Color(0xFF047857)
                        )
                    }
                }
            } else {

            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        filePickerLauncher.launch(
                            arrayOf(
                                "application/pdf",
                                "image/jpeg",
                                "image/jpg",
                                "image/*"
                            )
                        )
                    },
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFCBD5E1))
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
                            imageVector = if (selectedFileName.endsWith(".pdf", ignoreCase = true)) Icons.Default.PictureAsPdf else Icons.Default.AttachFile,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = if (selectedFileName.isNotBlank()) selectedFileName else stringResource(R.string.tap_to_select),
                            fontSize = 13.sp,
                            fontWeight = if (selectedFileName.isNotBlank()) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedFileName.isNotBlank()) Color(0xFF0F172A) else Color(0xFF64748B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = if (selectedFileName.isNotBlank()) stringResource(R.string.change) else stringResource(R.string.browse),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2563EB)
                    )
                }
            }

            if (uploadErrorMessage != null) {
                Text(
                    text = uploadErrorMessage!!,
                    fontSize = 12.sp,
                    color = Color(0xFFDC2626),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

fun sendProofToWhatsApp(
    context: Context,
    fileUri: Uri?,
    bookingId: String,
    customerName: String,
    finalPrice: String,
    destination: String
) {
    val phone = "601169776714"
    val cleanDestination = destination.split(" - Note:").firstOrNull()?.trim() ?: destination
    val messageText = context.getString(R.string.proof_of_payment_for_booking) + "#$bookingId\n" + context.getString(R.string.customer) + ": $customerName \n" + context.getString(R.string.destination) + ": $destination \n" + context.getString(R.string.amount_paid) + ": $finalPrice"

    if (fileUri != null) {
        val mimeType = try {
            context.contentResolver.getType(fileUri) ?: "*/*"
        } catch (_: Exception) {
            "*/*"
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, fileUri)
            putExtra(Intent.EXTRA_TEXT, messageText)
            putExtra("jid", "$phone@s.whatsapp.net")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val whatsappIntent = Intent(intent).apply { setPackage("com.whatsapp") }
        try {
            context.startActivity(whatsappIntent)
            return
        } catch (_: Exception) {}

        val w4bIntent = Intent(intent).apply { setPackage("com.whatsapp.w4b") }
        try {
            context.startActivity(w4bIntent)
            return
        } catch (_: Exception) {}

        try {
            val chooser = Intent.createChooser(intent, "Send Proof of Payment via WhatsApp")
            context.startActivity(chooser)
            return
        } catch (_: Exception) {}
    }

    try {
        val url = "https://api.whatsapp.com/send?phone=$phone&text=${Uri.encode(messageText)}"
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(webIntent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open WhatsApp: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}

enum class PaymentOption {
    STRIPE_CARD,
    BANK_TRANSFER
}

@Composable
private fun PaymentMethodOptionSelector(
    selectedOption: PaymentOption,
    onOptionSelected: (PaymentOption) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.select_payment_method),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            // Section 1: Credit / Debit Card (Stripe)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOptionSelected(PaymentOption.STRIPE_CARD) },
                shape = RoundedCornerShape(14.dp),
                color = if (selectedOption == PaymentOption.STRIPE_CARD) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                border = BorderStroke(
                    1.5.dp,
                    if (selectedOption == PaymentOption.STRIPE_CARD) Color(0xFF2563EB) else Color(0xFFE2E8F0)
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RadioButton(
                        selected = selectedOption == PaymentOption.STRIPE_CARD,
                        onClick = { onOptionSelected(PaymentOption.STRIPE_CARD) },
                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF2563EB))
                    )

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CreditCard,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.credit_debit_card),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = stringResource(R.string.instant_payment),
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            // Section 2: Bank Transfer
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOptionSelected(PaymentOption.BANK_TRANSFER) },
                shape = RoundedCornerShape(14.dp),
                color = if (selectedOption == PaymentOption.BANK_TRANSFER) Color(0xFFFEF3C7).copy(alpha = 0.5f) else Color(0xFFF8FAFC),
                border = BorderStroke(
                    1.5.dp,
                    if (selectedOption == PaymentOption.BANK_TRANSFER) Color(0xFFD97706) else Color(0xFFE2E8F0)
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RadioButton(
                        selected = selectedOption == PaymentOption.BANK_TRANSFER,
                        onClick = { onOptionSelected(PaymentOption.BANK_TRANSFER) },
                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFD97706))
                    )

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.bank_transfer),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = stringResource(R.string.send_proof_of_payment),
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BankTransferSection(
    booking: Booking,
    firebaseRepo: FirebaseRepository,
    onBookingUpdated: (Booking) -> Unit,
    onFilePicked: (Uri?, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFEF3C7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = stringResource(R.string.bank_transfer_account_details),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = stringResource(R.string.transfer_to_our_official_company_account),
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFF1F5F9))

            Column {
                Text(
                    text = stringResource(R.string.account_holder_name),
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
                Text(
                    text = "PT ZUHANTO TRAVEL INDONESIA",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }

            BankAccountRow(
                bankName = "Bank Central Asia (BCA)",
                accountNumber = "8830-192-888",
                onCopy = {
                    clipboardManager.setText(AnnotatedString("8830192888"))
                    Toast.makeText(context, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
                }
            )

            BankAccountRow(
                bankName = "Bank Mandiri",
                accountNumber = "142-00-1982345-1",
                onCopy = {
                    clipboardManager.setText(AnnotatedString("1420019823451"))
                    Toast.makeText(context, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show()
                }
            )

            ProofOfPaymentCard(
                booking = booking,
                firebaseRepo = firebaseRepo,
                onBookingUpdated = onBookingUpdated,
                onFilePicked = onFilePicked
            )
        }
    }
}

@Composable
private fun BankAccountRow(
    bankName: String,
    accountNumber: String,
    onCopy: () -> Unit
) {
    Surface(
        color = Color(0xFFF8FAFC),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = bankName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF475569)
                )
                Text(
                    text = accountNumber,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F172A)
                )
            }

            IconButton(
                onClick = onCopy,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy account number",
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

