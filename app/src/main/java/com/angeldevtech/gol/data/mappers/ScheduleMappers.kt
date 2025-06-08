package com.angeldevtech.gol.data.mappers

import android.util.Base64
import com.angeldevtech.gol.BuildConfig
import com.angeldevtech.gol.data.models.ScheduleItemDto
import com.angeldevtech.gol.domain.models.Embed
import com.angeldevtech.gol.domain.models.ScheduleItem
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun ScheduleItemDto.toSimplified(): ScheduleItem? {
    if (id == null) return null

    val validEmbeds = attributes.embeds.data.mapNotNull { embed ->
        val decodedUrl = decodeEmbedUrl(embed.attributes.embed_iframe)
        decodedUrl?.let {
            Embed(
                name = embed.attributes.embed_name,
                language = embed.attributes.idioma,
                url = it
            )
        }
    }

    if (validEmbeds.isEmpty()) return null

    val (localDate, localTime) = processApiDateTime(attributes.date_diary, attributes.diary_hour)

    val fullImageUrl = attributes.country.data.attributes.image.data.attributes.url.takeIf { it.isNotBlank() }?.let { BuildConfig.IMG_BASE_URL + it } ?: ""

    return ScheduleItem(
        id = id,
        hour = localTime,
        name = attributes.diary_description.trim(),
        date = localDate,
        relativeDay = localDate,
        category = attributes.deportes.trim(),
        embeds = validEmbeds,
        leagueName = attributes.country.data.attributes.name.trim(),
        leagueImageUrl = fullImageUrl
    )
}

fun decodeEmbedUrl(iframeUrl: String): String? {
    if (iframeUrl.isBlank()) return null

    val base64Encoded = iframeUrl
        .substringAfter("r=", "")
        .replace("//n", "")
        .replace("/n", "")

    if (base64Encoded.isBlank()) return null

    return try {
        val decodedBytes = Base64.decode(base64Encoded, Base64.DEFAULT)
        val decoded = String(decodedBytes)
        if ("?get=" in decoded || "drm.php" in decoded) {
            null
        } else {
            decoded
        }
    } catch (e: Exception) {
        null
    }
}

private fun processApiDateTime(dateStr: String, timeStr: String): Pair<String, String> {
    if (dateStr == "Fecha desconocida" || timeStr == "Hora desconocida") return Pair(dateStr, timeStr)

    return try {
        val utcFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("America/Lima")
            isLenient = false
        }
        val combinedDateTime = "${dateStr.trim()} ${timeStr.trim()}"
        val utcDate = utcFormatter.parse(combinedDateTime) ?: return Pair(dateStr, timeStr)

        val localFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }

        val localDateTime = localFormatter.format(utcDate)
        val parts = localDateTime.split(" ")
        Pair(parts[0], parts[1])
    } catch (e: Exception) {
        Pair(dateStr, timeStr)
    }
}