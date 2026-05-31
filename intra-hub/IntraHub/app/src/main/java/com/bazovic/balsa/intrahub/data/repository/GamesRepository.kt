package com.bazovic.balsa.intrahub.data.repository

import com.bazovic.balsa.intrahub.data.Game
import com.bazovic.balsa.intrahub.data.remote.GameDto
import com.bazovic.balsa.intrahub.data.remote.TeamDto
import com.bazovic.balsa.intrahub.data.remote.toDomain
import com.bazovic.balsa.intrahub.data.supabase
import io.github.jan.supabase.postgrest.from

class GamesRepository {

    // ─── SECTION: Queries ─── //
    suspend fun getGames(
        myTeamIds: Set<String>,
        seasonId: String = "spring-2026",
    ): List<Game> {
        val gameDtos = supabase
            .from("games")
            .select {
                filter {
                    eq("season_id", seasonId)
                }
            }
            .decodeList<GameDto>()

        val referencedTeamIds = gameDtos
            .flatMap {
                listOfNotNull(it.homeTeamId, it.awayTeamId)
            }
            .toSet()

        val teamNames: Map<String, String> = if (referencedTeamIds.isEmpty()) {
            emptyMap()
        } else {
            supabase
                .from("teams")
                .select()
                .decodeList<TeamDto>()
                .associate { it.id to it.name }
        }

        return gameDtos.map {
            it.toDomain(myTeamIds, teamNames)
        }
    }//getGames

    suspend fun getGame(gameId: String, myTeamIds: Set<String>): Game? {
        val dto = supabase
            .from("games")
            .select {
                filter {
                    eq("id", gameId)
                }
            }
            .decodeSingleOrNull<GameDto>() ?: return null

        val teamNames: Map<String, String> = supabase
            .from("teams")
            .select()
            .decodeList<TeamDto>()
            .associate { it.id to it.name }

        return dto.toDomain(myTeamIds, teamNames)
    }//getGame

    suspend fun getTeamGames(
        teamId: String,
        myTeamIds: Set<String>,
        seasonId: String = "spring-2026",
    ): List<Game> {
        val teamNames: Map<String, String> = supabase
            .from("teams")
            .select()
            .decodeList<TeamDto>()
            .associate { it.id to it.name }

        return supabase
            .from("games")
            .select {
                filter {
                    eq("season_id", seasonId)
                }
            }
            .decodeList<GameDto>()
            .filter { it.homeTeamId == teamId || it.awayTeamId == teamId }
            .map { it.toDomain(myTeamIds, teamNames) }
    }//getTeamGames
}//GamesRepository
