package com.example.appproject.data.remote

import com.example.appproject.data.model.ChatRequest
import com.example.appproject.data.model.ChatResponse
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface TravelApiService {
    @POST("api/chat-suggest")
    suspend fun getAiSuggestion(@Body request: ChatRequest): ChatResponse
}

object NetworkClient {
    // 10.0.2.2 maps to localhost (127.0.0.1) when running inside the Android Emulator
    private const val BASE_URL = "https://pt-zuhanto-travel-app-project.onrender.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request()
            val user = FirebaseAuth.getInstance().currentUser
            val token = runBlocking {
                try {
                    user?.getIdToken(false)?.await()?.token
                } catch (_: Exception) {
                    null
                }
            }

            val requestBuilder = original.newBuilder()
            if (!token.isNullOrEmpty()) {
                requestBuilder.header("Authorization", "Bearer $token")
            }
            chain.proceed(requestBuilder.build())
        }
        .build()

    val apiService: TravelApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TravelApiService::class.java)
    }
}
