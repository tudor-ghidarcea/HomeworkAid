package com.example.schoolqa.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {

    fun currentUser() = auth.currentUser

    suspend fun signIn(identifier: String, password: String) {
        try {
            val emailPattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")

            val emailToUse = if (emailPattern.matches(identifier)) {
                identifier
            } else {
                val snap = db.collection("users")
                    .whereEqualTo("displayName", identifier)
                    .get()
                    .await()

                if (snap.isEmpty) throw Exception("Numele de utilizator nu a fost găsit.")
                snap.documents.first().getString("email")
                    ?: throw Exception("Nu există email asociat acestui nume de utilizator.")
            }

            auth.signInWithEmailAndPassword(emailToUse, password).await()

        } catch (e: Exception) {
            when (e) {
                is FirebaseAuthInvalidCredentialsException ->
                    throw Exception("Email/nume utilizator sau parolă incorecte.")
                else ->
                    throw Exception("Autentificarea a eșuat: ${e.localizedMessage ?: "Eroare necunoscută"}")
            }
        }
    }

    suspend fun signUp(email: String, password: String, displayName: String) {
        try {
            // verificăm dacă username există deja
            val usernameSnap = db.collection("users")
                .whereEqualTo("displayName", displayName)
                .get()
                .await()
            if (!usernameSnap.isEmpty) {
                throw UsernameAlreadyExistsException()
            }

            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Crearea utilizatorului a eșuat.")

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            user.updateProfile(profileUpdates).await()

            val userData = mapOf(
                "uid" to user.uid,
                "displayName" to displayName,
                "email" to email
            )
            db.collection("users").document(user.uid).set(userData).await()

        } catch (e: Exception) {
            when (e) {
                is FirebaseAuthWeakPasswordException ->
                    throw Exception("Parola este prea simplă. Alege o parolă mai puternică.")
                is FirebaseAuthInvalidCredentialsException ->
                    throw Exception("Format de email invalid.")
                is FirebaseAuthUserCollisionException ->
                    throw Exception("Există deja un cont cu acest email.")
                is UsernameAlreadyExistsException ->
                    throw Exception("Acest nume de utilizator este deja folosit.")
                else ->
                    throw Exception(e.localizedMessage ?: "Eroare necunoscută la înregistrare.")
            }
        }
    }

    fun signOut() {
        auth.signOut()
    }
}

class UsernameAlreadyExistsException : Exception()
