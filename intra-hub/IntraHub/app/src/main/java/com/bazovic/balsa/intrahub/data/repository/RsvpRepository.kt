package com.bazovic.balsa.intrahub.data.repository

import com.bazovic.balsa.intrahub.data.remote.RsvpDto
import com.bazovic.balsa.intrahub.data.supabase
import io.github.jan.supabase.postgrest.from

class RsvpRepository {

    // ─── SECTION: Queries ─── //
    suspend fun getRsvp(gameId: String, userId: String): String? =
        supabase
            .from("rsvp")
            .select {
                filter {
                    eq("game_id", gameId);
                    eq("user_id", userId)
                }
            }
            .decodeSingleOrNull<RsvpDto>()
            ?.response

    suspend fun upsertRsvp(gameId: String, userId: String, response: String) {
        supabase
            .from("rsvp")
            .upsert(RsvpDto(gameId = gameId, userId = userId, response = response)) {
                onConflict = "game_id,user_id"
            }
    }//upsertRsvp
}//RsvpRepository
