package com.example.appproject.data.model

import android.content.Context
import android.util.Patterns
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.os.LocaleListCompat
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.fromHtml
import com.example.appproject.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

//Set up the language change between Indonesian and English
object LanguageManager {
    fun setLanguage(code: String) {
        val local = Locale(code)
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.create(local))
    }
}

data class LanguageOption(
    val code: String,
    val displayName: String,
    val nativeName: String
)

data class ItineraryStep(
    val time: String = "",
    val activity: String = ""
)

data class ItineraryDay(
    val dayTitle: String = "",
    val steps: List<ItineraryStep> = emptyList()
)

data class PricingTier(
    val paxCount: Int = 2,
    val paxLabel: String = "",
    val pricePerPaxInt: Int = 0,
    val priceFormatted: String = ""
)

fun getFormattedTierPrice(tier: PricingTier): String {
    val pax = tier.paxCount
    val isCouple = pax == 2 || tier.paxLabel.contains("Couple", ignoreCase = true) || tier.paxLabel.contains("2 Pax", ignoreCase = true)

    if (isCouple) {
        if (tier.priceFormatted.isNotBlank()) {
            val cleaned = tier.priceFormatted.replace(Regex("""\s*\([^)]*/\s*pax\)""", RegexOption.IGNORE_CASE), "").trim()
            if (cleaned.isNotBlank()) return cleaned
        }
        if (tier.pricePerPaxInt <= 0) return ""
        val totalInt = tier.pricePerPaxInt * 2
        val totalFormatted = "IDR " + String.format(Locale.US, "%,d", totalInt)
        return "$totalFormatted / couple"
    }

    if (tier.priceFormatted.isNotBlank()) return tier.priceFormatted
    if (tier.pricePerPaxInt <= 0) return ""
    val perPax = tier.pricePerPaxInt
    val totalInt = perPax * pax
    val perPaxFormatted = "IDR " + String.format(Locale.US, "%,d", perPax)
    if (pax > 1) {
        val totalFormatted = "IDR " + String.format(Locale.US, "%,d", totalInt)
        return "$totalFormatted ($perPaxFormatted / pax)"
    }
    return "$perPaxFormatted / pax"
}

fun parseBookingDate(dateStr: String): Date? {
    if (dateStr.isBlank()) return null
    val locales = listOf(Locale.US, Locale("id"), Locale.getDefault(), Locale.ENGLISH)
    val patterns = listOf("dd MMM yyyy", "dd-MM-yyyy", "yyyy-MM-dd", "dd/MM/yyyy")
    for (pat in patterns) {
        for (loc in locales) {
            try {
                val sdf = SimpleDateFormat(pat, loc)
                val parsed = sdf.parse(dateStr)
                if (parsed != null) return parsed
            } catch (_: Exception) {}
        }
    }
    return null
}

fun safeAnnotatedStringHtml(html: String): AnnotatedString {
    return try {
        AnnotatedString.fromHtml(html)
    } catch (_: Throwable) {
        AnnotatedString(html.replace(Regex("<[^>]*>"), ""))
    }
}

data class TripPromo(
    val name: String,
    val description: String,
    val price: String,
    val imageRes: Int = R.drawable.background_dashboard,
    val subtext: AnnotatedString = safeAnnotatedStringHtml(""),
    val badge: String = "",
    val duration: String = "",
    val transport: String = "",
    val participant: String = "",
    val hotel: String = "",
    val itineraries: List<ItineraryDay>? = emptyList(),
    val pricingTiers: List<PricingTier> = emptyList(),
    val inclusions: List<String> = emptyList(),
    val exclusions: List<String> = emptyList(),
    val availableDates: List<String> = emptyList(),
    val averageRating: Float = 0f,
    val totalReview: Int = 0
)

data class GuestStory(
    val quote: String,
    val guestName: String,
    val country: String
)

data class Partner(
    val name: String,
    val description: String,
    val imageRes: Int,
    val highlights: String,
    val websiteUrl: String = "",
    val contactUrl: String = ""
)

data class ValueProp(
    val title: String,
    val description: String,
    val icon: ImageVector = Icons.Default.Info
)

data class CoreValue(
    val title: String,
    val description: String
)

data class ManagementMember(
    val name: String,
    val role: String
)


val PhonePattern = Regex("^\\+?[0-9][0-9 ()-]{6,18}[0-9]$")

