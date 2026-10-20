package com.example.appproject.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.appproject.R
import com.example.appproject.data.model.ChatMessage
import com.example.appproject.data.model.ChatRequest
import com.example.appproject.data.remote.NetworkClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.plus

class TravelChatViewModel(application: Application) : AndroidViewModel(application) {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var currentDestination: String = ""

    val aiIntro = getApplication<Application>().getString(R.string.ai_intro)
    val aiIntroSubtext = getApplication<Application>().getString(R.string.ai_intro_subtext)
    val aiIntro2 = getApplication<Application>().getString(R.string.ai_intro2)

    fun initializeChat(destination: String) {
        if (currentDestination == destination && _messages.value.isNotEmpty()) return
        currentDestination = destination

        val welcomeText = if (destination.isNotBlank() && !destination.equals("General Travel", ignoreCase = true)) {
            "$aiIntro **$destination** $aiIntroSubtext"
        } else {
            aiIntro2
        }
        _messages.value = listOf(ChatMessage(text = welcomeText, isUser = false))
    }

    fun sendMessage(
        userText: String,
        destination: String = currentDestination,
        days: Int = 3,
        currentItinerary: List<String> = emptyList()
    ) {
        if (userText.isBlank()) return

        val activeDest = if (destination.isNotBlank()) destination else currentDestination

        // 1. Immediately render user's message on UI
        val userMsg = ChatMessage(text = userText, isUser = true)
        _messages.update { it + userMsg }
        _isLoading.value = true

        // 2. Send request to live Gemini AI Node.js server
        viewModelScope.launch {
            try {
                val request = ChatRequest(
                    userMessage = userText,
                    destination = activeDest,
                    days = days,
                    currentItinerary = currentItinerary
                )
                val response = NetworkClient.apiService.getAiSuggestion(request)

                val suggestions = when {
                    response.suggestionsList.isNotEmpty() -> response.suggestionsList
                    response.suggestion != null -> listOf(response.suggestion)
                    else -> emptyList()
                }

                val aiMsg = ChatMessage(
                    text = response.message,
                    isUser = false,
                    suggestion = response.suggestion,
                    suggestionsList = suggestions
                )
                _messages.update { it + aiMsg }
            } catch (e: Exception) {
                _messages.update {
                    it + ChatMessage(
                        text = getApplication<Application>().getString(R.string.ai_error),
                        isUser = false
                    )
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
}