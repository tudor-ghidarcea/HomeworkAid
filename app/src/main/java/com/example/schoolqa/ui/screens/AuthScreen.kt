package com.example.schoolqa.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.schoolqa.Routes
import com.example.schoolqa.data.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(nav: NavController, auth: FirebaseAuth) {
    val authRepo = remember { AuthRepository(auth, FirebaseFirestore.getInstance()) }
    val scope = rememberCoroutineScope()

    var identifier by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var isLogin by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (isLogin) "Autentificare" else "Creare cont",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(Modifier.height(16.dp))

            if (isLogin) {
                OutlinedTextField(
                    value = identifier,
                    onValueChange = { identifier = it },
                    label = { Text("Email sau Nume utilizator") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Nume utilizator") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Parolă") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (loading) {
                Spacer(Modifier.height(12.dp))
                CircularProgressIndicator()
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    if (isLogin) {
                        if (identifier.isBlank() || password.isBlank()) {
                            error = "Introduceți email/nume utilizator și parola."
                            return@Button
                        }
                    } else {
                        if (email.isBlank() || username.isBlank() || password.isBlank()) {
                            error = "Toate câmpurile sunt obligatorii."
                            return@Button
                        }
                    }

                    error = null
                    loading = true
                    scope.launch {
                        try {
                            if (isLogin) {
                                authRepo.signIn(identifier.trim(), password.trim())
                            } else {
                                authRepo.signUp(email.trim(), password.trim(), username.trim())
                            }

                            nav.navigate(Routes.Feed.route) {
                                popUpTo(Routes.Auth.route) { inclusive = true }
                            }
                        } catch (e: Exception) {
                            error = e.message
                        } finally {
                            loading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isLogin) "Intră" else "Creează cont")
            }

            TextButton(onClick = { isLogin = !isLogin }) {
                Text(if (isLogin) "Nu ai cont? Înregistrează-te" else "Ai deja cont? Autentifică-te")
            }

            Spacer(Modifier.height(24.dp))

            // login anonim (opțional)
            Button(
                onClick = {
                    error = null
                    loading = true
                    auth.signInAnonymously()
                        .addOnCompleteListener { task ->
                            loading = false
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                Log.d("AuthScreen", "Anonymous sign-in successful. UID: ${user?.uid}")
                                nav.navigate(Routes.Feed.route) {
                                    popUpTo(Routes.Auth.route) { inclusive = true }
                                }
                            } else {
                                val exception = task.exception
                                Log.e("AuthScreen", "Anonymous sign-in failed.", exception)
                                error = exception?.localizedMessage ?: "Anonymous sign-in failed."
                            }
                        }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continuă ca anonim")
            }
        }
    }
}
