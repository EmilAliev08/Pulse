package com.example.pulse

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseUser

@Composable
fun LoginScreen(
    onLoginSuccess: (FirebaseUser) -> Unit,
    viewModel: AuthViewModel
) {
    val Background = Color(0xFF101114)
    val Accent = Color(0xFFFF8057)
    val MainText = Color(0xFFF4F3F1)
    val SecondaryText = Color(0xFF9B9CA5)

    val currentUser by viewModel.currentUser.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoginMode by remember { mutableStateOf(true) } // true = вход, false = регистрация

    val context = LocalContext.current

    // Если пользователь вошёл — переходим в приложение
    LaunchedEffect(currentUser) {
        currentUser?.let { onLoginSuccess(it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Логотип
            Text(
                text = "pulse.",
                color = Accent,
                fontSize = 48.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = if (isLoginMode) "Войди в свой аккаунт" else "Создай аккаунт",
                color = MainText,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(48.dp))

            // Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Email") },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = SecondaryText) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Accent,
                    unfocusedBorderColor = Color(0xFF2D2F34),
                    focusedTextColor = MainText,
                    unfocusedTextColor = MainText
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            Spacer(Modifier.height(16.dp))

            // Пароль
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Пароль (мин. 6 символов)") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = SecondaryText) },
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Accent,
                    unfocusedBorderColor = Color(0xFF2D2F34),
                    focusedTextColor = MainText,
                    unfocusedTextColor = MainText
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Ошибка
            errorMessage?.let { error ->
                Spacer(Modifier.height(12.dp))
                Text(text = error, color = Color(0xFFFF5252), fontSize = 14.sp)
            }

            Spacer(Modifier.height(24.dp))

            // Кнопка входа/регистрации
            Button(
                onClick = {
                    if (isLoginMode) {
                        viewModel.login(email, password)
                    } else {
                        viewModel.register(email, password)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Background, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = if (isLoginMode) "Войти" else "Зарегистрироваться",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Background
                    )
                }
            }

            Spacer(Modifier.height(24.dp))


            // Переключатель вход/регистрация
            TextButton(onClick = { isLoginMode = !isLoginMode }) {
                Text(
                    text = if (isLoginMode) "Нет аккаунта? Зарегистрируйся" else "Уже есть аккаунт? Войди",
                    color = Accent,
                    fontSize = 14.sp
                )
            }
        }
    }
}