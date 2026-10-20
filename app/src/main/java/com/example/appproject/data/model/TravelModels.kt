package com.example.appproject.data.model

import java.util.UUID

// Request object sent to the backend server
data class ChatRequest(
    val userMessage: String,
    val destination: String,
    val days: Int = 3,
    val currentItinerary: List<String> = emptyList()
)

// Item recommended by AI (Place to visit, stay, or eat)
data class SuggestionItem(
    val title: String,
    val description: String,
    val dayNumber: Int = 1,
    val estimatedPrice: String = "Free / Varies",
    val category: String = "Visit", // "Visit", "Stay", "Eat"
    val imageUrl: String = ""
)

// Response object received from the backend server or local AI
data class ChatResponse(
    val message: String,
    val suggestion: SuggestionItem? = null,
    val suggestionsList: List<SuggestionItem> = emptyList()
)

// Model used in UI to render message bubbles
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val suggestion: SuggestionItem? = null,
    val suggestionsList: List<SuggestionItem> = emptyList(),
    val timestamp: String = ""
)