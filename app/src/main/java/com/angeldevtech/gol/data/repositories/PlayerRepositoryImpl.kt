package com.angeldevtech.gol.data.repositories

import com.angeldevtech.gol.domain.repositories.PlayerRepository
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class PlayerRepositoryImpl @Inject constructor() : PlayerRepository {

    private val m3u8Regex = Regex("""https?://[^\s"']+\.m3u8(\?[^\s"']*)?""")

    override suspend fun extractM3U8Url(webPlayerUrl: String): Result<String> = withContext(Dispatchers.IO) {
        try {

            if (webPlayerUrl.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("¡Ups! La fuente no tiene una url válida"))
            }

            val doc = Jsoup.connect(webPlayerUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.212 Safari/537.36")
                .timeout(15000)
                .get()

            val scripts = doc.select("script")
            for (script in scripts) {
                val scriptData = script.data()
                if (scriptData.isNotEmpty()) {
                    m3u8Regex.find(scriptData)?.let {
                        return@withContext Result.success(it.value)
                    }
                }
                val scriptHtml = script.html()
                if(scriptHtml.isNotEmpty()){
                    m3u8Regex.find(scriptHtml)?.let {
                        return@withContext Result.success(it.value)
                    }
                }
            }

            val bodyHtml = doc.body().html()
            m3u8Regex.find(bodyHtml)?.let {
                return@withContext Result.success(it.value)
            }


            Result.failure(RuntimeException("¡Ups! No se pudo obtener la url de la fuente"))

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}