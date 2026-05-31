package com.bazovic.balsa.intrahub.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bazovic.balsa.intrahub.data.Game
import com.bazovic.balsa.intrahub.data.GameResult
import com.bazovic.balsa.intrahub.data.GameStatus
import com.bazovic.balsa.intrahub.data.Player
import com.bazovic.balsa.intrahub.data.Team
import com.bazovic.balsa.intrahub.data.repository.AuthRepository
import com.bazovic.balsa.intrahub.data.repository.GamesRepository
import com.bazovic.balsa.intrahub.data.repository.TeamsRepository
import kotlinx.coroutines.launch

class TeamDetailViewModel(
    private val teamsRepo: TeamsRepository = TeamsRepository(),
    private val gamesRepo: GamesRepository = GamesRepository(),
    private val authRepo: AuthRepository = AuthRepository(),
) : ViewModel() {

    // ─── SECTION: Data State ─── //

    var team: Team? by mutableStateOf(null)
        private set

    var roster: List<Player> by mutableStateOf(emptyList())
        private set

    var teamGames: List<Game> by mutableStateOf(emptyList())
        private set

    var form: List<String> by mutableStateOf(emptyList())
        private set

    var isCaptain: Boolean by mutableStateOf(false)
        private set

    var isLoading: Boolean by mutableStateOf(false)
        private set

    var errorMessage: String? by mutableStateOf(null)
        private set

    private var loadedTeamId: String? = null

    // ─── SECTION: Actions ─── //

    fun load(teamId: String) {
        if (loadedTeamId == teamId) return
        loadedTeamId = teamId
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val userId = authRepo.getCurrentUserId() ?: ""
                val myTeamIds = if (userId.isNotEmpty()) {
                    teamsRepo.getMyTeamIds(userId)
                } else emptySet()

                val loadedTeam = teamsRepo.getTeam(teamId)
                team = loadedTeam

                if (loadedTeam != null) {
                    isCaptain = userId.isNotEmpty() && loadedTeam.captainId == userId

                    roster = teamsRepo.getRoster(loadedTeam.id, loadedTeam.captainId)

                    val games = gamesRepo.getTeamGames(teamId, myTeamIds)
                    teamGames = games.sortedBy { it.whenMs }

                    form = games
                        .filter { it.status == GameStatus.Final }
                        .sortedByDescending { it.whenMs }
                        .take(5)
                        .map { if (it.result == GameResult.Win) "W" else "L" }
                        .reversed()
                }
            } catch (e: Exception) {
                errorMessage = "Could not load team details."
            } finally {
                isLoading = false
            }
        }
    }
}//TeamDetailViewModel
