# IntraHub — Project Memory

## What Is This

IntraHub is an Android intramural sports companion app for **RIT (Rochester Institute of Technology)** students. It lets players track games, standings, team rosters, and personal profiles. Built entirely with Jetpack Compose and Navigation 3.

**App ID:** `com.bazovic.balsa.intrahub`  
**Version:** 1.0  
**Min SDK:** 23 · **Target/Compile SDK:** 36  

---

## Tech Stack

| Layer | Library |
|---|---|
| UI | Jetpack Compose + Material 3 |
| Navigation | **Navigation 3** (`androidx.navigation3`) — NOT the classic NavController |
| State | `mutableStateOf` / ViewModel (Compose-aware) |
| Serialization | `kotlinx.serialization` (routes are `@Serializable`) |
| Architecture | MVVM — `ViewModel` per screen feature |
| Backend | **Supabase** (Auth + PostgREST) via Supabase Kotlin SDK 3.1.4 |
| HTTP | Ktor 3.1.3 with **OkHttp engine** (`ktor-client-okhttp`) — must be set explicitly via `httpEngine = OkHttp.create()` |

> **Navigation 3 note:** Routes implement `NavKey`. Back-stack is a `NavBackStack` (`rememberNavBackStack`). Entry definitions use `entryProvider { entry<RouteType> { } }`. `NavDisplay` renders the current back-stack entry. `rememberSavedStateNavEntryDecorator`, `rememberViewModelStoreNavEntryDecorator`, `rememberSceneSetupNavEntryDecorator` are wired as decorators.

> **Ktor engine note:** Ktor 3.x does NOT auto-detect the engine from the classpath. `httpEngine = OkHttp.create()` must be set explicitly inside `createSupabaseClient { }` or network calls will fail with a hostname error.

---

## Package Structure

```
com.bazovic.balsa.intrahub
├── MainActivity.kt               — App entry, IntraHubApp(), MainNavigation(), bottom nav
├── data/
│   ├── Models.kt                 — All domain data classes & enums
│   ├── MockData.kt               — Legacy hardcoded data (kept for reference; screens now use Supabase)
│   ├── SupabaseClient.kt         — Supabase singleton (URL, anon key, OkHttp engine)
│   ├── remote/
│   │   └── RemoteModels.kt       — DTOs + toDomain() mappers + string helpers
│   └── repository/
│       ├── AuthRepository.kt     — signIn, getCurrentProfile, getCurrentUserId, signOut
│       ├── GamesRepository.kt    — getGames, getGame, getTeamGames
│       ├── TeamsRepository.kt    — getTeams, getMyTeamIds, getTeam, getRoster, getAggregatedStats
│       ├── StandingsRepository.kt— getStandings (no season filter — view is already scoped)
│       ├── RsvpRepository.kt     — getRsvp, upsertRsvp
│       └── AnnouncementsRepository.kt
├── navigation/
│   └── Routes.kt                 — Sealed AppRoute, route helpers, TAB_ORDER
├── ui/
│   ├── components/
│   │   └── Components.kt         — Shared composables (Avatar, GameRow, TeamRow, etc.)
│   ├── screens/
│   │   ├── LoginScreen.kt
│   │   ├── LoadingScreen.kt
│   │   ├── HomeScreen.kt
│   │   ├── ScheduleScreen.kt
│   │   ├── StandingsScreen.kt
│   │   ├── TeamsScreen.kt
│   │   ├── ProfileScreen.kt
│   │   ├── GameDetailScreen.kt
│   │   └── TeamDetailScreen.kt
│   └── theme/
│       ├── Color.kt              — Full palette + Sport color extensions
│       ├── Theme.kt              — IntraHubTheme
│       └── Type.kt
└── viewmodel/
    ├── AppViewModel.kt           — App-level state (Login/Loading/App)
    ├── HomeViewModel.kt          — myTeams, upcomingGames, recentGames, streakText
    ├── ScheduleViewModel.kt      — allGames, filter, sportFilter, search
    ├── StandingsViewModel.kt     — standings (Map<Sport, List<StandingRow>>), selectedSport
    ├── TeamsViewModel.kt         — teams, myTeamIds, selectedSport
    ├── ProfileViewModel.kt       — myTeams, gamesPlayed, wins
    ├── GameDetailViewModel.kt    — game, myTeam, roster, rsvp, load(gameId)
    └── TeamDetailViewModel.kt    — team, roster, teamGames, form, isCaptain, load(teamId)
```

