package com.example.schoolqa.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider // Import for GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class AuthRepository(private val auth: FirebaseAuth) {

    fun currentUser() = auth.currentUser

    suspend fun signIn(email: String, password: String) {
        try {
            auth.signInWithEmailAndPassword(email, password).await()
        } catch (e: Exception) {
            when (e) {
                is FirebaseAuthInvalidCredentialsException -> throw Exception("Invalid email or password.")
                else -> throw Exception("Login failed: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    suspend fun signUp(email: String, password: String) {
        try {
            auth.createUserWithEmailAndPassword(email, password).await()
        } catch (e: Exception) {
            when (e) {
                is FirebaseAuthWeakPasswordException -> throw Exception("Password is too weak. Please choose a stronger password.")
                is FirebaseAuthInvalidCredentialsException -> throw Exception("Invalid email address format.")
                is FirebaseAuthUserCollisionException -> throw Exception("An account with this email already exists.")
                else -> throw Exception("Registration failed: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    // NEW: Method for Google Sign-In
    suspend fun signInWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        try {
            auth.signInWithCredential(credential).await()
        } catch (e: Exception) {
            throw Exception("Google sign-in failed: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
