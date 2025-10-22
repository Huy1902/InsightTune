package com.example.frontend.ui.start

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.R
import com.example.frontend.core.AppPreferences
import com.example.frontend.ui.theme.AppTheme

// Stateful Composable (chứa logic)
@Composable
fun StartScreen(
    onNextSignUp: () -> Unit,
    onNextLogIn: () -> Unit,
    onGoogleLogin: () -> Unit,
    prefs: AppPreferences
) {
    val context = LocalContext.current
    val signInWithGoogle = rememberGoogleSignInManager(
        onLoginSuccess = { jwt ->
            Log.d("LoginScreen", "Got system JWT: $jwt")
            prefs.saveToken(jwt.token)
            prefs.saveRefreshToken(jwt.refreshToken)
            onGoogleLogin()
        },
        onLoginFailure = { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        }
    )

    StartScreenContent(
        onSignUpClick = onNextSignUp,
        onLoginClick = onNextLogIn,
        onGoogleClick = {
            signInWithGoogle()
        },
        onFacebookClick = {
            Toast.makeText(context, "Facebook Login is not implemented yet.", Toast.LENGTH_SHORT)
                .show()
        }
    )
}

@Composable
fun StartScreenContent(
    onSignUpClick: () -> Unit,
    onLoginClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onFacebookClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppTheme.spacing().M),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.spotube_cropped),
                    contentDescription = "App logo",
                    modifier = Modifier.size(160.dp)
                )
                Text(
                    stringResource(R.string.slogan_1),
                    style = AppTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    stringResource(R.string.slogan_2),
                    style = AppTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onSignUpClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.color().Primary
                ),
                modifier = Modifier.width(250.dp)
            ) {
                Text(
                    stringResource(R.string.sign_up),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = AppTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.height(AppTheme.spacing().S))
            OutlinedButton(
                onClick = onGoogleClick,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                modifier = Modifier.width(250.dp),
                border = BorderStroke(2.dp, Color.Gray)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.google),
                        contentDescription = "Google icon",
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        stringResource(R.string.Google),
                        style = AppTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(Modifier.height(AppTheme.spacing().S))
            OutlinedButton(
                onClick = onFacebookClick,
                modifier = Modifier.width(250.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                border = BorderStroke(2.dp, Color.Gray)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.facebook),
                        contentDescription = "Facebook icon",
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        stringResource(R.string.Facebook),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = AppTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
            Spacer(Modifier.height(AppTheme.spacing().XS))
            TextButton(onClick = onLoginClick) {
                Text(
                    stringResource(R.string.Log_in),
                    color = MaterialTheme.colorScheme.onBackground,
                    style = AppTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing().L))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun StartScreenPreview() {
    AppTheme {
        StartScreenContent(
            onSignUpClick = {},
            onLoginClick = {},
            onGoogleClick = {},
            onFacebookClick = {}
        )
    }
}