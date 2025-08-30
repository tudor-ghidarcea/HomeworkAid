package com.example.schoolqa.ui.screens

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
fun AuthScreen(nav: NavController) {
    val authRepo = remember { AuthRepository(FirebaseAuth.getInstance()) }
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLogin by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (authRepo.currentUser() != null) {
            nav.navigate(Routes.Feed.route) { popUpTo(Routes.Auth.route) { inclusive = true } }
        }
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (isLogin) "Autentificare" else "Creare cont", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Parolă") },
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(Modifier.height(12.dp))
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Spacer(Modifier.height(12.dp))
            Button(onClick = {
                scope.launch {
                    try {
                        if (isLogin) authRepo.signIn(email, password) else authRepo.signUp(email, password)
                        nav.navigate(Routes.Feed.route) { popUpTo(Routes.Auth.route) { inclusive = true } }
                    } catch (e: Exception) {
                        error = e.message
                    }
                }
            }, modifier = Modifier.fillMaxWidth()) {
                Text(if (isLogin) "Intră" else "Creează cont")
            }
            TextButton(onClick = { isLogin = !isLogin }) {
                Text(if (isLogin) "Nu ai cont? Înregistrează-te" else "Ai deja cont? Autentifică-te")
            }
        }
    }
}
