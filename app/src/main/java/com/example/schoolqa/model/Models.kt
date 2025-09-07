package com.example.schoolqa.model

import com.google.firebase.Timestamp

data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val password: String = ""
)

data class Question(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val authorUid: String = "",
    val authorName: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val subject: String = "General",
    val imageUrl: String? = null,
)

data class Answer(
    val id: String = "",
    val questionId: String = "",
    val body: String = "",
    val authorUid: String = "",
    val authorName: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val likes: Int = 0,
    val dislikes: Int = 0
)

data class Vote(
    val userId: String = "",
    val value: Int = 0, // +1 like, -1 dislike
    val questionId: String = ""
)
