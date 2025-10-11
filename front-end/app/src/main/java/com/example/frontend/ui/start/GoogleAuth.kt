package com.example.frontend.ui.start

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.frontend.data.models.user.GoogleResponseResult
import com.example.frontend.data.remote.ApiClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun rememberGoogleSignInManager(
    onLoginSuccess: (tokens: GoogleResponseResult) -> Unit,
    onLoginFailure: (errorMessage: String) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val activity = context as? Activity
        ?: throw IllegalStateException("Google Sign-In must be launched from an Activity context.")
    val coroutineScope = rememberCoroutineScope()

    Log.d("GoogleSignIn", "Setting up GoogleSignInOptions...")

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken("707539669485-hvb782jf95k54qogpjmcalbjiiec9nrd.apps.googleusercontent.com")
        .requestEmail()
        .build()

    val googleSignInClient = GoogleSignIn.getClient(activity, gso)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("GoogleSignIn", "Launcher callback triggered. Result code: ${result.resultCode}")

        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                Log.d("GoogleSignIn", "Account: ${account?.email}, ID Token: ${idToken}...")

                if (idToken != null) {
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            Log.d("GoogleSignIn", "Sending ID token to backend for verification...")
                            val systemJwt = ApiClient.loginWithGoogleIdToken(idToken)
                            withContext(Dispatchers.Main) {
                                Log.d("GoogleSignIn", "Backend verified successfully! JWT received.")
                                onLoginSuccess(systemJwt)
                            }
                        } catch (e: Exception) {
                            Log.e("GoogleSignIn", "Backend verification failed", e)
                            withContext(Dispatchers.Main) {
                                onLoginFailure("Verification with server failed ${e.message}")
                            }
                        }
                    }
                } else {
                    Log.e("GoogleSignIn", "Cannot get ID Token from Google account")
                    onLoginFailure("Cannot get ID Token from Google account.")
                }
            } catch (e: ApiException) {
                Log.e("GoogleSignIn", "Sign-in failed with code: ${e.statusCode}", e)
                onLoginFailure("Log in failed (code ${e.statusCode})")
            }
        } else {
            Log.w("GoogleSignIn", "Result code different from RESULT_OK: ${result.resultCode}")
            onLoginFailure("User canceled the sign-in process or an error occurred.")
        }
    }

    return {
        Log.d("GoogleSignIn", "Launching Google Sign-In intent...")
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }
}
