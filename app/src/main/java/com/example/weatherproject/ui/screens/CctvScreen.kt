package com.example.weatherproject.ui.screens

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
// import androidx.compose.material.icons.filled.CameraAlt
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.weatherproject.data.CctvInfo
import com.example.weatherproject.ui.MainViewModel
import com.example.weatherproject.ui.SearchViewModel

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction

import androidx.compose.ui.text.style.TextOverflow

@Composable
fun CctvScreen(
    navController: NavController,
    viewModel: MainViewModel,
    searchViewModel: SearchViewModel // 검색 기능 복구를 위해 추가
) {
    val uiState by viewModel.uiState.collectAsState()
    val allCctvList = uiState.cctvList
    
    // 페이징 상태: 처음에 4개만 보여줌
    var visibleCount by remember { mutableStateOf(4) }
    val displayList = allCctvList.take(visibleCount)
    
    // 검색 관련 상태
    val searchText by searchViewModel.searchText.collectAsState()
    val searchResults by searchViewModel.searchResults.collectAsState()
    val context = LocalContext.current

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

    // 바닥에 닿으면 더 로드 (무한 스크롤 효과)
    LaunchedEffect(isAtBottom.value) {
        if (isAtBottom.value && visibleCount < allCctvList.size) {
            // 잠시 딜레이 후 로드하는 척 (사용자가 인지할 수 있게)
            // kotlinx.coroutines.delay(500) 
            visibleCount += 4
        }
    }

    Scaffold(
        topBar = {
            // 검색바가 포함된 커스텀 TopBar (TopAppBar 대신 Row 사용으로 높이 제한 해결)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(1f)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp) // 넉넉한 높이
                            .padding(horizontal = 4.dp, vertical = 8.dp), // NavigationIcon 공간 고려하여 padding 조정
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // 뒤로가기 버튼
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기", tint = Color.White)
                        }

                        // 검색 입력창 (가중치 부여)
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

                        // 현재 위치 버튼
                        IconButton(onClick = { 
                            android.widget.Toast.makeText(context, "주변 CCTV를 탐색합니다...", android.widget.Toast.LENGTH_SHORT).show()
                            viewModel.fetchCurrentLocationCctvs() 
                        }) {
                            Icon(Icons.Default.LocationOn, "현재 위치", tint = Color.White)
                        }
                    }

                    // 검색 결과 리스트 (WeatherTopAppBar와 동일 로직)
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
                                                viewModel.updateWeatherByLocation(city, lat, lon)
                                                // TODO: CCTV도 해당 지역으로 갱신해야 함
                                            }
                                        }
                                        .padding(16.dp),
                                    color = Color.Black,
                                    maxLines = 1, // ⬅️ 한 줄 제한
                                    overflow = TextOverflow.Ellipsis // ⬅️ 말줄임표(...) 처리
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
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            itemsIndexed(displayList) { index, cctv ->
                CctvListItem(cctv = cctv)
            }

            // 하단 안내 문구 (더 데이터가 남았을 때만 표시)
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
}

@Composable
fun CctvListItem(cctv: CctvInfo) {
    Card(
        shape = RoundedCornerShape(12.dp),
        backgroundColor = Color.White.copy(alpha = 0.8f),
        elevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { /* TODO: 영상 재생 */ }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 썸네일 (플레이스홀더)
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
                    tint = Color.Gray
                )
                // 재생 버튼 오버레이
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 텍스트 정보
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = cctv.roadName,
                    fontSize = 16.sp,
                    color = Color.Black,
                    maxLines = 1,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "거리: ${cctv.distance}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}