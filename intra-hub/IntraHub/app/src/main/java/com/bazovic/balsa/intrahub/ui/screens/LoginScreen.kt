package com.bazovic.balsa.intrahub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bazovic.balsa.intrahub.ui.theme.*

@Composable
fun LoginScreen(
    onLogin: (username: String, password: String) -> Unit,
    isLoading: Boolean,
    errorMessage: String? = null,
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPass by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf("") }

    val displayError = errorMessage?.takeIf { it.isNotEmpty() } ?: localError

    fun submit() {
        val id = username.trim().lowercase().replace(Regex("@.*$"), "")
        when {
            id.isEmpty() -> localError = "Enter your RIT username."
            password.length < 4 -> localError = "Enter your password."
            else -> { localError = ""; onLogin(id, password) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Canvas)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
    ) {
        Spacer(Modifier.height(28.dp))

        // ─── SECTION: Logo ─── //
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Ink),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(OrangeRIT)
                )
            }
            Column {
                Text("IntraHub", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, letterSpacing = (-0.6).sp, lineHeight = 22.sp)
                Text("RIT INTRAMURALS", color = Ink4, fontSize = 12.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.sp)
            }
        }

        Spacer(Modifier.height(28.dp))

        // ─── SECTION: Headline ─── //
        Text(
            buildAnnotatedString {
                append("Welcome back, ")
                withStyle(SpanStyle(color = OrangeRIT)) { append("Tiger.") }
            },
            fontWeight = FontWeight.ExtraBold,
            fontSize = 30.sp,
            letterSpacing = (-0.98).sp,
            lineHeight = 34.sp,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Sign in with your RIT computer account. Access is limited to current students, faculty, and staff.",
            color = Ink4,
            fontSize = 16.sp,
            lineHeight = 24.sp,
        )

        Spacer(Modifier.height(24.dp))

        // ─── SECTION: Username Field ─── //
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("RIT USERNAME", color = Ink4, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace, letterSpacing = 0.72.sp)
            OutlinedTextField(
                value = username,
                onValueChange = { username = it; localError = "" },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("abc1234", color = Ink5) },
                leadingIcon = { Icon(Icons.Default.Person, null, tint = Ink5) },
                trailingIcon = {
                    Text("@rit.edu", color = Ink5, fontSize = 14.sp, fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(end = 14.dp))
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                ),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangeRIT,
                    unfocusedBorderColor = Line,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                ),
            )
        }

        Spacer(Modifier.height(14.dp))

        // ─── SECTION: Password Field ─── //
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("PASSWORD", color = Ink4, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace, letterSpacing = 0.72.sp)
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; localError = "" },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("••••••••", color = Ink5) },
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = Ink5) },
                trailingIcon = {
                    IconButton(onClick = { showPass = !showPass }) {
                        Icon(
                            if (showPass) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (showPass) "Hide password" else "Show password",
                            tint = Ink5,
                        )
                    }
                },
                singleLine = true,
                visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangeRIT,
                    unfocusedBorderColor = Line,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                ),
            )
        }

        // ─── SECTION: Error ─── //
        if (displayError.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(LossTint)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Text(displayError, color = Loss, fontSize = 14.sp, lineHeight = 20.sp)
            }
        }

        Spacer(Modifier.height(14.dp))

        // ─── SECTION: Sign In Button ─── //
        Button(
            onClick = { submit() },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = OrangeRIT,
                contentColor = Color.White,
                disabledContainerColor = Ink5,
                disabledContentColor = Color.White,
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Authenticating…", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            } else {
                Text("Sign In", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(18.dp))
            }
        }

        // ─── SECTION: Forgot Password ─── //
        Spacer(Modifier.height(12.dp))
        Text(
            buildAnnotatedString {
                append("Forgot password? Reset via ")
                withStyle(SpanStyle(color = OrangeRIT, fontWeight = FontWeight.SemiBold)) {
                    append("helpdesk@rit.edu")
                }
                append(".")
            },
            color = Ink4,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally),
        )

        Spacer(Modifier.weight(1f))

        // ─── SECTION: Footer ─── //
        HorizontalDivider(color = Line, modifier = Modifier.padding(top = 24.dp, bottom = 16.dp))
        Text(
            buildAnnotatedString {
                append("Need help? Contact ")
                withStyle(SpanStyle(color = OrangeRIT, fontWeight = FontWeight.SemiBold)) {
                    append("campusrec@rit.edu")
                }
                append(".")
            },
            color = Ink5,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "ROCHESTER INSTITUTE OF TECHNOLOGY · v1.0",
            color = Ink5,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.2.sp,
            modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally),
        )
        Spacer(Modifier.height(8.dp))
    }
}//LoginScreen
