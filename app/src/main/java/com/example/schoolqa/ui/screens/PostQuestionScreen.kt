package com.example.schoolqa.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.schoolqa.Routes
import com.example.schoolqa.data.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun PostQuestionScreen(nav: NavController) {
    val repo = remember { FirestoreRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance()) }
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("General") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(Modifier.padding(16.dp)) {
        Text("Postează o întrebare", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Titlu") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text("Descriere/Enunț") }, modifier = Modifier.fillMaxWidth(), minLines = 5)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Materie (ex: Matematică)") }, modifier = Modifier.fillMaxWidth())
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Spacer(Modifier.height(12.dp))
        Button(onClick = {
            scope.launch {
                try {
                    repo.postQuestion(title.trim(), body.trim(), subject.trim())
                    nav.navigate(Routes.Feed.route) { popUpTo(Routes.Feed.route) { inclusive = true } }
                } catch (e: Exception) { error = e.message }
            }
        }, enabled = title.isNotBlank() && body.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
            Text("Publică")
        }
    }
}
