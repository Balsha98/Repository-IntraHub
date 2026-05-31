package com.bazovic.balsa.intrahub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bazovic.balsa.intrahub.data.*
import com.bazovic.balsa.intrahub.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

// ─── SECTION: Avatar ─── //
fun initialsOf(name: String): String =
    name.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")

@Composable
fun Avatar(name: String, size: Dp = 40.dp) {
    val bg = avatarColorFor(name)
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initialsOf(name),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.36f).sp,
            letterSpacing = (-0.5).sp,
        )
    }
}//Avatar

// ─── SECTION: Role Badge ─── //
@Composable
fun RoleBadge(role: UserRole) {
    val (bg, fg) = when (role) {
        UserRole.Student -> Pair(Color(0xFFE8EBF0), Color(0xFF3A4A6B))
        UserRole.Captain -> Pair(OrangeTint, OrangeDark)
        UserRole.Admin -> Pair(Ink, Color.White)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 9.dp, vertical = 4.dp),
    ) {
        Text(
            text = role.name.uppercase(),
            color = fg,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.8.sp,
        )
    }
}//RoleBadge

// ─── SECTION: Score Chip ─── //
@Composable
fun ScoreChip(status: GameStatus, result: GameResult) {
    val (bg, fg, label) = when {
        status == GameStatus.Final && result == GameResult.Win -> Triple(WinTint, Win, "W · FINAL")
        status == GameStatus.Final && result == GameResult.Loss -> Triple(LossTint, Loss, "L · FINAL")
        status == GameStatus.Final && result == GameResult.Tie -> Triple(TieTint, Tie, "T · FINAL")
        else -> Triple(Line2, Ink4, "UPCOMING")
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = label,
            color = fg,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.4.sp,
        )
    }
}//ScoreChip

// ─── SECTION: Sport Icon Box ─── //
@Composable
fun SportIconBox(sport: Sport, size: Dp = 44.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.27f))
            .background(sport.tintColor),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = sport.emoji,
            fontSize = (size.value * 0.5f).sp,
        )
    }
}//SportIconBox

// ─── SECTION: Filter Chip ─── //
@Composable
fun FilterChip(
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    small: Boolean = false,
) {
    val (bg, fg) = if (active) Pair(Ink, Color.White) else Pair(Line2, Ink3)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(
                horizontal = if (small) 9.dp else 12.dp,
                vertical = if (small) 4.dp else 6.dp,
            ),
    ) {
        Text(
            text = label,
            color = fg,
            fontSize = if (small) 12.sp else 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}//FilterChip

// ─── SECTION: Sport Tab Chip ─── //
@Composable
fun SportTabChip(sport: Sport, active: Boolean, onClick: () -> Unit) {
    val bg = if (active) sport.tintColor else Color.White
    val fg = if (active) sport.accentColor else Ink3
    val border = if (active) sport.accentColor else Line
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .border(1.5.dp, border, RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = sport.emoji, fontSize = 16.sp)
        Text(
            text = sport.displayName,
            color = fg,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}//SportTabChip

// ─── SECTION: Section Header ─── //
@Composable
fun SectionHeader(
    title: String,
    action: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, letterSpacing = (-0.3).sp)
        if (action != null && onAction != null) {
            Row(
                modifier = Modifier.clickable(onClick = onAction),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(action, color = OrangeRIT, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = OrangeRIT,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}//SectionHeader

// ─── SECTION: Eyebrow Label ─── //
@Composable
fun EyebrowLabel(text: String, modifier: Modifier = Modifier, color: Color = Ink4) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 1.2.sp,
    )
}//EyebrowLabel

// ─── SECTION: Info Row ─── //
@Composable
fun InfoRow(
    icon: @Composable () -> Unit,
    label: String,
    value: String,
    last: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Line2),
            contentAlignment = Alignment.Center,
        ) { icon() }
        Column(modifier = Modifier.weight(1f)) {
            EyebrowLabel(label.uppercase())
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}//InfoRow

// ─── SECTION: Game Row ─── //
@Composable
fun GameRow(game: Game, onClick: () -> Unit) {
    val isFinal = game.status == GameStatus.Final
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, Line, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        SportIconBox(game.sport, size = 40.dp)

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            if (isFinal) {
                ScoreChip(game.status, game.result)
                Text(
                    text = "${relativeDay(game.whenMs).uppercase()} · ${formatTime(game.whenMs)}",
                    color = Ink4,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.4.sp,
                )
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ScoreChip(game.status, game.result)
                    Text(
                        text = "${relativeDay(game.whenMs).uppercase()} · ${formatTime(game.whenMs)}",
                        color = Ink4,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.4.sp,
                    )
                }
            }
            Text(
                text = "vs ${game.oppName}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                letterSpacing = (-0.14).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = game.venue,
                color = Ink4,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (isFinal) {
            Text(
                text = "${game.myScore}–${game.oppScore}",
                color = if (game.result == GameResult.Win) Win else Loss,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = (-0.36).sp,
                textAlign = TextAlign.End,
                modifier = Modifier.align(Alignment.Bottom),
            )
        } else {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Ink5,
                modifier = Modifier.size(18.dp).align(Alignment.CenterVertically),
            )
        }
    }
}//GameRow

// ─── SECTION: Team Row ─── //
@Composable
fun TeamRow(team: Team, mine: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (mine) OrangeTint2 else Color.White)
            .border(1.dp, if (mine) OrangeTint else Line, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SportIconBox(team.sport, size = 40.dp)
        Column(modifier = Modifier.weight(1f)) {
            Text(team.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = (-0.14).sp)
            Text(
                text = "${team.league.uppercase()} · ${team.record} · #${team.rank}",
                color = Ink4,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.4.sp,
            )
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Ink5, modifier = Modifier.size(18.dp))
    }
}//TeamRow

// ─── SECTION: Date / Time Helpers ─── //
private val REF_NOW_MS: Long = run {
    val cal = Calendar.getInstance()
    cal.set(2026, Calendar.APRIL, 27, 0, 0, 0)
    cal.set(Calendar.MILLISECOND, 0)
    cal.timeInMillis
}

fun relativeDay(ms: Long): String {
    val todayMs = REF_NOW_MS
    val targetMs = run {
        val c = Calendar.getInstance().apply { timeInMillis = ms }
        c.set(Calendar.HOUR_OF_DAY, 0); c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0); c.set(Calendar.MILLISECOND, 0)
        c.timeInMillis
    }
    return when (val diffDays = ((targetMs - todayMs) / 86_400_000L).toInt()) {
        0 -> "Today"
        1 -> "Tomorrow"
        -1 -> "Yesterday"
        in Int.MIN_VALUE..-2 -> "${-diffDays}d ago"
        else -> SimpleDateFormat("EEE, MMM d", Locale.US).format(Date(ms))
    }
}

fun formatTime(ms: Long): String = SimpleDateFormat("h:mm a", Locale.US).format(Date(ms))

fun formatDateShort(ms: Long): String = SimpleDateFormat("EEE, MMM d", Locale.US).format(Date(ms)).uppercase()
