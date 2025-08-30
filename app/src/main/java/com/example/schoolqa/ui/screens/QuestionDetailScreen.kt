package com.example.schoolqa.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.schoolqa.data.FirestoreRepository
import com.example.schoolqa.model.Answer
import com.example.schoolqa.model.Question
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun QuestionDetailScreen(nav: NavController, questionId: String) {
    val repo = remember { FirestoreRepository(FirebaseFirestore.getInstance(), FirebaseAuth.getInstance()) }
    var question by remember { mutableStateOf<Question?>(null) }
    var answers by remember { mutableStateOf<List<Answer>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(questionId) {
        question = repo.getQuestion(questionId)
        repo.observeAnswers(questionId).collectLatest { answers = it }
    }

    var newAnswer by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize()) {
        question?.let { q ->
            Column(Modifier.padding(16.dp)) {
                Text(q.title, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(6.dp))
                Text(q.body)
                Spacer(Modifier.height(6.dp))
                Text("de ${q.authorName} • ${q.subject}", style = MaterialTheme.typography.labelMedium)
            }
        }
        Divider()
        Text("Răspunsuri", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))
        LazyColumn(Modifier.weight(1f)) {
            items(answers) { a -> AnswerItem(a, onVote = { value ->
                scope.launch {
                    try { repo.voteAnswer(questionId, a.id, value) } catch (e: Exception) { /* handle */ }
                }
            }) }
        }
        Divider()
        Column(Modifier.padding(16.dp)) {
            OutlinedTextField(value = newAnswer, onValueChange = { newAnswer = it }, label = { Text("Scrie un răspuns") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                scope.launch {
                    try {
                        repo.addAnswer(questionId, newAnswer.trim())
                        newAnswer = ""
                    } catch (e: Exception) { error = e.message }
                }
            }, enabled = newAnswer.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Trimite răspuns") }
        }
    }
}

@Composable
private fun AnswerItem(a: Answer, onVote: (Int) -> Unit) {
    Card(Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(a.body)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AssistChip(onClick = { onVote(1) }, label = { Text("Like ${a.likes}") }, leadingIcon = { Icon(Icons.Default.ThumbUp, contentDescription = null) })
                AssistChip(onClick = { onVote(-1) }, label = { Text("Dislike ${a.dislikes}") }, leadingIcon = { Icon(Icons.Default.ThumbDown, contentDescription = null) })
            }
        }
    }
}
