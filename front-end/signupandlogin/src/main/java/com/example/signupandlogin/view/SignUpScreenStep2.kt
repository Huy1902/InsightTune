package com.example.signupandlogin.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.example.signupandlogin.controller.AuthController
import com.example.signupandlogin.R
import com.example.signupandlogin.controller.SignUpAndLogInController
import com.example.signupandlogin.model.SignupModel
import java.nio.file.WatchEvent
fun isValidPassword(password: String): Boolean {
    // Tối thiểu 8 ký tự, có ít nhất 1 chữ cái và 1 số
    val passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$".toRegex()
    return passwordRegex.matches(password)
}
@Composable
fun SignUpScreenStep2(controller: SignUpAndLogInController) {
    var password by remember { mutableStateOf("") }
    var isValid by remember { mutableStateOf(true) }
    var isVisible by remember { mutableStateOf(false) }
    val gradient = Brush.verticalGradient(
        colorStops = arrayOf(
            0.0f to Color(0xFFFF0000),
            0.3f to Color(0xFF8B0000),
            0.6f to Color(0xFF000000)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradient)
            .padding(16.dp)
            .statusBarsPadding(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Icon(
                painter = painterResource(id = R.drawable.back_button),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp)
                    .size(32.dp)
                    .clickable{
                        controller.goBack()
                    }
            )

            Text(
                text = "Create account",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Spacer(modifier = Modifier.size(20.dp))

        Column(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start

        ) {
            Text(
                "Create your password",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.White,
                modifier = Modifier.align(Alignment.Start)
            )

            TextField(
                value = password,
                onValueChange = {
                    password = it
                    isValid = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp)
                    .clip(RoundedCornerShape(12.dp)),
                visualTransformation = if (isVisible) VisualTransformation.None
                else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(
                        onClick = { isVisible = !isVisible }
                    ) {
                        Icon(
                            painter = if (isVisible)
                                painterResource(id = R.drawable.opened_eye)
                            else
                                painterResource(id = R.drawable.closed_eye),
                            contentDescription = if (isVisible) "Hide password" else "Show password"
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.size(6.dp))

            Text(
                "Password must be at least 8 characters long and contain both letters and numbers.",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = Color.White,
                modifier = Modifier.align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.size(30.dp))

        Button(
            onClick = {
                isValid = isValidPassword(password)
                controller.onPasswordEntered(password)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White
            )
        ) {
            Text(
                "Next",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.Black,
            )
        }

        if(!isValid) {
            Text(
                "Invalid password. Please try again.",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = Color.White,
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignUpScreenStep2Preview() {
    val navController = rememberNavController()
    val model = SignupModel()
    val controller = SignUpAndLogInController(navController, model)

    SignUpScreenStep2(controller = controller)
}
