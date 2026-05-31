package com.bazovic.balsa.intrahub.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bazovic.balsa.intrahub.data.Team
import com.bazovic.balsa.intrahub.data.repository.AuthRepository
import com.bazovic.balsa.intrahub.data.repository.TeamsRepository
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val teamsRepo: TeamsRepository = TeamsRepository(),
    private val authRepo: AuthRepository = AuthRepository(),
) : ViewModel() {

    // ─── SECTION: Data State ─── //

    var myTeams: List<Team> by mutableStateOf(emptyList())
        private set

    var gamesPlayed: Int by mutableStateOf(0)
        private set

    var wins: Int by mutableStateOf(0)
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
                val userId = authRepo.getCurrentUserId() ?: return@launch
                val myTeamIds = teamsRepo.getMyTeamIds(userId)
                val allTeams = teamsRepo.getTeams()
                myTeams = allTeams.filter { it.id in myTeamIds }

                val (played, won, _) = teamsRepo.getAggregatedStats(userId)
                gamesPlayed = played
                wins = won
            } catch (e: Exception) {
                errorMessage = "Could not load profile data."
            } finally {
                isLoading = false
            }
        }
    }
}//ProfileViewModel
