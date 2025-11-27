package com.example.weatherproject.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.weatherproject.R
import com.example.weatherproject.data.CctvInfo
import com.example.weatherproject.data.CurrentWeather
import com.example.weatherproject.data.HourlyForecast
import com.example.weatherproject.data.WeatherDetails
import com.example.weatherproject.data.WeeklyForecast

@Composable
fun CurrentWeatherCard(
    weather: CurrentWeather,
    address: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        backgroundColor = Color.White.copy(alpha = 0.3f),
        elevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = address,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = weather.iconUrl,
                    contentDescription = weather.description,
                    modifier = Modifier.size(80.dp),
                    placeholder = painterResource(id = R.drawable.ic_launcher_foreground)
                )
                Column(
                    modifier = Modifier.padding(start = 16.dp)
                ) {
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
                        text = "${weather.maxTemp} / ${weather.minTemp}",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Text(
                        text = weather.feelsLike,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherDetailCard(details: WeatherDetails) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        backgroundColor = Color.White.copy(alpha = 0.3f),
        elevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "날씨 상세 정보",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // 1. 강수량
            WeatherContextItem(
                label = "강수량",
                value = details.precipitation,
                icon = "☔"
            ) { rawValue ->
                if (rawValue > 0) "우산을 챙기세요" else "비 소식 없음"
            }
            Divider(color = Color.White.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))

            // 2. 습도
            WeatherContextItem(
                label = "습도",
                value = details.humidity,
                icon = "💧"
            ) { rawValue ->
                when {
                    rawValue < 40 -> "건조함 (수분 섭취 필수)"
                    rawValue in 40..60 -> "쾌적함"
                    else -> "습함 (불쾌지수 주의)"
                }
            }
            Divider(color = Color.White.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))

            // 3. 풍속
            WeatherContextItem(
                label = "풍속",
                value = details.wind,
                icon = "🌬️"
            ) { rawValue ->
                when {
                    rawValue < 5 -> "바람 거의 없음"
                    rawValue < 15 -> "산들바람"
                    else -> "다소 강한 바람"
                }
            }
            Divider(color = Color.White.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 12.dp))

            // 4. 미세먼지 (게이지 바 형태)
            PmGaugeItem(
                label = "미세먼지",
                value = details.pm10
            )
        }
    }
}

@Composable
fun WeatherContextItem(
    label: String,
    value: String,
    icon: String,
    interpret: (Int) -> String
) {
    // 숫자만 추출 (예: "12 km/h" -> 12)
    val rawValue = value.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
    val description = interpret(rawValue)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        Text(
            text = description,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun PmGaugeItem(label: String, value: String) {
    val rawValue = value.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
    
    // 미세먼지 기준 (PM10 기준)
    // 0~30: 좋음(파랑/초록), 31~80: 보통(초록/노랑), 81~150: 나쁨(노랑/빨강), 151~: 매우나쁨(빨강)
    // 색상 및 상태 결정
    val (status, color, progress) = when {
        rawValue <= 30 -> Triple("좋음", Color(0xFF4CAF50), rawValue / 150f) // 초록
        rawValue <= 80 -> Triple("보통", Color(0xFFFFC107), rawValue / 150f) // 노랑
        rawValue <= 150 -> Triple("나쁨", Color(0xFFFF9800), rawValue / 150f) // 주황
        else -> Triple("매우 나쁨", Color(0xFFF44336), 1f) // 빨강
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "😷", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "$value ($status)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        
        // 게이지 바
        LinearProgressIndicator(
            progress = progress.coerceIn(0f, 1f),
            color = color,
            backgroundColor = Color.White.copy(alpha = 0.3f),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.height(4.dp))
        
        // 범위 라벨 (0 ... 150+)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("0", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
            Text("150+", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
        }
    }
}

@Composable
fun NearbyCctvCard(
    cctvList: List<CctvInfo>,
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
                    Icon(
                        imageVector = Icons.Filled.CameraAlt,
                        contentDescription = "CCTV",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "주변 도로 상황",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Text(
                    text = "더보기 >",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.clickable { onMoreClick() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(cctvList) { cctv ->
                    CctvThumbnailItem(cctv = cctv, onClick = { onCctvClick(cctv) })
                }
            }
        }
    }
}

@Composable
fun CctvThumbnailItem(cctv: CctvInfo, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .height(80.dp)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.2f), shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt, 
                contentDescription = null, 
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(32.dp)
            )
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "재생",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = cctv.roadName,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
        Text(
            text = cctv.distance,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun ClothingRecommendationCard(feelsLike: String, tempAdjustment: Int) {
    val rawTemp = feelsLike.replace(Regex("[^0-9-]"), "").toIntOrNull() ?: 20
    val adjustedTemp = rawTemp + tempAdjustment

    val recommendationText = when {
        adjustedTemp >= 28 -> "민소매, 반팔, 반바지, 원피스"
        adjustedTemp >= 23 -> "반팔, 얇은 셔츠, 반바지, 면바지"
        adjustedTemp >= 20 -> "얇은 가디건, 긴팔, 면바지, 청바지"
        adjustedTemp >= 17 -> "얇은 니트, 맨투맨, 가디건, 청바지"
        adjustedTemp >= 12 -> "자켓, 가디건, 야상, 스타킹, 청바지, 면바지"
        adjustedTemp >= 9 -> "자켓, 트렌치코트, 야상, 니트, 청바지, 스타킹"
        adjustedTemp >= 5 -> "코트, 가죽자켓, 히트텍, 니트, 레깅스"
        else -> "패딩, 두꺼운 코트, 목도리, 기모제품"
    }

    val adjustmentText = if (tempAdjustment > 0) "(더위 많이 탐)" else if (tempAdjustment < 0) "(추위 많이 탐)" else ""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        backgroundColor = Color.White.copy(alpha = 0.3f),
        elevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Face,
                    contentDescription = "옷차림 추천",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "오늘의 옷차림 추천 $adjustmentText",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = recommendationText,
                fontSize = 16.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "체감 온도: $feelsLike (보정: ${if(tempAdjustment > 0) "+" else ""}$tempAdjustment)",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun HourlyForecastCard(
    hourlyForecasts: List<HourlyForecast>,
    onClick: () -> Unit
) {
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
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "더보기",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(hourlyForecasts) { forecast ->
                    HourlyForecastItem(forecast = forecast)
                }
            }
        }
    }
}

@Composable
fun HourlyForecastItem(forecast: HourlyForecast) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = forecast.time, fontSize = 14.sp, color = Color.White)

        AsyncImage(
            model = forecast.iconUrl,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            placeholder = painterResource(id = R.drawable.ic_launcher_foreground)
        )

        Text(text = forecast.temperature, fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun WeeklyForecastCard(weeklyForecasts: List<WeeklyForecast>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        backgroundColor = Color.White.copy(alpha = 0.3f),
        elevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "주간 예보",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                weeklyForecasts.forEach { forecast ->
                    WeeklyForecastItem(forecast = forecast)
                    if (forecast != weeklyForecasts.last()) {
                        Divider(color = Color.White.copy(alpha = 0.3f))
                    }
                }
            }
        }
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
            text = forecast.date,
            fontSize = 14.sp,
            color = Color.White,
            modifier = Modifier.weight(2.3f)
        )
        Text(
            text = forecast.pm10Status,
            fontSize = 13.sp,
            color = Color.White,
            modifier = Modifier.weight(2.5f)
        )
        Text(
            text = forecast.precipitation,
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
