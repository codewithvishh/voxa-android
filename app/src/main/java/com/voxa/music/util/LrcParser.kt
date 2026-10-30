package com.voxa.music.util

data class LyricLine(
    val timeMs: Long,
    val text: String
)

object LrcParser {
    private val LRC_REGEX = Regex("""\[(\d{2}):(\d{2})(?:\.(\d{2,3}))?](.*)""")

    fun parse(lrcContent: String?): List<LyricLine> {
        if (lrcContent.isNullOrBlank()) return emptyList()

        val lines = mutableListOf<LyricLine>()

        lrcContent.lines().forEach { line ->
            val match = LRC_REGEX.find(line.trim())
            if (match != null) {
                val (minStr, secStr, fracStr, text) = match.destructured
                val minutes = minStr.toLongOrNull() ?: 0L
                val seconds = secStr.toLongOrNull() ?: 0L
                val fraction = when (fracStr.length) {
                    2 -> (fracStr.toLongOrNull() ?: 0L) * 10
                    3 -> fracStr.toLongOrNull() ?: 0L
                    else -> 0L
                }

                val totalMs = (minutes * 60 * 1000) + (seconds * 1000) + fraction
                if (text.isNotBlank()) {
                    lines.add(LyricLine(timeMs = totalMs, text = text.trim()))
                }
            }
        }

        return lines.sortedBy { it.timeMs }
    }

    fun getActiveIndex(lines: List<LyricLine>, currentMs: Long): Int {
        if (lines.isEmpty()) return -1
        for (i in lines.indices.reversed()) {
            if (currentMs >= lines[i].timeMs) {
                return i
            }
        }
        return 0
    }
}
