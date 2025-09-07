package com.example.schoolqa.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.schoolqa.data.FirestoreRepository
import com.example.schoolqa.model.Question
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun QuestionCard(
    q: Question,
    repo: FirestoreRepository,
    onClick: () -> Unit = {}
) {
    val user = FirebaseAuth.getInstance().currentUser
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clickable { onClick() }
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(q.title, style = MaterialTheme.typography.titleLarge)

                if (q.authorUid == user?.uid) {
                    IconButton(onClick = {
                        scope.launch {
                            repo.deleteQuestion(q.id!!)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Șterge"
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            Text(
                q.body.take(140) + if (q.body.length > 140) "…" else ""
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "de ${q.authorName} • ${q.subject}",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
