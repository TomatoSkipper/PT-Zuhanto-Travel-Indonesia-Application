package com.example.appproject.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.appproject.MainActivity
import com.example.appproject.R
import com.example.appproject.data.model.Booking
import com.example.appproject.data.remote.FetchPromoTripsFromWebsite
import com.example.appproject.data.repository.FirebaseRepository
import com.example.appproject.data.model.isUserAdmin
import com.example.appproject.data.model.normalizeBookingStatus
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class PriceCheckWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            FetchPromoTripsFromWebsite(context)
            Result.success()
        } catch (e: Exception) {
            Log.e("PriceCheckWorker", "Error checking website promo prices", e)
            Result.retry()
        }
    }
}

fun createBookingNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channelId = "booking_status_updates_channel"
        val channelName = context.getString(R.string.notification_channel_name)
        val channelDesc = context.getString(R.string.notification_channel_desc)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, channelName, importance).apply {
            description = channelDesc
            enableVibration(true)
            enableLights(true)
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

fun showBookingConfirmedNotification(context: Context, booking: Booking) {
    try {
        createBookingNotificationChannel(context)

        val channelId = "booking_status_updates_channel"
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("navigate_to_booking_id", booking.bookingId)
            putExtra("navigate_to_username", booking.customerUid)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            booking.bookingId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val destination = booking.destination.ifBlank { "Tour Package" }
        val title = context.getString(R.string.notification_booking_confirmed_title)
        val message = context.getString(
            R.string.notification_booking_confirmed_text,
            destination,
            booking.bookingId
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.app_icon_round)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(booking.bookingId.hashCode(), notification)
        Log.d("BookingNotification", "Posted notification for confirmed booking: ${booking.bookingId}")
    } catch (e: Exception) {
        Log.e("BookingNotification", "Error posting booking confirmation notification", e)
    }
}

fun isBookingConfirmedNotified(context: Context, bookingId: String): Boolean {
    if (bookingId.isBlank()) return true
    val prefs = context.getSharedPreferences("booking_notifications_prefs", Context.MODE_PRIVATE)
    val notifiedSet = prefs.getStringSet("notified_booking_ids", emptySet()) ?: emptySet()
    return notifiedSet.contains(bookingId)
}

fun markBookingConfirmedNotified(context: Context, bookingId: String) {
    if (bookingId.isBlank()) return
    val prefs = context.getSharedPreferences("booking_notifications_prefs", Context.MODE_PRIVATE)
    val currentSet = prefs.getStringSet("notified_booking_ids", emptySet())?.toMutableSet() ?: mutableSetOf()
    currentSet.add(bookingId)
    prefs.edit().putStringSet("notified_booking_ids", currentSet).apply()
}

fun showAdminNewBookingNotification(context: Context, booking: Booking) {
    try {
        createBookingNotificationChannel(context)

        val channelId = "booking_status_updates_channel"
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("navigate_to_admin", true)
            putExtra("navigate_to_booking_id", booking.bookingId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            ("admin_" + booking.bookingId).hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val customer = booking.customerName.ifBlank { booking.customerUsername.ifBlank { "User" } }
        val destination = booking.destination.ifBlank { "Tour Package" }
        val title = context.getString(R.string.notification_admin_new_booking_title)
        val message = context.getString(
            R.string.notification_admin_new_booking_text,
            customer,
            destination,
            booking.bookingId
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.app_icon_round)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(("admin_" + booking.bookingId).hashCode(), notification)
        Log.d("BookingNotification", "Posted admin notification for new booking: ${booking.bookingId}")
    } catch (e: Exception) {
        Log.e("BookingNotification", "Error posting admin new booking notification", e)
    }
}

fun isAdminNewBookingNotified(context: Context, bookingId: String): Boolean {
    if (bookingId.isBlank()) return true
    val prefs = context.getSharedPreferences("booking_notifications_prefs", Context.MODE_PRIVATE)
    val notifiedSet = prefs.getStringSet("notified_admin_booking_ids", emptySet()) ?: emptySet()
    return notifiedSet.contains(bookingId)
}

fun markAdminNewBookingNotified(context: Context, bookingId: String) {
    if (bookingId.isBlank()) return
    val prefs = context.getSharedPreferences("booking_notifications_prefs", Context.MODE_PRIVATE)
    val currentSet = prefs.getStringSet("notified_admin_booking_ids", emptySet())?.toMutableSet() ?: mutableSetOf()
    currentSet.add(bookingId)
    prefs.edit().putStringSet("notified_admin_booking_ids", currentSet).apply()
}

class BookingCheckWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            if (firebaseUser != null) {
                val uid = firebaseUser.uid
                val tokenResult = firebaseUser.getIdToken(true).await()
                val isAdmin = isUserAdmin(uid, tokenResult?.claims)
                val firebaseRepo = FirebaseRepository()
                if (isAdmin) {
                    val allBookings = firebaseRepo.getAllBookings()
                    val prefs = context.getSharedPreferences("booking_notifications_prefs", Context.MODE_PRIVATE)
                    val isInitialized = prefs.getBoolean("admin_booking_notifications_initialized", false)

                    if (!isInitialized) {
                        val existingIds = allBookings.map { it.bookingId }.toSet()
                        prefs.edit()
                            .putStringSet("notified_admin_booking_ids", existingIds)
                            .putBoolean("admin_booking_notifications_initialized", true)
                            .apply()
                    } else {
                        allBookings.forEach { booking ->
                            if (!isAdminNewBookingNotified(context, booking.bookingId)) {
                                showAdminNewBookingNotification(context, booking)
                                markAdminNewBookingNotified(context, booking.bookingId)
                            }
                        }
                    }
                } else {
                    val userBookings = firebaseRepo.getBookingsForUser(uid)
                    userBookings.forEach { booking ->
                        if (normalizeBookingStatus(booking.status) == "Confirmed") {
                            if (!isBookingConfirmedNotified(context, booking.bookingId)) {
                                showBookingConfirmedNotification(context, booking)
                                markBookingConfirmedNotified(context, booking.bookingId)
                            }
                        }
                    }
                }
            }
            Result.success()
        } catch (e: Exception) {
            Log.e("BookingCheckWorker", "Error checking confirmed bookings", e)
            Result.retry()
        }
    }
}
