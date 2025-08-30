package com.example.schoolqa.data

import com.example.schoolqa.model.Answer
import com.example.schoolqa.model.Question
import com.example.schoolqa.model.Vote
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.Flow

class FirestoreRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val questions = db.collection("questions")

    fun observeQuestions(): Flow<List<Question>> = callbackFlow {
        val reg = questions
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snap?.documents?.mapNotNull { doc ->
                    doc.toObject(Question::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    suspend fun postQuestion(title: String, body: String, subject: String): String {
        val user = auth.currentUser ?: throw IllegalStateException("Not signed in")
        val q = hashMapOf(
            "title" to title,
            "body" to body,
            "authorUid" to user.uid,
            "authorName" to (user.displayName ?: user.email ?: "Anon"),
            "createdAt" to Timestamp.now(),
            "subject" to subject,
            "imageUrl" to null
        )
        val ref = questions.add(q).await()
        return ref.id
    }

    suspend fun getQuestion(questionId: String): Question? {
        val doc = questions.document(questionId).get().await()
        return doc.toObject(Question::class.java)?.copy(id = doc.id)
    }

    fun observeAnswers(questionId: String): Flow<List<Answer>> = callbackFlow {
        val ref = questions.document(questionId).collection("answers")
        val reg = ref
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snap?.documents?.mapNotNull { doc ->
                    doc.toObject(Answer::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    suspend fun addAnswer(questionId: String, body: String) {
        val user = auth.currentUser ?: throw IllegalStateException("Not signed in")
        val answer = hashMapOf(
            "questionId" to questionId,
            "body" to body,
            "authorUid" to user.uid,
            "authorName" to (user.displayName ?: user.email ?: "Anon"),
            "createdAt" to Timestamp.now(),
            "likes" to 0,
            "dislikes" to 0
        )
        questions.document(questionId).collection("answers").add(answer).await()
    }

    /** Votează un răspuns. value: +1 like, -1 dislike. Un singur vot per utilizator. */
    suspend fun voteAnswer(questionId: String, answerId: String, value: Int) {
        val user = auth.currentUser ?: throw IllegalStateException("Not signed in")
        val answerRef = questions.document(questionId).collection("answers").document(answerId)
        val voteRef = answerRef.collection("votes").document(user.uid)

        db.runTransaction { tr ->
            val current = tr.get(answerRef)
            val answer = current.toObject(Answer::class.java)
            val likes = answer?.likes ?: 0
            val dislikes = answer?.dislikes ?: 0
            val existing = tr.get(voteRef)
            val prevValue = if (existing.exists()) existing.getLong("value")?.toInt() ?: 0 else 0

            var newLikes = likes
            var newDislikes = dislikes

            // Retrage efectul votului precedent
            when (prevValue) {
                1 -> newLikes -= 1
                -1 -> newDislikes -= 1
            }
            // Aplică noul vot (dacă e diferit de precedent)
            if (value == 1) newLikes += 1 else if (value == -1) newDislikes += 1

            tr.update(answerRef, mapOf("likes" to newLikes, "dislikes" to newDislikes))
            tr.set(voteRef, mapOf("userId" to user.uid, "value" to value))
        }.await()
    }
}
