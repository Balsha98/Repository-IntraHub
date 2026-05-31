package com.bazovic.balsa.intrahub.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bazovic.balsa.intrahub.data.Game
import com.bazovic.balsa.intrahub.data.GameStatus
import com.bazovic.balsa.intrahub.data.Player
import com.bazovic.balsa.intrahub.data.Team
import com.bazovic.balsa.intrahub.data.repository.AuthRepository
import com.bazovic.balsa.intrahub.data.repository.GamesRepository
import com.bazovic.balsa.intrahub.data.repository.RsvpRepository
import com.bazovic.balsa.intrahub.data.repository.TeamsRepository
import kotlinx.coroutines.launch

class GameDetailViewModel(
    private val gamesRepo: GamesRepository = GamesRepository(),
    private val teamsRepo: TeamsRepository = TeamsRepository(),
    private val rsvpRepo: RsvpRepository = RsvpRepository(),
    private val authRepo: AuthRepository = AuthRepository(),
) : ViewModel() {

    // ─── SECTION: Data State ─── //

    var game: Game? by mutableStateOf(null)
        private set

    var myTeam: Team? by mutableStateOf(null)
        private set

    var roster: List<Player> by mutableStateOf(emptyList())
        private set

    var rsvp: String by mutableStateOf("yes")
        private set

    var isLoading: Boolean by mutableStateOf(false)
        private set

    var errorMessage: String? by mutableStateOf(null)
        private set

    private var loadedGameId: String? = null

    // ─── SECTION: Actions ─── //

    fun load(gameId: String) {
        if (loadedGameId == gameId) return
        loadedGameId = gameId
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val userId = authRepo.getCurrentUserId() ?: ""
                val myTeamIds = if (userId.isNotEmpty()) {
                    teamsRepo.getMyTeamIds(userId)
                } else emptySet()

                val loadedGame = gamesRepo.getGame(gameId, myTeamIds)
                game = loadedGame

                val teamId = loadedGame?.myTeamId
                if (teamId != null) {
                    val team = teamsRepo.getTeam(teamId)
                    myTeam = team

                    if (loadedGame.status == GameStatus.Upcoming) {
                        roster = team?.let {
                            teamsRepo.getRoster(it.id, it.captainId)
                        } ?: emptyList()

                        if (userId.isNotEmpty()) {
                            rsvp = rsvpRepo.getRsvp(gameId, userId) ?: "yes"
                        }
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Could not load game details."
            } finally {
                isLoading = false
            }
        }
    }

    fun updateRsvp(response: String) {
        rsvp = response // optimistic update
        viewModelScope.launch {
            try {
                val userId = authRepo.getCurrentUserId() ?: return@launch
                val gameId = game?.id ?: return@launch
                rsvpRepo.upsertRsvp(gameId, userId, response)
            } catch (_: Exception) {
                // revert on failure is omitted for brevity
            }
        }
    }
}//GameDetailViewModel
