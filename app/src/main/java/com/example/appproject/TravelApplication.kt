package com.example.appproject

import android.app.Application
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.FirebaseApp
import java.util.concurrent.TimeUnit
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.example.appproject.worker.BookingCheckWorker
import com.example.appproject.worker.PriceCheckWorker

class TravelApplication : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.5)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(400 * 1024 * 1024) // 400 MB
                    .build()
            }
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .crossfade(true)
            .build()
    }
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase once for the entire application
        try {
            val app = FirebaseApp.initializeApp(this)
            Log.d("TravelApp", "Firebase initialized: ${app?.name}. Apps count: ${FirebaseApp.getApps(this).size}")
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            if (auth.currentUser == null) {
                auth.signInAnonymously().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("TravelApp", "Anonymous sign-in successful for guest")
                    } else {
                        Log.e("TravelApp", "Anonymous sign-in failed", task.exception)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("TravelApp", "Firebase init failed", e)
        }

        // Schedule periodic background sync for promo trip website updates
        try {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncWorkRequest = PeriodicWorkRequestBuilder<PriceCheckWorker>(1, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "WebsitePromoTripSyncWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                syncWorkRequest
            )

            val bookingCheckWorkRequest = PeriodicWorkRequestBuilder<BookingCheckWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "BookingStatusCheckWorker",
                ExistingPeriodicWorkPolicy.KEEP,
                bookingCheckWorkRequest
            )
            Log.d("TravelApp", "WorkManager website sync worker & booking status worker scheduled")
        } catch (e: Exception) {
            Log.e("TravelApp", "Failed to schedule WorkManager sync worker", e)
        }
    }
}
