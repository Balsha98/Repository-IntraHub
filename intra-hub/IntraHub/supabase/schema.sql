-- =============================================================================
-- IntraHub — Supabase Schema + Seed
-- Paste the entire file into Dashboard → SQL Editor → Run
-- Safe to re-run: uses IF NOT EXISTS / ON CONFLICT DO NOTHING throughout.
--
-- Test accounts (all passwords: Password123!)
--   tjr1234@rit.edu  →  Tyler Reed   (student)
--   mlc8821@rit.edu  →  Marcus Chen  (captain)
--   arp4502@rit.edu  →  Alex Park    (admin)
-- =============================================================================


-- =============================================================================
-- 0. EXTENSIONS
-- =============================================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA extensions;


-- =============================================================================
-- 1. LOOKUP TABLES  (no FK dependencies)
-- =============================================================================

-- ── sports ────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS public.sports (
    id           TEXT        PRIMARY KEY,            -- 'basketball', 'soccer', …
    display_name TEXT        NOT NULL,
    short        TEXT        NOT NULL,               -- 'BBALL', 'SOC', …
    emoji        TEXT        NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ── seasons ───────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS public.seasons (
    id             TEXT        PRIMARY KEY,          -- 'spring-2026'
    name           TEXT        NOT NULL,             -- 'Spring 2026'
    start_date     DATE        NOT NULL,
    end_date       DATE        NOT NULL,
    playoff_start  DATE,
    is_active      BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);


-- =============================================================================
-- 2. USER PROFILES  (1-to-1 extension of Supabase auth.users)
-- =============================================================================

CREATE TABLE IF NOT EXISTS public.profiles (
    id          UUID        PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    rit_id      TEXT        NOT NULL UNIQUE,
    name        TEXT        NOT NULL,
    first_name  TEXT        NOT NULL,
    role        TEXT        NOT NULL DEFAULT 'student'
                                CHECK (role IN ('student', 'captain', 'admin')),
    major       TEXT,
    year        TEXT,                                -- 'Junior', 'Staff', …
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);


-- =============================================================================
-- 3. TEAMS
-- =============================================================================

CREATE TABLE IF NOT EXISTS public.teams (
    id          TEXT        PRIMARY KEY,
    name        TEXT        NOT NULL,
    sport_id    TEXT        NOT NULL REFERENCES public.sports(id),
    league      TEXT        NOT NULL,               -- 'Mens A', 'Coed B', 'Open'
    captain_id  UUID        NOT NULL REFERENCES public.profiles(id),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
    -- NOTE: rank and record are NOT stored here.
    --       Read them from the team_standings / team_record views instead.
);


-- =============================================================================
-- 4. ROSTER  (team_members — many-to-many, profiles ↔ teams, per season)
-- =============================================================================

CREATE TABLE IF NOT EXISTS public.team_members (
    id             UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    team_id        TEXT        NOT NULL REFERENCES public.teams(id)    ON DELETE CASCADE,
    user_id        UUID        NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    season_id      TEXT        NOT NULL REFERENCES public.seasons(id),
    jersey_number  INT,
    position       TEXT,                            -- 'Guard', 'Forward', 'Center'
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (team_id, user_id, season_id)
);


-- =============================================================================
-- 5. PLAYER STATS  (per team_member, per season)
-- =============================================================================

CREATE TABLE IF NOT EXISTS public.player_stats (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    team_member_id  UUID        NOT NULL REFERENCES public.team_members(id) ON DELETE CASCADE,
    season_id       TEXT        NOT NULL REFERENCES public.seasons(id),
    games_played    INT         NOT NULL DEFAULT 0,
    ppg             FLOAT       NOT NULL DEFAULT 0,
    mvp_count       INT         NOT NULL DEFAULT 0,
    attendance_pct  FLOAT       NOT NULL DEFAULT 0   -- 0.0 – 1.0
                                    CHECK (attendance_pct BETWEEN 0 AND 1),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (team_member_id, season_id)
);


-- =============================================================================
-- 6. GAMES
-- =============================================================================

CREATE TABLE IF NOT EXISTS public.games (
    id             TEXT        PRIMARY KEY,
    sport_id       TEXT        NOT NULL REFERENCES public.sports(id),
    season_id      TEXT        NOT NULL REFERENCES public.seasons(id),
    -- Either team FK may be NULL when the opponent is not in the database
    -- (external teams, spectator match-ups, etc.). Use *_name as fallback.
    home_team_id   TEXT        REFERENCES public.teams(id),
    away_team_id   TEXT        REFERENCES public.teams(id),
    home_name      TEXT,                            -- fallback display name
    away_name      TEXT,                            -- fallback display name
    venue          TEXT        NOT NULL,
    scheduled_at   TIMESTAMPTZ NOT NULL,
    status         TEXT        NOT NULL DEFAULT 'upcoming'
                                   CHECK (status IN ('upcoming', 'final')),
    home_score     INT,                             -- NULL until final
    away_score     INT,                             -- NULL until final
    referee        TEXT,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);


-- =============================================================================
-- 7. RSVP
-- =============================================================================

CREATE TABLE IF NOT EXISTS public.rsvp (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    game_id     TEXT        NOT NULL REFERENCES public.games(id)    ON DELETE CASCADE,
    user_id     UUID        NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    response    TEXT        NOT NULL CHECK (response IN ('yes', 'maybe', 'no')),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (game_id, user_id)                       -- safe to UPSERT on this pair
);


-- =============================================================================
-- 8. ANNOUNCEMENTS
-- =============================================================================

CREATE TABLE IF NOT EXISTS public.announcements (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    title       TEXT        NOT NULL,
    body        TEXT        NOT NULL,
    author_id   UUID        NOT NULL REFERENCES public.profiles(id),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);


-- =============================================================================
-- 9. INDEXES
-- =============================================================================

CREATE INDEX IF NOT EXISTS idx_team_members_team    ON public.team_members(team_id);
CREATE INDEX IF NOT EXISTS idx_team_members_user    ON public.team_members(user_id);
CREATE INDEX IF NOT EXISTS idx_team_members_season  ON public.team_members(season_id);
CREATE INDEX IF NOT EXISTS idx_player_stats_member  ON public.player_stats(team_member_id);
CREATE INDEX IF NOT EXISTS idx_games_sport          ON public.games(sport_id);
CREATE INDEX IF NOT EXISTS idx_games_season         ON public.games(season_id);
CREATE INDEX IF NOT EXISTS idx_games_scheduled      ON public.games(scheduled_at);
CREATE INDEX IF NOT EXISTS idx_games_home_team      ON public.games(home_team_id);
CREATE INDEX IF NOT EXISTS idx_games_away_team      ON public.games(away_team_id);
CREATE INDEX IF NOT EXISTS idx_rsvp_game            ON public.rsvp(game_id);
CREATE INDEX IF NOT EXISTS idx_rsvp_user            ON public.rsvp(user_id);
CREATE INDEX IF NOT EXISTS idx_announcements_author ON public.announcements(author_id);


-- =============================================================================
-- 10. VIEWS
-- =============================================================================

-- ── team_standings ────────────────────────────────────────────────────────────
-- Live W / L / points-for / points-against / diff / rank per team per season.
-- Each finalized game contributes two rows (one from each team's perspective)
-- via UNION ALL, so points are attributed correctly to both sides.

CREATE OR REPLACE VIEW public.team_standings AS
WITH game_results AS (

    -- Home team's perspective
    SELECT
        home_team_id                                                AS team_id,
        sport_id,
        season_id,
        home_score                                                  AS points_for,
        away_score                                                  AS points_against,
        CASE WHEN home_score > away_score THEN 1 ELSE 0 END         AS is_win,
        CASE WHEN home_score < away_score THEN 1 ELSE 0 END         AS is_loss
    FROM public.games
    WHERE status = 'final'
      AND home_team_id IS NOT NULL

    UNION ALL

    -- Away team's perspective
    SELECT
        away_team_id                                                AS team_id,
        sport_id,
        season_id,
        away_score                                                  AS points_for,
        home_score                                                  AS points_against,
        CASE WHEN away_score > home_score THEN 1 ELSE 0 END         AS is_win,
        CASE WHEN away_score < home_score THEN 1 ELSE 0 END         AS is_loss
    FROM public.games
    WHERE status = 'final'
      AND away_team_id IS NOT NULL

),
aggregated AS (
    SELECT
        team_id,
        sport_id,
        season_id,
        SUM(is_win)                             AS wins,
        SUM(is_loss)                            AS losses,
        SUM(points_for)                         AS points_for,
        SUM(points_against)                     AS points_against,
        SUM(points_for) - SUM(points_against)   AS diff
    FROM game_results
    GROUP BY team_id, sport_id, season_id
)
SELECT
    t.id                            AS team_id,
    t.name                          AS team_name,
    t.sport_id,
    t.league,
    a.season_id,
    COALESCE(a.wins,          0)    AS wins,
    COALESCE(a.losses,        0)    AS losses,
    COALESCE(a.points_for,    0)    AS points_for,
    COALESCE(a.points_against,0)    AS points_against,
    COALESCE(a.diff,          0)    AS diff,
    -- Rank resets per sport + season bucket.
    -- Primary:   most wins
    -- Tiebreak:  best point differential
    RANK() OVER (
        PARTITION BY t.sport_id, a.season_id
        ORDER BY COALESCE(a.wins, 0) DESC,
                 COALESCE(a.diff, 0) DESC
    )                               AS rank
FROM public.teams t
LEFT JOIN aggregated a ON a.team_id = t.id;


-- ── team_record ───────────────────────────────────────────────────────────────
-- Assembles the human-readable "W-L" string shown across the app
-- (Team cards, Profile screen, Standings table, etc.).

CREATE OR REPLACE VIEW public.team_record AS
SELECT
    team_id,
    season_id,
    wins || '-' || losses   AS record
FROM public.team_standings;


-- ── team_form ─────────────────────────────────────────────────────────────────
-- Last 5 finalized game results per team as a Postgres TEXT array,
-- most recent result first — e.g. ARRAY['W','W','L','W','W'].
-- Consumed by the form strip on TeamDetailScreen.
--
-- Each game is evaluated from both teams' perspectives (same UNION ALL pattern),
-- then ROW_NUMBER() picks only the 5 most recent per team before aggregation.

CREATE OR REPLACE VIEW public.team_form AS
WITH all_results AS (

    SELECT
        home_team_id    AS team_id,
        scheduled_at,
        CASE
            WHEN home_score > away_score THEN 'W'
            WHEN home_score < away_score THEN 'L'
            ELSE 'T'
        END             AS result
    FROM public.games
    WHERE status = 'final'
      AND home_team_id IS NOT NULL

    UNION ALL

    SELECT
        away_team_id    AS team_id,
        scheduled_at,
        CASE
            WHEN away_score > home_score THEN 'W'
            WHEN away_score < home_score THEN 'L'
            ELSE 'T'
        END             AS result
    FROM public.games
    WHERE status = 'final'
      AND away_team_id IS NOT NULL

),
ranked AS (
    SELECT
        team_id,
        result,
        scheduled_at,
        ROW_NUMBER() OVER (
            PARTITION BY team_id
            ORDER BY scheduled_at DESC          -- row 1 = most recent game
        ) AS rn
    FROM all_results
)
SELECT
    team_id,
    ARRAY_AGG(result ORDER BY scheduled_at DESC)    AS form
FROM ranked
WHERE rn <= 5
GROUP BY team_id;


-- =============================================================================
-- 11. ROW-LEVEL SECURITY
-- =============================================================================

ALTER TABLE public.profiles       ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.teams          ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.team_members   ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.player_stats   ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.games          ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.rsvp           ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.announcements  ENABLE ROW LEVEL SECURITY;

-- profiles ────────────────────────────────────────────────────────────────────
-- Any authenticated user can read all profiles.
-- Users can only update their own row.
CREATE POLICY "profiles_read_all"
    ON public.profiles FOR SELECT
    USING (true);

CREATE POLICY "profiles_update_own"
    ON public.profiles FOR UPDATE
    USING (auth.uid() = id);

-- teams ───────────────────────────────────────────────────────────────────────
-- All authenticated users can read every team.
-- Only admins can create / modify / delete teams.
CREATE POLICY "teams_read_all"
    ON public.teams FOR SELECT
    USING (auth.role() = 'authenticated');

CREATE POLICY "teams_admin_write"
    ON public.teams FOR ALL
    USING (
        EXISTS (
            SELECT 1 FROM public.profiles
            WHERE id = auth.uid() AND role = 'admin'
        )
    );

-- team_members ────────────────────────────────────────────────────────────────
-- All authenticated users can view rosters.
-- The captain of a team (or any admin) can add/remove members.
CREATE POLICY "team_members_read_all"
    ON public.team_members FOR SELECT
    USING (auth.role() = 'authenticated');

CREATE POLICY "team_members_captain_or_admin_write"
    ON public.team_members FOR ALL
    USING (
        EXISTS (
            SELECT 1 FROM public.teams
            WHERE id = team_id AND captain_id = auth.uid()
        )
        OR EXISTS (
            SELECT 1 FROM public.profiles
            WHERE id = auth.uid() AND role = 'admin'
        )
    );

-- player_stats ────────────────────────────────────────────────────────────────
-- All authenticated users can read stats.
-- Only admins can write (stats are updated by campus rec staff).
CREATE POLICY "player_stats_read_all"
    ON public.player_stats FOR SELECT
    USING (auth.role() = 'authenticated');

CREATE POLICY "player_stats_admin_write"
    ON public.player_stats FOR ALL
    USING (
        EXISTS (
            SELECT 1 FROM public.profiles
            WHERE id = auth.uid() AND role = 'admin'
        )
    );

-- games ───────────────────────────────────────────────────────────────────────
-- All authenticated users can read every game.
-- Only admins can schedule / update / finalize games.
CREATE POLICY "games_read_all"
    ON public.games FOR SELECT
    USING (auth.role() = 'authenticated');

CREATE POLICY "games_admin_write"
    ON public.games FOR ALL
    USING (
        EXISTS (
            SELECT 1 FROM public.profiles
            WHERE id = auth.uid() AND role = 'admin'
        )
    );

-- rsvp ────────────────────────────────────────────────────────────────────────
-- Users can only read and manage their own RSVP rows.
-- The UNIQUE (game_id, user_id) constraint makes INSERT … ON CONFLICT safe.
CREATE POLICY "rsvp_read_own"
    ON public.rsvp FOR SELECT
    USING (auth.uid() = user_id);

CREATE POLICY "rsvp_insert_own"
    ON public.rsvp FOR INSERT
    WITH CHECK (auth.uid() = user_id);

CREATE POLICY "rsvp_update_own"
    ON public.rsvp FOR UPDATE
    USING (auth.uid() = user_id);

CREATE POLICY "rsvp_delete_own"
    ON public.rsvp FOR DELETE
    USING (auth.uid() = user_id);

-- announcements ───────────────────────────────────────────────────────────────
-- All authenticated users can read announcements.
-- Only admins can post them.
CREATE POLICY "announcements_read_all"
    ON public.announcements FOR SELECT
    USING (auth.role() = 'authenticated');

CREATE POLICY "announcements_admin_write"
    ON public.announcements FOR ALL
    USING (
        EXISTS (
            SELECT 1 FROM public.profiles
            WHERE id = auth.uid() AND role = 'admin'
        )
    );


-- =============================================================================
-- 12. TRIGGERS  (keep updated_at current automatically)
-- =============================================================================

CREATE OR REPLACE FUNCTION public.set_updated_at()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$;

DO $$ BEGIN
    CREATE TRIGGER trg_profiles_updated_at
        BEFORE UPDATE ON public.profiles
        FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    CREATE TRIGGER trg_teams_updated_at
        BEFORE UPDATE ON public.teams
        FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    CREATE TRIGGER trg_games_updated_at
        BEFORE UPDATE ON public.games
        FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    CREATE TRIGGER trg_rsvp_updated_at
        BEFORE UPDATE ON public.rsvp
        FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    CREATE TRIGGER trg_announcements_updated_at
        BEFORE UPDATE ON public.announcements
        FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
    CREATE TRIGGER trg_player_stats_updated_at
        BEFORE UPDATE ON public.player_stats
        FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL; END $$;


-- =============================================================================
-- 13. SEED DATA  (mirrors MockData.kt + TEST_CREDENTIALS exactly)
-- =============================================================================

-- ── Sports ────────────────────────────────────────────────────────────────────

INSERT INTO public.sports (id, display_name, short, emoji) VALUES
    ('basketball', 'Basketball', 'BBALL', '🏀'),
    ('soccer',     'Soccer',     'SOC',   '⚽'),
    ('volleyball', 'Volleyball', 'VBALL', '🏐'),
    ('dodgeball',  'Dodgeball',  'DODGE', '🎯')
ON CONFLICT (id) DO NOTHING;


-- ── Season ────────────────────────────────────────────────────────────────────

INSERT INTO public.seasons (id, name, start_date, end_date, playoff_start, is_active) VALUES
    ('spring-2026', 'Spring 2026', '2026-01-15', '2026-05-10', '2026-05-04', TRUE)
ON CONFLICT (id) DO NOTHING;


-- ── Auth users  (dev / seed only — DO NOT run this block in production) ────────
-- UUID layout:  a1000000-0000-0000-0000-00000000000X
--   0001  Tyler Reed   (tjr1234)  — primary test account, captain of t1 + t2
--   0002  Marcus Chen  (mlc8821)  — captain test account
--   0003  Alex Park    (arp4502)  — admin test account
--   0004  Marcus Reed             — Tiger Dunkers roster, Guard
--   0005  Kai Watanabe            — Tiger Dunkers roster, Forward
--   0006  Devon Pierce            — Tiger Dunkers roster + captain of Sting Ops (t4)
--   0007  Aaron Lin               — Tiger Dunkers roster + captain of Net Ninjas (t5)
--   0008  Theo Brooks             — Tiger Dunkers roster + captain of Park Point FC (t6)
--   0009  Jamal Okoro             — Tiger Dunkers roster + captain of Henrietta Heat (t7)
--   0010  Sam Lawson              — captain of Slamhattan (t8)
--   0011  Carlos Ibarra           — captain of CIAS United (t3)

INSERT INTO auth.users (
    id, instance_id, aud, role, email,
    encrypted_password, email_confirmed_at,
    raw_app_meta_data, raw_user_meta_data,
    created_at, updated_at
) VALUES
    ('a1000000-0000-0000-0000-000000000001',
     '00000000-0000-0000-0000-000000000000', 'authenticated', 'authenticated',
     'tjr1234@rit.edu',
     extensions.crypt('Password123!', extensions.gen_salt('bf')), now(),
     '{"provider":"email","providers":["email"]}', '{}', now(), now()),

    ('a1000000-0000-0000-0000-000000000002',
     '00000000-0000-0000-0000-000000000000', 'authenticated', 'authenticated',
     'mlc8821@rit.edu',
     extensions.crypt('Password123!', extensions.gen_salt('bf')), now(),
     '{"provider":"email","providers":["email"]}', '{}', now(), now()),

    ('a1000000-0000-0000-0000-000000000003',
     '00000000-0000-0000-0000-000000000000', 'authenticated', 'authenticated',
     'arp4502@rit.edu',
     extensions.crypt('Password123!', extensions.gen_salt('bf')), now(),
     '{"provider":"email","providers":["email"]}', '{}', now(), now()),

    ('a1000000-0000-0000-0000-000000000004',
     '00000000-0000-0000-0000-000000000000', 'authenticated', 'authenticated',
     'mrd0002@rit.edu',
     extensions.crypt('Password123!', extensions.gen_salt('bf')), now(),
     '{"provider":"email","providers":["email"]}', '{}', now(), now()),

    ('a1000000-0000-0000-0000-000000000005',
     '00000000-0000-0000-0000-000000000000', 'authenticated', 'authenticated',
     'kwt3300@rit.edu',
     extensions.crypt('Password123!', extensions.gen_salt('bf')), now(),
     '{"provider":"email","providers":["email"]}', '{}', now(), now()),

    ('a1000000-0000-0000-0000-000000000006',
     '00000000-0000-0000-0000-000000000000', 'authenticated', 'authenticated',
     'dpc4401@rit.edu',
     extensions.crypt('Password123!', extensions.gen_salt('bf')), now(),
     '{"provider":"email","providers":["email"]}', '{}', now(), now()),

    ('a1000000-0000-0000-0000-000000000007',
     '00000000-0000-0000-0000-000000000000', 'authenticated', 'authenticated',
     'aln5501@rit.edu',
     extensions.crypt('Password123!', extensions.gen_salt('bf')), now(),
     '{"provider":"email","providers":["email"]}', '{}', now(), now()),

    ('a1000000-0000-0000-0000-000000000008',
     '00000000-0000-0000-0000-000000000000', 'authenticated', 'authenticated',
     'tbr6601@rit.edu',
     extensions.crypt('Password123!', extensions.gen_salt('bf')), now(),
     '{"provider":"email","providers":["email"]}', '{}', now(), now()),

    ('a1000000-0000-0000-0000-000000000009',
     '00000000-0000-0000-0000-000000000000', 'authenticated', 'authenticated',
     'jok7701@rit.edu',
     extensions.crypt('Password123!', extensions.gen_salt('bf')), now(),
     '{"provider":"email","providers":["email"]}', '{}', now(), now()),

    ('a1000000-0000-0000-0000-000000000010',
     '00000000-0000-0000-0000-000000000000', 'authenticated', 'authenticated',
     'slm8801@rit.edu',
     extensions.crypt('Password123!', extensions.gen_salt('bf')), now(),
     '{"provider":"email","providers":["email"]}', '{}', now(), now()),

    ('a1000000-0000-0000-0000-000000000011',
     '00000000-0000-0000-0000-000000000000', 'authenticated', 'authenticated',
     'cias9901@rit.edu',
     extensions.crypt('Password123!', extensions.gen_salt('bf')), now(),
     '{"provider":"email","providers":["email"]}', '{}', now(), now())

ON CONFLICT (id) DO NOTHING;


-- ── Profiles ──────────────────────────────────────────────────────────────────

INSERT INTO public.profiles (id, rit_id, name, first_name, role, major, year) VALUES
    -- Primary test accounts (TEST_CREDENTIALS in MockData.kt)
    ('a1000000-0000-0000-0000-000000000001', 'tjr1234', 'Tyler Reed',    'Tyler',  'student', 'Computing & Info Tech',     'Junior'),
    ('a1000000-0000-0000-0000-000000000002', 'mlc8821', 'Marcus Chen',   'Marcus', 'captain', 'Mechanical Engineering',    'Senior'),
    ('a1000000-0000-0000-0000-000000000003', 'arp4502', 'Alex Park',     'Alex',   'admin',   'Campus Recreation',         'Staff'),
    -- Roster players / additional captains
    ('a1000000-0000-0000-0000-000000000004', 'mrd0002', 'Marcus Reed',   'Marcus', 'student', 'Game Design & Development', 'Sophomore'),
    ('a1000000-0000-0000-0000-000000000005', 'kwt3300', 'Kai Watanabe',  'Kai',    'student', 'Computer Science',          'Junior'),
    ('a1000000-0000-0000-0000-000000000006', 'dpc4401', 'Devon Pierce',  'Devon',  'captain', 'Civil Engineering',         'Senior'),
    ('a1000000-0000-0000-0000-000000000007', 'aln5501', 'Aaron Lin',     'Aaron',  'captain', 'Software Engineering',      'Junior'),
    ('a1000000-0000-0000-0000-000000000008', 'tbr6601', 'Theo Brooks',   'Theo',   'captain', 'Business Administration',   'Senior'),
    ('a1000000-0000-0000-0000-000000000009', 'jok7701', 'Jamal Okoro',   'Jamal',  'captain', 'Electrical Engineering',    'Junior'),
    ('a1000000-0000-0000-0000-000000000010', 'slm8801', 'Sam Lawson',    'Sam',    'captain', 'Imaging Science',           'Senior'),
    ('a1000000-0000-0000-0000-000000000011', 'cias9901','Carlos Ibarra', 'Carlos', 'captain', 'Industrial Engineering',    'Senior')

ON CONFLICT (id) DO NOTHING;


-- ── Teams  (TEAMS list in MockData.kt) ───────────────────────────────────────
-- rank is intentionally omitted — read from team_standings view.

INSERT INTO public.teams (id, name, sport_id, league, captain_id) VALUES
    ('t1', 'Tiger Dunkers',  'basketball', 'Mens A', 'a1000000-0000-0000-0000-000000000001'),
    ('t2', 'Brick House',    'volleyball', 'Coed B', 'a1000000-0000-0000-0000-000000000001'),
    ('t3', 'CIAS United',    'soccer',     'Mens A', 'a1000000-0000-0000-0000-000000000011'),
    ('t4', 'Sting Ops',      'dodgeball',  'Open',   'a1000000-0000-0000-0000-000000000006'),
    ('t5', 'Net Ninjas',     'volleyball', 'Coed B', 'a1000000-0000-0000-0000-000000000007'),
    ('t6', 'Park Point FC',  'soccer',     'Mens A', 'a1000000-0000-0000-0000-000000000008'),
    ('t7', 'Henrietta Heat', 'basketball', 'Mens A', 'a1000000-0000-0000-0000-000000000009'),
    ('t8', 'Slamhattan',     'basketball', 'Mens A', 'a1000000-0000-0000-0000-000000000010')

ON CONFLICT (id) DO NOTHING;


-- ── Team members  (ROSTER_T1 in MockData.kt — Tiger Dunkers only) ─────────────

INSERT INTO public.team_members (team_id, user_id, season_id, jersey_number, position) VALUES
    ('t1', 'a1000000-0000-0000-0000-000000000001', 'spring-2026', 23, 'Captain'),
    ('t1', 'a1000000-0000-0000-0000-000000000004', 'spring-2026', 11, 'Guard'),
    ('t1', 'a1000000-0000-0000-0000-000000000005', 'spring-2026',  7, 'Forward'),
    ('t1', 'a1000000-0000-0000-0000-000000000006', 'spring-2026', 32, 'Center'),
    ('t1', 'a1000000-0000-0000-0000-000000000007', 'spring-2026',  4, 'Guard'),
    ('t1', 'a1000000-0000-0000-0000-000000000008', 'spring-2026', 15, 'Forward'),
    ('t1', 'a1000000-0000-0000-0000-000000000009', 'spring-2026', 21, 'Guard')

ON CONFLICT (team_id, user_id, season_id) DO NOTHING;


-- ── Player stats  (Spring 2026, Tiger Dunkers) ────────────────────────────────
-- Joined via subquery so we don't need to hardcode team_member UUIDs.

INSERT INTO public.player_stats
    (team_member_id, season_id, games_played, ppg, mvp_count, attendance_pct)
SELECT
    tm.id,
    'spring-2026',
    data.games_played,
    data.ppg,
    data.mvp_count,
    data.attendance_pct
FROM public.team_members tm
JOIN (VALUES
    ('a1000000-0000-0000-0000-000000000001'::UUID, 5, 18.4, 3, 1.00),
    ('a1000000-0000-0000-0000-000000000004'::UUID, 5, 14.2, 0, 0.80),
    ('a1000000-0000-0000-0000-000000000005'::UUID, 5, 11.0, 0, 1.00),
    ('a1000000-0000-0000-0000-000000000006'::UUID, 5,  9.6, 0, 0.80),
    ('a1000000-0000-0000-0000-000000000007'::UUID, 5,  8.2, 0, 1.00),
    ('a1000000-0000-0000-0000-000000000008'::UUID, 4,  6.8, 0, 0.60),
    ('a1000000-0000-0000-0000-000000000009'::UUID, 5,  5.4, 0, 1.00)
) AS data(user_id, games_played, ppg, mvp_count, attendance_pct)
  ON tm.user_id = data.user_id
WHERE tm.team_id   = 't1'
  AND tm.season_id = 'spring-2026'

ON CONFLICT (team_member_id, season_id) DO NOTHING;


-- ── Games  (GAMES list in MockData.kt) ───────────────────────────────────────
-- Timestamps use the same offsets as day() in MockData.kt, anchored to
-- 2026-04-27 (Eastern time, UTC-4 during daylight saving).
-- Both home_team_id and away_team_id are NULL when an opponent is external
-- (not seeded in the teams table); home_name / away_name carry the label.

INSERT INTO public.games
    (id, sport_id, season_id,
     home_team_id, away_team_id, home_name, away_name,
     venue, scheduled_at, status, home_score, away_score)
VALUES
    -- ── Upcoming — user's teams ────────────────────────────────────────────
    ('g1',  'basketball', 'spring-2026',
     't1', 't7', NULL, NULL,
     'Gordon Field House — Court 2', '2026-04-27 19:00:00-04', 'upcoming', NULL, NULL),

    ('g2',  'volleyball',  'spring-2026',
     't2', 't5', NULL, NULL,
     'Gordon — Court A',             '2026-04-27 21:00:00-04', 'upcoming', NULL, NULL),

    ('g4',  'basketball', 'spring-2026',
     't1', 't8', NULL, NULL,
     'Gordon — Court 1',             '2026-04-29 20:00:00-04', 'upcoming', NULL, NULL),

    -- away opponent not in DB → use away_name fallback
    ('g5',  'dodgeball',  'spring-2026',
     't4', NULL, NULL, 'Wreckless',
     'Clark Gym',                    '2026-04-30 18:30:00-04', 'upcoming', NULL, NULL),

    ('g6',  'volleyball',  'spring-2026',
     't2', NULL, NULL, 'Block Party',
     'Gordon — Court B',             '2026-05-01 19:00:00-04', 'upcoming', NULL, NULL),

    -- ── Upcoming — spectator games (neither side is the user's team) ───────
    ('g3',  'soccer',     'spring-2026',
     't6', 't3', NULL, NULL,
     'Turf Field 1',                 '2026-04-28 17:30:00-04', 'upcoming', NULL, NULL),

    ('g10', 'soccer',     'spring-2026',
     't3', NULL, NULL, 'Brick City Boys',
     'Turf Field 2',                 '2026-04-29 16:00:00-04', 'upcoming', NULL, NULL),

    -- ── Finals ─────────────────────────────────────────────────────────────
    -- g7: Tiger Dunkers 58 – 51 Brick City Ballers  (Win)
    ('g7',  'basketball', 'spring-2026',
     't1', NULL, NULL, 'Brick City Ballers',
     'Gordon — Court 2',             '2026-04-25 19:00:00-04', 'final',    58, 51),

    -- g8: Brick House 3 – 1 Spike Force  (Win, volleyball sets)
    ('g8',  'volleyball',  'spring-2026',
     't2', NULL, NULL, 'Spike Force',
     'Gordon — Court A',             '2026-04-24 20:00:00-04', 'final',     3,  1),

    -- g9: Tiger Dunkers 49 – 54 Tiger Pride  (Loss)
    ('g9',  'basketball', 'spring-2026',
     't1', NULL, NULL, 'Tiger Pride',
     'Gordon — Court 1',             '2026-04-22 21:00:00-04', 'final',    49, 54)

ON CONFLICT (id) DO NOTHING;


-- ── Announcements  (ANNOUNCEMENTS list in MockData.kt) ───────────────────────

INSERT INTO public.announcements (title, body, author_id, created_at) VALUES
    ('Spring playoffs bracket released',
     'Top 4 in each league advance. Brackets posted Friday.',
     'a1000000-0000-0000-0000-000000000003',
     now() - INTERVAL '2 hours'),

    ('Gordon Field House Court 1 closed Saturday',
     'Maintenance 8am–noon. Affected games rescheduled.',
     'a1000000-0000-0000-0000-000000000003',
     now() - INTERVAL '1 day')

ON CONFLICT DO NOTHING;


-- =============================================================================
-- 14. SMOKE-TEST QUERIES
-- Uncomment and run these after the script to verify everything landed correctly.
-- =============================================================================

-- SELECT * FROM public.profiles;
-- SELECT * FROM public.teams;
-- SELECT * FROM public.team_members;
-- SELECT * FROM public.player_stats;
-- SELECT * FROM public.games ORDER BY scheduled_at;
-- SELECT * FROM public.announcements;

-- -- Standings for Basketball (should show t1 and t7 from seeded finals)
-- SELECT * FROM public.team_standings
-- WHERE sport_id = 'basketball'
-- ORDER BY rank;

-- -- W-L record strings
-- SELECT t.name, tr.record
-- FROM public.team_record tr
-- JOIN public.teams t ON t.id = tr.team_id;

-- -- Last-5 form for Tiger Dunkers (t1) — expects ['W','L'] from g7 + g9
-- SELECT * FROM public.team_form WHERE team_id = 't1';
