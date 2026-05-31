package com.bazovic.balsa.intrahub.data.repository

import com.bazovic.balsa.intrahub.data.Sport
import com.bazovic.balsa.intrahub.data.StandingRow
import com.bazovic.balsa.intrahub.data.remote.StandingRowDto
import com.bazovic.balsa.intrahub.data.remote.toDomain
import com.bazovic.balsa.intrahub.data.remote.toSport
import com.bazovic.balsa.intrahub.data.supabase
import io.github.jan.supabase.postgrest.from

class StandingsRepository {

    // ─── SECTION: Queries ─── //
    suspend fun getStandings(
        myTeamIds: Set<String>,
        seasonId: String = "spring-2026",
    ): Map<Sport, List<StandingRow>> {
        val rows = supabase
            .from("team_standings")
            .select()
            .decodeList<StandingRowDto>()

        return rows
            .groupBy { it.sportId.toSport() }
            .mapValues { (_, sportRows) ->
                sportRows
                    .sortedBy {
                        it.rank
                    }
                    .map { dto ->
                        dto.toDomain().copy(isMine = dto.teamId in myTeamIds)
                    }
            }
    }//getStandings
}//StandingsRepository
