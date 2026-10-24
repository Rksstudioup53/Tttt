package com.hindivideo.translator.pipeline

object SubtitleParser {

    fun parseSrt(content: String): List<SubtitleCue> {
        val result = mutableListOf<SubtitleCue>()
        val blocks = content.replace("\r\n", "\n").replace("\r", "\n").split("\n\s*\n".toRegex())
        var idCounter = 1

        for (block in blocks) {
            val lines = block.trim().lines()
            if (lines.size < 2) continue

            val timeIndex = if (lines[0].contains("-->")) 0 else 1
            if (timeIndex >= lines.size || !lines[timeIndex].contains("-->")) continue

            val parts = lines[timeIndex].split("-->")
            val startTime = parseTimestamp(parts[0].trim())
            val endTime = parseTimestamp(parts[1].trim())

            val text = lines.subList(timeIndex + 1, lines.size).joinToString("\n").trim()
            if (text.isNotBlank()) {
                result.add(
                    SubtitleCue(
                        id = idCounter++,
                        startTime = startTime,
                        endTime = endTime,
                        hindiText = text
                    )
                )
            }
        }
        return result
    }

    private fun parseTimestamp(timeString: String): Float {
        // Format: 00:01:23,456 or 00:01:23.456
        val clean = timeString.replace(',', '.')
        val parts = clean.split(':')
        return when (parts.size) {
            3 -> parts[0].toFloat() * 3600f + parts[1].toFloat() * 60f + parts[2].toFloat()
            2 -> parts[0].toFloat() * 60f + parts[1].toFloat()
            else -> clean.toFloatOrNull() ?: 0f
        }
    }
}