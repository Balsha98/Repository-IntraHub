package com.bazovic.balsa.intrahub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.bazovic.balsa.intrahub.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    user: UserProfile,
    onLogout: () -> Unit,
    onNavTeam: (String) -> Unit,
    vm: ProfileViewModel = viewModel(),
) {
    val myTeams = vm.myTeams

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Canvas)
            .verticalScroll(rememberScrollState()),
    ) {
        // ─── SECTION: Orange Banner ─── //
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(OrangeRIT, Color(0xFFE04E00))))
                .padding(horizontal = 20.dp, vertical = 14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .offset(x = 270.dp, y = (-30).dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.08f))
            )
            Text(
                "MY PROFILE",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp,
            )
        }

        // ─── SECTION: Profile Card ─── //
        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .border(1.dp, Line, RoundedCornerShape(20.dp)),
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp),
                ) {
                    Box {
                        Avatar(user.name, size = 64.dp)
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(Win)
                                .border(3.dp, Color.White, CircleShape)
                                .align(Alignment.BottomEnd)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(user.name, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, letterSpacing = (-0.36).sp)
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            RoleBadge(user.role)
                            Text(user.ritId, color = Ink4, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text("${user.major} · ${user.year}", color = Ink4, fontSize = 14.sp)
                    }
                }

                // ─── Stats strip ─── //
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Canvas)
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    StatTile("GAMES", "${vm.gamesPlayed}")
                    VerticalDivider(modifier = Modifier.height(32.dp), color = Line)
                    StatTile("WINS", "${vm.wins}", accent = Win)
                    VerticalDivider(modifier = Modifier.height(32.dp), color = Line)
                    StatTile("TEAMS", "${vm.myTeams.size}", accent = OrangeRIT)
                }
            }
        }

        // ─── SECTION: My Teams ─── //
        SectionHeader("My teams · ${myTeams.size}")
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            myTeams.forEach { team ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.dp, Line, RoundedCornerShape(14.dp))
                        .clickable { onNavTeam(team.id) }
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SportIconBox(team.sport, size = 44.dp)
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(team.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            if (team.captainId == user.id) {
                                Icon(Icons.Default.Star, null, tint = OrangeRIT, modifier = Modifier.size(12.dp))
                            }
                        }
                        Text(
                            "${team.league.uppercase()} · ${team.record} · #${team.rank} IN LEAGUE",
                            color = Ink4,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.4.sp,
                        )
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Ink5, modifier = Modifier.size(18.dp))
                }
            }
        }

        // ─── SECTION: Season Performance ─── //
        SectionHeader("Season performance")
        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .border(1.dp, Line, RoundedCornerShape(14.dp))
                .padding(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PerfRow("Avg points / game", "14.2", sportEmoji = Sport.Basketball.emoji)
                PerfRow("Hit rate (volleyball)", "62%", sportEmoji = Sport.Volleyball.emoji)
                PerfRow("Sportsmanship rating", "4.8 / 5", star = true)
                PerfRow("Attendance", "100%", highlight = true)
            }
        }

        // ─── SECTION: Settings ─── //
        SectionHeader("Settings")
        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .border(1.dp, Line, RoundedCornerShape(14.dp)),
        ) {
            Column {
                SettingRow(Icons.Default.Person, "Account", "Email, password, RIT ID")
                HorizontalDivider(color = Line2)
                SettingRow(Icons.Default.Group, "Sport preferences", "Basketball, Volleyball")
                HorizontalDivider(color = Line2)
                SettingRow(Icons.Default.Notifications, "Notifications", badge = "3 new")
                HorizontalDivider(color = Line2)
                SettingRow(Icons.Default.CalendarMonth, "Calendar sync", right = "Google")
                HorizontalDivider(color = Line2)
                SettingRow(Icons.Default.Language, "Language", right = "English")
                HorizontalDivider(color = Line2)
                SettingRow(Icons.Default.Shield, "Privacy & visibility")
                HorizontalDivider(color = Line2)
                SettingRow(Icons.Default.Flag, "Report an issue", isLast = true)
            }
        }

        Spacer(Modifier.height(14.dp))

        // ─── SECTION: Sign Out ─── //
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(52.dp),
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Loss),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, LossTint),
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Sign Out", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        }

        Spacer(Modifier.height(16.dp))
        Text(
            "INTRAHUB v1.0 · ROCHESTER INSTITUTE OF TECHNOLOGY",
            color = Ink5,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.0.sp,
            modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally),
        )
        Spacer(Modifier.height(24.dp))
    }
}//ProfileScreen

@Composable
private fun StatTile(label: String, value: String, accent: Color = Ink) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = accent, fontSize = 24.sp, fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace, letterSpacing = (-0.44).sp, lineHeight = 24.sp)
        Spacer(Modifier.height(6.dp))
        Text(label, color = Ink4, fontSize = 12.sp, fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp)
    }
}//StatTile

@Composable
private fun PerfRow(label: String, value: String, sportEmoji: String? = null, star: Boolean = false, highlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (sportEmoji != null) Text(sportEmoji, fontSize = 14.sp)
            if (star) Icon(Icons.Default.Star, null, tint = OrangeRIT, modifier = Modifier.size(14.dp))
            Text(label, color = Ink3, fontSize = 16.sp)
        }
        Text(value, color = if (highlight) Win else Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace)
    }
}//PerfRow

@Composable
private fun SettingRow(
    icon: ImageVector,
    label: String,
    sub: String? = null,
    badge: String? = null,
    right: String? = null,
    isLast: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {}
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(Line2),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, null, tint = Ink3, modifier = Modifier.size(16.dp)) }
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            if (sub != null) Text(sub, color = Ink4, fontSize = 14.sp)
        }
        if (badge != null) {
            Box(
                modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(OrangeRIT).padding(horizontal = 8.dp, vertical = 3.dp),
            ) { Text(badge, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
        }
        if (right != null) {
            Text(right, color = Ink4, fontSize = 14.sp)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Ink5, modifier = Modifier.size(16.dp))
    }
}//SettingRow
