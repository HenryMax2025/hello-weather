package com.example.hello.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hello.data.getWeatherEmoji
import com.example.hello.ui.DailyItem
import com.example.hello.ui.HourlyItem
import com.example.hello.ui.WeatherUiState
import com.example.hello.ui.WeatherViewModel
import com.example.hello.ui.components.GlassCard
import com.example.hello.ui.components.GlassCardSolid

@Composable
fun WeatherScreen(
    modifier: Modifier = Modifier,
    viewModel: WeatherViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = getBackgroundGradient(uiState.weatherCode)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            CitySelector(
                cities = viewModel.cities,
                currentCity = uiState.currentCity.name,
                onCitySelected = { city -> viewModel.loadWeather(city) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                CurrentWeatherCard(uiState)
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.hourlyData.isNotEmpty()) {
                Text(
                    text = "小时预报",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                HourlyForecast(uiState.hourlyData)
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.dailyData.isNotEmpty()) {
                Text(
                    text = "7天预报",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                DailyForecast(uiState.dailyData)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun CitySelector(
    cities: List<com.example.hello.data.City>,
    currentCity: String,
    onCitySelected: (com.example.hello.data.City) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(cities) { city ->
            val isSelected = city.name == currentCity
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isSelected) Color.White.copy(alpha = 0.3f)
                        else Color.White.copy(alpha = 0.15f)
                    )
                    .clickable { onCitySelected(city) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = city.name,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun CurrentWeatherCard(uiState: WeatherUiState) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = uiState.currentCity.name,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${getWeatherEmoji(uiState.weatherCode)} ${uiState.weatherDesc}",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "${uiState.temperature}°",
                color = Color.White,
                fontSize = 72.sp,
                fontWeight = FontWeight.Light
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeatherInfoItem("体感", "${uiState.feelsLike}°")
                WeatherInfoItem("湿度", "${uiState.humidity}%")
                WeatherInfoItem("风速", "${uiState.windSpeed} km/h")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeatherInfoItem("云量", "${uiState.cloudCover}%")
                WeatherInfoItem("气压", "${uiState.pressure} hPa")
            }
        }
    }
}

@Composable
fun WeatherInfoItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun HourlyForecast(hourlyData: List<HourlyItem>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(hourlyData.take(24)) { item ->
            GlassCardSolid(
                cornerRadius = 16.dp,
                modifier = Modifier.width(60.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(
                        text = item.time.takeLast(5),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = getWeatherEmoji(item.code),
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${item.temp}°",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun DailyForecast(dailyData: List<DailyItem>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        dailyData.forEach { item ->
            GlassCardSolid(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 16.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.width(70.dp)) {
                        Text(
                            text = item.dayName,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = item.date,
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                        if (item.sunrise != "--") {
                            Text(
                                text = "🌅 ${item.sunrise}",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 10.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = getWeatherEmoji(item.code), fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        if (item.precipChance != "--" && item.precipChance != "0") {
                            Text(
                                text = "💧${item.precipChance}%",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row {
                        Text(
                            text = "${item.minTemp}°",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${item.maxTemp}°",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

fun getBackgroundGradient(code: Int): List<Color> {
    return when (code) {
        0 -> listOf(Color(0xFF1E90FF), Color(0xFF00BFFF), Color(0xFF87CEEB))
        1, 2, 3 -> listOf(Color(0xFF5F9EA0), Color(0xFF778899), Color(0xFFB0C4DE))
        45, 48, 51, 53, 55, 61, 63, 65, 80, 81, 82 -> listOf(Color(0xFF2F4F4F), Color(0xFF696969), Color(0xFFA9A9A9))
        71, 73, 75, 77, 85, 86 -> listOf(Color(0xFF708090), Color(0xFFB0C4DE), Color(0xFFE0FFFF))
        95, 96, 99 -> listOf(Color(0xFF4B0082), Color(0xFF8A2BE2), Color(0xFF9370DB))
        else -> listOf(Color(0xFF1E90FF), Color(0xFF00BFFF), Color(0xFF87CEEB))
    }
}