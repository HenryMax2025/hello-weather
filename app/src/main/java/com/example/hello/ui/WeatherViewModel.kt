package com.example.hello.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hello.data.City
import com.example.hello.data.WeatherRepository
import com.example.hello.data.chineseCities
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

data class WeatherUiState(
    val isLoading: Boolean = false,
    val currentCity: City = chineseCities[0],
    val temperature: String = "--",
    val humidity: String = "--",
    val feelsLike: String = "--",
    val weatherCode: Int = 0,
    val weatherDesc: String = "加载中...",
    val windSpeed: String = "--",
    val cloudCover: String = "--",
    val pressure: String = "--",
    val hourlyData: List<HourlyItem> = emptyList(),
    val dailyData: List<DailyItem> = emptyList(),
    val error: String? = null
)

data class HourlyItem(
    val time: String,
    val temp: String,
    val humidity: String = "--",
    val code: Int,
    val precipChance: String = "--",
    val cloudCover: String = "--"
)

data class DailyItem(
    val date: String,
    val dayName: String,
    val maxTemp: String,
    val minTemp: String,
    val maxFeelsLike: String = "--",
    val minFeelsLike: String = "--",
    val code: Int,
    val precipChance: String = "--",
    val precipAmount: String = "--",
    val maxWindSpeed: String = "--",
    val sunrise: String = "--",
    val sunset: String = "--"
)

class WeatherViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    val cities: List<City> = chineseCities

    init {
        loadWeather(chineseCities[0])
    }

    fun loadWeather(city: City) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, currentCity = city, error = null)

                val result = WeatherRepository.getWeather(city)

                result.onSuccess { response ->
                    val current = response.current
                    val hourly = response.hourly
                    val daily = response.daily

                    val hourlyList = if (hourly != null && hourly.time.size >= 24) {
                        (0 until minOf(24, hourly.time.size)).map { i ->
                            HourlyItem(
                                time = try { hourly.time.getOrNull(i)?.substring(11, 16) ?: "" } catch (e: Exception) { "" },
                                temp = try { hourly.temperature_2m.getOrNull(i)?.toInt()?.toString() ?: "--" } catch (e: Exception) { "--" },
                                humidity = try { hourly.relative_humidity_2m?.getOrNull(i)?.toString() ?: "--" } catch (e: Exception) { "--" },
                                code = hourly.weather_code.getOrNull(i) ?: 0,
                                precipChance = try { hourly.precipitation_probability?.getOrNull(i)?.toString() ?: "--" } catch (e: Exception) { "--" },
                                cloudCover = try { hourly.cloud_cover?.getOrNull(i)?.toString() ?: "--" } catch (e: Exception) { "--" }
                            )
                        }
                    } else emptyList()

                    val dailyList = if (daily != null && daily.time.isNotEmpty()) {
                        daily.time.mapIndexed { i, date ->
                            val dayOfWeek = try {
                                val localDate = LocalDate.parse(date)
                                localDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINESE)
                            } catch (e: Exception) {
                                getDayName(date)
                            }

                            val sunriseStr = try {
                                daily.sunrise?.getOrNull(i)?.substring(11, 16) ?: "--"
                            } catch (e: Exception) { "--" }

                            val sunsetStr = try {
                                daily.sunset?.getOrNull(i)?.substring(11, 16) ?: "--"
                            } catch (e: Exception) { "--" }

                            DailyItem(
                                date = try { date.substring(5) } catch (e: Exception) { date },
                                dayName = dayOfWeek,
                                maxTemp = try { daily.temperature_2m_max.getOrNull(i)?.toInt()?.toString() ?: "--" } catch (e: Exception) { "--" },
                                minTemp = try { daily.temperature_2m_min.getOrNull(i)?.toInt()?.toString() ?: "--" } catch (e: Exception) { "--" },
                                maxFeelsLike = try { daily.apparent_temperature_max?.getOrNull(i)?.toInt()?.toString() ?: "--" } catch (e: Exception) { "--" },
                                minFeelsLike = try { daily.apparent_temperature_min?.getOrNull(i)?.toInt()?.toString() ?: "--" } catch (e: Exception) { "--" },
                                code = daily.weather_code.getOrNull(i) ?: 0,
                                precipChance = try { daily.precipitation_probability_max?.getOrNull(i)?.toString() ?: "--" } catch (e: Exception) { "--" },
                                precipAmount = try { daily.precipitation_sum?.getOrNull(i)?.let { String.format("%.1f", it) } ?: "0" } catch (e: Exception) { "0" },
                                maxWindSpeed = try { daily.wind_speed_10m_max?.getOrNull(i)?.toInt()?.toString() ?: "--" } catch (e: Exception) { "--" },
                                sunrise = sunriseStr,
                                sunset = sunsetStr
                            )
                        }
                    } else emptyList()

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        temperature = current?.temperature_2m?.let { it.toInt().toString() } ?: "--",
                        humidity = current?.relative_humidity_2m?.toString() ?: "--",
                        feelsLike = current?.apparent_temperature?.let { it.toInt().toString() } ?: "--",
                        weatherCode = current?.weather_code ?: 0,
                        weatherDesc = getWeatherDesc(current?.weather_code ?: 0),
                        windSpeed = current?.wind_speed_10m?.let { it.toInt().toString() } ?: "--",
                        cloudCover = current?.cloud_cover?.toString() ?: "--",
                        pressure = current?.pressure_msl?.let { String.format("%.0f", it) } ?: "--",
                        hourlyData = hourlyList,
                        dailyData = dailyList
                    )
                }.onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "获取天气失败"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "未知错误"
                )
            }
        }
    }

    private fun getWeatherDesc(code: Int): String {
        return when (code) {
            0 -> "晴朗"
            1, 2, 3 -> "多云"
            45, 48 -> "雾"
            51, 53, 55 -> "小雨"
            56, 57 -> "冻雨"
            61, 63, 65 -> "雨"
            66, 67 -> "冻雨"
            71, 73, 75 -> "雪"
            77 -> "雪粒"
            80, 81, 82 -> "阵雨"
            85, 86 -> "阵雪"
            95, 96, 99 -> "雷暴"
            else -> "未知"
        }
    }

    private fun getDayName(date: String): String {
        return try {
            val parts = date.split("-")
            if (parts.size >= 3) {
                val month = parts[1].toIntOrNull() ?: 1
                val day = parts[2].toIntOrNull() ?: 1
                val weekdays = listOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")
                val fakeDate = java.util.Calendar.getInstance().apply {
                    set(2024, month - 1, day)
                }
                weekdays[fakeDate.get(java.util.Calendar.DAY_OF_WEEK) - 1]
            } else date
        } catch (e: Exception) {
            date
        }
    }
}