package com.cscareerguide.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.cscareerguide.app.ui.theme.CareerPalette

@Composable
fun RegisterScreen(
    loading: Boolean,
    error: String?,
    onRegister: (String, String, String) -> Unit,
    onBack: () -> Unit,
    onClearError: () -> Unit,
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CareerPalette.Canvas)
            .imePadding()
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 24.dp),
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("register_back")) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "返回登录")
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "建立你的本地示例账号",
                style = MaterialTheme.typography.headlineMedium,
                color = CareerPalette.Ink,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "本阶段只保存界面内存状态，不会上传账号或创建真实用户。",
                style = MaterialTheme.typography.bodyMedium,
                color = CareerPalette.Muted,
            )
            Spacer(Modifier.height(24.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = CareerPalette.Surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, CareerPalette.Border),
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            onClearError()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_username"),
                        label = { Text("账号") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            onClearError()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_password"),
                        label = { Text("密码") },
                        supportingText = { Text("至少 6 位") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next,
                        ),
                    )
                    OutlinedTextField(
                        value = confirmation,
                        onValueChange = {
                            confirmation = it
                            onClearError()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("register_confirmation"),
                        label = { Text("确认密码") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                        ),
                    )
                    if (error != null) {
                        Text(
                            text = error,
                            color = CareerPalette.Error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.testTag("register_error"),
                        )
                    }
                    Button(
                        onClick = { onRegister(username, password, confirmation) },
                        enabled = !loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("register_submit"),
                        colors = ButtonDefaults.buttonColors(containerColor = CareerPalette.Accent),
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = CareerPalette.Surface,
                            )
                        } else {
                            Text("创建示例账号")
                        }
                    }
                }
            }
        }
    }
}
