package com.example.weatherproject.ui.components

import com.example.weatherproject.data.CctvInfo
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import com.example.weatherproject.ui.icons.MyCameraAlt
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.weatherproject.R
import com.example.weatherproject.data.CurrentWeather
import com.example.weatherproject.data.HourlyForecast
import com.example.weatherproject.data.WeatherDetails
import com.example.weatherproject.data.WeeklyForecast
import com.example.weatherproject.util.ClothingRecommender

// 1. 현재 날씨 카드 (확장 가능)
@Composable
fun CurrentWeatherCard(
    weather: CurrentWeather,
    address: String,
    details: WeatherDetails, // 상세 정보도 받아옴
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    val rotationState by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        backgroundColor = Color.White.copy(alpha = 0.3f),
        elevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 상단: 주소 및 펼치기 화살표
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = address, fontSize = 14.sp, color = Color.White)
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "펼치기",
                    tint = Color.White,
                    modifier = Modifier.rotate(rotationState)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 기본 정보: 아이콘 + 온도
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = weather.iconUrl,
                    contentDescription = weather.description,
                    modifier = Modifier.size(80.dp),
                    placeholder = painterResource(id = R.drawable.ic_launcher_foreground)
                )
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    Text(
                        text = weather.temperature,
                        fontSize = 48.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = weather.description, 
                        fontSize = 16.sp, 
                        color = Color.White
                    )
                    Text(
                        text = "최고: ${weather.maxTemp} / 최저: ${weather.minTemp}", 
                        fontSize = 16.sp, 
                        color = Color.White
                    )
                    Text(text = "체감 ${weather.feelsLike}", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                }
            }

            // 닫혀있을 때만 보이는 '더보기' 안내 문구
            if (!isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "상세 날씨 더보기",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // ⭐️ 확장 영역 (상세 날씨)
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 24.dp)) {
                    Divider(color = Color.White.copy(alpha = 0.3f), modifier = Modifier.padding(bottom = 16.dp))
                    // 상세 날씨 내용 재사용
                    WeatherDetailContent(details)
                }
            }
        }
    }
}

// 2. 시간별 예보 카드 (확장 가능 -> 주간 예보 표시)
@Composable
fun HourlyForecastCard(
    hourlyForecasts: List<HourlyForecast>,
    weeklyForecasts: List<WeeklyForecast>, // 주간 예보도 받아옴
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    val rotationState by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        backgroundColor = Color.White.copy(alpha = 0.3f),
        elevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "시간별 예보",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if(isExpanded) "주간 예보 접기" else "주간 예보 보기",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "더보기",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp).rotate(rotationState)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(hourlyForecasts) { forecast ->
                    HourlyForecastItem(forecast = forecast)
                }
            }

            // ⭐️ 확장 영역 (주간 예보)
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 24.dp)) {
                    Divider(color = Color.White.copy(alpha = 0.3f), modifier = Modifier.padding(bottom = 16.dp))
                    Text(
                        text = "주간 예보",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    // 주간 예보 내용 재사용
                    WeeklyForecastContent(weeklyForecasts)
                }
            }
        }
    }
}

// --- 아래는 재사용 가능한 Content 컴포넌트들 (Card 래퍼 없음) ---

@Composable
fun WeatherDetailContent(details: WeatherDetails) {
    Column {
        // 1. 강수량
        WeatherContextItem(label = "강수량", value = details.precipitation, icon = "☔") { rawValue ->
            if (rawValue > 0) "우산을 챙기세요" else "비 소식 없음"
        }
        Divider(color = Color.White.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))

        // 2. 습도
        WeatherContextItem(label = "습도", value = details.humidity, icon = "💧") { rawValue ->
            when {
                rawValue < 40 -> "건조함"
                rawValue in 40..60 -> "쾌적함"
                else -> "습함"
            }
        }
        Divider(color = Color.White.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))

        // 3. 풍속
        WeatherContextItem(label = "풍속", value = details.wind, icon = "🌬️") { rawValue ->
            when {
                rawValue < 5 -> "바람 거의 없음"
                rawValue < 15 -> "산들바람"
                else -> "다소 강한 바람"
            }
        }
        Divider(color = Color.White.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))

        // 4. 미세먼지
        PmGaugeItem(label = "미세먼지", value = details.pm10)
    }
}

@Composable
fun WeeklyForecastContent(weeklyForecasts: List<WeeklyForecast>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        weeklyForecasts.forEach { forecast ->
            WeeklyForecastItem(forecast = forecast)
            if (forecast != weeklyForecasts.last()) {
                Divider(color = Color.White.copy(alpha = 0.3f))
            }
        }
    }
}

@Composable
fun WeatherDetailCard(details: WeatherDetails) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        backgroundColor = Color.White.copy(alpha = 0.3f),
        elevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("날씨 상세 정보", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 24.dp))
            WeatherDetailContent(details)
        }
    }
}

