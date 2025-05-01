package com.angeldevtech.gol.data.mappers

import android.util.Base64
import com.angeldevtech.gol.BuildConfig
import com.angeldevtech.gol.data.models.ScheduleItemDto
import com.angeldevtech.gol.domain.models.Embed
import com.angeldevtech.gol.domain.models.ScheduleItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

const val imgBaseUrl = BuildConfig.IMG_BASE_URL

fun ScheduleItemDto.toSimplified(): ScheduleItem? {
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

    val countryImageUrl = attributes.country.data.attributes.image.data.attributes.url.takeIf { it.isNotBlank() }
    val fullImageUrl = countryImageUrl?.let { imgBaseUrl + it }
        ?: ""

    return ScheduleItem(
        id = id,
        hour = attributes.diary_hour.trim(),
        name = attributes.diary_description.trim(),
        date = getRelativeDay(attributes.date_diary),
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

fun getRelativeDay(dateStr: String): String {
    return try {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val itemDate = formatter.parse(dateStr)
        val today = Calendar.getInstance()
        val itemCal = Calendar.getInstance().apply { time = itemDate!! }

        val diffDays = ((itemCal.timeInMillis - today.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

        when (diffDays) {
            0 -> "HOY"
            1 -> "MAÑANA"
            -1 -> "AYER"
            else -> dateStr
        }
    } catch (e: Exception) {
        dateStr
    }
}