package com.bazovic.balsa.intrahub.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bazovic.balsa.intrahub.data.Announcement
import com.bazovic.balsa.intrahub.data.Game
import com.bazovic.balsa.intrahub.data.GameResult
import com.bazovic.balsa.intrahub.data.GameStatus
import com.bazovic.balsa.intrahub.data.Team
import com.bazovic.balsa.intrahub.data.repository.AnnouncementsRepository
import com.bazovic.balsa.intrahub.data.repository.AuthRepository
import com.bazovic.balsa.intrahub.data.repository.GamesRepository
import com.bazovic.balsa.intrahub.data.repository.TeamsRepository
import kotlinx.coroutines.launch

class HomeViewModel(
    private val gamesRepo: GamesRepository = GamesRepository(),
    private val teamsRepo: TeamsRepository = TeamsRepository(),
    private val authRepo: AuthRepository = AuthRepository(),
    private val announcementsRepo: AnnouncementsRepository = AnnouncementsRepository(),
) : ViewModel() {

    // ─── SECTION: Data State ─── //

    var myTeams: List<Team> by mutableStateOf(emptyList())
        private set

    var upcomingGames: List<Game> by mutableStateOf(emptyList())
        private set

    var recentGames: List<Game> by mutableStateOf(emptyList())
        private set

    var announcements: List<Announcement> by mutableStateOf(emptyList())
        private set

    var streakText: String by mutableStateOf("—")
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

                val allGames = gamesRepo.getGames(myTeamIds)
                val allTeams = teamsRepo.getTeams()

                myTeams = allTeams.filter { it.id in myTeamIds }

                upcomingGames = allGames
                    .filter { it.status == GameStatus.Upcoming && it.myTeamId != null }
                    .sortedBy { it.whenMs }
                    .take(3)

                recentGames = allGames
                    .filter { it.status == GameStatus.Final && it.myTeamId != null }
                    .sortedByDescending { it.whenMs }
                    .take(2)

                streakText = computeStreak(allGames.filter {
                    it.status == GameStatus.Final && it.myTeamId != null
                }.sortedByDescending { it.whenMs })

                announcements = announcementsRepo.getAnnouncements()
            } catch (e: Exception) {
                errorMessage = "Could not load home data. Check your connection."
            } finally {
                isLoading = false
            }
        }
    }

    // ─── SECTION: Helpers ─── //

    private fun computeStreak(sortedFinals: List<Game>): String {
        if (sortedFinals.isEmpty()) return "—"
        val ref = sortedFinals.first().result
        var count = 0
        for (g in sortedFinals) {
            if (g.result == ref) count++ else break
        }
        return if (ref == GameResult.Win) "$count win${if (count == 1) "" else "s"}"
        else "$count loss${if (count == 1) "" else "es"}"
    }
}//HomeViewModel
