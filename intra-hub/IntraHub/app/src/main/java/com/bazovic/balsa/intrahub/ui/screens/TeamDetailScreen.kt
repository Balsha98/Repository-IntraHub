package com.bazovic.balsa.intrahub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bazovic.balsa.intrahub.data.*
import com.bazovic.balsa.intrahub.ui.components.*
import com.bazovic.balsa.intrahub.ui.theme.*
import com.bazovic.balsa.intrahub.viewmodel.TeamDetailViewModel

@Composable
fun TeamDetailScreen(
    teamId: String,
    user: UserProfile,
    onBack: () -> Unit,
    onNavGame: (String) -> Unit,
    vm: TeamDetailViewModel = viewModel(),
) {
    LaunchedEffect(teamId) { vm.load(teamId) }

    val team = vm.team
    val isAdmin = user.role == UserRole.Admin

    var selectedTab by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize().background(Canvas)) {
        // ─── SECTION: Sticky Coloured Header ─── //
        val headerColor = team?.sport?.tintColor ?: Line2
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerColor)
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.6f)),
                ) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, modifier = Modifier.size(20.dp))
                }
                if (vm.isCaptain || isAdmin) {
                    IconButton(
                        onClick = {},
                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = 0.6f)),
                    ) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                    }
                }
            }

            if (vm.isLoading || team == null) {
                Spacer(Modifier.height(32.dp))
                if (vm.errorMessage != null) {
                    Text(vm.errorMessage!!, color = Loss, fontSize = 14.sp)
                } else {
                    CircularProgressIndicator(color = OrangeRIT, modifier = Modifier.size(24.dp))
                }
                return@Column
            }

            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                SportIconBox(team.sport, size = 56.dp)
                Column {
                    Text(
                        "${team.league.uppercase()} · ${team.sport.displayName.uppercase()}",
                        color = team.sport.accentColor,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                    )
                    Text(team.name, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, letterSpacing = (-0.66).sp, lineHeight = 28.sp)
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.5f))
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                TeamStat("RECORD", team.record)
                VerticalDivider(modifier = Modifier.height(32.dp), color = Color.White.copy(alpha = 0.5f))
                TeamStat("RANK", "#${team.rank}", OrangeRIT)
                VerticalDivider(modifier = Modifier.height(32.dp), color = Color.White.copy(alpha = 0.5f))
                val streak = if (vm.form.isNotEmpty()) vm.form.last().let {
                    val count = vm.form.takeLastWhile { r -> r == it }.size
                    "$count$it"
                } else "—"
                TeamStat("STREAK", streak, if (vm.form.lastOrNull() == "W") Win else Ink4)
            }
        }

        if (team == null) return@Column

        // ─── SECTION: Tab Bar ─── //
        val tabs = listOf("Overview", "Roster (${vm.roster.size})", "Games")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            tabs.forEachIndexed { i, label ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedTab == i) Ink else Color.Transparent)
                        .clickable { selectedTab = i }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        label,
                        color = if (selectedTab == i) Color.White else Ink4,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        // ─── SECTION: Tab Content ─── //
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            when (selectedTab) {
                0 -> OverviewTab(team, vm.teamGames, vm.roster, vm.form, onNavGame)
                1 -> RosterTab(vm.roster, vm.isCaptain, isAdmin)
                2 -> GamesTab(vm.teamGames, onNavGame)
            }
        }
    }
}//TeamDetailScreen

@Composable
private fun OverviewTab(
    team: Team,
    teamGames: List<Game>,
    roster: List<Player>,
    form: List<String>,
    onNavGame: (String) -> Unit,
) {
    val nextGame = teamGames.firstOrNull { it.status == GameStatus.Upcoming }

    EyebrowLabel("NEXT GAME")
    Spacer(Modifier.height(8.dp))
    if (nextGame != null) {
        GameRow(nextGame) { onNavGame(nextGame.id) }
    } else {
        Text("No upcoming games.", color = Ink4, fontSize = 14.sp)
    }

    // ─── Form (last 5) ─── //
    if (form.isNotEmpty()) {
        Spacer(Modifier.height(18.dp))
        EyebrowLabel("FORM (LAST 5)")
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            form.forEach { r ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (r == "W") WinTint else LossTint)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(r, color = if (r == "W") Win else Loss, fontSize = 16.sp,
                        fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }

    // ─── Top scorers ─── //
    Spacer(Modifier.height(18.dp))
    EyebrowLabel("TOP SCORERS")
    Spacer(Modifier.height(8.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, Line, RoundedCornerShape(14.dp)),
    ) {
        Column {
            roster.take(3).forEachIndexed { i, player ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("#${i + 1}", color = Ink4, fontSize = 14.sp, fontFamily = FontFamily.Monospace,
                        modifier = Modifier.width(20.dp))
                    Avatar(player.name, size = 32.dp)
                    Text(player.name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                    Text(
                        "${player.ppg}",
                        color = OrangeRIT,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                    )
                    Text(" PPG", color = Ink4, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
                if (i < 2) HorizontalDivider(color = Line2)
            }
        }
    }
}//OverviewTab

@Composable
private fun RosterTab(roster: List<Player>, isCaptain: Boolean, isAdmin: Boolean) {
    if (isCaptain || isAdmin) {
        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth().height(48.dp).padding(bottom = 12.dp),
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OrangeRIT),
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Invite Player", fontWeight = FontWeight.SemiBold)
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, Line, RoundedCornerShape(14.dp)),
    ) {
        Column {
            roster.forEachIndexed { i, player ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Avatar(player.name, size = 36.dp)
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(player.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            if (player.role == "Captain") Icon(Icons.Default.Star, null, tint = OrangeRIT, modifier = Modifier.size(12.dp))
                        }
                        Text("#${player.number} · ${player.role.uppercase()}",
                            color = Ink4, fontSize = 12.sp, fontFamily = FontFamily.Monospace, letterSpacing = 0.4.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${player.ppg}", fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text("PPG", color = Ink5, fontSize = 12.sp, fontFamily = FontFamily.Monospace, letterSpacing = 0.8.sp)
                    }
                }
                if (i < roster.size - 1) HorizontalDivider(color = Line2)
            }
        }
    }
}//RosterTab

@Composable
private fun GamesTab(teamGames: List<Game>, onNavGame: (String) -> Unit) {
    if (teamGames.isEmpty()) {
        Text("No games yet.", color = Ink4, fontSize = 16.sp, modifier = Modifier.fillMaxWidth().padding(24.dp))
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            teamGames.forEach { game -> GameRow(game) { onNavGame(game.id) } }
        }
    }
}//GamesTab

@Composable
private fun TeamStat(label: String, value: String, accent: Color = Ink) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = accent, fontSize = 24.sp, fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace, letterSpacing = (-0.44).sp)
        Spacer(Modifier.height(6.dp))
        Text(label, color = Ink4, fontSize = 12.sp, fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp)
    }
}//TeamStat
