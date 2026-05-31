package com.bazovic.balsa.intrahub.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bazovic.balsa.intrahub.data.Sport
import com.bazovic.balsa.intrahub.data.Team
import com.bazovic.balsa.intrahub.data.repository.AuthRepository
import com.bazovic.balsa.intrahub.data.repository.TeamsRepository
import kotlinx.coroutines.launch

class TeamsViewModel(
    private val teamsRepo: TeamsRepository = TeamsRepository(),
    private val authRepo: AuthRepository = AuthRepository(),
) : ViewModel() {

    // ─── SECTION: Ui Filter State ─── //

    var sportFilter: Sport? by mutableStateOf(null)

    // ─── SECTION: Data State ─── //

    var allTeams: List<Team> by mutableStateOf(emptyList())
        private set

    var myTeamIds: Set<String> by mutableStateOf(emptySet())
        private set

    var isLoading: Boolean by mutableStateOf(false)
        private set

    var errorMessage: String? by mutableStateOf(null)
        private set

    init { load() }

    // ─── SECTION: Actions ─── //

    fun load() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val userId = authRepo.getCurrentUserId() ?: ""
                allTeams = teamsRepo.getTeams()
                myTeamIds = if (userId.isNotEmpty()) {
                    teamsRepo.getMyTeamIds(userId)
                } else emptySet()
            } catch (e: Exception) {
                errorMessage = "Could not load teams. Check your connection."
            } finally {
                isLoading = false
            }
        }
    }
}//TeamsViewModel
