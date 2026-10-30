package com.voxa.music.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voxa.music.ui.theme.*
import com.voxa.music.util.LrcParser

@Composable
fun LiveLyricsRibbon(
    lyricsLrc: String?,
    currentPositionMs: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (lyricsLrc.isNullOrBlank()) return

    val parsedLines = remember(lyricsLrc) { LrcParser.parse(lyricsLrc) }
    if (parsedLines.isEmpty()) return

    val activeIndex = remember(currentPositionMs, parsedLines) {
        LrcParser.getActiveIndex(parsedLines, currentPositionMs)
    }
    val currentLine = if (activeIndex in parsedLines.indices) parsedLines[activeIndex].text else ""

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = VoxaSurfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Lyrics",
                tint = VoxaPrimary,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            AnimatedContent(
                targetState = currentLine,
                label = "lyric_transition",
                modifier = Modifier.weight(1f)
            ) { targetText ->
                Text(
                    text = if (targetText.isNotBlank()) "“$targetText”" else "Listening for lyrics...",
                    color = VoxaTextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = "LYRICS",
                color = VoxaPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 6.dp)
            )
        }
    }
}
