package com.example.schoolqa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.schoolqa.ui.screens.*
import com.example.schoolqa.ui.theme.AppTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        auth = FirebaseAuth.getInstance()

        setContent { App(auth) }
    }
}

@Composable
fun App(auth: FirebaseAuth) {
    AppTheme {
        Surface {
            val nav = rememberNavController()

            val startDestination = if (auth.currentUser != null) {
                Routes.Feed.route
            } else {
                Routes.Auth.route
            }
            NavHost(navController = nav, startDestination = startDestination) {
                composable(Routes.Auth.route) { AuthScreen(nav, auth) }
                composable(Routes.Feed.route) { FeedScreen(nav) }
                composable(Routes.Post.route) { PostQuestionScreen(nav) }
                composable(
                    route = Routes.QuestionDetail.route + "/{qid}",
                    arguments = listOf(navArgument("qid") { type = NavType.StringType })
                ) { backStackEntry ->
                    val qid = backStackEntry.arguments?.getString("qid") ?: ""
                    QuestionDetailScreen(nav, qid)
                }
            }
        }
    }
}

sealed class Routes(val route: String) {
    data object Auth : Routes("auth")
    data object Feed : Routes("feed")
    data object Post : Routes("post")
    data object QuestionDetail : Routes("question")
}
