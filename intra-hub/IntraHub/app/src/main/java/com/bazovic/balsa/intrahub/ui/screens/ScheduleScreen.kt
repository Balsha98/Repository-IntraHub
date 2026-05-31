package com.bazovic.balsa.intrahub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bazovic.balsa.intrahub.data.*
import com.bazovic.balsa.intrahub.ui.components.*
import com.bazovic.balsa.intrahub.ui.theme.*
import com.bazovic.balsa.intrahub.viewmodel.ScheduleFilter
import com.bazovic.balsa.intrahub.viewmodel.ScheduleViewModel

@Composable
fun ScheduleScreen(
    user: UserProfile,
    onNavGame: (String) -> Unit,
    vm: ScheduleViewModel = viewModel(),
) {
    val filtered = vm.allGames.filter { g ->
        when (vm.filter) {
            ScheduleFilter.Mine -> g.myTeamId != null
            ScheduleFilter.Upcoming -> g.status == GameStatus.Upcoming
            ScheduleFilter.Past -> g.status == GameStatus.Final
            ScheduleFilter.All -> true
        } &&
            (vm.sportFilter == null || g.sport == vm.sportFilter) &&
            (vm.search.isBlank() ||
                g.oppName.contains(vm.search, ignoreCase = true) ||
                g.venue.contains(vm.search, ignoreCase = true))
    }.sortedBy { it.whenMs }

    val grouped = filtered.groupBy { relativeDay(it.whenMs) }

    Column(modifier = Modifier.fillMaxSize().background(Canvas)) {
        // ─── SECTION: Screen Header ─── //
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                EyebrowLabel("SPRING '26 SEASON")
                Text("Schedule", fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, letterSpacing = (-0.84).sp)
            }
            if (user.role == UserRole.Admin) {
                Button(
                    onClick = {},
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeRIT),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Game", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // ─── SECTION: Search Bar ─── //
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = vm.search,
                onValueChange = { vm.search = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search opponents, venues…", color = Ink5, fontSize = 16.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Ink5, modifier = Modifier.size(16.dp)) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangeRIT,
                    unfocusedBorderColor = Line,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
            )
            IconButton(
                onClick = { vm.showSportPanel = !vm.showSportPanel },
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (vm.showSportPanel) Ink else Color.White),
            ) {
                Icon(Icons.Default.FilterList, null, tint = if (vm.showSportPanel) Color.White else Ink3)
            }
        }

        // ─── SECTION: Filter Chips ─── //
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            listOf(
                ScheduleFilter.All to "All Games",
                ScheduleFilter.Mine to "My Teams",
                ScheduleFilter.Upcoming to "Upcoming",
                ScheduleFilter.Past to "Final",
            ).forEach { (f, label) ->
                FilterChip(label, vm.filter == f, { vm.filter = f })
            }
        }

        // ─── SECTION: Sport Filter Panel ─── //
        if (vm.showSportPanel) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp).padding(top = 8.dp)) {
                EyebrowLabel("SPORT")
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip("All", vm.sportFilter == null, { vm.sportFilter = null }, small = true)
                    Sport.entries.forEach { sport ->
                        FilterChip(sport.displayName, vm.sportFilter == sport, { vm.sportFilter = sport }, small = true)
                    }
                }
            }
        }

        // ─── SECTION: Grouped Game List ─── //
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp),
        ) {
            if (grouped.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Default.CalendarMonth, null, tint = Ink5, modifier = Modifier.size(32.dp))
                    Text("No games match these filters", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text("Try clearing search or sport.", color = Ink4, fontSize = 14.sp)
                }
            } else {
                grouped.forEach { (day, games) ->
                    Row(
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Text(day, fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = (-0.13).sp)
                        Text(
                            "${games.size} GAME${if (games.size == 1) "" else "S"}",
                            color = Ink5,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.8.sp,
                        )
                    }
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        games.forEach { game -> GameRow(game) { onNavGame(game.id) } }
                    }
                }
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("END OF SCHEDULE · PLAYOFFS START MAY 4", color = Ink5,
                        fontSize = 12.sp, fontFamily = FontFamily.Monospace, letterSpacing = 1.2.sp)
                }
            }
        }
    }
}//ScheduleScreen