---

## App Flow

```
Login (AppState.Login)
  → LoadingScreen (AppState.Loading)
  → MainNavigation (AppState.App)
```

`AppViewModel` drives this with `appState: AppState` (`mutableStateOf`).  
`login(u, p)` calls `AuthRepository.signIn()` (constructs `<username>@rit.edu` email), then fetches `getCurrentProfile()`. On success sets `currentUser` and transitions to Loading.  
`onLoadingComplete()` moves to App. `logout()` calls `authRepository.signOut()` and resets to Login.

---

## Navigation

**Tab routes** (bottom nav, each clears the back-stack):
- `HomeRoute` → `HomeScreen`
- `ScheduleRoute` → `ScheduleScreen`
- `StandingsRoute` → `StandingsScreen`
- `TeamsRoute` → `TeamsScreen`
- `ProfileRoute` → `ProfileScreen`

**Detail routes** (pushed on top, have Back button):
- `GameDetailRoute(gameId: String)` → `GameDetailScreen`
- `TeamDetailRoute(teamId: String)` → `TeamDetailScreen`

`switchTab()` wipes back-stack and sets a single tab root.  
`pushRoute()` appends a detail route on top.  
Bottom nav is hidden on detail screens (`isTabRoute()` check).

---

## Data Models (`data/Models.kt`)

```kotlin
enum class UserRole { Student, Captain, Admin }
enum class Sport(displayName, short, emoji) { Basketball, Soccer, Volleyball, Dodgeball }
enum class GameStatus { Upcoming, Final }
enum class GameResult { Win, Loss, Tie, None }

data class UserProfile(id /*Supabase UUID*/, ritId, name, firstName, role, major, year, email)
data class Team(id, name, sport, record /*"W-L"*/, rank, league, captainId)
data class Player(id, name, number, role, ppg)
data class Game(id, sport, myTeamId /*null=spectator*/, oppName, oppShort, whenMs, venue, status, myScore, oppScore, result, isSpectator, referee /*nullable*/)
data class StandingRow(teamId, rank, teamName, wins, losses, pointsFor, pointsAgainst, isMine) // diff = pF - pA
data class Announcement(id, title, body, timeAgo)
```

Extension properties: `Team.wins`, `Team.losses` (parsed from `record`).

---

## Supabase Schema

### Tables
| Table | Key columns |
|---|---|
| `profiles` | `id` (UUID, FK → auth.users), `rit_id`, `name`, `first_name`, `role` (lowercase: student/captain/admin), `major`, `year` |
| `teams` | `id`, `name`, `sport_id`, `league`, `captain_id` |
| `team_members` | `id`, `team_id`, `user_id`, `jersey_number`, `position`, `season_id` |
| `player_stats` | `id`, `team_member_id`, `season_id`, `ppg`, `games_played`, `wins` |
| `games` | `id`, `sport_id`, `season_id`, `home_team_id`, `away_team_id`, `home_name`, `away_name`, `venue`, `scheduled_at`, `status` (upcoming/final), `home_score`, `away_score`, `referee` |
| `rsvp` | `id`, `game_id`, `user_id`, `response` (yes/maybe/no) — UNIQUE on (game_id, user_id) |
| `announcements` | `id`, `title`, `body`, `author_id`, `created_at` |
| `sports` | `id`, `display_name`, `short`, `emoji` |
| `seasons` | `id`, `name`, `start_date`, `end_date`, `is_active` |

### Views (computed, no direct writes)
| View | Description |
|---|---|
| `team_standings` | Per-sport rank, W, L, points for/against, diff — **no season_id filter needed in queries**, the view handles scoping internally |
| `team_record` | W-L string per team per season |

### RLS Notes
- All tables need `SELECT` policies for the `authenticated` role with `using (true)` — views inherit RLS from their underlying tables.
- `rsvp` upsert uses `onConflict = "game_id,user_id"`.
- `profiles` RLS allows each user to read their own row at minimum.

