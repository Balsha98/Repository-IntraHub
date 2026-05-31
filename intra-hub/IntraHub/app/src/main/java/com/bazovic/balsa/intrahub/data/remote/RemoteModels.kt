package com.bazovic.balsa.intrahub.data.remote

import com.bazovic.balsa.intrahub.data.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.*

// ─── SECTION: DTOs ─── //
@Serializable
data class ProfileDto(
    val id: String,
    @SerialName("rit_id") val ritId: String,
    val name: String,
    @SerialName("first_name") val firstName: String,
    val role: String,
    val major: String? = null,
    val year: String? = null,
)//ProfileDto

@Serializable
data class TeamDto(
    val id: String,
    val name: String,
    @SerialName("sport_id") val sportId: String,
    val league: String,
    @SerialName("captain_id") val captainId: String,
)//TeamDto

@Serializable
data class TeamMemberDto(
    val id: String,
    @SerialName("team_id")
    val teamId: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("jersey_number") val jerseyNumber: Int? = null,
    val position: String? = null,
)//TeamMemberDto

@Serializable
data class TeamRecordDto(
    @SerialName("team_id")
    val teamId: String,
    @SerialName("season_id")
    val seasonId: String,
    val record: String,
)//TeamRecordDto

@Serializable
data class StandingRowDto(
    @SerialName("team_id")
    val teamId: String,
    @SerialName("team_name")
    val teamName: String,
    @SerialName("sport_id")
    val sportId: String,
    val league: String,
    @SerialName("season_id")
    val seasonId: String? = null,
    val wins: Int,
    val losses: Int,
    @SerialName("points_for")
    val pointsFor: Int,
    @SerialName("points_against")
    val pointsAgainst: Int,
    val diff: Int,
    val rank: Long,
)//StandingRowDto

@Serializable
data class GameDto(
    val id: String,
    @SerialName("sport_id")
    val sportId: String,
    @SerialName("season_id")
    val seasonId: String,
    @SerialName("home_team_id")
    val homeTeamId: String? = null,
    @SerialName("away_team_id")
    val awayTeamId: String? = null,
    @SerialName("home_name")
    val homeName: String? = null,
    @SerialName("away_name")
    val awayName: String? = null,
    val venue: String,
    @SerialName("scheduled_at")
    val scheduledAt: String,
    val status: String,
    @SerialName("home_score")
    val homeScore: Int? = null,
    @SerialName("away_score")
    val awayScore: Int? = null,
    val referee: String? = null,
)//GameDto

@Serializable
data class AnnouncementDto(
    val id: String,
    val title: String,
    val body: String,
    @SerialName("created_at")
    val createdAt: String,
)//AnnouncementDto

@Serializable
data class PlayerStatDto(
    val id: String? = null,
    @SerialName("team_member_id")
    val teamMemberId: String,
    @SerialName("season_id")
    val seasonId: String? = null,
    val ppg: Double? = null,
    @SerialName("games_played")
    val gamesPlayed: Int? = null,
    val wins: Int? = null,
)//PlayerStatDto

@Serializable
data class TeamMemberWithProfileDto(
    val id: String,
    @SerialName("team_id")
    val teamId: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("jersey_number")
    val jerseyNumber: Int? = null,
    val position: String? = null,
    @SerialName("season_id")
    val seasonId: String? = null,
    val profiles: ProfileDto,
    @SerialName("player_stats")
    val playerStats: List<PlayerStatDto> = emptyList(),
)//TeamMemberWithProfileDto

@Serializable
data class RsvpDto(
    @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.NEVER)
    val id: String? = null,
    @SerialName("game_id")
    val gameId: String,
    @SerialName("user_id")
    val userId: String,
    val response: String,
)//RsvpDto

// ─── SECTION: Mappers ─── //
fun ProfileDto.toDomain() = UserProfile(
    id = id,
    ritId = ritId,
    name = name,
    firstName = firstName,
    role = role.toUserRole(),
    major = major ?: "",
    year = year ?: "",
    email = "$ritId@rit.edu",
)//ProfileDto.toDomain

fun TeamDto.toDomain(record: String = "0-0", rank: Int = 0) = Team(
    id = id,
    name = name,
    sport = sportId.toSport(),
    record = record,
    rank = rank,
    league = league,
    captainId = captainId,
)//TeamDto.toDomain

fun StandingRowDto.toDomain() = StandingRow(
    teamId = teamId,
    rank = rank.toInt(),
    teamName = teamName,
    wins = wins,
    losses = losses,
    pointsFor = pointsFor,
    pointsAgainst = pointsAgainst,
    isMine = false,
)//StandingRowDto.toDomain

fun GameDto.toDomain(myTeamIds: Set<String>, teamNames: Map<String, String>): Game {
    val isHome = homeTeamId != null && homeTeamId in myTeamIds
    val isAway = awayTeamId != null && awayTeamId in myTeamIds

    val myTeamId = when {
        isHome -> homeTeamId
        isAway -> awayTeamId
        else -> null
    }

    val myScore = if (isHome) homeScore ?: 0 else awayScore ?: 0
    val oppScore = if (isHome) awayScore ?: 0 else homeScore ?: 0

    val oppName = when {
        isHome -> teamNames[awayTeamId] ?: awayName ?: "Opponent"
        isAway -> teamNames[homeTeamId] ?: homeName ?: "Opponent"
        else -> buildString {
            append(teamNames[homeTeamId] ?: homeName ?: "Home")
            append(" vs ")
            append(teamNames[awayTeamId] ?: awayName ?: "Away")
        }
    }

    val gameStatus = if (status == "final") GameStatus.Final else GameStatus.Upcoming

    val result = when {
        gameStatus == GameStatus.Upcoming -> GameResult.None
        myTeamId == null -> GameResult.None
        myScore > oppScore -> GameResult.Win
        myScore < oppScore -> GameResult.Loss
        else -> GameResult.Tie
    }

    return Game(
        id = id,
        sport = sportId.toSport(),
        myTeamId = myTeamId,
        oppName = oppName,
        oppShort = oppName.take(3).uppercase(),
        whenMs = scheduledAt.toEpochMs(),
        venue = venue,
        status = gameStatus,
        myScore = myScore,
        oppScore = oppScore,
        result = result,
        isSpectator = myTeamId == null,
        referee = referee,
    )//Game
}//GameDto.toDomain

fun TeamMemberWithProfileDto.toPlayer(captainId: String): Player {
    val stat = playerStats.firstOrNull()

    return Player(
        id = id,
        name = profiles.name,
        number = jerseyNumber ?: 0,
        role = if (userId == captainId) "Captain" else position ?: "Player",
        ppg = stat?.ppg ?: 0.0,
    )//Player
}//TeamMemberWithProfileDto

// ─── SECTION: Helpers ─── //
fun String.toSport(): Sport = when (this) {
    "basketball" -> Sport.Basketball
    "soccer" -> Sport.Soccer
    "volleyball" -> Sport.Volleyball
    "dodgeball" -> Sport.Dodgeball
    else -> Sport.Basketball
}//String.toSport()

fun String.toUserRole(): UserRole = when (this) {
    "captain" -> UserRole.Captain
    "admin" -> UserRole.Admin
    else -> UserRole.Student
}//String.toUserRole()

fun String.toEpochMs(): Long = try {
    val normalized = this.replace(Regex("\\.\\d+"), "").replace(Regex("([+-])(\\d{2}):(\\d{2})$"), "$1$2$3")

    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US).parse(normalized)?.time ?: 0L
} catch (e: Exception) { 0L }
