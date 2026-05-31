package com.bazovic.balsa.intrahub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.bazovic.balsa.intrahub.viewmodel.StandingsViewModel

@Composable
fun StandingsScreen(
    onNavTeam: (String) -> Unit,
    vm: StandingsViewModel = viewModel(),
) {
    val rows = vm.standings[vm.selectedSport] ?: emptyList()

    Column(modifier = Modifier.fillMaxSize().background(Canvas)) {
        // ─── SECTION: Screen Header ─── //
        Row(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 8.dp),
        ) {
            Column {
                EyebrowLabel("LEAGUE TABLES")
                Text("Standings", fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, letterSpacing = (-0.84).sp)
            }
        }

        // ─── SECTION: Sport Tabs ─── //
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Sport.entries.forEach { sport ->
                SportTabChip(sport, vm.selectedSport == sport) { vm.selectedSport = sport }
            }
        }

        // ─── SECTION: Table ─── //
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
        ) {
            // Column headers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Text("#", color = Ink5, fontSize = 12.sp, fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(28.dp))
                Text("TEAM", color = Ink5, fontSize = 12.sp, fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Text("W", color = Ink5, fontSize = 12.sp, fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                Text("L", color = Ink5, fontSize = 12.sp, fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center)
                Text("DIFF", color = Ink5, fontSize = 12.sp, fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(56.dp), textAlign = TextAlign.End)
            }

            // Table rows
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .border(1.dp, Line, RoundedCornerShape(14.dp)),
            ) {
                Column {
                    rows.forEachIndexed { i, row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (row.isMine) OrangeTint2 else Color.White)
                                .then(if (row.isMine) Modifier.clickable { onNavTeam(row.teamId) } else Modifier)
                                .padding(horizontal = 12.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // ─── Rank ─── //
                            Row(
                                modifier = Modifier.width(28.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                if (row.rank <= 3) {
                                    Box(modifier = Modifier.width(4.dp).height(12.dp).clip(RoundedCornerShape(1.dp)).background(OrangeRIT))
                                }
                                Text(
                                    "${row.rank}",
                                    color = if (row.rank <= 3) OrangeRIT else Ink4,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                )
                            }

                            // ─── Team name + avatar ─── //
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Avatar(row.teamName, size = 28.dp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        row.teamName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.13).sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    if (row.isMine) {
                                        Text(
                                            "YOUR TEAM",
                                            color = OrangeDark,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            letterSpacing = 1.sp,
                                        )
                                    }
                                }
                            }

                            // ─── W ─── //
                            Text("${row.wins}", fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace, modifier = Modifier.width(36.dp),
                                textAlign = TextAlign.Center)
                            // ─── L ─── //
                            Text("${row.losses}", color = Ink4, fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace, modifier = Modifier.width(36.dp),
                                textAlign = TextAlign.Center)
                            // ─── Diff ─── //
                            val diff = row.diff
                            Text(
                                text = "${if (diff > 0) "+" else ""}$diff",
                                color = when {
                                    diff > 0 -> Win
                                    diff < 0 -> Loss
                                    else -> Ink4
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.width(56.dp),
                                textAlign = TextAlign.End,
                            )
                        }

                        if (i < rows.size - 1) {
                            HorizontalDivider(color = Line2, thickness = 1.dp)
                        }
                    }
                }
            }

            // ─── SECTION: Legend ─── //
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Line2)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(Icons.Default.EmojiEvents, null, tint = OrangeRIT, modifier = Modifier.size(14.dp).padding(top = 1.dp))
                Text(
                    "Top 4 in each league advance to playoffs. Tiebreakers: head-to-head, then point differential.",
                    color = Ink3,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}//StandingsScreen
