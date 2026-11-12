package com.example.pulse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener { _isLoading.value = false }
                    .addOnFailureListener { e ->
                        _isLoading.value = false
                        _errorMessage.value = e.message ?: "Ошибка входа"
                    }
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = e.message ?: "Неизвестная ошибка"
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener { _isLoading.value = false }
                    .addOnFailureListener { e ->
                        _isLoading.value = false
                        _errorMessage.value = e.message ?: "Ошибка регистрации"
                    }
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = e.message ?: "Неизвестная ошибка"
            }
        }
    }

    fun logout() {
        auth.signOut()
        _currentUser.value = null
    }
}