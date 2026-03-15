package com.example.academicleveling.ui.auth

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.academicleveling.R
import com.example.academicleveling.ui.theme.* val PrimaryPurple = Color(0xFFBD00FF)
val FieldBackground = Color(0xFF4A3F6D).copy(alpha = 0.6f)

// ─────────────────────────────────────────────────────────────────────────────
//  SIMPLE SPACE BACKGROUND (Moving Stars Only)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SpaceBackground(content: @Composable () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "simple_space")

    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "time"
    )

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Image(
            painter = painterResource(id = R.drawable.albg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val random = kotlin.random.Random(42)

            repeat(100) { i ->
                val startX = random.nextFloat() * size.width
                val startY = random.nextFloat() * size.height

                // Direction: alternate up (-1) and down (1)
                val direction = if (i % 2 == 0) -1 else 1
                val speed = (0.3f + random.nextFloat() * 0.4f) * direction

                val currentY = (startY + (time * speed)) % size.height
                val finalY = if (currentY < 0) currentY + size.height else currentY

                drawCircle(
                    color = Color.White,
                    radius = 1f + random.nextFloat() * 2.5f,
                    center = Offset(startX, finalY),
                    alpha = 0.3f + random.nextFloat() * 0.5f
                )
            }
        }
        content()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  AUTH SCREENS (NO ANIMATIONS)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun LoginScreen(onLogin: () -> Unit, onSignup: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    SpaceBackground {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 45.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(350.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AuthLabel("Email")
                AuthTextField(value = email, onValueChange = { email = it }, placeholder = "Value")

                Spacer(Modifier.height(16.dp))

                AuthLabel("Password")
                AuthTextField(value = password, onValueChange = { password = it }, placeholder = "Value", isPassword = true)

                Spacer(Modifier.height(30.dp))

                Button(
                    onClick = onLogin,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                ) {
                    Text("Login", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Text(
                    "Forgot Password?",
                    color = Color(0xFF9181FF).copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 20.dp).clickable { }
                )

                Spacer(Modifier.height(60.dp))

                Row(Modifier.padding(bottom = 40.dp)) {
                    Text("Don't have an account? ", color = Color.White, fontSize = 13.sp)
                    Text(
                        "Signup",
                        color = Color(0xFF9181FF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.clickable { onSignup() }
                    )
                }
            }
        }
    }
}

@Composable
fun SignupScreen(onSignup: () -> Unit, onLogin: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    SpaceBackground {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 45.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(320.dp))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AuthLabel("Username")
                AuthTextField(value = username, onValueChange = { username = it }, placeholder = "Value")
                Spacer(Modifier.height(14.dp))
                AuthLabel("Email")
                AuthTextField(value = email, onValueChange = { email = it }, placeholder = "Value")
                Spacer(Modifier.height(14.dp))
                AuthLabel("Password")
                AuthTextField(value = password, onValueChange = { password = it }, placeholder = "Value", isPassword = true)
                Spacer(Modifier.height(14.dp))
                AuthLabel("Confirm Password")
                AuthTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, placeholder = "Value", isPassword = true)

                Spacer(Modifier.height(30.dp))

                Button(
                    onClick = onSignup,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                ) {
                    Text("Signup", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(Modifier.height(40.dp))

                Row(Modifier.padding(bottom = 30.dp)) {
                    Text("Already have an account? ", color = Color.White, fontSize = 13.sp)
                    Text(
                        "Login",
                        color = Color(0xFF9181FF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.clickable { onLogin() }
                    )
                }
            }
        }
    }
}

@Composable
fun AuthLabel(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 13.sp,
        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp, start = 4.dp)
    )
}

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth().height(54.dp),
        placeholder = { Text(placeholder, color = Color.White.copy(0.3f), fontSize = 13.sp) },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = FieldBackground,
            unfocusedContainerColor = FieldBackground,
            focusedBorderColor = Color.White.copy(0.4f),
            unfocusedBorderColor = Color.White.copy(0.2f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        singleLine = true
    )
}