---

## Remote Models (`data/remote/RemoteModels.kt`)

DTOs mirror Supabase column names via `@SerialName`. Key mappers:
- `ProfileDto.toDomain()` — maps `role` string via `toUserRole()` (lowercase match: `"captain"`, `"admin"`, else Student)
- `GameDto.toDomain(myTeamIds, teamNames)` — determines home/away perspective, computes result
- `StandingRowDto.toDomain()` — `isMine` always `false` here; set via `.copy(isMine = teamId in myTeamIds)` at call site
- `String.toSport()` — exact lowercase match: `"basketball"`, `"soccer"`, `"volleyball"`, `"dodgeball"`, else Basketball
- `String.toEpochMs()` — parses ISO 8601 timestamps from Supabase (strips fractional seconds, normalises timezone offset)

---

## Design System / Theme

**Color palette** (all defined in `ui/theme/Color.kt`):

| Name | Hex | Usage |
|---|---|---|
| `OrangeRIT` | `#F76902` | Primary brand, selected items, accents |
| `OrangeDark` | `#C24E00` | Darker orange for text on tint backgrounds |
| `OrangeTint` | `#FFEFE3` | Selected chip/card backgrounds |
| `OrangeTint2` | `#FFF8F2` | Very light orange (my team rows) |
| `Ink` → `Ink5` | `#0A0A0A` → `#9B9B9B` | Text shades (darkest to lightest) |
| `Line` / `Line2` | `#E8E4DE` / `#F0ECE6` | Borders / dividers |
| `Canvas` | `#FAF8F5` | Screen backgrounds |
| `Win` | `#1F7A4D` | Win state green |
| `Loss` | `#B43A3A` | Loss state red |

Sport accent / tint colors for each of the 4 sports — access via `sport.accentColor` and `sport.tintColor`.

Avatar colors: deterministic from name hash, 7-color palette including OrangeRIT.

---

## Shared Components (`ui/components/Components.kt`)

| Component | Description |
|---|---|
| `Avatar(name, size)` | Circular avatar with initials, color derived from name |
| `RoleBadge(role)` | Colored pill for Student / Captain / Admin |
| `ScoreChip(status, result)` | W·FINAL / L·FINAL / T·FINAL / UPCOMING pill |
| `SportIconBox(sport, size)` | Rounded square with sport emoji + tint background |
| `FilterChip(label, active, onClick)` | Toggle chip (dark when active) |
| `SportTabChip(sport, active, onClick)` | Sport filter tab with emoji + sport colors |
| `SectionHeader(title, action?, onAction?)` | Section label with optional "See all →" action |
| `EyebrowLabel(text, modifier, color)` | Small monospace caps label |
| `InfoRow(icon, label, value, last)` | Icon + label + value row for detail cards |
| `GameRow(game, onClick)` | Full game card — sport icon top-aligned; final games stack chip+date vertically so date gets full width; upcoming games show chip+date on one line |
| `TeamRow(team, mine, onClick)` | Team list row, orange-tinted if mine |
| `relativeDay(ms)` | "Today" / "Tomorrow" / "2d ago" / formatted date |
| `formatTime(ms)` | "7:00 PM" |
| `formatDateShort(ms)` | "MON, APR 27" |

---

## Role Separation

| Feature | Student | Captain | Admin |
|---|---|---|---|
| Home quick action | Check In → next game | Manage Roster → team detail | Approve Teams → schedule |
| Schedule `+ Game` button | — | — | ✓ (not yet wired) |
| Team detail `isCaptain` flag | false | true (if captainId matches) | false |
| Profile star on team row | — | ✓ | — |
| Everything else | same | same | same |

---

## Screen Summaries

### HomeScreen (via `HomeViewModel`)
- Header: greeting + live date eyebrow (`System.currentTimeMillis()`)
- Dark "Record Hero Card" with combined W-L across all my teams; mini team chips navigate to TeamDetail
- "Up next" — upcoming games from Supabase
- "Quick Actions" — role-adaptive card + Standings shortcut
- "Recent results" — finalized games

