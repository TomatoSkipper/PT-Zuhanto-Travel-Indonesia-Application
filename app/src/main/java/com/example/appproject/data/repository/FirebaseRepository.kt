package com.example.appproject.data.repository

import android.util.Log
import com.example.appproject.data.model.Booking
import com.example.appproject.data.model.HelpRequest
import com.example.appproject.data.model.User
import com.example.appproject.data.model.normalizeBookingStatus
import com.example.appproject.data.model.normalizePaymentStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale

class FirebaseRepository {
    private val TAG = "FirebaseRepository"

    val db by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firestore: ${e.message}")
            null
        }
    }

    private val usersCollection by lazy { db?.collection("users") }
    private val bookingsCollection by lazy { db?.collection("bookings") }
    private val vouchersCollection by lazy { db?.collection("vouchers") }
    private val usedVouchersCollection by lazy { db?.collection("usedVouchers") }
    private val helpRequestsCollection by lazy { db?.collection("helpRequests") }
    private val tripReviewsCollection by lazy { db?.collection("tripReviews") }

    suspend fun testConnection(): String {
        if (db == null) return "Firestore is not initialized. Check your google-services.json and Firebase configuration."
        return try {
            db!!.collection("bookings").limit(1).get().await()
            "Connected"
        } catch (e: Exception) {
            Log.e(TAG, "Connection test failed: ${e.message}")
            e.message ?: "Unknown Error"
        }
    }

    suspend fun ensureNewcomerVoucher() {
        try {
            val doc = vouchersCollection?.document("NEWCOMER")?.get()?.await()
            if (doc == null || !doc.exists()) {
                val newcomerData = mapOf(
                    "code" to "NEWCOMER",
                    "discountPercentage" to 10,
                    "description" to "Special for new account",
                    "startDate" to "",
                    "endDate" to "",
                    "backgroundPicture" to ""
                )
                vouchersCollection?.document("NEWCOMER")?.set(newcomerData)?.await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error ensuring newcomer voucher: ${e.message}")
        }
    }

    suspend fun saveUser(user: User) {
        try {
            ensureNewcomerVoucher()
            val uid = user.uid.ifBlank { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
            if (uid.isNotBlank()) {
                val userToSave = if (user.uid.isBlank()) user.copy(uid = uid) else user
                usersCollection?.document(uid)?.set(userToSave)?.await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user: ${e.message}")
        }
    }

    suspend fun getUser(query: String): User? {
        if (query.isBlank()) return null
        val cleanQuery = query.trim()
        val normalized = cleanQuery.lowercase(Locale.ROOT)
        return try {
            // 1. Direct document lookup by query (UID)
            val snapshot = usersCollection?.document(cleanQuery)?.get()?.await()
            var user = snapshot?.toObject(User::class.java)

            if (user == null && cleanQuery != normalized) {
                val normSnapshot = usersCollection?.document(normalized)?.get()?.await()
                user = normSnapshot?.toObject(User::class.java)
            }

            // 2. Query by uid field
            if (user == null) {
                val uidDocs = usersCollection?.whereEqualTo("uid", cleanQuery)?.get()?.await()
                user = uidDocs?.documents?.firstOrNull()?.toObject(User::class.java)
            }

            // 3. Query by email
            if (user == null && cleanQuery.contains("@")) {
                val emailDocs = usersCollection?.whereEqualTo("email", cleanQuery)?.get()?.await()
                user = emailDocs?.documents?.firstOrNull()?.toObject(User::class.java)
                if (user == null && cleanQuery != normalized) {
                    val lowerEmailDocs = usersCollection?.whereEqualTo("email", normalized)?.get()?.await()
                    user = lowerEmailDocs?.documents?.firstOrNull()?.toObject(User::class.java)
                }
            }

            // 4. Query by phone
            if (user == null) {
                val phoneDocs = usersCollection?.whereEqualTo("phone", cleanQuery)?.get()?.await()
                user = phoneDocs?.documents?.firstOrNull()?.toObject(User::class.java)

                if (user == null && cleanQuery.startsWith("+")) {
                    val noPlus = cleanQuery.removePrefix("+")
                    val noPlusDocs = usersCollection?.whereEqualTo("phone", noPlus)?.get()?.await()
                    user = noPlusDocs?.documents?.firstOrNull()?.toObject(User::class.java)
                }

                if (user == null && cleanQuery.startsWith("+60")) {
                    val local = "0" + cleanQuery.removePrefix("+60")
                    val localDocs = usersCollection?.whereEqualTo("phone", local)?.get()?.await()
                    user = localDocs?.documents?.firstOrNull()?.toObject(User::class.java)
                }

                if (user == null && cleanQuery.startsWith("+62")) {
                    val local = "0" + cleanQuery.removePrefix("+62")
                    val localDocs = usersCollection?.whereEqualTo("phone", local)?.get()?.await()
                    user = localDocs?.documents?.firstOrNull()?.toObject(User::class.java)
                }
            }

            // 5. Query by username property
            if (user == null) {
                val usernameDocs = usersCollection?.whereEqualTo("username", normalized)?.get()?.await()
                user = usernameDocs?.documents?.firstOrNull()?.toObject(User::class.java)
                if (user == null) {
                    val exactUsernameDocs = usersCollection?.whereEqualTo("username", cleanQuery)?.get()?.await()
                    user = exactUsernameDocs?.documents?.firstOrNull()?.toObject(User::class.java)
                }
            }

            user
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user: ${e.message}")
            null
        }
    }

    suspend fun isPhoneRegistered(phone: String, excludeUid: String = ""): Boolean {
        val cleanPhone = phone.trim()
        if (cleanPhone.isBlank()) return false
        return try {
            val docs = usersCollection?.whereEqualTo("phone", cleanPhone)?.get()?.await()
            if (docs != null && !docs.isEmpty) {
                if (excludeUid.isBlank()) return true
                docs.documents.any { doc ->
                    val user = doc.toObject(User::class.java)
                    user != null && user.uid != excludeUid
                }
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun saveBooking(booking: Booking) {
        try {
            val uid = booking.customerUid.ifBlank { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
            val finalBooking = if (uid.isNotBlank() && booking.customerUid.isBlank()) booking.copy(customerUid = uid) else booking
            bookingsCollection?.document(finalBooking.bookingId)?.set(finalBooking)?.await()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving booking: ${e.message}")
        }
    }

    suspend fun processBookingsWithAutoCancellation(bookings: List<Booking>): List<Booking> {
        val currentTime = System.currentTimeMillis()
        val twoDaysMs = 2L * 24 * 60 * 60 * 1000L
        val updatedList = mutableListOf<Booking>()
        for (booking in bookings) {
            var currentBooking = booking
            if (currentBooking.customerEmail.isBlank()) {
                val userKey = currentBooking.customerUid.ifBlank { currentBooking.customerUsername }
                if (userKey.isNotBlank()) {
                    val user = getUser(userKey)
                    if (user != null && user.email.isNotBlank()) {
                        currentBooking = currentBooking.copy(customerEmail = user.email)
                    }
                }
            }
            val normStatus = normalizeBookingStatus(currentBooking.status)
            val normPayment = normalizePaymentStatus(currentBooking.paymentStatus)
            if (normStatus == "Confirmed" && normPayment != "Paid" && currentBooking.confirmationTimestamp > 0L && !currentBooking.proofOfPaymentUploaded) {
                if (currentTime - currentBooking.confirmationTimestamp > twoDaysMs) {
                    val cancelledBooking = currentBooking.copy(status = "Cancelled")
                    updateBooking(cancelledBooking)
                    updatedList.add(cancelledBooking)
                    continue
                }
            }
            updatedList.add(currentBooking)
        }
        return updatedList
    }

    suspend fun getAllBookings(): List<Booking> {
        val list = try {
            bookingsCollection?.orderBy("bookingDate")?.get()?.await()?.toObjects(Booking::class.java) ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all bookings with order: ${e.message}")
            try {
                bookingsCollection?.get()?.await()?.toObjects(Booking::class.java) ?: emptyList()
            } catch (e2: Exception) {
                Log.e(TAG, "Error getting all bookings fallback: ${e2.message}")
                emptyList()
            }
        }
        return processBookingsWithAutoCancellation(list)
    }

    suspend fun getBookingsForUser(query: String): List<Booking> {
        if (query.isBlank()) return emptyList()
        return try {
            val bookings = mutableListOf<Booking>()
            val seenIds = mutableSetOf<String>()

            // 1. Query by customerUid == query
            val uidDocs = bookingsCollection?.whereEqualTo("customerUid", query)?.get()?.await()?.toObjects(Booking::class.java)
            if (uidDocs != null) {
                for (b in uidDocs) {
                    if (seenIds.add(b.bookingId)) bookings.add(b)
                }
            }

            // 2. Query by customerUsername == query
            val usernameDocs = bookingsCollection?.whereEqualTo("customerUsername", query)?.get()?.await()?.toObjects(Booking::class.java)
            if (usernameDocs != null) {
                for (b in usernameDocs) {
                    if (seenIds.add(b.bookingId)) bookings.add(b)
                }
            }

            // 3. If query is a UID, also check if user object exists and query by their username
            val user = getUser(query)
            if (user != null && user.username.isNotBlank()) {
                val userUsernameDocs = bookingsCollection?.whereEqualTo("customerUsername", user.username)?.get()?.await()?.toObjects(Booking::class.java)
                if (userUsernameDocs != null) {
                    for (b in userUsernameDocs) {
                        if (seenIds.add(b.bookingId)) bookings.add(b)
                    }
                }
            }

            val processed = processBookingsWithAutoCancellation(bookings)
            processed.sortedBy { it.bookingDate }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting bookings for user: ${e.message}")
            emptyList()
        }
    }

    fun listenToBookingsForUser(query: String, onUpdate: (List<Booking>) -> Unit): ListenerRegistration? {
        if (query.isBlank()) return null
        return try {
            bookingsCollection?.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to bookings for user: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val allBookings = snapshot.toObjects(Booking::class.java)
                    val scope = CoroutineScope(Dispatchers.IO)
                    scope.launch {
                        val bookings = mutableListOf<Booking>()
                        val seenIds = mutableSetOf<String>()

                        val user = getUser(query)
                        val targetUsername = user?.username?.ifBlank { query } ?: query
                        val targetUid = user?.uid?.ifBlank { query } ?: query

                        for (b in allBookings) {
                            if ((b.customerUid == query || b.customerUsername == query ||
                                 b.customerUsername == targetUsername || b.customerUid == targetUsername ||
                                 b.customerUid == targetUid) && seenIds.add(b.bookingId)) {
                                bookings.add(b)
                            }
                        }
                        val processed = processBookingsWithAutoCancellation(bookings)
                        onUpdate(processed.sortedBy { it.bookingDate })
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error listening to bookings for user: ${e.message}")
            null
        }
    }

    fun listenToAllBookings(onUpdate: (List<Booking>) -> Unit): ListenerRegistration? {
        return try {
            bookingsCollection
                ?.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Listen to all bookings failed: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val bookings = snapshot.toObjects(Booking::class.java)
                        val scope = CoroutineScope(Dispatchers.IO)
                        scope.launch {
                            val processed = processBookingsWithAutoCancellation(bookings)
                            onUpdate(processed)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error listening to all bookings: ${e.message}")
            null
        }
    }

    suspend fun updateBooking(booking: Booking) {
        try {
            bookingsCollection?.document(booking.bookingId)?.set(booking)?.await()
            if (booking.userRating > 0 || booking.userReview.isNotBlank()) {
                savePublicReview(booking)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating booking: ${e.message}")
        }
    }

    private val rtdbUrl = "https://appproject-8a77a099-default-rtdb.asia-southeast1.firebasedatabase.app"

    suspend fun savePublicReview(booking: Booking) {
        try {
            val reviewMap = mapOf(
                "bookingId" to booking.bookingId,
                "destination" to booking.destination,
                "userRating" to booking.userRating,
                "userReview" to booking.userReview,
                "customerName" to booking.customerName.ifBlank { "Traveler" },
                "bookingDate" to booking.bookingDate
            )
            tripReviewsCollection?.document(booking.bookingId)?.set(reviewMap)?.await()
            val rtdbRef = FirebaseDatabase.getInstance(rtdbUrl).getReference("tripReviews").child(booking.bookingId)
            rtdbRef.setValue(reviewMap).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving public review: ${e.message}")
        }
    }

    suspend fun getPublicReviews(): List<Booking> {
        val list = mutableListOf<Booking>()
        // 1. Fetch from Realtime Database
        try {
            val rtdbSnapshot = FirebaseDatabase.getInstance(rtdbUrl).getReference("tripReviews").get().await()
            for (child in rtdbSnapshot.children) {
                val bookingId = child.child("bookingId").getValue(String::class.java) ?: child.key ?: ""
                val destination = child.child("destination").getValue(String::class.java) ?: ""
                val userRating = child.child("userRating").getValue(Long::class.java)?.toInt() ?: 0
                val userReview = child.child("userReview").getValue(String::class.java) ?: ""
                val customerName = child.child("customerName").getValue(String::class.java) ?: "Traveler"
                val bookingDate = child.child("bookingDate").getValue(String::class.java) ?: ""
                if (userRating > 0 || userReview.isNotBlank()) {
                    list.add(
                        Booking(
                            bookingId = bookingId,
                            destination = destination,
                            userRating = userRating,
                            userReview = userReview,
                            customerName = customerName,
                            bookingDate = bookingDate
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting public reviews from RTDB: ${e.message}")
        }

        // 2. Fetch from Firestore tripReviewsCollection
        try {
            val snapshot = tripReviewsCollection?.get()?.await()
            snapshot?.documents?.forEach { doc ->
                val bookingId = doc.getString("bookingId") ?: doc.id
                val destination = doc.getString("destination") ?: ""
                val userRating = doc.getLong("userRating")?.toInt() ?: 0
                val userReview = doc.getString("userReview") ?: ""
                val customerName = doc.getString("customerName") ?: "Traveler"
                val bookingDate = doc.getString("bookingDate") ?: ""
                if (userRating > 0 || userReview.isNotBlank()) {
                    list.add(
                        Booking(
                            bookingId = bookingId,
                            destination = destination,
                            userRating = userRating,
                            userReview = userReview,
                            customerName = customerName,
                            bookingDate = bookingDate
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting public reviews from Firestore tripReviews: ${e.message}")
        }

        // 3. Fetch from bookingsCollection for any reviewed bookings
        try {
            val bookingsSnapshot = bookingsCollection?.get()?.await()
            bookingsSnapshot?.documents?.forEach { doc ->
                val booking = doc.toObject(Booking::class.java)
                if (booking != null && (booking.userRating > 0 || booking.userReview.isNotBlank())) {
                    list.add(booking)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting reviewed bookings from Firestore bookings: ${e.message}")
        }

        return list.distinctBy { it.bookingId }
    }

    suspend fun getBooking(bookingId: String): Booking? {
        return try {
            val snapshot = bookingsCollection?.document(bookingId)?.get()?.await()
            snapshot?.toObject(Booking::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting booking: ${e.message}")
            null
        }
    }

    suspend fun deleteBooking(bookingId: String) {
        try {
            bookingsCollection?.document(bookingId)?.delete()?.await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting booking: ${e.message}")
        }
    }

    suspend fun getAllUsers(): List<User> {
        return try {
            val list = usersCollection?.get()?.await()?.toObjects(User::class.java) ?: emptyList()
            list.sortedBy { it.username }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all users: ${e.message}")
            emptyList()
        }
    }

    suspend fun updateUser(user: User) {
        try {
            val uid = user.uid.ifBlank { FirebaseAuth.getInstance().currentUser?.uid ?: "" }
            if (uid.isNotBlank()) {
                usersCollection?.document(uid)?.set(user)?.await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user: ${e.message}")
        }
    }

    suspend fun deleteUser(uid: String) {
        if (uid.isBlank()) return
        try {
            // 1. Delete all bookings associated with this user uid
            val userBookingsSnapshot = bookingsCollection?.whereEqualTo("customerUid", uid)?.get()?.await()
            userBookingsSnapshot?.documents?.forEach { document ->
                document.reference.delete().await()
            }

            // 2. Delete the user document itself
            usersCollection?.document(uid)?.delete()?.await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting user and their bookings: ${e.message}")
        }
    }

    suspend fun saveVoucher(code: String, discountPercentage: Int, description: String = "", startDate: String = "", endDate: String = "", backgroundPicture: String = "", isDisabled: Boolean = false) {
        try {
            val voucherData = mapOf(
                "code" to code.trim().uppercase(),
                "discountPercentage" to discountPercentage,
                "description" to description.trim(),
                "startDate" to startDate.trim(),
                "endDate" to endDate.trim(),
                "backgroundPicture" to backgroundPicture.trim(),
                "isDisabled" to isDisabled
            )
            vouchersCollection?.document(code.trim().uppercase())?.set(voucherData)?.await()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving voucher: ${e.message}")
        }
    }

    suspend fun updateVoucherDisabled(code: String, isDisabled: Boolean) {
        try {
            val cleanCode = code.trim().uppercase()
            val existing = vouchersCollection?.document(cleanCode)?.get()?.await()?.data
            if (existing != null) {
                val updated = existing.toMutableMap().apply {
                    put("isDisabled", isDisabled)
                }
                vouchersCollection?.document(cleanCode)?.set(updated)?.await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating voucher disabled status: ${e.message}")
        }
    }

    suspend fun deleteVoucher(code: String) {
        try {
            vouchersCollection?.document(code.trim().uppercase())?.delete()?.await()
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting voucher: ${e.message}")
        }
    }

    suspend fun getVoucherData(code: String): Map<String, Any>? {
        try {
            ensureNewcomerVoucher()
            val cleanCode = code.trim().uppercase()
            val snapshot = vouchersCollection?.document(cleanCode)?.get()?.await()
            if (snapshot != null && snapshot.exists()) {
                return snapshot.data
            }
            if (cleanCode == "NEWCOMER") {
                return mapOf(
                    "code" to "NEWCOMER",
                    "discountPercentage" to 10,
                    "description" to "Special for new account",
                    "startDate" to "",
                    "endDate" to "",
                    "backgroundPicture" to ""
                )
            }
            return null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting voucher: ${e.message}")
            if (code.trim().equals("NEWCOMER", ignoreCase = true)) {
                return mapOf(
                    "code" to "NEWCOMER",
                    "discountPercentage" to 10,
                    "description" to "Special for new account",
                    "startDate" to "",
                    "endDate" to "",
                    "backgroundPicture" to ""
                )
            }
            return null
        }
    }

    suspend fun getVoucherDiscount(code: String): Int {
        val data = getVoucherData(code)
        return data?.get("discountPercentage")?.toString()?.toIntOrNull() ?: 0
    }

    suspend fun recordVoucherUsage(code: String, uid: String) {
        if (uid.isBlank() || code.isBlank()) return
        try {
            val usageId = "${uid}_${code.trim().uppercase()}"
            val usageData = mapOf(
                "uid" to uid,
                "voucherCode" to code.trim().uppercase(),
                "usedAt" to System.currentTimeMillis()
            )
            usedVouchersCollection?.document(usageId)?.set(usageData)?.await()
        } catch (e: Exception) {
            Log.e(TAG, "Error recording voucher usage: ${e.message}")
        }
    }

    suspend fun isVoucherUsedByUser(code: String, uid: String): Boolean {
        if (uid.isBlank() || code.isBlank()) return false
        return try {
            val usageId = "${uid}_${code.trim().uppercase()}"
            val snapshot = usedVouchersCollection?.document(usageId)?.get()?.await()
            if (snapshot != null && snapshot.exists()) return true

            // Check bookings collection as a fallback
            val querySnapshot = bookingsCollection
                ?.whereEqualTo("customerUid", uid)
                ?.whereEqualTo("voucherUsed", code.trim().uppercase())
                ?.get()
                ?.await()
            querySnapshot != null && !querySnapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getAllVouchers(): List<Map<String, Any>> {
        return try {
            ensureNewcomerVoucher()
            val list = vouchersCollection?.get()?.await()?.documents?.mapNotNull { it.data } ?: emptyList()
            if (list.none { it["code"]?.toString()?.equals("NEWCOMER", ignoreCase = true) == true }) {
                listOf(
                    mapOf(
                        "code" to "NEWCOMER",
                        "discountPercentage" to 10,
                        "description" to "Special for new account",
                        "startDate" to "",
                        "endDate" to "",
                        "backgroundPicture" to ""
                    )
                ) + list
            } else {
                list
            }
        } catch (e: Exception) {
            listOf(
                mapOf(
                    "code" to "NEWCOMER",
                    "discountPercentage" to 10,
                    "description" to "Special for new account",
                    "startDate" to "",
                    "endDate" to "",
                    "backgroundPicture" to ""
                )
            )
        }
    }

    suspend fun isUserAuthorizedForBooking(booking: Booking?, loggedInUser: String?): Boolean {
        if (booking == null) return false

        val currentFirebaseUid = try {
            FirebaseAuth.getInstance().currentUser?.uid ?: ""
        } catch (_: Exception) {
            ""
        }
        val userParam = loggedInUser?.trim() ?: ""

        // 1. If booking customer identifiers are blank, treat as accessible to logged-in user
        if (booking.customerUid.isBlank() && booking.customerUsername.isBlank()) {
            return currentFirebaseUid.isNotBlank() || userParam.isNotBlank()
        }

        // 2. Direct match against customerUid or customerUsername
        if (currentFirebaseUid.isNotBlank()) {
            if (currentFirebaseUid == booking.customerUid || currentFirebaseUid == booking.customerUsername) {
                return true
            }
        }
        if (userParam.isNotBlank()) {
            if (userParam == booking.customerUid || userParam == booking.customerUsername) {
                return true
            }
        }

        // 3. Admin check
        if (currentFirebaseUid.isNotBlank() && com.example.appproject.data.model.isUserAdmin(currentFirebaseUid, null)) {
            return true
        }

        // 4. Resolve User document
        val query = currentFirebaseUid.ifBlank { userParam }
        if (query.isNotBlank()) {
            val user = getUser(query)
            if (user != null) {
                if (com.example.appproject.data.model.isUserAdmin(user.uid, null)) {
                    return true
                }
                if (user.uid.isNotBlank() && (user.uid == booking.customerUid || user.uid == booking.customerUsername)) {
                    return true
                }
                if (user.username.isNotBlank() && (user.username == booking.customerUid || user.username == booking.customerUsername)) {
                    return true
                }
            }
        }

        return false
    }

    suspend fun saveHelpRequest(request: HelpRequest): Boolean {
        return try {
            val id = request.id.ifBlank { helpRequestsCollection?.document()?.id ?: System.currentTimeMillis().toString() }
            val finalRequest = if (request.id.isBlank()) request.copy(id = id) else request
            helpRequestsCollection?.document(id)?.set(finalRequest)?.await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving help request: ${e.message}")
            false
        }
    }

    suspend fun getAllHelpRequests(): List<HelpRequest> {
        return try {
            helpRequestsCollection?.get()?.await()?.toObjects(HelpRequest::class.java) ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting help requests: ${e.message}")
            emptyList()
        }
    }

    suspend fun getHelpRequest(requestId: String): HelpRequest? {
        if (requestId.isBlank()) return null
        return try {
            val doc = helpRequestsCollection?.document(requestId)?.get()?.await()
            doc?.toObject(HelpRequest::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting help request: ${e.message}")
            null
        }
    }

    suspend fun updateHelpRequestStatus(requestId: String, status: String) {
        try {
            helpRequestsCollection?.document(requestId)?.update("status", status)?.await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating help request status: ${e.message}")
        }
    }
}
