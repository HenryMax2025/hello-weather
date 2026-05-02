package com.example.hello.data

data class WeatherResponse(
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val current: CurrentWeather?,
    val hourly: HourlyWeather?,
    val daily: DailyWeather?
)

data class CurrentWeather(
    val time: String,
    val temperature_2m: Double,
    val relative_humidity_2m: Int,
    val apparent_temperature: Double,
    val weather_code: Int,
    val wind_speed_10m: Double,
    val wind_direction_10m: Int,
    val cloud_cover: Int? = null,
    val pressure_msl: Double? = null
)

data class HourlyWeather(
    val time: List<String>,
    val temperature_2m: List<Double>,
    val relative_humidity_2m: List<Int>? = null,
    val weather_code: List<Int>,
    val precipitation_probability: List<Int>? = null,
    val cloud_cover: List<Int>? = null,
    val wind_speed_10m: List<Double>? = null
)

data class DailyWeather(
    val time: List<String>,
    val weather_code: List<Int>,
    val temperature_2m_max: List<Double>,
    val temperature_2m_min: List<Double>,
    val apparent_temperature_max: List<Double>? = null,
    val apparent_temperature_min: List<Double>? = null,
    val sunrise: List<String>? = null,
    val sunset: List<String>? = null,
    val precipitation_probability_max: List<Int>? = null,
    val precipitation_sum: List<Double>? = null,
    val wind_speed_10m_max: List<Double>? = null
)

data class City(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val district: String? = null,
    val province: String? = null
)

enum class LocationStatus {
    IDLE,
    REQUESTING,
    GRANTED,
    DENIED,
    ERROR
}

fun getWeatherDescription(code: Int): String {
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
        95 -> "雷暴"
        96, 99 -> "雷暴加冰雹"
        else -> "未知"
    }
}

fun getWeatherEmoji(code: Int): String {
    return when (code) {
        0 -> "☀️"
        1, 2, 3 -> "⛅"
        45, 48 -> "🌫️"
        51, 53, 55 -> "🌧️"
        56, 57 -> "🌨️"
        61, 63, 65 -> "🌧️"
        66, 67 -> "🌨️"
        71, 73, 75 -> "❄️"
        77 -> "🌨️"
        80, 81, 82 -> "🌦️"
        85, 86 -> "🌨️"
        95, 96, 99 -> "⛈️"
        else -> "🌤️"
    }
}