@Composable
fun WeeklyForecastCard(weeklyForecasts: List<WeeklyForecast>) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        backgroundColor = Color.White.copy(alpha = 0.3f),
        elevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("주간 예보", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            WeeklyForecastContent(weeklyForecasts)
        }
    }
}

@Composable
fun WeatherContextItem(label: String, value: String, icon: String, interpret: (Int) -> String) {
    val rawValue = value.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
    val description = interpret(rawValue)
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = label, fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
        Text(text = description, fontSize = 14.sp, color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.Medium)
    }
}

@Composable
fun PmGaugeItem(label: String, value: String) {
    val rawValue = value.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
    val (status, color, progress) = when {
        rawValue <= 30 -> Triple("좋음", Color(0xFF4CAF50), rawValue / 150f)
        rawValue <= 80 -> Triple("보통", Color(0xFFFFC107), rawValue / 150f)
        rawValue <= 150 -> Triple("나쁨", Color(0xFFFF9800), rawValue / 150f)
        else -> Triple("매우 나쁨", Color(0xFFF44336), 1f)
    }

    val recommendation = when (status) {
        "보통" -> "건강을 위해 마스크 권고"
        "나쁨", "매우 나쁨" -> "마스크 착용 필수"
        else -> ""
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "😷", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = label, fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                    Text(text = status, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            Text(text = recommendation, fontSize = 14.sp, color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.Medium)
        }
        Spacer(modifier = Modifier.height(12.dp))
        LinearProgressIndicator(progress = progress.coerceIn(0f, 1f), color = color, backgroundColor = Color.White.copy(alpha = 0.3f), modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)))
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("0", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
            Text("150+", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun NearbyCctvCard(
    isLoading: Boolean,
    cctvInfo: CctvInfo?,
    error: String?,
    onMoreClick: () -> Unit,
    onCctvClick: (CctvInfo) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        backgroundColor = Color.White.copy(alpha = 0.3f),
        elevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.MyCameraAlt, contentDescription = "CCTV", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "주변 도로 상황",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp), // 콘텐츠 영역 높이 고정
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    cctvInfo != null -> {
                        // 성공 시 CCTV 정보 표시
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp))
                                .clickable { onCctvClick(cctvInfo) }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = cctvInfo.roadName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(text = cctvInfo.distance, color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                        }
                    }
                    error != null -> {
                        Text(
                            text = error,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                    else -> {
                        Text(
                            text = "위치 정보 확인 후 주변 CCTV 정보를 표시합니다.",
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { onMoreClick() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.White.copy(alpha = 0.2f)),
                elevation = ButtonDefaults.elevation(0.dp)
            ) {
                Text("CCTV 목록 전체 보기", color = Color.White)
            }
        }
    }
}

@Composable
fun ClothingRecommendationCard(currentTemp: String, feelsLike: String, tempAdjustment: Int) {
    val rawFeelsLike = feelsLike.replace(Regex("[^0-9-]"), "").toIntOrNull() ?: 20
    
    val (recommendationText, items) = ClothingRecommender.getRecommendation(rawFeelsLike, tempAdjustment)
    
    val adjustmentText = if (tempAdjustment > 0) "(더위 많이 탐)" else if (tempAdjustment < 0) "(추위 많이 탐)" else ""
    
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), backgroundColor = Color.White.copy(alpha = 0.3f), elevation = 0.dp) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Face, contentDescription = "옷차림 추천", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "오늘의 옷차림 추천 $adjustmentText", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = recommendationText, fontSize = 16.sp, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(items) { item ->
                    ClothingItemChip(text = item)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "체감 온도: $feelsLike (보정: ${if(tempAdjustment > 0) "+" else ""}$tempAdjustment)", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

@Composable
fun ClothingItemChip(text: String) {
    Surface(
        color = Color.White.copy(alpha = 0.2f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.height(32.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
            Text(text = text, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun HourlyForecastItem(forecast: HourlyForecast) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = forecast.time, fontSize = 14.sp, color = Color.White)
        AsyncImage(model = forecast.iconUrl, contentDescription = null, modifier = Modifier.size(40.dp), placeholder = painterResource(id = R.drawable.ic_launcher_foreground))
        Text(text = forecast.temperature, fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun WeeklyForecastItem(forecast: WeeklyForecast) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = forecast.day,
            fontSize = 14.sp,
            color = Color.White,
            modifier = Modifier.weight(2.3f)
        )

        AsyncImage(
            model = forecast.iconUrl,
            contentDescription = null,
            modifier = Modifier
                .size(32.dp)
                .weight(2.5f)
        )

        Text(
            text = "",
            fontSize = 13.sp,
            color = Color.White,
            modifier = Modifier.weight(1.2f)
        )

        Text(
            text = "${forecast.minTemp} / ${forecast.maxTemp}",
            fontSize = 15.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(2f)
        )
    }
}