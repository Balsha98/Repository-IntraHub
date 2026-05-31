package com.bazovic.balsa.intrahub.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bazovic.balsa.intrahub.data.UserProfile
import com.bazovic.balsa.intrahub.data.repository.AuthRepository
import kotlinx.coroutines.launch

enum class AppState {
    Login,
    Loading,
    App
}//AppState

class AppViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
) : ViewModel() {

    // ─── SECTION: State ─── //
    var appState: AppState by mutableStateOf(AppState.Login)
        private set

    var currentUser: UserProfile? by mutableStateOf(null)
        private set

    var isLoading: Boolean by mutableStateOf(false)
        private set

    var errorMessage: String? by mutableStateOf(null)
        private set

    // ─── SECTION: Actions ─── //
    fun login(username: String, password: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                authRepository.signIn(username, password)

                val profile = authRepository.getCurrentProfile()

                if (profile != null) {
                    currentUser = profile
                    appState = AppState.Loading
                } else {
                    errorMessage = "Account found but profile is missing. Contact Campus Rec."
                }
            } catch (_: Exception) {
                errorMessage = "Invalid username or password."
            } finally {
                isLoading = false
            }//try/catch/finally
        }//launch
    }//login

    fun onLoadingComplete() {
        if (appState == AppState.Loading) appState = AppState.App
    }//onLoadingComplete

    fun logout() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
            } catch (_: Exception) {}

            currentUser = null
            errorMessage = null

            appState = AppState.Login
        }//launch
    }//logout
}//AppViewModel
