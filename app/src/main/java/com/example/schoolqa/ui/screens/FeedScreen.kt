package com.example.schoolqa.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.schoolqa.Routes
import com.example.schoolqa.data.FirestoreRepository
import com.example.schoolqa.model.Question
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(nav: NavController) {
    val repo = remember { FirestoreRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance()) }
    var items by remember { mutableStateOf<List<Question>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        repo.observeQuestions().collectLatest {
            items = it
            loading = false
        }
    }

    val auth = FirebaseAuth.getInstance()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Întrebări") },
                actions = {
                    IconButton(onClick = { nav.navigate(Routes.Post.route) }) {
                        Icon(Icons.Default.Add, contentDescription = "Postează")
                    }
                    IconButton(onClick = {
                        auth.signOut()
                        nav.navigate(Routes.Auth.route) {
                            popUpTo(Routes.Feed.route) { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Delogare")
                    }
                }
            )
        }
    ) { padding ->
        when {
            loading -> {
                Box(Modifier
                    .padding(padding)
                    .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            items.isEmpty() -> {
                Box(Modifier
                    .padding(padding)
                    .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nu există întrebări încă.")
                }
            }
            else -> {
                LazyColumn(Modifier.padding(padding)) {
                    items(items) { q ->
                        QuestionCard(q) { nav.navigate(Routes.QuestionDetail.route + "/${q.id}") }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionCard(q: Question, onClick: () -> Unit) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clickable { onClick() }
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(q.title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(6.dp))
            Text(q.body.take(140) + if (q.body.length > 140) "…" else "")
            Spacer(Modifier.height(8.dp))
            Text("de ${q.authorName} • ${q.subject}", style = MaterialTheme.typography.labelMedium)
        }
    }
}
