package com.example.schoolqa.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.* // Keep only one of these if there are duplicates
import androidx.compose.material3.* // Keep only one of these if there are duplicates
import androidx.compose.runtime.* // Keep only one of these if there are duplicates
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext // Keep only one of these if there are duplicates
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp // Keep only one of these if there are duplicates
import androidx.navigation.NavController
import com.example.schoolqa.Routes
import com.example.schoolqa.data.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException // <-- ADD THIS IMPORT!
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import com.example.schoolqa.R

@Composable
fun AuthScreen(nav: NavController, auth: FirebaseAuth) {
    val authRepo = remember { AuthRepository(auth) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current // Get the current Context

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLogin by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Configure Google Sign In
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            // Change this line to:
            .requestIdToken(context.getString(R.string.default_web_client_id)) // <-- Use R.string directly
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    // Launcher for handling the Google Sign-In flow
    val googleSignInLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java) // Import ApiException
                account.idToken?.let { idToken ->
                    scope.launch {
                        try {
                            authRepo.signInWithGoogle(idToken)
                            nav.navigate(Routes.Feed.route) { popUpTo(Routes.Auth.route) { inclusive = true } }
                        } catch (e: Exception) {
                            error = e.message
                        }
                    }
                } ?: run {
                    error = "Google ID Token was null."
                }
            } catch (e: ApiException) {
                error = "Google Sign-In failed: ${e.statusCode} ${e.localizedMessage}"
            } catch (e: Exception) {
                error = "An unexpected error occurred during Google Sign-In: ${e.localizedMessage}"
            }
        } else {
            error = "Google Sign-In cancelled or failed with result code: ${result.resultCode}"
        }
    }


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
            Spacer(Modifier.height(12.dp))
            // NEW: Google Sign-In Button
            Button(
                onClick = {
                    error = null // Clear previous errors
                    val signInIntent = googleSignInClient.signInIntent
                    googleSignInLauncher.launch(signInIntent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Conectează-te cu Google")
            }
        }
    }
}
