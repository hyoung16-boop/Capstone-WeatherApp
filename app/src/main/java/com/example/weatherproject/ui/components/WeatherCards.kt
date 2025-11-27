package com.example.weatherproject.ui.components

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
import androidx.compose.material.icons.filled.CameraAlt
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.weatherproject.R
import com.example.weatherproject.data.CctvInfo
import com.example.weatherproject.data.CurrentWeather
import com.example.weatherproject.data.HourlyForecast
import com.example.weatherproject.data.WeatherDetails
import com.example.weatherproject.data.WeeklyForecast

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
                        text = "${weather.maxTemp} / ${weather.minTemp}", 
                        fontSize = 16.sp, 
                        color = Color.White
                    )
                    Text(text = weather.feelsLike, fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
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
                rawValue < 40 -> "건조함 (수분 섭취 필수)"
                rawValue in 40..60 -> "쾌적함"
                else -> "습함 (불쾌지수 주의)"
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

// --- 기존의 독립 Card 컴포넌트들 (이제 안 쓰이지만 호환성을 위해 남겨두거나 Content를 감싸도록 수정) ---

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

// --- 나머지 하위 컴포넌트들 (변화 없음) ---

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
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "😷", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = label, fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                    Text(text = "$value ($status)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
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
fun NearbyCctvCard(cctvList: List<CctvInfo>, onMoreClick: () -> Unit, onCctvClick: (CctvInfo) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), 
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
                    Icon(imageVector = Icons.Filled.CameraAlt, contentDescription = "CCTV", tint = Color.White)
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
            
            // 텍스트 리스트 형태 (썸네일 제거)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                cctvList.forEach { cctv ->
                     Row(
                         modifier = Modifier
                             .fillMaxWidth()
                             .background(Color.Black.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp))
                             .clickable { onCctvClick(cctv) } // 여기서는 상세 페이지 이동이 아니므로, 그냥 둬도 무방하나 
                                                              // 사용자가 '목록으로 가야 볼 수 있다'는 걸 인지하도록
                                                              // onMoreClick()을 호출하게 하거나, 토스트를 띄울 수도 있음.
                                                              // 현재 로직상 onCctvClick은 동작 X (TODO 상태). 
                                                              // UX상 목록 화면으로 가는게 자연스러움.
                             .padding(12.dp),
                         horizontalArrangement = Arrangement.SpaceBetween,
                         verticalAlignment = Alignment.CenterVertically
                     ) {
                         Text(text = cctv.roadName, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                         Text(text = cctv.distance, color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                     }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // 전체 보기 버튼
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
}

// CctvThumbnailItem은 더 이상 사용하지 않으므로 삭제하거나 주석 처리 가능하지만, 
// 깔끔하게 제거하고 필요한 경우 CctvScreen에서 자체적으로 구현하도록 함.
// (현재 CctvScreen은 자체 구현체를 사용하고 있지 않고 이 파일의 컴포넌트를 참조하지 않는 것으로 보임 - CctvListItem 별도 존재)


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
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), backgroundColor = Color.White.copy(alpha = 0.3f), elevation = 0.dp) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Face, contentDescription = "옷차림 추천", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "오늘의 옷차림 추천 $adjustmentText", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = recommendationText, fontSize = 16.sp, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "체감 온도: $feelsLike (보정: ${if(tempAdjustment > 0) "+" else ""}$tempAdjustment)", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
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
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text = forecast.date, fontSize = 14.sp, color = Color.White, modifier = Modifier.weight(2.3f))
        Text(text = forecast.pm10Status, fontSize = 13.sp, color = Color.White, modifier = Modifier.weight(2.5f))
        Text(text = forecast.precipitation, fontSize = 13.sp, color = Color.White, modifier = Modifier.weight(1.2f))
        Text(text = "${forecast.minTemp} / ${forecast.maxTemp}", fontSize = 15.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
    }
}