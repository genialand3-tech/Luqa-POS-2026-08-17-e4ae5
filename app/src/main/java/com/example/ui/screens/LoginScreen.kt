package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    var isLoginMode by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Luqa POS",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isLoginMode) "Ingresa para gestionar tu negocio" else "Crea tu cuenta gratis",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo Electrónico") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            if (!isLoginMode) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirmar Contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp),
                    fontSize = 14.sp
                )
            }

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Por favor completa todos los campos"
                        return@Button
                    }
                    if (!isLoginMode && password != confirmPassword) {
                        errorMessage = "Las contraseñas no coinciden"
                        return@Button
                    }

                    isLoading = true
                    errorMessage = null
                    coroutineScope.launch {
                        try {
                            val auth = FirebaseAuth.getInstance()
                            val result = if (isLoginMode) {
                                auth.signInWithEmailAndPassword(email, password).await()
                            } else {
                                auth.createUserWithEmailAndPassword(email, password).await()
                            }
                            
                            result.user?.uid?.let { uid ->
                                onLoginSuccess(uid)
                            } ?: run {
                                errorMessage = "Error al obtener ID de usuario"
                            }
                        } catch (e: Exception) {
                            errorMessage = "Error: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (isLoginMode) "Ingresar" else "Registrarse", fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    isLoading = true
                    errorMessage = null
                    coroutineScope.launch {
                        try {
                            val webClientIdResId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
                            val webClientId = if (webClientIdResId != 0) context.getString(webClientIdResId) else null
                            
                            if (webClientId == null) {
                                errorMessage = "No se encontró default_web_client_id. Revisa tu google-services.json."
                                isLoading = false
                                return@launch
                            }

                            val credentialManager = CredentialManager.create(context)
                            val googleIdOption = GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId(webClientId)
                                .build()
                            
                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdOption)
                                .build()
                            
                            val result = credentialManager.getCredential(context, request)
                            val credential = result.credential
                            
                            if (credential is androidx.credentials.CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                                val authResult = FirebaseAuth.getInstance().signInWithCredential(authCredential).await()
                                
                                authResult.user?.uid?.let { uid ->
                                    onLoginSuccess(uid)
                                } ?: run {
                                    errorMessage = "Error al obtener ID de usuario de Google"
                                }
                            } else {
                                errorMessage = "Credencial no válida"
                            }
                        } catch (e: GetCredentialException) {
                            errorMessage = "Se canceló o falló la autenticación con Google"
                        } catch (e: Exception) {
                            errorMessage = "Error al iniciar sesión con Google: ${e.localizedMessage}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isLoading
            ) {
                Text("Continuar con Google", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isLoginMode) "¿No tienes cuenta? Regístrate aquí" else "¿Ya tienes cuenta? Ingresa aquí",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    isLoginMode = !isLoginMode
                    errorMessage = null
                },
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
