package com.angeldevtech.gol.ui.screens.home

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.angeldevtech.gol.domain.usecases.GetScheduleCategoriesUseCase
import com.angeldevtech.gol.domain.usecases.RefreshScheduleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.core.graphics.scale
import androidx.palette.graphics.Palette
import com.angeldevtech.gol.domain.models.ScheduleCategories
import com.angeldevtech.gol.domain.models.ScheduleItem
import com.angeldevtech.gol.utils.PaletteResult
import com.angeldevtech.gol.utils.WhiteFilter
import com.angeldevtech.gol.utils.isLightColor
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.withContext
import java.time.LocalTime

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getScheduleCategories: GetScheduleCategoriesUseCase,
    private val refreshSchedule: RefreshScheduleUseCase
): ViewModel() {

    private val _uiState = MutableStateFlow<HomeUIState>(HomeUIState.Loading)
    val uiState: StateFlow<HomeUIState> = _uiState

    private val _paletteCache = mutableStateMapOf<String, PaletteResult>()
    val paletteCache: Map<String, PaletteResult> get() = _paletteCache

    private fun observeSchedule() {
        _uiState.value = HomeUIState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            val result = refreshSchedule()

            if (result.isFailure) {
                withContext(Dispatchers.Main) {
                    _uiState.value = HomeUIState.Error("¡Ups! Hubo un error al obtener los eventos")
                }
                return@launch
            }

            getScheduleCategories()
                .catch {
                    withContext(Dispatchers.Main) {
                        _uiState.value =
                            HomeUIState.Error("¡Ups! Hubo un error procesando las categorías")
                    }
                }
                .collect { scheduleResult ->
                    scheduleResult.fold(
                        onSuccess = { categories ->
                            val currentOrUpcomingEvents =
                                calculateCurrentOrUpcomingEvents(categories)
                            withContext(Dispatchers.Main) {
                                _uiState.value =
                                    HomeUIState.Success(categories, currentOrUpcomingEvents)
                            }
                        },
                        onFailure = {
                            withContext(Dispatchers.Main) {
                                _uiState.value =
                                    HomeUIState.Error("¡Ups! Hubo un error al obtener los eventos ordenados")
                            }
                        }
                    )
                }
        }
    }

    private fun calculateCurrentOrUpcomingEvents(categories: List<ScheduleCategories>): List<ScheduleItem> {
        if (categories.isEmpty()) {
            return emptyList()
        }

        val now = LocalTime.now()
        val windowStart = now.minusMinutes(30)
        val windowEnd = now.plusMinutes(30)

        return categories
            .flatMap { it.items }
            .filter { item ->
                val eventTime = runCatching { LocalTime.parse(item.hour) }.getOrNull()
                eventTime?.let {
                    if (windowStart > windowEnd) {
                        it >= windowStart || it <= windowEnd
                    } else {
                        it in windowStart..windowEnd
                    }
                } == true
            }
            .sortedBy { it.hour }
    }

    fun onRefresh(force: Boolean = false) {
        if (force || _uiState.value !is HomeUIState.Success) {
            observeSchedule()
        }
    }

    fun triggerTimeBasedUpdate() {
        val currentState = _uiState.value
        if (currentState is HomeUIState.Success) {
            viewModelScope.launch(Dispatchers.Default) {
                val relevantEvents = calculateCurrentOrUpcomingEvents(currentState.categories)
                if (currentState.currentOrUpcomingEvents != relevantEvents) {
                    _uiState.value = currentState.copy(currentOrUpcomingEvents = relevantEvents)
                }
            }
        }
    }

    fun generatePalette(originalBitmap: Bitmap, key: String) {
        if (_paletteCache.containsKey(key)) {
            return
        }
        viewModelScope.launch(Dispatchers.Default) {
            val smallBitmap = originalBitmap.scale(50, 50).copy(Bitmap.Config.ARGB_8888, true)

            try {
                val palette = Palette.from(smallBitmap)
                    .maximumColorCount(4)
                    .addFilter(WhiteFilter())
                    .generate()
                palette.let {
                    val predominantColor =
                        it.getDominantColor(Color(red = 73, green = 69, blue = 79).toArgb())

                    val contrastColor = if (predominantColor.isLightColor()) {
                        Color.DarkGray.copy(alpha = 0.8f)
                    } else {
                        Color.White.copy(alpha = 0.8f)
                    }

                    _paletteCache[key] = PaletteResult(
                        cardColor = Color(predominantColor),
                        textColor = contrastColor
                    )
                }
            } finally {
                smallBitmap.recycle()
            }
        }
    }
}