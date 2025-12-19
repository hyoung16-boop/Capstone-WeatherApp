package com.example.weatherproject.ui.screens

import com.example.weatherproject.data.CctvInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import com.example.weatherproject.ui.icons.MyCameraAlt
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.weatherproject.ui.CctvViewModel
import com.example.weatherproject.ui.MainViewModel
import com.example.weatherproject.ui.SearchViewModel
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import android.content.Intent
import android.net.Uri
import android.util.Log

@Composable
fun CctvScreen(
    navController: NavController,
    mainViewModel: MainViewModel,
    searchViewModel: SearchViewModel,
    cctvViewModel: CctvViewModel
) {
    val mainUiState by mainViewModel.uiState.collectAsState()
    val currentLocation by mainViewModel.currentLocation.collectAsState()

    // CCTV 상태 가져오기 from CctvViewModel
    val cctvInfo by cctvViewModel.cctvInfo.collectAsState()
    val cctvError by cctvViewModel.cctvError.collectAsState()

    // CCTV 리스트 (단일 CCTV를 리스트로 변환)
    val allCctvList = cctvInfo?.let { listOf(it) } ?: emptyList()

    // 페이징 상태: 처음에 4개만 보여줌
    var visibleCount by remember { mutableStateOf(4) }
    val displayList = allCctvList.take(visibleCount)

    // 검색 관련 상태
    val searchText by searchViewModel.searchText.collectAsState()
    val searchResults by searchViewModel.searchResults.collectAsState()
    val context = LocalContext.current

    // 화면이 처음 구성되거나(Unit), 현재 위치가 변경될 때 데이터를 로드
    LaunchedEffect(Unit, currentLocation) {
        // 위치 정보가 없거나, CCTV 데이터가 없거나, 에러가 있는 경우 -> 강제 갱신
        val needRefresh = cctvViewModel.selectedLocationInfo.value == null || 
                          cctvInfo == null || 
                          cctvError != null

        if (needRefresh) {
            currentLocation?.let {
                val address = mainUiState.address.takeIf { it.isNotBlank() } ?: "현재 위치"
                cctvViewModel.updateSelectedLocation(it.latitude, it.longitude, address, it, forceRefresh = true)
            }
        }
    }

    // 스크롤 감지
    val listState = rememberLazyListState()
    val isAtBottom = remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            if (totalItems == 0) return@derivedStateOf false

            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItemIndex >= totalItems - 1
        }
    }

    // 바닥에 닿으면 더 로드
    LaunchedEffect(isAtBottom.value) {
        if (isAtBottom.value && visibleCount < allCctvList.size) {
            visibleCount += 4
        }
    }

    Scaffold(
        topBar = {
            val selectedLocationInfo by cctvViewModel.selectedLocationInfo.collectAsState()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(1f)
            ) {
                Column {
                    // 뒤로가기, 검색, 현재위치 버튼
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기", tint = Color.White)
                        }
                        OutlinedTextField(
                            value = searchText,
                            onValueChange = { searchViewModel.onSearchTextChange(it) },
                            label = { Text("CCTV 위치 검색", color = Color.White.copy(alpha = 0.7f)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { searchViewModel.performSearch() }),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                textColor = Color.White,
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                                cursorColor = Color.White,
                                focusedLabelColor = Color.White,
                                unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        )
                        IconButton(onClick = {
                            android.widget.Toast.makeText(context, "주변 CCTV를 탐색합니다...", android.widget.Toast.LENGTH_SHORT).show()
                            currentLocation?.let {
                                val address = mainUiState.address.takeIf { it.isNotBlank() } ?: "현재 위치"
                                cctvViewModel.updateSelectedLocation(it.latitude, it.longitude, address, it, forceRefresh = true)
                            }
                        }) {
                            Icon(Icons.Default.LocationOn, "현재 위치", tint = Color.White)
                        }
                    }

                    // 현재 선택된 위치 주소 표시
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "선택된 위치",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedLocationInfo?.address ?: "위치를 검색하거나 현재 위치 버튼을 눌러주세요.",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }


                    // 검색 결과 리스트
                    if (searchResults.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .background(Color.White, shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                        ) {
                            itemsIndexed(searchResults) { index, city ->
                                Text(
                                    text = city,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            searchViewModel.onCitySelected(context, city) { lat, lon ->
                                                mainViewModel.updateWeatherByLocation(city, lat, lon)
                                                cctvViewModel.updateSelectedLocation(lat, lon, city, currentLocation)
                                            }
                                        }
                                        .padding(16.dp),
                                    color = Color.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Divider(color = Color.LightGray.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        },
        backgroundColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 로딩/에러/데이터 상태 처리
            when {
                // 에러 상태
                cctvError != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "⚠️",
                                fontSize = 48.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = cctvError ?: "알 수 없는 오류",
                                color = Color.White,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = {
                                    cctvViewModel.retryFetchCctv()
                                },
                                colors = ButtonDefaults.buttonColors(backgroundColor = Color.White)
                            ) {
                                Text("다시 시도", color = Color.Black)
                            }
                        }
                    }
                }

                // 데이터 있음
                allCctvList.isNotEmpty() -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        itemsIndexed(displayList) { index, cctv ->
                            CctvListItem(
                                cctv = cctv,
                                onClick = {
                                    Log.d("CctvScreen", "CCTV 클릭: ${cctv.cctvName}, URL: ${cctv.cctvUrl}")

                                    if (cctv.cctvUrl.isNotEmpty()) {
                                        try {
                                            // ✅ Base64로 URL 인코딩 (특수문자 안전하게 처리)
                                            val encodedUrl = android.util.Base64.encodeToString(
                                                cctv.cctvUrl.toByteArray(),
                                                android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP
                                            )
                                            navController.navigate("cctvPlayer/${cctv.cctvName}/$encodedUrl")
                                        } catch (e: Exception) {
                                            Log.e("CctvScreen", "Navigation 실패: ${e.message}", e)
                                            android.widget.Toast.makeText(
                                                context,
                                                "CCTV 재생 실패: ${e.message}",
                                                android.widget.Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    } else {
                                        android.widget.Toast.makeText(
                                            context,
                                            "CCTV URL이 없습니다",
                                            android.widget.Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            )
                        }

                        // 하단 안내 문구
                        if (visibleCount < allCctvList.size) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "밀어서 더 보기",
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = Color.White.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 로딩 상태
                else -> {
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
                                text = "주변 CCTV를 검색 중...",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CctvListItem(
    cctv: CctvInfo,
    onClick: () -> Unit  // ✅ 클릭 콜백 추가
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        backgroundColor = Color.White.copy(alpha = 0.9f),
        elevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)  // ✅ 클릭 이벤트 연결
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 썸네일
            Box(
                modifier = Modifier
                    .size(120.dp, 80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MyCameraAlt,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 텍스트 정보
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                // CCTV 이름
                Text(
                    text = cctv.cctvName,
                    fontSize = 16.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 도로명
                if (cctv.roadName.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = cctv.roadName,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 타입과 거리
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = cctv.type,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )

                    if (cctv.distance.isNotEmpty()) {
                        Text(
                            text = cctv.distance,
                            fontSize = 13.sp,
                            color = Color.Blue,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // 재생 아이콘
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "영상 보기",
                tint = Color.Blue,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}