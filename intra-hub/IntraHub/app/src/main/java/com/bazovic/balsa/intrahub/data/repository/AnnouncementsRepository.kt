package com.bazovic.balsa.intrahub.data.repository

import com.bazovic.balsa.intrahub.data.Announcement
import com.bazovic.balsa.intrahub.data.remote.AnnouncementDto
import com.bazovic.balsa.intrahub.data.remote.toEpochMs
import com.bazovic.balsa.intrahub.data.supabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class AnnouncementsRepository {

    // ─── SECTION: Queries ─── //
    suspend fun getAnnouncements(): List<Announcement> {
        return supabase
            .from("announcements")
            .select {
                order(
                    "created_at",
                    order = Order.DESCENDING
                )
            }
            .decodeList<AnnouncementDto>()
            .map { dto ->
                Announcement(
                    id = dto.id,
                    title = dto.title,
                    body = dto.body,
                    timeAgo = dto.createdAt.toRelativeTimeAgo(),
                )
            }
    }//getAnnouncements
}//AnnouncementsRepository

// ─── SECTION: Helpers ─── //
private fun String.toRelativeTimeAgo(): String {
    val epochMs = toEpochMs()

    if (epochMs == 0L) return ""

    val diffMs = System.currentTimeMillis() - epochMs
    val minutes = diffMs / 60_000
    val hours = minutes / 60
    val days = hours / 24

    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 7 -> "${days}d ago"
        else -> "${days / 7}w ago"
    }
}//String.toRelativeTimeAgo()
