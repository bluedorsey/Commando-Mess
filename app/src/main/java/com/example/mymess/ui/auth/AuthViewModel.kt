package com.example.mymess.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class AuthViewModel : ViewModel() {
    var username by mutableStateOf("")
    var password by mutableStateOf("")
    var loginError by mutableStateOf<String?>(null)

    fun login(onSuccess: (Boolean) -> Unit) {
        if (username.isBlank() || password.isBlank()) {
            loginError = "Please enter both username and password"
            return
        }

        if (username == "" && password == "") {
            loginError = null
            onSuccess(true) 
        } else if (username == "" && password == "") {
            loginError = null
            onSuccess(false) 
        } else {
            loginError = "Invalid credentials"
        }
    }
}
