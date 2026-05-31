package com.bazovic.balsa.intrahub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bazovic.balsa.intrahub.data.*
import com.bazovic.balsa.intrahub.ui.components.*
import com.bazovic.balsa.intrahub.ui.theme.*
import com.bazovic.balsa.intrahub.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    user: UserProfile,
    onNavGame: (String) -> Unit,
    onNavTeam: (String) -> Unit,
    onNavSchedule: () -> Unit,
    onNavStandings: () -> Unit,
    vm: HomeViewModel = viewModel(),
) {
    val totalW = vm.myTeams.sumOf { it.wins }
    val totalL = vm.myTeams.sumOf { it.losses }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Canvas)
            .verticalScroll(rememberScrollState()),
    ) {
        // ─── SECTION: Header ─── //
        Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 4.dp)) {
            EyebrowLabel(formatDateShort(System.currentTimeMillis()))
            Spacer(Modifier.height(4.dp))
            Text("Hey, ${user.firstName}", fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, letterSpacing = (-0.78).sp, lineHeight = 30.sp)
        }

        // ─── SECTION: Record Hero Card ─── //
        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Ink),
        ) {
            Box(modifier = Modifier.size(160.dp).offset(x = 210.dp, y = (-40).dp).clip(RoundedCornerShape(999.dp)).background(OrangeRIT.copy(alpha = 0.18f)))
            Box(modifier = Modifier.size(80.dp).offset(x = 250.dp, y = (-20).dp).clip(RoundedCornerShape(999.dp)).background(OrangeRIT.copy(alpha = 0.30f)))

            Column(modifier = Modifier.padding(20.dp)) {
                EyebrowLabel("MY COMBINED RECORD · SPRING '26", color = Color.White.copy(alpha = 0.55f))
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("$totalW", color = Color.White, fontSize = 56.sp, fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace, letterSpacing = (-2.24).sp, lineHeight = 56.sp)
                    Text("—", color = Color.White.copy(alpha = 0.4f), fontSize = 28.sp,
                        fontFamily = FontFamily.Monospace, modifier = Modifier.padding(bottom = 6.dp))
                    Text("$totalL", color = Color.White.copy(alpha = 0.6f), fontSize = 34.sp,
                        fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 2.dp))
                    Spacer(Modifier.weight(1f))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(bottom = 6.dp)) {
                        Icon(Icons.Default.ArrowUpward, null, tint = Color(0xFF7FD3A3), modifier = Modifier.size(14.dp))
                        Text(vm.streakText, color = Color(0xFF7FD3A3), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    vm.myTeams.forEach { team ->
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(10.dp))
                                .clickable { onNavTeam(team.id) }
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(team.sport.emoji, fontSize = 16.sp)
                            Column {
                                Text(team.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.11).sp)
                                Text("${team.record} · #${team.rank}", color = Color.White.copy(alpha = 0.55f),
                                    fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
        }

        // ─── SECTION: Up Next ─── //
        SectionHeader("Up next", action = "See all", onAction = onNavSchedule)
        Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            when {
                vm.isLoading -> CircularProgressIndicator(
                    color = OrangeRIT,
                    modifier = Modifier.size(24.dp).align(Alignment.CenterHorizontally),
                )
                vm.upcomingGames.isEmpty() -> Text("No upcoming games.", color = Ink4, fontSize = 14.sp)
                else -> vm.upcomingGames.forEach { game -> GameRow(game) { onNavGame(game.id) } }
            }
        }

        // ─── SECTION: Quick Actions ─── //
        Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp)) {
            EyebrowLabel("QUICK ACTIONS")
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                when (user.role) {
                    UserRole.Admin -> QuickActionCard(Icons.Default.CheckCircle, "Approve Teams", "3 pending", Modifier.weight(1f)) { onNavSchedule() }
                    UserRole.Captain -> QuickActionCard(Icons.Default.Group, "Manage Roster", vm.myTeams.firstOrNull()?.name ?: "My Team", Modifier.weight(1f)) {
                        vm.myTeams.firstOrNull()?.let { onNavTeam(it.id) }
                    }
                    UserRole.Student -> QuickActionCard(Icons.Default.CheckCircle, "Check In", "Tonight at 7pm", Modifier.weight(1f)) {
                        vm.upcomingGames.firstOrNull()?.let { onNavGame(it.id) }
                    }
                }
                QuickActionCard(Icons.Default.EmojiEvents, "Standings", "See Where You Rank", Modifier.weight(1f)) { onNavStandings() }
            }
        }

        // ─── SECTION: Recent Results ─── //
        SectionHeader("Recent results")
        Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            when {
                vm.recentGames.isEmpty() && !vm.isLoading -> Text("No recent games.", color = Ink4, fontSize = 14.sp)
                else -> vm.recentGames.forEach { game -> GameRow(game) { onNavGame(game.id) } }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}//HomeScreen

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    label: String,
    sub: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, Line, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(OrangeTint),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = OrangeDark, modifier = Modifier.size(16.dp))
        }
        Column {
            Text(label, fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = (-0.13).sp)
            Text(sub, color = Ink4, fontSize = 14.sp)
        }
    }
}//QuickActionCard
