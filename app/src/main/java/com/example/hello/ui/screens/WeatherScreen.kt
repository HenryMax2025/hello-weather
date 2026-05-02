package com.example.hello.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hello.data.City
import com.example.hello.data.LocationStatus
import com.example.hello.data.getWeatherEmoji
import com.example.hello.ui.DailyItem
import com.example.hello.ui.HourlyItem
import com.example.hello.ui.WeatherUiState
import com.example.hello.ui.WeatherViewModel
import com.example.hello.ui.components.GlassCard
import com.example.hello.ui.components.GlassCardSolid

@OptIn(ExperimentalMaterial3Api::class)
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
        when {
            uiState.locationStatus == LocationStatus.DENIED -> {
                PermissionDeniedContent(
                    onRetry = { viewModel.retry() }
                )
            }
            uiState.error != null && uiState.currentCity == null -> {
                ErrorContent(
                    message = uiState.error!!,
                    onRetry = { viewModel.retry() }
                )
            }
            uiState.isLoading && uiState.currentCity == null -> {
                LoadingContent()
            }
            else -> {
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = { viewModel.refresh() },
                    modifier = Modifier.fillMaxSize()
                ) {
                    MainContent(
                        uiState = uiState,
                        onSearchQueryChange = { viewModel.searchCities(it) },
                        onCitySelected = { city ->
                            viewModel.clearSearch()
                            viewModel.loadWeather(city)
                        },
                        onClearSearch = { viewModel.clearSearch() },
                        onRequestLocation = { viewModel.requestLocation() }
                    )
                }
            }
        }
    }
}

@Composable
private fun MainContent(
    uiState: WeatherUiState,
    onSearchQueryChange: (String) -> Unit,
    onCitySelected: (City) -> Unit,
    onClearSearch: () -> Unit,
    onRequestLocation: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        SearchBar(
            query = searchQuery,
            onQueryChange = {
                searchQuery = it
                onSearchQueryChange(it)
            },
            onClear = {
                searchQuery = ""
                onClearSearch()
            },
            onRequestLocation = onRequestLocation
        )

        AnimatedVisibility(
            visible = uiState.searchResults.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            SearchResults(
                results = uiState.searchResults,
                onCitySelected = onCitySelected
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (uiState.isLoading && uiState.currentCity != null) {
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

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onRequestLocation: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlassCardSolid(
            modifier = Modifier.weight(1f),
            cornerRadius = 24.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔍",
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.weight(1f),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color.White,
                        fontSize = 14.sp
                    ),
                    cursorBrush = SolidColor(Color.White),
                    decorationBox = { innerTextField ->
                        Box {
                            if (query.isEmpty()) {
                                Text(
                                    text = "搜索城市...",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 14.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                if (query.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onClear() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✕",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f))
                .clickable { onRequestLocation() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "📍",
                fontSize = 20.sp
            )
        }
    }
}

@Composable
private fun SearchResults(
    results: List<City>,
    onCitySelected: (City) -> Unit
) {
    GlassCardSolid(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        cornerRadius = 16.dp
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            if (results.isEmpty()) {
                Text(
                    text = "未找到相关城市",
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                results.forEach { city ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCitySelected(city) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📍",
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = city.name,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            if (city.province != null) {
                                Text(
                                    text = city.province,
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrentWeatherCard(uiState: WeatherUiState) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = uiState.currentCity?.name ?: "未知位置",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            if (uiState.currentCity?.district != null) {
                Text(
                    text = uiState.currentCity.district,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            }

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
private fun WeatherInfoItem(label: String, value: String) {
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
private fun HourlyForecast(hourlyData: List<HourlyItem>) {
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
private fun DailyForecast(dailyData: List<DailyItem>) {
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

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "正在获取位置...",
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "⚠️",
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                color = Color.White,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            TextButton(onClick = onRetry) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔄",
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "重试",
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionDeniedContent(
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "📍",
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "需要位置权限",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "请授权位置权限以获取本地天气",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            TextButton(onClick = onRetry) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📍",
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "授予权限",
                        color = Color.White
                    )
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