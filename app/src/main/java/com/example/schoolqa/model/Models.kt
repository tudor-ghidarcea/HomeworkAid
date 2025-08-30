package com.example.schoolqa.model

import com.google.firebase.Timestamp

/** Utilizator (profil minimal) */
data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val photoUrl: String? = null
)

/** Întrebare postată de un elev */
data class Question(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val authorUid: String = "",
    val authorName: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val subject: String = "General", // opțional: materie (Mate, Română etc.)
    val imageUrl: String? = null,
)

/** Răspuns la o întrebare */
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

/** Vot individual al unui utilizator pentru un răspuns */
data class Vote(
    val userId: String = "",
    val value: Int = 0 // +1 like, -1 dislike
)
