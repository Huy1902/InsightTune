package com.example.signinandlogin.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.signinandlogin.controller.AuthController
import com.example.signinandlogin.R

@Composable
fun StartScreen(onSwitchToSignUp: () -> Unit) {
    val controller = remember { AuthController() }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    val gradient = Brush.verticalGradient(
        colorStops = arrayOf(
            0.0f to Color(0xFFFF0000),
            0.3f to Color(0xFF8B0000),
            0.6f to Color(0xFF000000)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradient)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(

            ) {
                Image(
                    painter = painterResource(id = R.drawable.spotube),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(0.dp, 0.dp)
                        .size(320.dp)
                )
                Text(
                    "SpoTube",
                    style = TextStyle(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red,
                        fontFamily = FontFamily.Serif
                    ),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(60.dp)
                )
            }
            Text(
                "Stream the beat,",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color =  Color.White,
                    fontFamily = FontFamily.Serif
                )
            )
            Text(
                "feel the heat.",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color =  Color.White,
                    fontFamily = FontFamily.Serif
                )
            )
            Spacer(Modifier.height(50.dp))
            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red
                ),
                modifier = Modifier
                    .width(250.dp)
            ) {
                Text(
                    "Sign up free",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = {},
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black
                ),
                modifier = Modifier
                    .width(250.dp),
                border = BorderStroke(2.dp, Color.White)
            ) {
                Row (
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.google),
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(start = 0.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Continue with Google",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.width(30.dp))
                }
            }

            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = {},
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black
                ),
                modifier = Modifier
                    .width(250.dp),
                border = BorderStroke(2.dp, Color.White)
            ) {
                Row (
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(R.drawable.facebook),
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(start = 0.dp)
                    )

                    Spacer(modifier = Modifier.width(0.dp))

                    Text(
                        text = "Continue with Facebook",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )

                  //  Spacer(modifier = Modifier.width(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            TextButton(
                onClick = {}
            ) {
                Text(
                    text ="Log in",
                    color = Color.White
                )
            }
            message?.let { Text(it) }
        }

    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun StartScreenPreview() {
    StartScreen(onSwitchToSignUp = {})
}