package com.bazovic.balsa.intrahub.data.repository

import com.bazovic.balsa.intrahub.data.UserProfile
import com.bazovic.balsa.intrahub.data.remote.ProfileDto
import com.bazovic.balsa.intrahub.data.remote.toDomain
import com.bazovic.balsa.intrahub.data.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from

class AuthRepository {

    // ─── SECTION: Auth ─── //
    suspend fun signIn(usernameOrEmail: String, password: String) {
        val email = if (usernameOrEmail.contains("@")) {
            usernameOrEmail.trim().lowercase()
        } else {
            "${usernameOrEmail.trim().lowercase()}@rit.edu"
        }

        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }//signIn

    suspend fun getCurrentProfile(): UserProfile? {
        val uid = supabase.auth.currentSessionOrNull()?.user?.id ?: return null

        return supabase
            .from("profiles")
            .select {
                filter {
                    eq("id", uid)
                }
            }
            .decodeSingleOrNull<ProfileDto>()
            ?.toDomain()
    }//getCurrentProfile

    fun getCurrentUserId(): String? = supabase.auth.currentSessionOrNull()?.user?.id

    suspend fun signOut() = supabase.auth.signOut()
}//AuthRepository
