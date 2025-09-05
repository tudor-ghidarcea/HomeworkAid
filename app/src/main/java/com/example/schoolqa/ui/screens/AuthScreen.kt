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
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(nav: NavController, auth: FirebaseAuth) {
    val authRepo = remember { AuthRepository(auth) }
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
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

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Parolă") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation()
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
                    if (email.isBlank() || password.isBlank()) {
                        error = "Email și parola nu pot fi goale."
                        return@Button
                    }
                    error = null
                    loading = true
                    scope.launch {
                        try {
                            if (isLogin) authRepo.signIn(email, password)
                            else authRepo.signUp(email, password)

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
