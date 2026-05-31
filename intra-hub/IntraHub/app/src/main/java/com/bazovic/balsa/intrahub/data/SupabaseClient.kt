package com.bazovic.balsa.intrahub.data

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.engine.okhttp.OkHttp

// ─── SECTION: Credentials ─── //
const val SUPABASE_URL = "https://bnzvmvzdhrunqivgsdsj.supabase.co"
const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJuenZtdnpkaHJ1bnFpdmdzZHNqIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzczNTAzMDYsImV4cCI6MjA5MjkyNjMwNn0.RVWrkCWQNMC9gTTU900Z8tJxjILosqQArpfEJeW1QPc"

// ─── SECTION: Client Singleton ─── //
val supabase = createSupabaseClient(
    supabaseUrl = SUPABASE_URL,
    supabaseKey = SUPABASE_KEY,
) {
    httpEngine = OkHttp.create()
    install(Auth)
    install(Postgrest)
}//createSupabaseClient
