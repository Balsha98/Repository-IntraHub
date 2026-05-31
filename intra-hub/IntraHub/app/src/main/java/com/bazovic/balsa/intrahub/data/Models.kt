package com.bazovic.balsa.intrahub.data

// ─── SECTION: Domain Enums ─── //
enum class UserRole { Student, Captain, Admin }

enum class Sport(val displayName: String, val short: String, val emoji: String) {
    Basketball("Basketball", "BBALL", "🏀"),
    Soccer("Soccer", "SOC", "⚽"),
    Volleyball("Volleyball", "VBALL", "🏐"),
    Dodgeball("Dodgeball", "DODGE", "🎯"),
}

enum class GameStatus { Upcoming, Final }

enum class GameResult { Win, Loss, Tie, None }

// ─── SECTION: Data Classes ─── //
data class UserProfile(
    val id: String = "",
    val ritId: String,
    val name: String,
    val firstName: String,
    val role: UserRole,
    val major: String,
    val year: String,
    val email: String,
)//UserProfile

data class Team(
    val id: String,
    val name: String,
    val sport: Sport,
    val record: String, // "W-L"
    val rank: Int,
    val league: String,
    val captainId: String,
)//Team

val Team.wins: Int get() = record.split("-")[0].toInt()
val Team.losses: Int get() = record.split("-")[1].toInt()

data class Player(
    val id: String,
    val name: String,
    val number: Int,
    val role: String,
    val ppg: Double,
)//Player

data class Game(
    val id: String,
    val sport: Sport,
    val myTeamId: String?,
    val oppName: String,
    val oppShort: String,
    val whenMs: Long,
    val venue: String,
    val status: GameStatus,
    val myScore: Int = 0,
    val oppScore: Int = 0,
    val result: GameResult = GameResult.None,
    val isSpectator: Boolean = false,
    val referee: String? = null,
)//Game

data class StandingRow(
    val teamId: String,
    val rank: Int,
    val teamName: String,
    val wins: Int,
    val losses: Int,
    val pointsFor: Int,
    val pointsAgainst: Int,
    val isMine: Boolean = false,
) {
    val diff: Int get() = pointsFor - pointsAgainst
}//StandingRow

data class Announcement(
    val id: String,
    val title: String,
    val body: String,
    val timeAgo: String,
)//Announcement
