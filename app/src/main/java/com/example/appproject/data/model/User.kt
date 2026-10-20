package com.example.appproject.data.model

import com.google.firebase.firestore.PropertyName

data class User(
    val id: Long = 0L,

    @get:PropertyName("uid")
    val uid: String = "",

    @get:PropertyName("username")
    val username: String = "",

    @get:PropertyName("email")
    val email: String = "",

    @get:PropertyName("phone")
    val phone: String = "",

    @get:PropertyName("isBanned")
    val isBanned: Boolean = false,

    @get:PropertyName("isPhoneVerified")
    val isPhoneVerified: Boolean = false,

    @get:PropertyName("name")
    val name: String = "",

    @get:PropertyName("gender")
    val gender: String = "",

    @get:PropertyName("bannedReason")
    val bannedReason: String = "",

    @get:PropertyName("bannedDate")
    val bannedDate: String = ""
)
