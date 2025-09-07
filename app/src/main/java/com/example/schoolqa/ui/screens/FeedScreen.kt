package com.example.schoolqa.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(nav: NavController) {
    val repo = remember {
        FirestoreRepository(
            FirebaseFirestore.getInstance(),
            FirebaseAuth.getInstance()
        )
    }

    var items by remember { mutableStateOf<List<Question>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    val subjects = listOf("Toate", "General", "Matematică", "Română", "Fizică", "Istorie", "Informatică")
    var selectedSubject by remember { mutableStateOf("Toate") }
    var expandedFilter by remember { mutableStateOf(false) }

    LaunchedEffect(selectedSubject) {
        loading = true
        repo.observeQuestionsBySubject(selectedSubject).collect { list ->
            items = list
            loading = false
        }
    }

    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser
    val initial = (user?.displayName?.firstOrNull()
        ?: user?.email?.firstOrNull()
        ?: 'A').toString().uppercase()

    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Întrebări") },
                actions = {
                    IconButton(onClick = { nav.navigate(Routes.Post.route) }) {
                        Icon(Icons.Default.Add, contentDescription = "Postează")
                    }

                    Box {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { expanded = true }
                        ) {
                            Box(
                                modifier = Modifier.size(36.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initial,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clickable {
                                        expanded = false
                                        auth.signOut()
                                        nav.navigate(Routes.Auth.route) {
                                            popUpTo(Routes.Feed.route) { inclusive = true }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Logout,
                                    contentDescription = "Delogare"
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            ExposedDropdownMenuBox(
                expanded = expandedFilter,
                onExpandedChange = { expandedFilter = !expandedFilter },
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedSubject,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Filtrează după materie") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFilter)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expandedFilter,
                    onDismissRequest = { expandedFilter = false }
                ) {
                    subjects.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                selectedSubject = option
                                expandedFilter = false
                            }
                        )
                    }
                }
            }

            when {
                loading -> {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                items.isEmpty() -> {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Nu există întrebări pentru categoria selectată.")
                    }
                }

                else -> {
                    LazyColumn {
                        items(items) { q ->
                            QuestionCard(
                                q = q,
                                repo = repo,
                                onClick = {
                                    nav.navigate(Routes.QuestionDetail.route + "/${q.id}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