fun isValidEmail(email: String): Boolean =
    Patterns.EMAIL_ADDRESS.matcher(email).matches()

fun isValidPhone(phone: String): Boolean {
    val digits = phone.filter(Char::isDigit)
    return digits.length in 8..15 && PhonePattern.matches(phone)
}

data class Booking(
    val bookingId: String = "",
    val customerUid: String = "",
    val customerUsername: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val customerEmail: String = "",
    val destination: String = "",
    val bookingDate: String = "",
    val status: String = "",
    val paymentStatus: String = "",
    val finalPrice: String = "",
    val basePrice: String = "",
    val discountAmount: String = "",
    val voucherUsed: String = "",
    val paymentMethod: String = "",
    val userRating: Int = 0,
    val userReview: String = "",
    val confirmationTimestamp: Long = 0L
) {
    val discount: String
        get() = discountAmount
}

data class Feature(
    val title: String,
    val description: String,
    val icon: ImageVector = Icons.Default.Info
)

data class CountryCodeOption(
    val countryName: String,
    val code: String,
    val flag: String
)

val defaultCountryCodes = listOf(
    CountryCodeOption("Malaysia", "+60", "🇲🇾"),
    CountryCodeOption("Indonesia", "+62", "🇮🇩"),
    CountryCodeOption("Singapore", "+65", "🇸🇬"),
    CountryCodeOption("United States", "+1", "🇺🇸"),
    CountryCodeOption("United Kingdom", "+44", "🇬🇧"),
    CountryCodeOption("Australia", "+61", "🇦🇺"),
    CountryCodeOption("China", "+86", "🇨🇳"),
    CountryCodeOption("India", "+91", "🇮🇳"),
    CountryCodeOption("Japan", "+81", "🇯🇵"),
    CountryCodeOption("Thailand", "+66", "🇹🇭"),
    CountryCodeOption("Saudi Arabia", "+966", "🇸🇦"),
    CountryCodeOption("United Arab Emirates", "+971", "🇦🇪")
)

data class TargetClassConfig(
    val urlPath: String,
    val classA: String,
    val classB: String? = null,
    val isPaxCategory: Boolean = false,
    val keyword: String? = null // Added keyword for container filtering
)



val TARGET_PRICE_CONFIGS = mapOf(
    "https://www.zuhantotravelindo.id/bromo-sunrise-package" to TargetClassConfig(
        urlPath = "/bromo-sunrise-package",
        classA = "text-gray-300",
        classB = "font-bold text-[#F5A623] text-base",
        isPaxCategory = true
    ),
    "https://www.zuhantotravelindo.id/ijen-blue-fire-package" to TargetClassConfig(
        urlPath = "/ijen-blue-fire-package",
        classA = "text-gray-300",
        classB = "font-bold text-[#F5A623] text-base",
        isPaxCategory = true
    ),
    "https://www.zuhantotravelindo.id/jogja-dieng-package" to TargetClassConfig(
        urlPath = "/jogja-dieng-package",
        classA = "text-gray-300",
        classB = "font-bold text-[#F5A623] text-base",
        isPaxCategory = true
    ),
    "https://www.zuhantotravelindo.id/tumpak-sewu-package" to TargetClassConfig(
        urlPath = "/tumpak-sewu-package",
        classA = "text-5xl font-bold text-[#2AAEC5]",
        classB = "text-5xl font-bold text-[#F5A623]",
        isPaxCategory = false
    ),
    "https://www.zuhantotravelindo.id/mount-rinjani-trekking" to TargetClassConfig(
        urlPath = "/mount-rinjani-trekking",
        classA = "price-tag text-white mb-4",
        classB = null,
        isPaxCategory = false
    ),
    "https://www.zuhantotravelindo.id/yogyakarta-trip" to TargetClassConfig(
        urlPath = "/yogyakarta-trip",
        classA = "text-5xl md:text-6xl font-bold text-[#F5A623] tracking-tight",
        classB = null,
        isPaxCategory = false
    ),
    // Homepage / Specific Package items filtered by Trip Keyword
    "https://www.zuhantotravelindo.id/mount-bromo-package" to TargetClassConfig(
        urlPath = "/mount-bromo-package",
        classA = "truncate break-words",
        keyword = "Bromo"
    ),
    "https://www.zuhantotravelindo.id/borobudur-temple-package" to TargetClassConfig(
        urlPath = "/borobudur-temple-package",
        classA = "truncate break-words",
        keyword = "Borobudur"
    ),
    "https://www.zuhantotravelindo.id/ijen-crater-package" to TargetClassConfig(
        urlPath = "/ijen-crater-package",
        classA = "truncate break-words",
        keyword = "Ijen"
    ),
    "https://www.zuhantotravelindo.id/bali-package" to TargetClassConfig(
        urlPath = "/bali-package",
        classA = "truncate break-words",
        keyword = "Bali"
    ),
    "https://www.zuhantotravelindo.id/labuan-bajo-package" to TargetClassConfig(
        urlPath = "/labuan-bajo-package",
        classA = "truncate break-words",
        keyword = "Labuan"
    ),
    "https://www.zuhantotravelindo.id/raja-ampat-package" to TargetClassConfig(
        urlPath = "/raja-ampat-package",
        classA = "truncate break-words",
        keyword = "Raja"
    ),
    "mount_bromo" to TargetClassConfig(
        urlPath = "/",
        classA = "truncate break-words",
        keyword = "Bromo"
    ),
    "borobudur_temple" to TargetClassConfig(
        urlPath = "/",
        classA = "truncate break-words",
        keyword = "Borobudur"
    ),
    "ijen_crater" to TargetClassConfig(
        urlPath = "/",
        classA = "truncate break-words",
        keyword = "Ijen"
    ),
    "bali" to TargetClassConfig(
        urlPath = "/",
        classA = "truncate break-words",
        keyword = "Bali"
    ),
    "labuan_bajo" to TargetClassConfig(
        urlPath = "/",
        classA = "truncate break-words",
        keyword = "Labuan"
    ),
    "raja_ampat" to TargetClassConfig(
        urlPath = "/",
        classA = "truncate break-words",
        keyword = "Raja"
    )
)



