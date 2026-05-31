package com.bazovic.balsa.intrahub.data.repository

import com.bazovic.balsa.intrahub.data.Player
import com.bazovic.balsa.intrahub.data.Team
import com.bazovic.balsa.intrahub.data.remote.PlayerStatDto
import com.bazovic.balsa.intrahub.data.remote.ProfileDto
import com.bazovic.balsa.intrahub.data.remote.StandingRowDto
import com.bazovic.balsa.intrahub.data.remote.TeamDto
import com.bazovic.balsa.intrahub.data.remote.TeamMemberDto
import com.bazovic.balsa.intrahub.data.remote.TeamRecordDto
import com.bazovic.balsa.intrahub.data.remote.toDomain
import com.bazovic.balsa.intrahub.data.supabase
import io.github.jan.supabase.postgrest.from

class TeamsRepository {

    // ─── SECTION: Queries ─── //
    suspend fun getTeams(seasonId: String = "spring-2026"): List<Team> {
        val teamDtos = supabase
            .from("teams")
            .select()
            .decodeList<TeamDto>()

        val records = supabase
            .from("team_record")
            .select {
                filter {
                    eq("season_id", seasonId)
                }
            }
            .decodeList<TeamRecordDto>()

        val standings = supabase
            .from("team_standings")
            .select {
                filter {
                    eq("season_id", seasonId)
                }
            }
            .decodeList<StandingRowDto>()

        val recordMap = records.associateBy { it.teamId }
        val rankMap = standings.associateBy { it.teamId }

        return teamDtos.map { dto ->
            dto.toDomain(
                record = recordMap[dto.id]?.record ?: "0-0",
                rank = rankMap[dto.id]?.rank?.toInt() ?: 0,
            )
        }//map
    }//getTeams

    suspend fun getMyTeamIds(
        userId: String,
        seasonId: String = "spring-2026",
    ): Set<String> {
        return supabase
            .from("team_members")
            .select {
                filter {
                    eq("user_id", userId)
                    eq("season_id", seasonId)
                }
            }
            .decodeList<TeamMemberDto>()
            .map { it.teamId }
            .toSet()
    }//getMyTeamIds

    suspend fun getTeam(teamId: String, seasonId: String = "spring-2026"): Team? {
        val dto = supabase
            .from("teams")
            .select {
                filter {
                    eq("id", teamId)
                }
            }
            .decodeSingleOrNull<TeamDto>() ?: return null

        val record = supabase
            .from("team_record")
            .select {
                filter {
                    eq("team_id", teamId);
                    eq("season_id", seasonId)
                }
            }
            .decodeSingleOrNull<TeamRecordDto>()

        val ranking = supabase
            .from("team_standings")
            .select {
                filter {
                    eq("team_id", teamId);
                    eq("season_id", seasonId)
                }
            }
            .decodeSingleOrNull<StandingRowDto>()

        return dto.toDomain(
            record = record?.record ?: "0-0",
            rank = ranking?.rank?.toInt() ?: 0,
        )
    }//getTeam

    suspend fun getRoster(
        teamId: String,
        captainId: String,
        seasonId: String = "spring-2026",
    ): List<Player> {
        // Step 1 — team_members for this team + season
        val members = supabase
            .from("team_members")
            .select {
                filter {
                    eq("team_id", teamId)
                    eq("season_id", seasonId)
                }
            }
            .decodeList<TeamMemberDto>()

        if (members.isEmpty()) return emptyList()

        val memberIds = members.map { it.id }.toSet()
        val userIds = members.map { it.userId }.toSet()

        // Step 2 — profiles for those users
        val profileMap = supabase
            .from("profiles")
            .select()
            .decodeList<ProfileDto>()
            .filter { it.id in userIds }
            .associateBy { it.id }

        // Step 3 — player_stats for those members
        val statMap = supabase
            .from("player_stats")
            .select {
                filter {
                    eq("season_id", seasonId)
                }
            }
            .decodeList<PlayerStatDto>()
            .filter { it.teamMemberId in memberIds }
            .associateBy { it.teamMemberId }

        // Step 4 — assemble Player domain objects
        return members.mapNotNull { member ->
            val profile = profileMap[member.userId] ?: return@mapNotNull null
            val stat = statMap[member.id]

            Player(
                id = member.id,
                name = profile.name,
                number = member.jerseyNumber ?: 0,
                role = if (member.userId == captainId) "Captain" else member.position ?: "Player",
                ppg = stat?.ppg ?: 0.0,
            )
        }.sortedByDescending { it.ppg }
    }//getRoster

    suspend fun getAggregatedStats(
        userId: String,
        seasonId: String = "spring-2026",
    ): Triple<Int, Int, Int> {
        val memberIds = supabase
            .from("team_members")
            .select {
                filter {
                    eq("user_id", userId);
                    eq("season_id", seasonId)
                }
            }
            .decodeList<TeamMemberDto>()
            .map { it.id }
            .toSet()

        if (memberIds.isEmpty()) return Triple(0, 0, 0)

        val stats = supabase
            .from("player_stats")
            .select {
                filter {
                    eq("season_id", seasonId)
                }
            }
            .decodeList<PlayerStatDto>()
            .filter { it.teamMemberId in memberIds }

        return Triple(
            stats.sumOf { it.gamesPlayed ?: 0 },
            stats.sumOf { it.wins ?: 0 },
            0
        )//Triple
    }//getAggregatedStats
}//TeamsRepository
