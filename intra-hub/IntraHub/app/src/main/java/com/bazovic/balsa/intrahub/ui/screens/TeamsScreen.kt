package com.bazovic.balsa.intrahub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bazovic.balsa.intrahub.data.*
import com.bazovic.balsa.intrahub.ui.components.*
import com.bazovic.balsa.intrahub.ui.theme.*
import com.bazovic.balsa.intrahub.viewmodel.TeamsViewModel

@Composable
fun TeamsScreen(
    user: UserProfile,
    onNavTeam: (String) -> Unit,
    vm: TeamsViewModel = viewModel(),
) {
    val myTeams = vm.allTeams.filter { it.id in vm.myTeamIds }
    val filtered = if (vm.sportFilter == null) vm.allTeams else vm.allTeams.filter { it.sport == vm.sportFilter }

    Column(modifier = Modifier.fillMaxSize().background(Canvas)) {
        // ─── SECTION: Screen Header ─── //
        Row(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                EyebrowLabel("SPRING '26 · ${vm.allTeams.size} TEAMS")
                Text("Teams", fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, letterSpacing = (-0.84).sp)
            }
        }

        // ─── SECTION: Sport Filter Chips ─── //
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            FilterChip("All", vm.sportFilter == null, { vm.sportFilter = null })
            Sport.entries.forEach { sport ->
                FilterChip(sport.displayName, vm.sportFilter == sport, { vm.sportFilter = sport })
            }
        }

        // ─── SECTION: Team Lists ─── //
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
        ) {
            Text(
                "MY TEAMS",
                color = Ink4,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                myTeams.forEach { team ->
                    TeamRow(team, mine = true) { onNavTeam(team.id) }
                }
            }

            Spacer(Modifier.height(18.dp))

            Text(
                "ALL TEAMS · ${filtered.size}",
                color = Ink4,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                filtered.forEach { team ->
                    TeamRow(team) { onNavTeam(team.id) }
                }
            }
        }
    }
}//TeamsScreen