data class StatusOption(val canonical: String, val display: String)

data class VoucherStatus(
    val data: Map<String, Any>,
    val isUsed: Boolean,
    val isExpired: Boolean
)

fun normalizeBookingStatus(status: String?): String {
    if (status.isNullOrBlank()) return "Pending"
    val lower = status.trim().lowercase()
    return when {
        lower.contains("confirm") || lower.contains("sah") -> "Confirmed"
        lower.contains("cancel") || lower.contains("batal") -> "Cancelled"
        lower.contains("complete") || lower.contains("selesai") -> "Completed"
        else -> "Pending"
    }
}

fun normalizePaymentStatus(status: String?): String {
    if (status.isNullOrBlank()) return "Pending"
    val lower = status.trim().lowercase()
    return when {
        lower.contains("paid") || lower.contains("bayar") || lower.contains("lunas") -> "Paid"
        lower.contains("cancel") || lower.contains("batal") -> "Cancelled"
        else -> "Pending"
    }
}

fun getBookingStatusDisplay(status: String?, context: Context? = null): String {
    val norm = normalizeBookingStatus(status)
    return when (norm) {
        "Confirmed" -> context?.getString(R.string.confirmed) ?: "Confirmed"
        "Cancelled" -> context?.getString(R.string.cancelled) ?: "Cancelled"
        "Completed" -> context?.getString(R.string.completed) ?: "Completed"
        else -> context?.getString(R.string.pending) ?: "Pending"
    }
}

fun getPaymentStatusDisplay(status: String?, context: Context? = null): String {
    val norm = normalizePaymentStatus(status)
    return when (norm) {
        "Paid" -> context?.getString(R.string.paid_lower) ?: "Paid"
        "Cancelled" -> context?.getString(R.string.cancelled) ?: "Cancelled"
        else -> context?.getString(R.string.pending) ?: "Pending"
    }
}

const val ADMIN_UID = "0TGdUZiaJnc92nOGPsRdQMqubWV2"

fun isUserAdmin(uid: String?, claims: Map<String, Any>?): Boolean {
    if (!uid.isNullOrBlank() && uid == ADMIN_UID) return true
    if (claims == null) return false
    val adminClaim = claims["admin"]
    return adminClaim == true || adminClaim?.toString()?.lowercase() == "true"
}

data class HelpRequest(
    val id: String = "",
    val category: String = "",
    val question: String = "",
    val customerName: String = "",
    val customerEmail: String = "",
    val customerPhone: String = "",
    val tripPackage: String = "",
    val timestamp: String = "",
    val status: String = "Pending",
    val customerUid: String = ""
)
