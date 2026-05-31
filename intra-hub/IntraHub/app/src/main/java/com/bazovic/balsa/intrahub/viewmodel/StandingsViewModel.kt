package com.bazovic.balsa.intrahub.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bazovic.balsa.intrahub.data.Sport
import com.bazovic.balsa.intrahub.data.StandingRow
import com.bazovic.balsa.intrahub.data.repository.AuthRepository
import com.bazovic.balsa.intrahub.data.repository.StandingsRepository
import com.bazovic.balsa.intrahub.data.repository.TeamsRepository
import kotlinx.coroutines.launch

class StandingsViewModel(
    private val standingsRepo: StandingsRepository = StandingsRepository(),
    private val teamsRepo: TeamsRepository = TeamsRepository(),
    private val authRepo: AuthRepository = AuthRepository(),
) : ViewModel() {

    // ─── SECTION: Ui Filter State ─── //

    var selectedSport: Sport by mutableStateOf(Sport.Basketball)

    // ─── SECTION: Data State ─── //

    var standings: Map<Sport, List<StandingRow>> by mutableStateOf(emptyMap())
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
                val myTeamIds = if (userId.isNotEmpty()) {
                    teamsRepo.getMyTeamIds(userId)
                } else emptySet()
                standings = standingsRepo.getStandings(myTeamIds)
            } catch (e: Exception) {
                errorMessage = "Could not load standings. Check your connection."
            } finally {
                isLoading = false
            }
        }
    }
}//StandingsViewModel
