package com.bazovic.balsa.intrahub.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bazovic.balsa.intrahub.data.Game
import com.bazovic.balsa.intrahub.data.Sport
import com.bazovic.balsa.intrahub.data.repository.AuthRepository
import com.bazovic.balsa.intrahub.data.repository.GamesRepository
import com.bazovic.balsa.intrahub.data.repository.TeamsRepository
import kotlinx.coroutines.launch

enum class ScheduleFilter { All, Mine, Upcoming, Past }

class ScheduleViewModel(
    private val gamesRepo: GamesRepository = GamesRepository(),
    private val teamsRepo: TeamsRepository = TeamsRepository(),
    private val authRepo: AuthRepository = AuthRepository(),
) : ViewModel() {

    // ─── SECTION: Ui Filter State ─── //

    var filter: ScheduleFilter by mutableStateOf(ScheduleFilter.All)
    var sportFilter: Sport? by mutableStateOf(null)
    var search: String by mutableStateOf("")
    var showSportPanel: Boolean by mutableStateOf(false)

    // ─── SECTION: Data State ─── //

    var allGames: List<Game> by mutableStateOf(emptyList())
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
                allGames = gamesRepo.getGames(myTeamIds)
            } catch (e: Exception) {
                errorMessage = "Could not load schedule. Check your connection."
            } finally {
                isLoading = false
            }
        }
    }
}//ScheduleViewModel