### ScheduleScreen (via `ScheduleViewModel`)
- `filter` (All Games / My Teams / Upcoming / Final) and `sportFilter` state
- Search bar + toggleable sport filter panel (extra 8dp top padding when open)
- Admin-only `+ Game` button pushed to the right of the header via `fillMaxWidth` + `SpaceBetween`
- Date-grouped game list with section dividers

### StandingsScreen (via `StandingsViewModel`)
- `selectedSport` tab filter
- Ranked table: rank bar (orange for top-3), avatar, team name, W, L, point-diff
- My team rows highlighted `OrangeTint2`, clickable to TeamDetail using real `row.teamId`
- `StandingsRepository.getStandings()` fetches `team_standings` view with **no season filter** (view handles it)

### TeamsScreen (via `TeamsViewModel`)
- `selectedSport` filter tabs
- My teams shown first with orange border, then all others
- `TeamRow` composable for each

### ProfileScreen (via `ProfileViewModel`)
- Orange gradient banner header
- Stats strip: GAMES / WINS / TEAMS (MVPs not yet in schema)
- "My teams" section — star icon if `team.captainId == user.id`
- "Season performance" — placeholder stats
- "Settings" — non-functional placeholder rows
- Sign Out button

### GameDetailScreen (via `GameDetailViewModel`)
- `LaunchedEffect(gameId) { vm.load(gameId) }` — idempotent load guard
- Scoreboard card, game info, RSVP buttons (yes/maybe/no → upserted to `rsvp` table)
- Box score Q1–Q4 hardcoded as 0 (per-quarter schema not yet implemented)

### TeamDetailScreen (via `TeamDetailViewModel`)
- `LaunchedEffect(teamId) { vm.load(teamId) }` — idempotent load guard
- `isCaptain` derived from `team.captainId == authRepo.getCurrentUserId()`
- `form` — last 5 finalized games as List<"W"|"L">

---

## Known Patterns & Conventions

1. **Supabase is the data source** — `MockData.kt` is kept but screens use repositories. Do not add new mock data; add Supabase rows instead.
2. **Screen-level VMs** use `viewModel()` (no factory needed — plain `ViewModel()`).
3. **Colors always imported from theme** — never use `Color(0xFF...)` inline in screens; add to `Color.kt` first.
4. **`Modifier` chaining order:** `fillMaxWidth()` → `clip()` → `background()` → `border()` → `clickable()` → `padding()`.
5. **Section comments** use `// ─── SECTION: Name ─── //` format.
6. **No Hilt/DI** — ViewModels instantiated via Compose `viewModel()`.
7. **Serialization** on routes is required — all `AppRoute` subclasses are `@Serializable`.
8. **`isTabRoute()`** determines bottom nav visibility and tab switching logic.
9. **Locale for date formatting** is always `Locale.US`.
10. **Button labels** use title case (first letter of each word capitalised): "Sign In", "Sign Out", "All Games", "My Teams", "Manage Roster", etc.
11. **Detail screen load pattern:** `LaunchedEffect(id) { vm.load(id) }` in the screen; VM uses a `loadedId` guard to prevent duplicate loads.
12. **`team_standings` view** — do NOT add a `season_id` filter when querying; the view's own definition handles season scoping.

---

## Files to Touch for Common Tasks

| Task | File(s) |
|---|---|
| Add a new screen | `Routes.kt` (new route), `MainActivity.kt` (add `entry<>`), new `*Screen.kt`, new `*ViewModel.kt` |
| Add a new sport | `Models.kt` (Sport enum), `Color.kt` (accent/tint), Supabase `sports` table |
| Add/edit games or teams | Supabase dashboard or SQL editor |
| Change bottom nav tabs | `MainActivity.kt` (`NAV_TABS`) |
| Change colors | `ui/theme/Color.kt` |
| Add a shared component | `ui/components/Components.kt` |
| Change app flow (login/loading) | `AppViewModel.kt` |
| Add a new Supabase query | New or existing `data/repository/*.kt` + DTO in `RemoteModels.kt` |
| Change RLS policies | Supabase Dashboard → Authentication → Policies (target the **table**, not the view) |
