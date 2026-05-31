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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bazovic.balsa.intrahub.data.*
import com.bazovic.balsa.intrahub.ui.components.*
import com.bazovic.balsa.intrahub.ui.theme.*
import com.bazovic.balsa.intrahub.viewmodel.GameDetailViewModel

private enum class RsvpOption(val label: String, val icon: ImageVector, val dbValue: String) {
    Yes("I'm in", Icons.Default.Check, "yes"),
    Maybe("Maybe", Icons.Default.Schedule, "maybe"),
    No("Can't", Icons.Default.Close, "no"),
}

@Composable
fun GameDetailScreen(
    gameId: String,
    user: UserProfile,
    onBack: () -> Unit,
    vm: GameDetailViewModel = viewModel(),
) {
    LaunchedEffect(gameId) { vm.load(gameId) }

    val game = vm.game
    val myTeam = vm.myTeam
    val isFinal = game?.status == GameStatus.Final

    val rsvpOption = when (vm.rsvp) {
        "maybe" -> RsvpOption.Maybe
        "no" -> RsvpOption.No
        else -> RsvpOption.Yes
    }

    Column(modifier = Modifier.fillMaxSize().background(Canvas)) {
        // ─── SECTION: Nav Header ─── //
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Canvas)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .border(1.dp, Line, RoundedCornerShape(10.dp)),
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, modifier = Modifier.size(20.dp))
            }
            Text(
                "GAME · ${gameId.uppercase()}",
                color = Ink4,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.2.sp,
            )
            Spacer(Modifier.size(36.dp))
        }

        // ─── SECTION: Loading / Error ─── //
        if (vm.isLoading || game == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (vm.errorMessage != null) {
                    Text(vm.errorMessage!!, color = Loss, fontSize = 14.sp, textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp))
                } else {
                    CircularProgressIndicator(color = OrangeRIT)
                }
            }
            return@Column
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
        ) {
            // ─── SECTION: Scoreboard Card ─── //
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Line, RoundedCornerShape(20.dp))
                    .padding(20.dp),
            ) {
                Column {
                    Text(
                        "${game.sport.displayName.uppercase()} · ${myTeam?.league?.uppercase() ?: "OPEN LEAGUE"}",
                        color = game.sport.accentColor,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // ─── My team (left) ─── //
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Avatar(myTeam?.name ?: "Home", size = 56.dp)
                            Spacer(Modifier.height(8.dp))
                            Text(myTeam?.name ?: "Home", fontWeight = FontWeight.Bold, fontSize = 16.sp,
                                letterSpacing = (-0.14).sp, textAlign = TextAlign.Center)
                            Text(myTeam?.record ?: "—", color = Ink4, fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace, letterSpacing = 0.4.sp)
                        }

                        // ─── Score / VS ─── //
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            if (isFinal) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        "${game.myScore}",
                                        color = if (game.result == GameResult.Win) Win else Ink,
                                        fontSize = 40.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = (-1.52).sp,
                                        lineHeight = 40.sp,
                                    )
                                    Text("·", color = Ink5, fontSize = 40.sp, fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(horizontal = 4.dp), lineHeight = 40.sp)
                                    Text(
                                        "${game.oppScore}",
                                        color = if (game.result == GameResult.Win) Ink4 else Ink,
                                        fontSize = 40.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = (-1.52).sp,
                                        lineHeight = 40.sp,
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                ScoreChip(game.status, game.result)
                            } else {
                                Text("VS", color = Ink4, fontSize = 24.sp, fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.44).sp)
                            }
                        }

                        // ─── Opponent (right) ─── //
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Avatar(game.oppName, size = 56.dp)
                            Spacer(Modifier.height(8.dp))
                            Text(game.oppName, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                                letterSpacing = (-0.14).sp, textAlign = TextAlign.Center,
                                maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }

            // ─── SECTION: Game Info ─── //
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .border(1.dp, Line, RoundedCornerShape(14.dp)),
            ) {
                Column {
                    InfoRow(
                        icon = { Icon(Icons.Default.CalendarMonth, null, tint = Ink3, modifier = Modifier.size(16.dp)) },
                        label = "WHEN",
                        value = "${formatDateShort(game.whenMs)} · ${formatTime(game.whenMs)}",
                    )
                    HorizontalDivider(color = Line2)
                    InfoRow(
                        icon = { Icon(Icons.Default.PinDrop, null, tint = Ink3, modifier = Modifier.size(16.dp)) },
                        label = "WHERE",
                        value = game.venue,
                    )
                    HorizontalDivider(color = Line2)
                    InfoRow(
                        icon = { Icon(Icons.Default.Sports, null, tint = Ink3, modifier = Modifier.size(16.dp)) },
                        label = "REFEREE",
                        value = game.referee ?: "TBD",
                        last = true,
                    )
                }
            }

            // ─── SECTION: Rsvp ─── //
            if (!isFinal && game.myTeamId != null) {
                Column {
                    EyebrowLabel("YOUR RSVP")
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        RsvpOption.entries.forEach { opt ->
                            val active = rsvpOption == opt
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (active) OrangeTint else Color.White)
                                    .border(1.5.dp, if (active) OrangeRIT else Line, RoundedCornerShape(14.dp))
                                    .clickable { vm.updateRsvp(opt.dbValue) }
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(opt.icon, null, tint = if (active) OrangeDark else Ink3, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(opt.label, color = if (active) OrangeDark else Ink3,
                                    fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }

            // ─── SECTION: Attendance ─── //
            if (!isFinal && game.myTeamId != null) {
                val confirmed = vm.roster.count { true } // all who RSVP'd yes — simplified
                val total = vm.roster.size
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.dp, Line, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Group, null, tint = OrangeRIT, modifier = Modifier.size(18.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${confirmed} of ${total} on roster", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Need at least 5 to play.", color = Ink4, fontSize = 14.sp)
                    }
                    Row {
                        vm.roster.take(4).forEachIndexed { i, player ->
                            Box(modifier = Modifier.offset(x = (-8 * i).dp)) {
                                Avatar(player.name, size = 26.dp)
                            }
                        }
                    }
                }
            }

            // ─── SECTION: Box Score ─── //
            if (isFinal) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.dp, Line, RoundedCornerShape(14.dp))
                        .padding(16.dp),
                ) {
                    Column {
                        EyebrowLabel("BOX SCORE")
                        Spacer(Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Spacer(Modifier.weight(1.4f))
                            listOf("Q1", "Q2", "Q3", "Q4", "TOT").forEachIndexed { i, label ->
                                Text(
                                    label,
                                    color = Ink5,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.8.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.width(if (i == 4) 48.dp else 36.dp),
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        // Quarter breakdown not yet in schema — displayed as placeholders
                        BoxScoreRow(myTeam?.name ?: "Us", listOf(0, 0, 0, 0), game.myScore, mine = true)
                        Spacer(Modifier.height(8.dp))
                        BoxScoreRow(game.oppName, listOf(0, 0, 0, 0), game.oppScore, mine = false)
                    }
                }
            }
        }
    }
}//GameDetailScreen

@Composable
private fun BoxScoreRow(teamName: String, quarters: List<Int>, total: Int, mine: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            teamName,
            fontWeight = if (mine) FontWeight.Bold else FontWeight.SemiBold,
            color = if (mine) Ink else Ink3,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1.4f),
        )
        quarters.forEach { s ->
            Text("$s", color = Ink3, fontSize = 16.sp, fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center, modifier = Modifier.width(36.dp))
        }
        Text(
            "$total",
            color = if (mine) OrangeRIT else Ink,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.End,
            modifier = Modifier.width(48.dp),
        )
    }
}//BoxScoreRow
